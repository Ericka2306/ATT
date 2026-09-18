package mg.itu.att.ui.autoecoles

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
import mg.itu.att.data.AutoEcole
import mg.itu.att.data.EntitesHistorique
import mg.itu.att.data.Historique
import mg.itu.att.data.Region
import mg.itu.att.data.Role
import mg.itu.att.data.Utilisateur
import mg.itu.att.data.resume
import mg.itu.att.data.tracer
import mg.itu.att.metier.ReglesConsultation
import mg.itu.att.metier.ValidationAutoEcole
import mg.itu.att.securite.MotDePasse
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.connexion.SessionUtilisateur

// ---------- ÉTATS ----------

/** Une auto-école accompagnée du nom de sa région (jointure faite dans le ViewModel). */
data class AutoEcoleAvecRegion(val autoEcole: AutoEcole, val nomRegion: String)

data class EtatListeAutoEcoles(
    val autoEcoles: List<AutoEcoleAvecRegion> = emptyList(),
    val regions: List<Region> = emptyList(),
    val regionFiltreId: Int? = null,
    /** Vrai pour un administrateur régional : le filtre est imposé. */
    val regionVerrouillee: Boolean = false,
)

data class EtatFormulaireAutoEcole(
    val id: Int = 0,
    val nom: String = "",
    val regionId: Int? = null,
    val numeroAgrement: String = "",
    val adresse: String = "",
    val telephone: String = "",
    val regions: List<Region> = emptyList(),
    val regionVerrouillee: Boolean = false,
    val erreur: String? = null,
    val enCours: Boolean = false,
)

data class EtatDetailAutoEcole(
    val autoEcole: AutoEcoleAvecRegion? = null,
    val comptes: List<Utilisateur> = emptyList(),
    val historique: List<Historique> = emptyList(),
)

data class EtatFormulaireCompte(
    val identifiant: String = "",
    val motDePasse: String = "",
    val erreur: String? = null,
    val enCours: Boolean = false,
)

// ---------- LE VIEWMODEL ----------

/**
 * Auto-écoles et leurs comptes (UC03). Toute écriture passe par `withTransaction` avec sa ligne d'historique.
 * Partagé par les quatre écrans du sous-parcours (voir `viewModelDuSousParcours`).
 */
class AutoEcolesViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private var session: SessionUtilisateur? = null

    override fun definirSession(session: SessionUtilisateur) {
        if (this.session == session) return
        this.session = session
        regionFiltre.value = session.regionId
    }

    /**
     * L'auteur d'une écriture, ou null si le rôle connecté n'a pas le droit de gérer les auto-écoles
     * et leurs comptes (règle R12 : vérifié dans le ViewModel, pas seulement dans le menu).
     */
    private fun auteurAutorise(): SessionUtilisateur? = session?.takeIf { ReglesConsultation.peutGererComptes(it.role) }

    // ----- Liste -----

    private val regionFiltre = MutableStateFlow<Int?>(null)

    val liste: StateFlow<EtatListeAutoEcoles> =
        combine(db.autoEcoleDao().toutes(), db.regionDao().toutes(), regionFiltre) { autoEcoles, regions, filtre ->
            val nomsRegions = regions.associate { it.id to it.nom }
            EtatListeAutoEcoles(
                autoEcoles = autoEcoles
                    .filter { filtre == null || it.regionId == filtre }
                    .map { AutoEcoleAvecRegion(it, nomsRegions[it.regionId] ?: "?") },
                regions = regions.filter { it.actif },
                regionFiltreId = filtre,
                regionVerrouillee = session?.regionId != null,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatListeAutoEcoles())

    fun filtrerParRegion(regionId: Int?) {
        if (session?.regionId == null) regionFiltre.value = regionId
    }

    // ----- Formulaire -----

    private val _formulaire = MutableStateFlow(EtatFormulaireAutoEcole())
    val formulaire: StateFlow<EtatFormulaireAutoEcole> = _formulaire
    private var idCharge: Int? = null

    /** Prépare le formulaire (vide ou rempli). Idempotent : rappelé à chaque recomposition sans effacer la saisie. */
    fun preparerFormulaire(autoEcoleId: Int?) {
        if (idCharge == (autoEcoleId ?: 0)) return
        idCharge = autoEcoleId ?: 0
        viewModelScope.launch {
            val existante = autoEcoleId?.let { db.autoEcoleDao().parId(it) }
            val regionImposee = session?.regionId
            _formulaire.value = EtatFormulaireAutoEcole(
                id = existante?.id ?: 0,
                nom = existante?.nom ?: "",
                regionId = existante?.regionId ?: regionImposee,
                numeroAgrement = existante?.numeroAgrement ?: "",
                adresse = existante?.adresse ?: "",
                telephone = existante?.telephone ?: "",
                regions = db.regionDao().listeActives(),
                regionVerrouillee = regionImposee != null,
            )
        }
    }

    /** Un seul point d'entrée pour toute saisie : l'écran signale `it.copy(champ = valeur)` (HORS_COURS n° 22). */
    fun modifierFormulaire(transformation: (EtatFormulaireAutoEcole) -> EtatFormulaireAutoEcole) =
        _formulaire.update { transformation(it).copy(erreur = null) }

    /** Crée ou modifie l'auto-école, avec sa ligne d'historique, puis appelle [onSucces] avec son id. */
    fun enregistrer(onSucces: (Int) -> Unit) {
        val f = _formulaire.value
        val utilisateur = auteurAutorise() ?: return
        viewModelScope.launch {
            val regionId = f.regionId
            val nomsExistants = if (regionId != null) db.autoEcoleDao().nomsDansRegion(regionId, f.id) else emptyList()
            val erreur = ValidationAutoEcole.validerFiche(f.nom, regionId, f.adresse, nomsExistants)
            if (erreur != null || regionId == null) {
                _formulaire.update { it.copy(erreur = erreur ?: "Choisissez la région.") }
                return@launch
            }
            _formulaire.update { it.copy(enCours = true) }
            val ancienne = if (f.id == 0) null else db.autoEcoleDao().parId(f.id)
            val nouvelle = AutoEcole(
                id = f.id,
                regionId = regionId,
                nom = f.nom.trim(),
                numeroAgrement = f.numeroAgrement.trim().ifBlank { null },
                adresse = f.adresse.trim(),
                telephone = f.telephone.trim().ifBlank { null },
                actif = ancienne?.actif ?: true,
            )
            val id = db.withTransaction {
                if (ancienne == null) {
                    val id = db.autoEcoleDao().inserer(nouvelle).toInt()
                    db.tracer(EntitesHistorique.AUTO_ECOLE, id, ActionsHistorique.CREATION, utilisateur.id, nouvelleValeur = nouvelle.resume())
                    id
                } else {
                    db.autoEcoleDao().modifier(nouvelle)
                    db.tracer(
                        EntitesHistorique.AUTO_ECOLE, f.id, ActionsHistorique.MODIFICATION, utilisateur.id,
                        ancienneValeur = ancienne.resume(), nouvelleValeur = nouvelle.resume(),
                    )
                    f.id
                }
            }
            idCharge = null
            _formulaire.value = EtatFormulaireAutoEcole()
            onSucces(id)
        }
    }

    // ----- Détail -----

    private val idDetail = MutableStateFlow(0)

    fun afficherDetail(autoEcoleId: Int) {
        idDetail.value = autoEcoleId
    }

    @OptIn(ExperimentalCoroutinesApi::class) // flatMapLatest est encore marqué « expérimental » (HORS_COURS n° 18)
    val detail: StateFlow<EtatDetailAutoEcole> =
        idDetail.flatMapLatest { id ->
            combine(
                db.autoEcoleDao().parIdEnDirect(id),
                db.regionDao().toutes(),
                db.utilisateurDao().parAutoEcole(id),
                db.historiqueDao().pourObjet(EntitesHistorique.AUTO_ECOLE, id),
            ) { autoEcole, regions, comptes, historique ->
                EtatDetailAutoEcole(
                    autoEcole = autoEcole?.let { AutoEcoleAvecRegion(it, regions.find { r -> r.id == it.regionId }?.nom ?: "?") },
                    comptes = comptes,
                    historique = historique,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatDetailAutoEcole())

    /** Désactive ou réactive l'auto-école (jamais de suppression), avec historique. */
    fun basculerActif(autoEcoleId: Int) {
        val utilisateur = auteurAutorise() ?: return
        viewModelScope.launch {
            db.withTransaction {
                val actuelle = db.autoEcoleDao().parId(autoEcoleId) ?: return@withTransaction
                val modifiee = actuelle.copy(actif = !actuelle.actif)
                db.autoEcoleDao().modifier(modifiee)
                db.tracer(
                    EntitesHistorique.AUTO_ECOLE, autoEcoleId,
                    if (modifiee.actif) ActionsHistorique.REACTIVATION else ActionsHistorique.DESACTIVATION,
                    utilisateur.id, ancienneValeur = actuelle.resume(), nouvelleValeur = modifiee.resume(),
                )
            }
        }
    }

    // ----- Compte de connexion -----

    private val _compte = MutableStateFlow(EtatFormulaireCompte())
    val compte: StateFlow<EtatFormulaireCompte> = _compte

    fun modifierCompte(transformation: (EtatFormulaireCompte) -> EtatFormulaireCompte) =
        _compte.update { transformation(it).copy(erreur = null) }

    /** Crée le compte AUTO_ECOLE lié, mot de passe haché, avec historique, puis appelle [onSucces]. */
    fun creerCompte(autoEcoleId: Int, onSucces: () -> Unit) {
        val c = _compte.value
        val utilisateur = auteurAutorise() ?: return
        viewModelScope.launch {
            val erreur = ValidationAutoEcole.validerCompte(c.identifiant, c.motDePasse, db.utilisateurDao().tousLesIdentifiants())
            val autoEcole = db.autoEcoleDao().parId(autoEcoleId)
            when {
                erreur != null -> _compte.update { it.copy(erreur = erreur) }
                autoEcole == null -> _compte.update { it.copy(erreur = "Auto-école introuvable.") }
                else -> {
                    _compte.update { it.copy(enCours = true) }
                    db.withTransaction {
                        val id = db.utilisateurDao().inserer(
                            Utilisateur(
                                identifiant = c.identifiant.trim(),
                                motDePasseHash = MotDePasse.hacher(c.motDePasse),
                                nom = autoEcole.nom,
                                role = Role.AUTO_ECOLE,
                                regionId = autoEcole.regionId,
                                autoEcoleId = autoEcoleId,
                            ),
                        ).toInt()
                        db.tracer(EntitesHistorique.UTILISATEUR, id, ActionsHistorique.CREATION_COMPTE, utilisateur.id, nouvelleValeur = "compte ${c.identifiant.trim()} (AUTO_ECOLE) pour ${autoEcole.nom}")
                        db.tracer(EntitesHistorique.AUTO_ECOLE, autoEcoleId, ActionsHistorique.CREATION_COMPTE, utilisateur.id, nouvelleValeur = "compte ${c.identifiant.trim()}")
                    }
                    _compte.value = EtatFormulaireCompte()
                    onSucces()
                }
            }
        }
    }
}
