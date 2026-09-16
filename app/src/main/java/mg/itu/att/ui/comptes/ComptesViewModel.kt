package mg.itu.att.ui.comptes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mg.itu.att.data.ActionsHistorique
import mg.itu.att.data.AppDatabase
import mg.itu.att.data.EntitesHistorique
import mg.itu.att.data.Examinateur
import mg.itu.att.data.Region
import mg.itu.att.data.Role
import mg.itu.att.data.Utilisateur
import mg.itu.att.data.resume
import mg.itu.att.data.tracer
import mg.itu.att.metier.ValidationCompte
import mg.itu.att.securite.MotDePasse
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.connexion.SessionUtilisateur

// ---------- ÉTATS ----------

data class ExaminateurLigne(val examinateur: Examinateur, val nomRegion: String, val identifiant: String?)

data class EtatExaminateurs(
    val examinateurs: List<ExaminateurLigne> = emptyList(),
    val regionVerrouillee: Boolean = false,
)

/** Formulaire d'examinateur ; à la création, un compte est créé en même temps (identifiant + mot de passe). */
data class EtatFormulaireExaminateur(
    val id: Int = 0,
    val nom: String = "",
    val matricule: String = "",
    val regionId: Int? = null,
    val identifiant: String = "",
    val motDePasse: String = "",
    val regions: List<Region> = emptyList(),
    val regionVerrouillee: Boolean = false,
    val actif: Boolean = true,
    val erreur: String? = null,
    val enCours: Boolean = false,
)

data class CompteLigne(val utilisateur: Utilisateur, val nomRegion: String?)

data class EtatComptes(val comptes: List<CompteLigne> = emptyList(), val idCourant: Int = 0)

/** Formulaire d'un compte Administrateur ATT (national si `regionId` est null). */
data class EtatFormulaireAdmin(
    val nom: String = "",
    val identifiant: String = "",
    val motDePasse: String = "",
    val regionId: Int? = null,
    val regions: List<Region> = emptyList(),
    val erreur: String? = null,
    val enCours: Boolean = false,
)

data class EtatMotDePasse(
    val ancien: String = "",
    val nouveau: String = "",
    val confirmation: String = "",
    val erreur: String? = null,
    val reussi: Boolean = false,
)

// ---------- LE VIEWMODEL ----------

/**
 * Comptes de connexion (étape C1b) : examinateurs (Admin ATT), administrateurs ATT (Super Admin),
 * changement de son propre mot de passe (tous). Aucune suppression : désactivation tracée.
 */
class ComptesViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private val session = MutableStateFlow<SessionUtilisateur?>(null)

    override fun definirSession(session: SessionUtilisateur) {
        if (this.session.value != session) this.session.value = session
    }

    // ----- Examinateurs -----

    val examinateurs: StateFlow<EtatExaminateurs> =
        combine(db.examinateurDao().tous(), db.regionDao().toutes(), db.utilisateurDao().tous(), session) { exams, regions, comptes, s ->
            val nomsRegions = regions.associate { it.id to it.nom }
            EtatExaminateurs(
                examinateurs = exams
                    .filter { s?.regionId == null || it.regionId == s.regionId }
                    .map { e -> ExaminateurLigne(e, e.regionId?.let { nomsRegions[it] } ?: "Toutes régions", comptes.find { it.examinateurId == e.id }?.identifiant) },
                regionVerrouillee = s?.regionId != null,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatExaminateurs())

    private val _formulaireExaminateur = MutableStateFlow(EtatFormulaireExaminateur())
    val formulaireExaminateur: StateFlow<EtatFormulaireExaminateur> = _formulaireExaminateur
    private var idCharge: Int? = null

    fun preparerExaminateur(examinateurId: Int?) {
        if (idCharge == (examinateurId ?: 0)) return
        idCharge = examinateurId ?: 0
        viewModelScope.launch {
            val existant = examinateurId?.let { db.examinateurDao().parId(it) }
            val regionImposee = session.value?.regionId
            _formulaireExaminateur.value = EtatFormulaireExaminateur(
                id = existant?.id ?: 0,
                nom = existant?.nom ?: "",
                matricule = existant?.matricule ?: "",
                regionId = existant?.regionId ?: regionImposee,
                regions = db.regionDao().listeActives(),
                regionVerrouillee = regionImposee != null,
                actif = existant?.actif ?: true,
            )
        }
    }

    fun modifierExaminateur(transformation: (EtatFormulaireExaminateur) -> EtatFormulaireExaminateur) =
        _formulaireExaminateur.update { transformation(it).copy(erreur = null) }

    /** Crée l'examinateur ET son compte dans une seule transaction, ou modifie la fiche ; historique dans les deux cas. */
    fun enregistrerExaminateur(onSucces: () -> Unit) {
        val f = _formulaireExaminateur.value
        val utilisateur = session.value ?: return
        viewModelScope.launch {
            if (f.nom.isBlank()) {
                _formulaireExaminateur.update { it.copy(erreur = "Le nom est obligatoire.") }
                return@launch
            }
            val creation = f.id == 0
            if (creation) {
                val erreur = ValidationCompte.validerCreation(f.identifiant, f.motDePasse, db.utilisateurDao().tousLesIdentifiants())
                if (erreur != null) {
                    _formulaireExaminateur.update { it.copy(erreur = erreur) }
                    return@launch
                }
            }
            _formulaireExaminateur.update { it.copy(enCours = true) }
            val ancien = if (creation) null else db.examinateurDao().parId(f.id)
            val nouveau = Examinateur(id = f.id, nom = f.nom.trim(), matricule = f.matricule.trim().ifBlank { null }, regionId = f.regionId, actif = ancien?.actif ?: true)
            db.withTransaction {
                if (ancien == null) {
                    val id = db.examinateurDao().inserer(nouveau).toInt()
                    db.tracer(EntitesHistorique.EXAMINATEUR, id, ActionsHistorique.CREATION, utilisateur.id, nouvelleValeur = nouveau.resume())
                    val compteId = db.utilisateurDao().inserer(
                        Utilisateur(identifiant = f.identifiant.trim(), motDePasseHash = MotDePasse.hacher(f.motDePasse), nom = nouveau.nom, role = Role.EXAMINATEUR, regionId = f.regionId, examinateurId = id),
                    ).toInt()
                    db.tracer(EntitesHistorique.UTILISATEUR, compteId, ActionsHistorique.CREATION_COMPTE, utilisateur.id, nouvelleValeur = "compte ${f.identifiant.trim()} (EXAMINATEUR) pour ${nouveau.nom}")
                } else {
                    db.examinateurDao().modifier(nouveau)
                    db.tracer(EntitesHistorique.EXAMINATEUR, f.id, ActionsHistorique.MODIFICATION, utilisateur.id, ancienneValeur = ancien.resume(), nouvelleValeur = nouveau.resume())
                }
            }
            idCharge = null
            _formulaireExaminateur.value = EtatFormulaireExaminateur()
            onSucces()
        }
    }

    /** Désactive ou réactive un examinateur et son compte, avec historique. */
    fun basculerExaminateur(examinateurId: Int) {
        val utilisateur = session.value ?: return
        viewModelScope.launch {
            db.withTransaction {
                val actuel = db.examinateurDao().parId(examinateurId) ?: return@withTransaction
                val modifie = actuel.copy(actif = !actuel.actif)
                db.examinateurDao().modifier(modifie)
                db.tracer(EntitesHistorique.EXAMINATEUR, examinateurId, if (modifie.actif) ActionsHistorique.REACTIVATION else ActionsHistorique.DESACTIVATION, utilisateur.id, ancienneValeur = actuel.resume(), nouvelleValeur = modifie.resume())
                // Le ou les comptes liés suivent l'examinateur.
                db.utilisateurDao().parExaminateur(examinateurId).forEach { compte ->
                    db.utilisateurDao().modifier(compte.copy(actif = modifie.actif))
                    db.tracer(EntitesHistorique.UTILISATEUR, compte.id, if (modifie.actif) ActionsHistorique.REACTIVATION else ActionsHistorique.DESACTIVATION, utilisateur.id, nouvelleValeur = compte.copy(actif = modifie.actif).resume())
                }
            }
            idCharge = null
        }
    }

    // ----- Comptes ATT (Super Admin) -----

    val comptes: StateFlow<EtatComptes> =
        combine(db.utilisateurDao().tous(), db.regionDao().toutes(), session) { utilisateurs, regions, s ->
            val nomsRegions = regions.associate { it.id to it.nom }
            EtatComptes(
                comptes = utilisateurs.map { CompteLigne(it, it.regionId?.let { r -> nomsRegions[r] }) },
                idCourant = s?.id ?: 0,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatComptes())

    private val _formulaireAdmin = MutableStateFlow(EtatFormulaireAdmin())
    val formulaireAdmin: StateFlow<EtatFormulaireAdmin> = _formulaireAdmin

    fun preparerAdmin() {
        viewModelScope.launch { _formulaireAdmin.update { it.copy(regions = db.regionDao().listeActives()) } }
    }

    fun modifierAdmin(transformation: (EtatFormulaireAdmin) -> EtatFormulaireAdmin) =
        _formulaireAdmin.update { transformation(it).copy(erreur = null) }

    /** Crée un compte ADMIN_ATT (réservé au Super Admin), national ou régional. */
    fun creerAdmin(onSucces: () -> Unit) {
        val f = _formulaireAdmin.value
        val utilisateur = session.value ?: return
        if (utilisateur.role != Role.SUPER_ADMIN) return
        viewModelScope.launch {
            val erreur = if (f.nom.isBlank()) "Le nom est obligatoire." else ValidationCompte.validerCreation(f.identifiant, f.motDePasse, db.utilisateurDao().tousLesIdentifiants())
            if (erreur != null) {
                _formulaireAdmin.update { it.copy(erreur = erreur) }
                return@launch
            }
            _formulaireAdmin.update { it.copy(enCours = true) }
            db.withTransaction {
                val nouveau = Utilisateur(identifiant = f.identifiant.trim(), motDePasseHash = MotDePasse.hacher(f.motDePasse), nom = f.nom.trim(), role = Role.ADMIN_ATT, regionId = f.regionId)
                val id = db.utilisateurDao().inserer(nouveau).toInt()
                db.tracer(EntitesHistorique.UTILISATEUR, id, ActionsHistorique.CREATION_COMPTE, utilisateur.id, nouvelleValeur = nouveau.copy(id = id).resume())
            }
            _formulaireAdmin.value = EtatFormulaireAdmin()
            onSucces()
        }
    }

    /** Désactive ou réactive un compte (jamais le sien), avec historique. */
    fun basculerCompte(compteId: Int) {
        val utilisateur = session.value ?: return
        if (utilisateur.role != Role.SUPER_ADMIN || compteId == utilisateur.id) return
        viewModelScope.launch {
            db.withTransaction {
                val actuel = db.utilisateurDao().parId(compteId) ?: return@withTransaction
                val modifie = actuel.copy(actif = !actuel.actif)
                db.utilisateurDao().modifier(modifie)
                db.tracer(EntitesHistorique.UTILISATEUR, compteId, if (modifie.actif) ActionsHistorique.REACTIVATION else ActionsHistorique.DESACTIVATION, utilisateur.id, ancienneValeur = actuel.resume(), nouvelleValeur = modifie.resume())
            }
        }
    }

    // ----- Mon mot de passe -----

    private val _motDePasse = MutableStateFlow(EtatMotDePasse())
    val motDePasse: StateFlow<EtatMotDePasse> = _motDePasse

    fun modifierMotDePasse(transformation: (EtatMotDePasse) -> EtatMotDePasse) =
        _motDePasse.update { transformation(it).copy(erreur = null, reussi = false) }

    /** Change le mot de passe de l'utilisateur connecté après vérification de l'ancien. */
    fun changerMotDePasse() {
        val f = _motDePasse.value
        val utilisateur = session.value ?: return
        viewModelScope.launch {
            val erreur = ValidationCompte.validerChangement(f.ancien, f.nouveau, f.confirmation)
            val compte = db.utilisateurDao().parId(utilisateur.id)
            when {
                erreur != null -> _motDePasse.update { it.copy(erreur = erreur) }
                compte == null || !MotDePasse.verifier(f.ancien, compte.motDePasseHash) ->
                    _motDePasse.update { it.copy(erreur = "Le mot de passe actuel est incorrect.") }
                else -> {
                    db.withTransaction {
                        db.utilisateurDao().modifier(compte.copy(motDePasseHash = MotDePasse.hacher(f.nouveau)))
                        db.tracer(EntitesHistorique.UTILISATEUR, compte.id, "CHANGEMENT_MOT_DE_PASSE", utilisateur.id)
                    }
                    _motDePasse.value = EtatMotDePasse(reussi = true)
                }
            }
        }
    }
}
