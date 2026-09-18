package mg.itu.att.ui.sessions

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
import mg.itu.att.data.CategoriePermis
import mg.itu.att.data.Centre
import mg.itu.att.data.ClesRegles
import mg.itu.att.data.Creneau
import mg.itu.att.data.EntitesHistorique
import mg.itu.att.data.Historique
import mg.itu.att.data.Inscription
import mg.itu.att.data.Role
import mg.itu.att.data.Session
import mg.itu.att.data.StatutInscription
import mg.itu.att.data.StatutSession
import mg.itu.att.data.TypeEpreuve
import mg.itu.att.data.resume
import mg.itu.att.data.tracer
import mg.itu.att.metier.ReglesPlanification
import mg.itu.att.metier.ValidationCandidat
import mg.itu.att.metier.dateDuJour
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.connexion.SessionUtilisateur

// ---------- ÉTATS ----------

/** Une session avec les libellés utiles à l'affichage (jointures faites dans le ViewModel). */
data class SessionLigne(
    val session: Session, val nomCentre: String, val nomRegion: String, val codeCategorie: String, val libelleEpreuve: String,
    val inscrits: Int,
)

enum class FiltreSessions { A_VENIR, DU_JOUR, PASSEES, TOUTES }

data class EtatListeSessions(
    val sessions: List<SessionLigne> = emptyList(),
    val filtre: FiltreSessions = FiltreSessions.A_VENIR,
    /** Vrai pour l'ATT : peut créer, ouvrir, annuler. */
    val peutGerer: Boolean = false,
)

data class EtatFormulaireSession(
    val categorieId: Int? = null, val typeEpreuveId: Int? = null, val centreId: Int? = null,
    val date: String = "", val heureConvocation: String = "08:00",
    val capacite: String = "", val capaciteCreneau: String = "", val dureeMin: String = "", val margeMin: String = "",
    val categories: List<CategoriePermis> = emptyList(), val epreuves: List<TypeEpreuve> = emptyList(), val centres: List<Centre> = emptyList(),
    val avertissement: String? = null, val erreur: String? = null, val enCours: Boolean = false,
) {
    /** Aperçu des créneaux que la saisie produirait (recalculé à chaque frappe, fonction pure). */
    val apercuCreneaux: List<ReglesPlanification.CreneauCalcule>
        get() = ReglesPlanification.genererCreneaux(heureConvocation, capacite.toIntOrNull() ?: 0, capaciteCreneau.toIntOrNull() ?: 0, dureeMin.toIntOrNull() ?: 0, margeMin.toIntOrNull() ?: 0)
}

data class CreneauLigne(val creneau: Creneau, val inscrits: Int)

data class EtatDetailSession(
    val ligne: SessionLigne? = null,
    val creneaux: List<CreneauLigne> = emptyList(),
    val inscriptions: List<Inscription> = emptyList(),
    val historique: List<Historique> = emptyList(),
    val erreur: String? = null,
    val peutGerer: Boolean = false,
)

/** Le motif d'annulation, exposé à part : la frappe ne doit pas traverser le flux du détail (défaut vu en D1). */
data class SaisieSession(val motif: String = "", val erreur: String? = null)

// ---------- LE VIEWMODEL ----------

/**
 * Sessions d'examen et créneaux (UC06). Une session = catégorie, épreuve, centre, date, capacité ;
 * ses créneaux sont générés à la création (fonction pure `genererCreneaux`). Jamais de suppression : annulation tracée.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private val session = MutableStateFlow<SessionUtilisateur?>(null)

    override fun definirSession(session: SessionUtilisateur) {
        if (this.session.value != session) this.session.value = session
    }

    private fun estAtt(s: SessionUtilisateur?) = s?.role == Role.ADMIN_ATT || s?.role == Role.SUPER_ADMIN

    // ----- Liste -----

    private val filtre = MutableStateFlow(FiltreSessions.A_VENIR)
    fun filtrer(f: FiltreSessions) { filtre.value = f }

    /** Sessions + libellés, avant filtre d'écran ; un administrateur régional ne voit que sa région. */
    private val lignes = combine(
        db.sessionDao().toutes(), db.centreDao().tous(), db.regionDao().toutes(),
        combine(db.categoriePermisDao().toutes(), db.typeEpreuveDao().toutes()) { c, e -> c to e },
        combine(db.inscriptionDao().toutes(), session) { i, s -> i to s },
    ) { sessions, centres, regions, (categories, epreuves), (inscriptions, s) ->
        sessions.mapNotNull { se ->
            val centre = centres.find { it.id == se.centreId }
            if (s?.regionId != null && centre?.regionId != s.regionId) return@mapNotNull null
            SessionLigne(
                session = se, nomCentre = centre?.nom ?: "?", nomRegion = regions.find { it.id == centre?.regionId }?.nom ?: "?",
                codeCategorie = categories.find { it.id == se.categorieId }?.code ?: "?", libelleEpreuve = epreuves.find { it.id == se.typeEpreuveId }?.libelle ?: "?",
                inscrits = inscriptions.count { it.sessionId == se.id && it.statut != StatutInscription.ANNULE },
            )
        }
    }

    val liste: StateFlow<EtatListeSessions> =
        combine(lignes, filtre, session) { lignes, f, s ->
            val aujourdHui = dateDuJour()
            EtatListeSessions(
                sessions = when (f) {
                    FiltreSessions.A_VENIR -> lignes.filter { it.session.date >= aujourdHui && it.session.statut != StatutSession.ANNULEE }.sortedBy { it.session.date + it.session.heureConvocation }
                    FiltreSessions.DU_JOUR -> lignes.filter { it.session.date == aujourdHui }.sortedBy { it.session.heureConvocation }
                    FiltreSessions.PASSEES -> lignes.filter { it.session.date < aujourdHui }.sortedByDescending { it.session.date }
                    FiltreSessions.TOUTES -> lignes.sortedByDescending { it.session.date }
                },
                filtre = f, peutGerer = estAtt(s),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatListeSessions())

    // ----- Formulaire -----

    private val _formulaire = MutableStateFlow(EtatFormulaireSession())
    val formulaire: StateFlow<EtatFormulaireSession> = _formulaire
    private var formulairePrepare = false

    /** Préremplit durée, marge et capacités depuis les règles configurables (jamais codées en dur). */
    fun preparerFormulaire() {
        if (formulairePrepare) return
        formulairePrepare = true
        viewModelScope.launch {
            val s = session.value
            suspend fun regle(cle: String): String = db.regleConfigDao().pour(cle, null)?.valeur ?: ""
            // Les centres actifs, limités à la région d'un administrateur régional.
            val centres = db.centreDao().listeActifs().filter { s?.regionId == null || it.regionId == s.regionId }
            _formulaire.value = EtatFormulaireSession(
                categories = db.categoriePermisDao().listeActives(),
                centres = centres, centreId = centres.singleOrNull()?.id,
                capacite = regle(ClesRegles.CAPACITE_SESSION_DEFAUT), capaciteCreneau = regle(ClesRegles.CAPACITE_CRENEAU_DEFAUT),
                dureeMin = regle(ClesRegles.DUREE_CRENEAU_MIN), margeMin = regle(ClesRegles.MARGE_CRENEAU_MIN),
            )
        }
    }

    fun modifierFormulaire(t: (EtatFormulaireSession) -> EtatFormulaireSession) = _formulaire.update { t(it).copy(erreur = null) }

    /** Changer de catégorie recharge ses épreuves (la capacité par défaut du centre s'applique si connue). */
    fun choisirCategorie(categorieId: Int?) {
        viewModelScope.launch {
            val epreuves = categorieId?.let { db.typeEpreuveDao().listeActivesPourCategorie(it) } ?: emptyList()
            _formulaire.update { it.copy(categorieId = categorieId, epreuves = epreuves, typeEpreuveId = epreuves.firstOrNull()?.id, erreur = null) }
        }
    }

    fun choisirCentre(centreId: Int?) {
        val centre = _formulaire.value.centres.find { it.id == centreId }
        _formulaire.update { f ->
            f.copy(centreId = centreId, capacite = centre?.capaciteParDefaut?.toString() ?: f.capacite, erreur = null)
        }
        verifierConflit()
    }

    fun changerDate(date: String) {
        _formulaire.update { it.copy(date = date, erreur = null) }
        verifierConflit()
    }

    private fun verifierConflit() {
        val f = _formulaire.value
        val centreId = f.centreId ?: return
        if (!ValidationCandidat.dateValide(f.date)) return
        viewModelScope.launch {
            val autres = db.sessionDao().memeCentreMemeJour(centreId, f.date).size
            _formulaire.update { it.copy(avertissement = ReglesPlanification.avertissementConflit(autres)) }
        }
    }

    /** Crée la session et ses créneaux dans une seule transaction, avec historique, puis appelle [onSucces]. */
    fun enregistrer(onSucces: (Int) -> Unit) {
        val f = _formulaire.value
        val utilisateur = session.value ?: return
        if (!estAtt(utilisateur)) return
        viewModelScope.launch {
            val erreur = ReglesPlanification.validerSession(f.categorieId, f.typeEpreuveId, f.centreId, f.date, f.heureConvocation, f.capacite, f.capaciteCreneau, f.dureeMin, f.margeMin, dateDuJour())
            if (erreur != null) return@launch _formulaire.update { it.copy(erreur = erreur) }
            val creneaux = f.apercuCreneaux
            if (creneaux.isEmpty()) return@launch _formulaire.update { it.copy(erreur = "Impossible de générer les créneaux avec ces valeurs.") }
            _formulaire.update { it.copy(enCours = true) }
            val nouvelle = Session(
                categorieId = f.categorieId ?: 0, typeEpreuveId = f.typeEpreuveId ?: 0, centreId = f.centreId ?: 0, date = f.date,
                heureConvocation = f.heureConvocation, capacite = f.capacite.toInt(), dureeCreneauMin = f.dureeMin.toInt(), margeMin = f.margeMin.toInt(),
                statut = StatutSession.PLANIFIEE, creeParId = utilisateur.id,
            )
            val id = db.withTransaction {
                val id = db.sessionDao().inserer(nouvelle).toInt()
                db.creneauDao().insererTous(creneaux.map { Creneau(sessionId = id, ordre = it.ordre, heureDebut = it.heureDebut, heureFinEstimee = it.heureFinEstimee, capacite = it.capacite) })
                db.tracer(EntitesHistorique.SESSION, id, ActionsHistorique.CREATION, utilisateur.id, nouvelleValeur = nouvelle.copy(id = id).resume() + ", ${creneaux.size} créneau(x)")
                id
            }
            formulairePrepare = false
            _formulaire.value = EtatFormulaireSession()
            onSucces(id)
        }
    }

    // ----- Détail -----

    private val idDetail = MutableStateFlow(0)
    private val erreurDetail = MutableStateFlow<String?>(null)

    private val _saisieSession = MutableStateFlow(SaisieSession())
    val saisieSession: StateFlow<SaisieSession> = _saisieSession

    fun afficherDetail(sessionId: Int) {
        if (idDetail.value != sessionId) { idDetail.value = sessionId; _saisieSession.value = SaisieSession(); erreurDetail.value = null }
    }

    fun changerMotif(texte: String) = _saisieSession.update { it.copy(motif = texte, erreur = null) }

    val detail: StateFlow<EtatDetailSession> =
        idDetail.flatMapLatest { id ->
            combine(
                lignes, db.creneauDao().parSession(id), db.inscriptionDao().parSession(id), db.historiqueDao().pourObjet(EntitesHistorique.SESSION, id),
                combine(erreurDetail, session) { e, s -> e to s },
            ) { lignes, creneaux, inscriptions, historique, (e, s) ->
                EtatDetailSession(
                    ligne = lignes.find { it.session.id == id },
                    creneaux = creneaux.map { c -> CreneauLigne(c, inscriptions.count { it.creneauId == c.id && it.statut != StatutInscription.ANNULE }) },
                    inscriptions = inscriptions, historique = historique, erreur = e, peutGerer = estAtt(s),
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatDetailSession())

    /**
     * Change le statut d'une session (ouvrir, démarrer, terminer, annuler) avec historique.
     * L'annulation exige un motif et annule les inscriptions actives (cadrage §11).
     */
    fun changerStatut(sessionId: Int, nouveau: StatutSession) {
        val utilisateur = session.value ?: return
        if (!estAtt(utilisateur)) return
        val texteMotif = _saisieSession.value.motif.trim()
        if (nouveau == StatutSession.ANNULEE && texteMotif.isBlank()) return run { erreurDetail.value = "Indiquez le motif de l'annulation." }
        viewModelScope.launch {
            db.withTransaction {
                val actuelle = db.sessionDao().parId(sessionId) ?: return@withTransaction
                val modifiee = actuelle.copy(statut = nouveau)
                db.sessionDao().modifier(modifiee)
                val action = if (nouveau == StatutSession.ANNULEE) ActionsHistorique.ANNULATION else ActionsHistorique.MODIFICATION
                db.tracer(EntitesHistorique.SESSION, sessionId, action, utilisateur.id, ancienneValeur = actuelle.resume(), nouvelleValeur = modifiee.resume(), motif = texteMotif.ifBlank { null })
                if (nouveau == StatutSession.ANNULEE) {
                    db.inscriptionDao().listePourSession(sessionId).filter { it.statut != StatutInscription.ANNULE }.forEach { i ->
                        db.inscriptionDao().modifier(i.copy(statut = StatutInscription.ANNULE, motif = "Session annulée : $texteMotif"))
                        db.tracer(EntitesHistorique.INSCRIPTION, i.id, ActionsHistorique.ANNULATION, utilisateur.id, motif = "Session annulée : $texteMotif")
                    }
                }
            }
            _saisieSession.value = SaisieSession()
        }
    }
}
