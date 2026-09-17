package mg.itu.att.ui.configuration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mg.itu.att.data.ActionsHistorique
import mg.itu.att.data.AppDatabase
import mg.itu.att.data.Bareme
import mg.itu.att.data.CategoriePermis
import mg.itu.att.data.Centre
import mg.itu.att.data.CriterePratique
import mg.itu.att.data.EntitesHistorique
import mg.itu.att.data.Question
import mg.itu.att.data.RegleConfig
import mg.itu.att.data.Region
import mg.itu.att.data.TypeEpreuve
import mg.itu.att.data.resume
import mg.itu.att.data.tracer
import mg.itu.att.metier.ValidationConfiguration
import mg.itu.att.metier.dateDuJour
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.connexion.SessionUtilisateur

// ---------- ÉTATS ----------

data class EtatFormulaireCategorie(
    val id: Int = 0, val code: String = "", val libelle: String = "", val ageMinimum: String = "",
    val prealable: String = "", val aConfirmer: Boolean = true, val actif: Boolean = true,
    val erreur: String? = null,
)

data class EtatDetailCategorie(val categorie: CategoriePermis? = null, val epreuves: List<TypeEpreuve> = emptyList())

data class EtatFormulaireEpreuve(
    val id: Int = 0, val categorieId: Int = 0, val code: String = "", val libelle: String = "", val ordre: String = "1",
    val duree: String = "", val obligatoire: Boolean = true, val aConfirmer: Boolean = true, val actif: Boolean = true,
    val erreur: String? = null,
)

data class EtatEpreuve(
    val epreuve: TypeEpreuve? = null,
    val categorie: CategoriePermis? = null,
    val baremes: List<Bareme> = emptyList(),
    val questions: List<Question> = emptyList(),
    val criteres: List<CriterePratique> = emptyList(),
)

data class EtatFormulaireBareme(val noteMax: String = "", val seuil: String = "", val aConfirmer: Boolean = true, val erreur: String? = null)

/** Une question orale : énoncé, points, réponse attendue facultative (aide-mémoire de l'examinateur). */
data class EtatFormulaireQuestion(
    val enonce: String = "", val points: String = "1", val reponseAttendue: String = "",
    val aConfirmer: Boolean = true, val erreur: String? = null,
)

data class EtatFormulaireCritere(val libelle: String = "", val points: String = "1", val eliminatoire: Boolean = false, val aConfirmer: Boolean = true, val erreur: String? = null)

data class RegleLigne(val regle: RegleConfig, val codeCategorie: String?)

data class EtatFormulaireRegle(val regle: RegleConfig? = null, val valeur: String = "", val description: String = "", val aConfirmer: Boolean = true, val erreur: String? = null)

data class CentreLigne(val centre: Centre, val nomRegion: String)

data class EtatFormulaireCentre(
    val id: Int = 0, val nom: String = "", val regionId: Int? = null, val adresse: String = "", val capacite: String = "",
    val actif: Boolean = true, val regions: List<Region> = emptyList(), val erreur: String? = null,
)

// ---------- LE VIEWMODEL ----------

/**
 * Configuration des référentiels (UC02, Super Admin) : catégories, épreuves, barèmes versionnés, règles,
 * questions, critères, centres. Rien n'est supprimé : on désactive. Un barème utilisé n'est jamais modifié :
 * on crée une nouvelle version. Toute écriture = `withTransaction` + `tracer`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConfigurationViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private var session: SessionUtilisateur? = null

    override fun definirSession(session: SessionUtilisateur) {
        this.session = session
    }

    private fun idUtilisateur(): Int? = session?.id

    // ----- Catégories -----

    val categories: StateFlow<List<CategoriePermis>> =
        db.categoriePermisDao().toutes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _formulaireCategorie = MutableStateFlow(EtatFormulaireCategorie())
    val formulaireCategorie: StateFlow<EtatFormulaireCategorie> = _formulaireCategorie
    private var categorieChargee: Int? = null

    fun preparerCategorie(id: Int?) {
        if (categorieChargee == (id ?: 0)) return
        categorieChargee = id ?: 0
        viewModelScope.launch {
            val c = id?.let { db.categoriePermisDao().parId(it) }
            _formulaireCategorie.value = EtatFormulaireCategorie(
                id = c?.id ?: 0, code = c?.code ?: "", libelle = c?.libelle ?: "", ageMinimum = c?.ageMinimum?.toString() ?: "",
                prealable = c?.categoriePrealableCode ?: "", aConfirmer = c?.aConfirmer ?: true, actif = c?.actif ?: true,
            )
        }
    }

    fun modifierCategorie(t: (EtatFormulaireCategorie) -> EtatFormulaireCategorie) = _formulaireCategorie.update { t(it).copy(erreur = null) }

    fun enregistrerCategorie(onSucces: (Int) -> Unit) {
        val f = _formulaireCategorie.value
        val auteur = idUtilisateur() ?: return
        viewModelScope.launch {
            val existantes = categories.value.filter { it.id != f.id }.map { it.code }
            val erreur = ValidationConfiguration.validerCategorie(f.code, f.libelle, f.ageMinimum, existantes)
            if (erreur != null) return@launch _formulaireCategorie.update { it.copy(erreur = erreur) }
            val ancienne = if (f.id == 0) null else db.categoriePermisDao().parId(f.id)
            val nouvelle = CategoriePermis(
                id = f.id, code = f.code.trim(), libelle = f.libelle.trim(), ageMinimum = f.ageMinimum.trim().toIntOrNull(),
                categoriePrealableCode = f.prealable.trim().ifBlank { null }, actif = f.actif, aConfirmer = f.aConfirmer,
            )
            val id = db.withTransaction {
                if (ancienne == null) {
                    val id = db.categoriePermisDao().inserer(nouvelle).toInt()
                    db.tracer(EntitesHistorique.CATEGORIE, id, ActionsHistorique.CREATION, auteur, nouvelleValeur = nouvelle.resume()); id
                } else {
                    db.categoriePermisDao().modifier(nouvelle)
                    db.tracer(EntitesHistorique.CATEGORIE, f.id, ActionsHistorique.MODIFICATION, auteur, ancienneValeur = ancienne.resume(), nouvelleValeur = nouvelle.resume()); f.id
                }
            }
            categorieChargee = null
            onSucces(id)
        }
    }

    // ----- Détail catégorie et épreuves -----

    private val idCategorie = MutableStateFlow(0)
    fun afficherCategorie(id: Int) { idCategorie.value = id }

    val detailCategorie: StateFlow<EtatDetailCategorie> =
        idCategorie.flatMapLatest { id ->
            combine(db.categoriePermisDao().parIdEnDirect(id), db.typeEpreuveDao().parCategorie(id)) { c, e -> EtatDetailCategorie(c, e) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatDetailCategorie())

    private val _formulaireEpreuve = MutableStateFlow(EtatFormulaireEpreuve())
    val formulaireEpreuve: StateFlow<EtatFormulaireEpreuve> = _formulaireEpreuve
    private var epreuveChargee: Pair<Int, Int>? = null

    fun preparerEpreuve(categorieId: Int, epreuveId: Int?) {
        if (epreuveChargee == (categorieId to (epreuveId ?: 0))) return
        epreuveChargee = categorieId to (epreuveId ?: 0)
        viewModelScope.launch {
            val e = epreuveId?.let { db.typeEpreuveDao().parId(it) }
            val prochainOrdre = (detailCategorie.value.epreuves.maxOfOrNull { it.ordre } ?: 0) + 1
            _formulaireEpreuve.value = EtatFormulaireEpreuve(
                id = e?.id ?: 0, categorieId = categorieId, code = e?.code ?: "", libelle = e?.libelle ?: "",
                ordre = (e?.ordre ?: prochainOrdre).toString(), duree = e?.dureeMinutes?.toString() ?: "",
                obligatoire = e?.obligatoire ?: true, aConfirmer = e?.aConfirmer ?: true, actif = e?.actif ?: true,
            )
        }
    }

    fun modifierEpreuve(t: (EtatFormulaireEpreuve) -> EtatFormulaireEpreuve) = _formulaireEpreuve.update { t(it).copy(erreur = null) }

    fun enregistrerEpreuve(onSucces: () -> Unit) {
        val f = _formulaireEpreuve.value
        val auteur = idUtilisateur() ?: return
        viewModelScope.launch {
            val existantes = detailCategorie.value.epreuves.filter { it.id != f.id }.map { it.code }
            val erreur = ValidationConfiguration.validerEpreuve(f.code, f.libelle, f.ordre, f.duree, existantes)
            if (erreur != null) return@launch _formulaireEpreuve.update { it.copy(erreur = erreur) }
            val ancienne = if (f.id == 0) null else db.typeEpreuveDao().parId(f.id)
            val nouvelle = TypeEpreuve(
                id = f.id, categorieId = f.categorieId, code = f.code.trim().uppercase(), libelle = f.libelle.trim(),
                ordre = f.ordre.toInt(), obligatoire = f.obligatoire, dureeMinutes = f.duree.trim().toIntOrNull(), actif = f.actif, aConfirmer = f.aConfirmer,
            )
            db.withTransaction {
                if (ancienne == null) {
                    val id = db.typeEpreuveDao().inserer(nouvelle).toInt()
                    db.tracer(EntitesHistorique.TYPE_EPREUVE, id, ActionsHistorique.CREATION, auteur, nouvelleValeur = nouvelle.resume())
                } else {
                    db.typeEpreuveDao().modifier(nouvelle)
                    db.tracer(EntitesHistorique.TYPE_EPREUVE, f.id, ActionsHistorique.MODIFICATION, auteur, ancienneValeur = ancienne.resume(), nouvelleValeur = nouvelle.resume())
                }
            }
            epreuveChargee = null
            onSucces()
        }
    }

    // ----- Épreuve : barèmes, questions, critères -----

    private val idEpreuve = MutableStateFlow(0)
    fun afficherEpreuve(id: Int) { idEpreuve.value = id }

    val epreuve: StateFlow<EtatEpreuve> =
        idEpreuve.flatMapLatest { id ->
            combine(
                db.typeEpreuveDao().parIdEnDirect(id), db.categoriePermisDao().toutes(), db.baremeDao().parEpreuve(id),
                db.questionDao().parEpreuve(id),
                db.criterePratiqueDao().parEpreuve(id),
            ) { e, categories, baremes, questions, criteres ->
                EtatEpreuve(epreuve = e, categorie = categories.find { it.id == e?.categorieId }, baremes = baremes, questions = questions, criteres = criteres)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatEpreuve())

    private val _formulaireBareme = MutableStateFlow(EtatFormulaireBareme())
    val formulaireBareme: StateFlow<EtatFormulaireBareme> = _formulaireBareme
    fun modifierBareme(t: (EtatFormulaireBareme) -> EtatFormulaireBareme) = _formulaireBareme.update { t(it).copy(erreur = null) }

    /** Nouvelle version du barème : l'ancienne version courante est fermée à la date du jour, jamais modifiée. */
    fun creerVersionBareme(epreuveId: Int, onSucces: () -> Unit) {
        val f = _formulaireBareme.value
        val auteur = idUtilisateur() ?: return
        viewModelScope.launch {
            val erreur = ValidationConfiguration.validerBareme(f.noteMax, f.seuil)
            if (erreur != null) return@launch _formulaireBareme.update { it.copy(erreur = erreur) }
            db.withTransaction {
                val courant = db.baremeDao().courant(epreuveId)
                if (courant != null) db.baremeDao().modifier(courant.copy(dateFinValidite = dateDuJour()))
                val nouveau = Bareme(
                    typeEpreuveId = epreuveId, version = (courant?.version ?: 0) + 1, noteMax = f.noteMax.toDouble(),
                    seuilReussite = f.seuil.toDouble(), dateDebutValidite = dateDuJour(), aConfirmer = f.aConfirmer,
                )
                val id = db.baremeDao().inserer(nouveau).toInt()
                db.tracer(EntitesHistorique.BAREME, id, ActionsHistorique.CREATION, auteur, ancienneValeur = courant?.resume(), nouvelleValeur = nouveau.resume())
            }
            _formulaireBareme.value = EtatFormulaireBareme()
            onSucces()
        }
    }

    private val _formulaireQuestion = MutableStateFlow(EtatFormulaireQuestion())
    val formulaireQuestion: StateFlow<EtatFormulaireQuestion> = _formulaireQuestion
    fun modifierQuestion(t: (EtatFormulaireQuestion) -> EtatFormulaireQuestion) = _formulaireQuestion.update { t(it).copy(erreur = null) }

    fun creerQuestion(epreuveId: Int, onSucces: () -> Unit) {
        val f = _formulaireQuestion.value
        val auteur = idUtilisateur() ?: return
        viewModelScope.launch {
            val erreur = ValidationConfiguration.validerQuestion(f.enonce, f.points)
            if (erreur != null) return@launch _formulaireQuestion.update { it.copy(erreur = erreur) }
            db.withTransaction {
                val question = Question(
                    typeEpreuveId = epreuveId, enonce = f.enonce.trim(), points = f.points.replace(',', '.').toDouble(),
                    reponseAttendue = f.reponseAttendue.trim().ifBlank { null }, ordre = epreuve.value.questions.size + 1, aConfirmer = f.aConfirmer,
                )
                val id = db.questionDao().inserer(question).toInt()
                db.tracer(EntitesHistorique.QUESTION, id, ActionsHistorique.CREATION, auteur, nouvelleValeur = question.resume())
            }
            _formulaireQuestion.value = EtatFormulaireQuestion()
            onSucces()
        }
    }

    /** Une question ne se supprime pas (d'anciennes évaluations la référencent) : on la désactive. */
    fun basculerQuestion(question: Question) {
        val auteur = idUtilisateur() ?: return
        viewModelScope.launch {
            db.withTransaction {
                val modifiee = question.copy(actif = !question.actif)
                db.questionDao().modifier(modifiee)
                db.tracer(EntitesHistorique.QUESTION, question.id, if (modifiee.actif) ActionsHistorique.REACTIVATION else ActionsHistorique.DESACTIVATION, auteur, nouvelleValeur = modifiee.resume())
            }
        }
    }

    private val _formulaireCritere = MutableStateFlow(EtatFormulaireCritere())
    val formulaireCritere: StateFlow<EtatFormulaireCritere> = _formulaireCritere
    fun modifierCritere(t: (EtatFormulaireCritere) -> EtatFormulaireCritere) = _formulaireCritere.update { t(it).copy(erreur = null) }

    fun creerCritere(epreuveId: Int, onSucces: () -> Unit) {
        val f = _formulaireCritere.value
        val auteur = idUtilisateur() ?: return
        viewModelScope.launch {
            val erreur = ValidationConfiguration.validerCritere(f.libelle, f.points)
            if (erreur != null) return@launch _formulaireCritere.update { it.copy(erreur = erreur) }
            db.withTransaction {
                val critere = CriterePratique(typeEpreuveId = epreuveId, libelle = f.libelle.trim(), points = f.points.toDouble(), eliminatoire = f.eliminatoire, aConfirmer = f.aConfirmer)
                val id = db.criterePratiqueDao().inserer(critere).toInt()
                db.tracer(EntitesHistorique.CRITERE, id, ActionsHistorique.CREATION, auteur, nouvelleValeur = critere.resume())
            }
            _formulaireCritere.value = EtatFormulaireCritere()
            onSucces()
        }
    }

    fun basculerCritere(critere: CriterePratique) {
        val auteur = idUtilisateur() ?: return
        viewModelScope.launch {
            db.withTransaction {
                val modifie = critere.copy(actif = !critere.actif)
                db.criterePratiqueDao().modifier(modifie)
                db.tracer(EntitesHistorique.CRITERE, critere.id, if (modifie.actif) ActionsHistorique.REACTIVATION else ActionsHistorique.DESACTIVATION, auteur, nouvelleValeur = modifie.resume())
            }
        }
    }

    // ----- Règles -----

    val regles: StateFlow<List<RegleLigne>> =
        combine(db.regleConfigDao().toutes(), db.categoriePermisDao().toutes()) { regles, categories ->
            regles.map { r -> RegleLigne(r, categories.find { it.id == r.categorieId }?.code) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _formulaireRegle = MutableStateFlow(EtatFormulaireRegle())
    val formulaireRegle: StateFlow<EtatFormulaireRegle> = _formulaireRegle
    private var regleChargee: Int? = null

    fun preparerRegle(id: Int) {
        if (regleChargee == id) return
        regleChargee = id
        viewModelScope.launch {
            val r = db.regleConfigDao().parId(id)
            _formulaireRegle.value = EtatFormulaireRegle(regle = r, valeur = r?.valeur ?: "", description = r?.description ?: "", aConfirmer = r?.aConfirmer ?: true)
        }
    }

    fun modifierRegle(t: (EtatFormulaireRegle) -> EtatFormulaireRegle) = _formulaireRegle.update { t(it).copy(erreur = null) }

    fun enregistrerRegle(onSucces: () -> Unit) {
        val f = _formulaireRegle.value
        val ancienne = f.regle ?: return
        val auteur = idUtilisateur() ?: return
        viewModelScope.launch {
            val erreur = ValidationConfiguration.validerRegle(f.valeur, ancienne.typeValeur)
            if (erreur != null) return@launch _formulaireRegle.update { it.copy(erreur = erreur) }
            val nouvelle = ancienne.copy(valeur = f.valeur.trim(), description = f.description.trim(), aConfirmer = f.aConfirmer)
            db.withTransaction {
                db.regleConfigDao().modifier(nouvelle)
                db.tracer(EntitesHistorique.REGLE_CONFIG, ancienne.id, ActionsHistorique.MODIFICATION, auteur, ancienneValeur = ancienne.resume(), nouvelleValeur = nouvelle.resume())
            }
            regleChargee = null
            onSucces()
        }
    }

    // ----- Centres -----

    val centres: StateFlow<List<CentreLigne>> =
        combine(db.centreDao().tous(), db.regionDao().toutes()) { centres, regions ->
            centres.map { c -> CentreLigne(c, regions.find { it.id == c.regionId }?.nom ?: "?") }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _formulaireCentre = MutableStateFlow(EtatFormulaireCentre())
    val formulaireCentre: StateFlow<EtatFormulaireCentre> = _formulaireCentre
    private var centreCharge: Int? = null

    fun preparerCentre(id: Int?) {
        if (centreCharge == (id ?: 0)) return
        centreCharge = id ?: 0
        viewModelScope.launch {
            val c = id?.let { db.centreDao().parId(it) }
            _formulaireCentre.value = EtatFormulaireCentre(
                id = c?.id ?: 0, nom = c?.nom ?: "", regionId = c?.regionId ?: session?.regionId, adresse = c?.adresse ?: "",
                capacite = c?.capaciteParDefaut?.toString() ?: "", actif = c?.actif ?: true, regions = db.regionDao().listeActives(),
            )
        }
    }

    fun modifierCentre(t: (EtatFormulaireCentre) -> EtatFormulaireCentre) = _formulaireCentre.update { t(it).copy(erreur = null) }

    fun enregistrerCentre(onSucces: () -> Unit) {
        val f = _formulaireCentre.value
        val auteur = idUtilisateur() ?: return
        viewModelScope.launch {
            val erreur = ValidationConfiguration.validerCentre(f.nom, f.regionId, f.adresse, f.capacite)
            val regionId = f.regionId
            if (erreur != null || regionId == null) return@launch _formulaireCentre.update { it.copy(erreur = erreur ?: "Choisissez la région.") }
            val ancien = if (f.id == 0) null else db.centreDao().parId(f.id)
            val nouveau = Centre(id = f.id, regionId = regionId, nom = f.nom.trim(), adresse = f.adresse.trim(), capaciteParDefaut = f.capacite.trim().toIntOrNull(), actif = f.actif)
            db.withTransaction {
                if (ancien == null) {
                    val id = db.centreDao().inserer(nouveau).toInt()
                    db.tracer(EntitesHistorique.CENTRE, id, ActionsHistorique.CREATION, auteur, nouvelleValeur = nouveau.resume())
                } else {
                    db.centreDao().modifier(nouveau)
                    db.tracer(EntitesHistorique.CENTRE, f.id, ActionsHistorique.MODIFICATION, auteur, ancienneValeur = ancien.resume(), nouvelleValeur = nouveau.resume())
                }
            }
            centreCharge = null
            onSucces()
        }
    }
}
