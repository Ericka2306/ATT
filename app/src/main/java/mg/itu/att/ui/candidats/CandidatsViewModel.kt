package mg.itu.att.ui.candidats

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mg.itu.att.data.ActionsHistorique
import mg.itu.att.data.AppDatabase
import mg.itu.att.data.AutoEcole
import mg.itu.att.data.Candidat
import mg.itu.att.data.CategoriePermis
import mg.itu.att.data.ClesRegles
import mg.itu.att.data.Dossier
import mg.itu.att.data.EntitesHistorique
import mg.itu.att.data.Historique
import mg.itu.att.data.Inscription
import mg.itu.att.data.PieceDossier
import mg.itu.att.data.Presence
import mg.itu.att.data.Role
import mg.itu.att.data.Session
import mg.itu.att.data.StatutDossier
import mg.itu.att.data.Tentative
import mg.itu.att.data.Utilisateur
import mg.itu.att.data.resume
import mg.itu.att.data.tracer
import mg.itu.att.metier.ReglesConsultation
import mg.itu.att.metier.ReglesDossier
import mg.itu.att.metier.ValidationCandidat
import mg.itu.att.metier.ValidationCompte
import mg.itu.att.metier.dateDuJour
import mg.itu.att.securite.MotDePasse
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.connexion.SessionUtilisateur

// ---------- ÉTATS ----------

data class CandidatLigne(val candidat: Candidat, val nomAutoEcole: String, val dernierDossier: Dossier?, val codeCategorie: String?)

data class EtatListeCandidats(
    val candidats: List<CandidatLigne> = emptyList(),
    val autoEcoles: List<AutoEcole> = emptyList(),
    val autoEcoleFiltreId: Int? = null,
    /** Vrai pour une auto-école : elle ne voit que ses candidats (instructions §7). */
    val autoEcoleVerrouillee: Boolean = false,
    val recherche: String = "",
)

data class EtatFormulaireCandidat(
    val id: Int = 0,
    val nom: String = "",
    val prenom: String = "",
    val dateNaissance: String = "",
    val cin: String = "",
    val telephone: String = "",
    val adresse: String = "",
    val autoEcoleId: Int? = null,
    val autoEcoles: List<AutoEcole> = emptyList(),
    val autoEcoleVerrouillee: Boolean = false,
    val erreur: String? = null,
    val enCours: Boolean = false,
)

data class DossierLigne(val dossier: Dossier, val codeCategorie: String, val libelleCategorie: String)

/** Une inscription du candidat avec sa session, son créneau et sa présence (le parcours, UC12). */
data class InscriptionParcours(
    val inscription: Inscription,
    val session: Session?,
    val libelleSession: String,
    val nomCentre: String,
    val heureCreneau: String?,
    val presence: Presence?,
    val aVenir: Boolean,
)

/** Un passage d'épreuve du candidat (« tentative » dans le cadrage), jamais effacé. */
data class PassageParcours(val tentative: Tentative, val libelleEpreuve: String, val libelleSession: String)

data class EtatDetailCandidat(
    val candidat: Candidat? = null,
    val nomAutoEcole: String = "",
    val dossiers: List<DossierLigne> = emptyList(),
    val categories: List<CategoriePermis> = emptyList(),
    val categorieChoisieId: Int? = null,
    val inscriptions: List<InscriptionParcours> = emptyList(),
    val passages: List<PassageParcours> = emptyList(),
    val comptes: List<Utilisateur> = emptyList(),
    val historique: List<Historique> = emptyList(),
    /** L'ATT et l'auto-école modifient la fiche et ouvrent des dossiers ; le candidat lit seulement. */
    val peutGerer: Boolean = false,
    val peutCreerCompte: Boolean = false,
    val erreur: String? = null,
)

data class EtatFormulaireCompteCandidat(
    val identifiant: String = "",
    val motDePasse: String = "",
    val erreur: String? = null,
    val enCours: Boolean = false,
)

data class EtatDossier(
    val dossier: Dossier? = null,
    val candidat: Candidat? = null,
    val categorie: CategoriePermis? = null,
    val pieces: List<PieceDossier> = emptyList(),
    val motif: String = "",
    val historique: List<Historique> = emptyList(),
    /** L'auto-école ou l'ATT peut soumettre un dossier BROUILLON ou INCOMPLET. */
    val peutSoumettre: Boolean = false,
    /** Seule l'ATT décide, et seulement sur un dossier SOUMIS. */
    val peutDecider: Boolean = false,
    val erreur: String? = null,
)

data class DossierATraiter(val dossier: Dossier, val nomCandidat: String, val codeCategorie: String, val nomAutoEcole: String)

// ---------- LE VIEWMODEL ----------

/**
 * Candidats et dossiers (UC04, UC05). Un candidat existe sans compte (smartphone non obligatoire).
 * Le filtrage par rôle se fait ici : une auto-école ne voit que ses candidats, un administrateur régional sa région.
 */
@OptIn(ExperimentalCoroutinesApi::class) // flatMapLatest (HORS_COURS n° 18)
class CandidatsViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private val session = MutableStateFlow<SessionUtilisateur?>(null)

    override fun definirSession(session: SessionUtilisateur) {
        if (this.session.value == session) return
        this.session.value = session
        autoEcoleFiltre.value = session.autoEcoleId
    }

    private fun estAtt(s: SessionUtilisateur?) = s?.role == Role.ADMIN_ATT || s?.role == Role.SUPER_ADMIN

    // ----- Liste -----

    private val autoEcoleFiltre = MutableStateFlow<Int?>(null)
    private val recherche = MutableStateFlow("")

    /** Les candidats visibles par l'utilisateur connecté (règle §7), avant filtres d'écran. */
    private val candidatsVisibles = session.flatMapLatest { s ->
        when {
            s?.autoEcoleId != null -> db.candidatDao().parAutoEcole(s.autoEcoleId)
            s?.regionId != null -> db.candidatDao().parRegion(s.regionId)
            else -> db.candidatDao().tous()
        }
    }

    val liste: StateFlow<EtatListeCandidats> =
        combine(candidatsVisibles, db.autoEcoleDao().toutes(), db.dossierDao().tous(), db.categoriePermisDao().toutes()) { candidats, autoEcoles, dossiers, categories ->
            Triple(candidats, autoEcoles, dossiers to categories)
        }.combine(combine(autoEcoleFiltre, recherche, session) { f, r, s -> Triple(f, r, s) }) { (candidats, autoEcoles, dc), (filtre, texte, s) ->
            val (dossiers, categories) = dc
            val nomsAutoEcoles = autoEcoles.associate { it.id to it.nom }
            val codes = categories.associate { it.id to it.code }
            EtatListeCandidats(
                candidats = candidats
                    .filter { filtre == null || it.autoEcoleId == filtre }
                    .filter { texte.isBlank() || "${it.nom} ${it.prenom}".contains(texte.trim(), ignoreCase = true) }
                    .map { c ->
                        val dernier = dossiers.filter { it.candidatId == c.id }.maxByOrNull { it.id }
                        CandidatLigne(c, nomsAutoEcoles[c.autoEcoleId] ?: "?", dernier, dernier?.let { codes[it.categorieId] })
                    },
                autoEcoles = autoEcoles.filter { it.actif && (s?.regionId == null || it.regionId == s.regionId) },
                autoEcoleFiltreId = filtre,
                autoEcoleVerrouillee = s?.autoEcoleId != null,
                recherche = texte,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatListeCandidats())

    fun filtrerParAutoEcole(id: Int?) {
        if (session.value?.autoEcoleId == null) autoEcoleFiltre.value = id
    }

    fun rechercher(texte: String) {
        recherche.value = texte
    }

    // ----- Formulaire candidat -----

    private val _formulaire = MutableStateFlow(EtatFormulaireCandidat())
    val formulaire: StateFlow<EtatFormulaireCandidat> = _formulaire
    private var idCharge: Int? = null

    fun preparerFormulaire(candidatId: Int?) {
        if (idCharge == (candidatId ?: 0)) return
        idCharge = candidatId ?: 0
        viewModelScope.launch {
            val s = session.value
            val existant = candidatId?.let { db.candidatDao().parId(it) }
            _formulaire.value = EtatFormulaireCandidat(
                id = existant?.id ?: 0,
                nom = existant?.nom ?: "",
                prenom = existant?.prenom ?: "",
                dateNaissance = existant?.dateNaissance ?: "",
                cin = existant?.cin ?: "",
                telephone = existant?.telephone ?: "",
                adresse = existant?.adresse ?: "",
                autoEcoleId = existant?.autoEcoleId ?: s?.autoEcoleId,
                autoEcoles = db.autoEcoleDao().listeActives().filter { s?.regionId == null || it.regionId == s.regionId },
                autoEcoleVerrouillee = s?.autoEcoleId != null,
            )
        }
    }

    fun modifierFormulaire(transformation: (EtatFormulaireCandidat) -> EtatFormulaireCandidat) =
        _formulaire.update { transformation(it).copy(erreur = null) }

    /** Crée ou modifie le candidat, avec historique, puis appelle [onSucces] avec son id. */
    fun enregistrer(onSucces: (Int) -> Unit) {
        val f = _formulaire.value
        val utilisateur = session.value ?: return
        viewModelScope.launch {
            val erreur = ValidationCandidat.validerFiche(f.nom, f.prenom, f.dateNaissance, f.autoEcoleId)
            val autoEcoleId = f.autoEcoleId
            if (erreur != null || autoEcoleId == null) {
                _formulaire.update { it.copy(erreur = erreur ?: "Choisissez l'auto-école.") }
                return@launch
            }
            val homonymes = db.candidatDao().homonymes(f.nom.trim(), f.prenom.trim(), f.dateNaissance).filter { it.id != f.id }
            if (homonymes.isNotEmpty()) {
                _formulaire.update { it.copy(erreur = "Un candidat avec les mêmes nom, prénom et date de naissance existe déjà (n° ${homonymes.first().id}).") }
                return@launch
            }
            _formulaire.update { it.copy(enCours = true) }
            val ancien = if (f.id == 0) null else db.candidatDao().parId(f.id)
            val nouveau = Candidat(
                id = f.id, autoEcoleId = autoEcoleId, nom = f.nom.trim(), prenom = f.prenom.trim(), dateNaissance = f.dateNaissance,
                cin = f.cin.trim().ifBlank { null }, telephone = f.telephone.trim().ifBlank { null }, adresse = f.adresse.trim().ifBlank { null },
                actif = ancien?.actif ?: true,
            )
            val id = db.withTransaction {
                if (ancien == null) {
                    val id = db.candidatDao().inserer(nouveau).toInt()
                    db.tracer(EntitesHistorique.CANDIDAT, id, ActionsHistorique.CREATION, utilisateur.id, nouvelleValeur = nouveau.resume())
                    id
                } else {
                    db.candidatDao().modifier(nouveau)
                    db.tracer(EntitesHistorique.CANDIDAT, f.id, ActionsHistorique.MODIFICATION, utilisateur.id, ancienneValeur = ancien.resume(), nouvelleValeur = nouveau.resume())
                    f.id
                }
            }
            idCharge = null
            _formulaire.value = EtatFormulaireCandidat()
            onSucces(id)
        }
    }

    // ----- Détail candidat -----

    private val idDetail = MutableStateFlow(0)
    private val categorieChoisie = MutableStateFlow<Int?>(null)
    private val erreurDetail = MutableStateFlow<String?>(null)

    fun afficherDetail(candidatId: Int) {
        if (idDetail.value != candidatId) {
            idDetail.value = candidatId
            erreurDetail.value = null
        }
    }

    fun choisirCategorie(id: Int?) {
        categorieChoisie.value = id
        erreurDetail.value = null
    }

    /** Inscriptions et passages du candidat, avec les libellés de session (jointures faites ici). */
    private fun parcours(id: Int) = combine(
        combine(db.inscriptionDao().parCandidat(id), db.tentativeDao().parCandidat(id), db.presenceDao().toutes()) { i, t, p -> Triple(i, t, p) },
        combine(db.sessionDao().toutes(), db.creneauDao().tous(), db.centreDao().tous()) { s, c, ce -> Triple(s, c, ce) },
        combine(db.categoriePermisDao().toutes(), db.typeEpreuveDao().toutes()) { c, e -> c to e },
    ) { (inscriptions, tentatives, presences), (sessions, creneaux, centres), (categories, epreuves) ->
        val aujourdHui = dateDuJour()
        fun libelle(se: Session?) = se?.let {
            ReglesConsultation.libelleSession(it.date, it.heureConvocation, categories.find { c -> c.id == it.categorieId }?.code ?: "?", epreuves.find { e -> e.id == it.typeEpreuveId }?.libelle ?: "?")
        } ?: "Session inconnue"
        val lignes = inscriptions.map { i ->
            val se = sessions.find { it.id == i.sessionId }
            InscriptionParcours(
                inscription = i, session = se, libelleSession = libelle(se),
                nomCentre = centres.find { it.id == se?.centreId }?.nom ?: "?",
                heureCreneau = creneaux.find { it.id == i.creneauId }?.heureDebut,
                presence = presences.find { it.inscriptionId == i.id },
                aVenir = se != null && ReglesConsultation.estAVenir(se.date, aujourdHui),
            )
        }
        val passages = tentatives.map { t ->
            val se = sessions.find { it.id == inscriptions.find { i -> i.id == t.inscriptionId }?.sessionId }
            PassageParcours(t, epreuves.find { it.id == t.typeEpreuveId }?.libelle ?: "?", libelle(se))
        }
        lignes to passages
    }

    val detail: StateFlow<EtatDetailCandidat> =
        idDetail.flatMapLatest { id ->
            combine(
                db.candidatDao().parIdEnDirect(id),
                db.dossierDao().parCandidat(id),
                db.categoriePermisDao().actives(),
                db.autoEcoleDao().toutes(),
                db.historiqueDao().pourObjet(EntitesHistorique.CANDIDAT, id),
            ) { candidat, dossiers, categories, autoEcoles, historique ->
                EtatDetailCandidat(
                    candidat = candidat,
                    nomAutoEcole = autoEcoles.find { it.id == candidat?.autoEcoleId }?.nom ?: "?",
                    dossiers = dossiers.map { d ->
                        val cat = categories.find { it.id == d.categorieId }
                        DossierLigne(d, cat?.code ?: "?", cat?.libelle ?: "")
                    },
                    categories = categories,
                    historique = historique,
                ) to autoEcoles.find { it.id == candidat?.autoEcoleId }?.regionId
            }.combine(combine(parcours(id), db.utilisateurDao().parCandidat(id), session) { p, c, s -> Triple(p, c, s) }) { (etat, regionAutoEcole), (parcours, comptes, s) ->
                val (inscriptions, passages) = parcours
                val c = etat.candidat
                // Un candidat hors du périmètre de l'utilisateur n'est pas affiché : « introuvable », comme un id invalide.
                val visible = c != null && s != null &&
                    ReglesConsultation.peutVoirCandidat(s.role, s.regionId, s.autoEcoleId, s.candidatId, c.id, c.autoEcoleId, regionAutoEcole)
                if (!visible) EtatDetailCandidat() else etat.copy(
                    inscriptions = inscriptions, passages = passages, comptes = comptes,
                    peutGerer = ReglesConsultation.peutGererCandidat(s.role),
                    peutCreerCompte = ReglesConsultation.peutCreerCompteCandidat(s.role) && comptes.isEmpty(),
                )
            }
        }.combine(combine(categorieChoisie, erreurDetail) { c, e -> c to e }) { etat, (choix, erreur) ->
            etat.copy(categorieChoisieId = choix, erreur = erreur)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatDetailCandidat())

    // ----- Compte de connexion du candidat (facultatif, cadrage §10) -----

    private val _compte = MutableStateFlow(EtatFormulaireCompteCandidat())
    val compte: StateFlow<EtatFormulaireCompteCandidat> = _compte

    fun modifierCompte(transformation: (EtatFormulaireCompteCandidat) -> EtatFormulaireCompteCandidat) =
        _compte.update { transformation(it).copy(erreur = null) }

    /** Crée le compte CANDIDAT lié, mot de passe haché, avec historique, puis appelle [onSucces]. */
    fun creerCompte(candidatId: Int, onSucces: () -> Unit) {
        val c = _compte.value
        val utilisateur = session.value ?: return
        if (!ReglesConsultation.peutCreerCompteCandidat(utilisateur.role)) return
        viewModelScope.launch {
            val erreur = ValidationCompte.validerCreation(c.identifiant, c.motDePasse, db.utilisateurDao().tousLesIdentifiants())
            val candidat = db.candidatDao().parId(candidatId)
            when {
                erreur != null -> _compte.update { it.copy(erreur = erreur) }
                candidat == null -> _compte.update { it.copy(erreur = "Candidat introuvable.") }
                else -> {
                    _compte.update { it.copy(enCours = true) }
                    val autoEcole = db.autoEcoleDao().parId(candidat.autoEcoleId)
                    db.withTransaction {
                        val id = db.utilisateurDao().inserer(
                            Utilisateur(
                                identifiant = c.identifiant.trim(),
                                motDePasseHash = MotDePasse.hacher(c.motDePasse),
                                nom = "${candidat.nom} ${candidat.prenom}",
                                role = Role.CANDIDAT,
                                regionId = autoEcole?.regionId,
                                candidatId = candidatId,
                            ),
                        ).toInt()
                        db.tracer(EntitesHistorique.UTILISATEUR, id, ActionsHistorique.CREATION_COMPTE, utilisateur.id, nouvelleValeur = "compte ${c.identifiant.trim()} (CANDIDAT) pour ${candidat.nom} ${candidat.prenom}")
                        db.tracer(EntitesHistorique.CANDIDAT, candidatId, ActionsHistorique.CREATION_COMPTE, utilisateur.id, nouvelleValeur = "compte ${c.identifiant.trim()}")
                    }
                    _compte.value = EtatFormulaireCompteCandidat()
                    onSucces()
                }
            }
        }
    }

    /** Ouvre un dossier BROUILLON pour la catégorie choisie, avec ses pièces attendues, puis appelle [onSucces] avec son id. */
    fun ouvrirDossier(candidatId: Int, onSucces: (Int) -> Unit) {
        val utilisateur = session.value ?: return
        val categorieId = categorieChoisie.value
        if (categorieId == null) {
            erreurDetail.value = "Choisissez la catégorie du permis."
            return
        }
        viewModelScope.launch {
            val erreur = ReglesDossier.peutOuvrirDossier(db.dossierDao().listePourCandidat(candidatId), categorieId)
            if (erreur != null) {
                erreurDetail.value = erreur
                return@launch
            }
            val regle = db.regleConfigDao().pour(ClesRegles.PIECES_DOSSIER, categorieId)
            val pieces = ReglesDossier.piecesDepuisRegle(regle?.valeur, ClesRegles.SEPARATEUR_PIECES)
            val id = db.withTransaction {
                val dossier = Dossier(candidatId = candidatId, categorieId = categorieId)
                val id = db.dossierDao().inserer(dossier).toInt()
                db.pieceDossierDao().insererToutes(pieces.map { PieceDossier(dossierId = id, typePiece = it) })
                db.tracer(EntitesHistorique.DOSSIER, id, ActionsHistorique.CREATION, utilisateur.id, nouvelleValeur = dossier.copy(id = id).resume())
                id
            }
            categorieChoisie.value = null
            onSucces(id)
        }
    }

    // ----- Dossier -----

    private val idDossier = MutableStateFlow(0)
    private val motif = MutableStateFlow("")
    private val erreurDossier = MutableStateFlow<String?>(null)

    fun afficherDossier(dossierId: Int) {
        if (idDossier.value != dossierId) {
            idDossier.value = dossierId
            motif.value = ""
            erreurDossier.value = null
        }
    }

    fun changerMotif(texte: String) {
        motif.value = texte
        erreurDossier.value = null
    }

    val dossier: StateFlow<EtatDossier> =
        idDossier.flatMapLatest { id ->
            combine(
                db.dossierDao().parIdEnDirect(id),
                db.pieceDossierDao().parDossier(id),
                db.historiqueDao().pourObjet(EntitesHistorique.DOSSIER, id),
                combine(motif, erreurDossier, session) { m, e, s -> Triple(m, e, s) },
            ) { dossier, pieces, historique, (m, e, s) ->
                // Le candidat et la catégorie sont lus une fois par changement (map suspend, comme un DAO ponctuel).
                EtatDossier(
                    dossier = dossier,
                    pieces = pieces,
                    historique = historique,
                    motif = m,
                    erreur = e,
                    peutSoumettre = dossier != null && (dossier.statut == StatutDossier.BROUILLON || dossier.statut == StatutDossier.INCOMPLET) && s != null && ReglesConsultation.peutGererCandidat(s.role),
                    peutDecider = dossier?.statut == StatutDossier.SOUMIS && estAtt(s),
                )
            }.map { etat ->
                etat.copy(
                    candidat = etat.dossier?.let { db.candidatDao().parId(it.candidatId) },
                    categorie = etat.dossier?.let { db.categoriePermisDao().parId(it.categorieId) },
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatDossier())

    /** Coche ou décoche une pièce fournie. */
    fun cocherPiece(piece: PieceDossier, fournie: Boolean) {
        viewModelScope.launch { db.pieceDossierDao().modifier(piece.copy(fournie = fournie)) }
    }

    /** Soumet le dossier à l'ATT après contrôle d'éligibilité (âge minimum de la catégorie, valeur configurée). */
    fun soumettre(dossierId: Int) {
        val utilisateur = session.value ?: return
        viewModelScope.launch {
            val d = db.dossierDao().parId(dossierId) ?: return@launch
            val candidat = db.candidatDao().parId(d.candidatId) ?: return@launch
            val categorie = db.categoriePermisDao().parId(d.categorieId) ?: return@launch
            val erreur = ReglesDossier.verifierEligibilite(candidat.dateNaissance, categorie, dateDuJour())
            if (erreur != null) {
                erreurDossier.value = erreur
                return@launch
            }
            val soumis = d.copy(statut = StatutDossier.SOUMIS, dateSoumission = dateDuJour(), motif = null)
            db.withTransaction {
                db.dossierDao().modifier(soumis)
                db.tracer(EntitesHistorique.DOSSIER, dossierId, "SOUMISSION", utilisateur.id, ancienneValeur = d.resume(), nouvelleValeur = soumis.resume())
            }
        }
    }

    /** Décision de l'ATT sur un dossier soumis : VALIDE, INCOMPLET (motif) ou REFUSE (motif). */
    fun decider(dossierId: Int, decision: StatutDossier) {
        val utilisateur = session.value ?: return
        if (!estAtt(utilisateur)) return
        val texteMotif = motif.value.trim()
        if (decision != StatutDossier.VALIDE && texteMotif.isBlank()) {
            erreurDossier.value = "Indiquez le motif (pièce manquante, non-éligibilité…)."
            return
        }
        viewModelScope.launch {
            val d = db.dossierDao().parId(dossierId) ?: return@launch
            if (d.statut != StatutDossier.SOUMIS) return@launch
            val decide = d.copy(statut = decision, motif = texteMotif.ifBlank { null }, dateDecision = dateDuJour(), decideParId = utilisateur.id)
            val action = when (decision) {
                StatutDossier.VALIDE -> ActionsHistorique.VALIDATION
                StatutDossier.REFUSE -> ActionsHistorique.REFUS
                else -> "INCOMPLET"
            }
            db.withTransaction {
                db.dossierDao().modifier(decide)
                db.tracer(EntitesHistorique.DOSSIER, dossierId, action, utilisateur.id, ancienneValeur = d.resume(), nouvelleValeur = decide.resume(), motif = texteMotif.ifBlank { null })
            }
            motif.value = ""
        }
    }

    // ----- Dossiers à traiter (ATT) -----

    val aTraiter: StateFlow<List<DossierATraiter>> =
        combine(db.dossierDao().parStatut(StatutDossier.SOUMIS), db.candidatDao().tous(), db.categoriePermisDao().toutes(), db.autoEcoleDao().toutes(), session) { dossiers, candidats, categories, autoEcoles, s ->
            dossiers.mapNotNull { d ->
                val c = candidats.find { it.id == d.candidatId } ?: return@mapNotNull null
                val a = autoEcoles.find { it.id == c.autoEcoleId }
                if (s?.regionId != null && a?.regionId != s.regionId) return@mapNotNull null
                DossierATraiter(d, "${c.nom} ${c.prenom}", categories.find { it.id == d.categorieId }?.code ?: "?", a?.nom ?: "?")
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
