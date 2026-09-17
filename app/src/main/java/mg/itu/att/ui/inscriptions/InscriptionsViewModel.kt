package mg.itu.att.ui.inscriptions

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
import mg.itu.att.data.Candidat
import mg.itu.att.data.ClesRegles
import mg.itu.att.data.Creneau
import mg.itu.att.data.EntitesHistorique
import mg.itu.att.data.Inscription
import mg.itu.att.data.Presence
import mg.itu.att.data.Role
import mg.itu.att.data.Session
import mg.itu.att.data.StatutDossier
import mg.itu.att.data.StatutInscription
import mg.itu.att.data.StatutSession
import mg.itu.att.data.regleBooleen
import mg.itu.att.data.regleEntier
import mg.itu.att.data.resume
import mg.itu.att.data.tracer
import mg.itu.att.metier.ReglesInscription
import mg.itu.att.metier.dateDuJour
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.connexion.SessionUtilisateur

// ---------- ÉTATS ----------

data class InscritLigne(val inscription: Inscription, val nomCandidat: String, val nomAutoEcole: String, val creneau: Creneau?)

data class CandidatEligible(val candidat: Candidat, val nomAutoEcole: String, val dossierId: Int)

data class EtatInscriptions(
    val session: Session? = null,
    val libelleSession: String = "",
    val creneaux: List<Creneau> = emptyList(),
    val inscrits: List<InscritLigne> = emptyList(),
    val placesRestantes: Int = 0,
    /** Candidats avec un dossier validé pour la catégorie de la session, pas encore inscrits (filtrés par la recherche). */
    val candidatsEligibles: List<CandidatEligible> = emptyList(),
    val recherche: String = "",
    val creneauChoisiId: Int? = null,
    val motif: String = "",
    val message: String? = null,
    val erreur: String? = null,
    /** ATT : inscrit, confirme, annule. Auto-école : demande seulement si la règle l'autorise. */
    val peutInscrire: Boolean = false,
    val peutDemander: Boolean = false,
    val estAtt: Boolean = false,
    /** Vrai pour l'ATT : les noms sont visibles ; l'examinateur ne verra que les numéros (C7). */
    val nomsVisibles: Boolean = true,
)

// ---------- LE VIEWMODEL ----------

/**
 * Inscriptions d'une session (UC07). Les contrôles (capacité, doublon, éligibilité, région, délai, tentatives,
 * théorie avant conduite) sont dans `ReglesInscription.verifier`, fonction pure ; ce ViewModel ne fait que
 * rassembler les faits en base et enregistrer. Chaque inscription reçoit un numéro d'appel anonyme et une présence.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InscriptionsViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private val session = MutableStateFlow<SessionUtilisateur?>(null)
    private val idSession = MutableStateFlow(0)
    private val recherche = MutableStateFlow("")
    private val creneauChoisi = MutableStateFlow<Int?>(null)
    private val motif = MutableStateFlow("")
    private val message = MutableStateFlow<String?>(null)
    private val erreur = MutableStateFlow<String?>(null)
    private val autoEcolePeutInscrire = MutableStateFlow(false)

    override fun definirSession(session: SessionUtilisateur) {
        if (this.session.value != session) this.session.value = session
    }

    fun afficher(sessionId: Int) {
        if (idSession.value == sessionId) return
        idSession.value = sessionId
        viewModelScope.launch { autoEcolePeutInscrire.value = db.regleBooleen(ClesRegles.AUTO_ECOLE_PEUT_INSCRIRE) }
    }

    fun rechercher(texte: String) { recherche.value = texte; erreur.value = null }
    fun choisirCreneau(id: Int?) { creneauChoisi.value = id; erreur.value = null }
    fun changerMotif(texte: String) { motif.value = texte; erreur.value = null }

    private fun estAtt(s: SessionUtilisateur?) = s?.role == Role.ADMIN_ATT || s?.role == Role.SUPER_ADMIN

    val etat: StateFlow<EtatInscriptions> =
        idSession.flatMapLatest { id ->
            combine(
                combine(db.sessionDao().parIdEnDirect(id), db.creneauDao().parSession(id), db.inscriptionDao().parSession(id)) { s, c, i -> Triple(s, c, i) },
                combine(db.candidatDao().tous(), db.autoEcoleDao().toutes(), db.dossierDao().tous()) { c, a, d -> Triple(c, a, d) },
                combine(db.categoriePermisDao().toutes(), db.typeEpreuveDao().toutes(), db.centreDao().tous()) { c, e, ce -> Triple(c, e, ce) },
                combine(recherche, creneauChoisi, motif) { r, c, m -> Triple(r, c, m) },
                combine(message, erreur, session, autoEcolePeutInscrire) { me, er, s, regle -> listOf(me, er, s, regle) },
            ) { (se, creneaux, inscriptions), (candidats, autoEcoles, dossiers), (categories, epreuves, centres), (texte, creneauId, m), divers ->
                val s = divers[2] as SessionUtilisateur?
                val regleAutoEcole = divers[3] as Boolean
                if (se == null) return@combine EtatInscriptions()
                val nomsAutoEcoles = autoEcoles.associate { it.id to it.nom }
                val actives = ReglesInscription.actives(inscriptions)
                val dejaInscrits = inscriptions.filter { it.statut != StatutInscription.ANNULE && it.statut != StatutInscription.REPORTE }.map { it.candidatId }.toSet()
                val eligibles = dossiers
                    .filter { it.categorieId == se.categorieId && it.statut == StatutDossier.VALIDE }
                    .mapNotNull { d -> candidats.find { it.id == d.candidatId && it.actif }?.let { c -> CandidatEligible(c, nomsAutoEcoles[c.autoEcoleId] ?: "?", d.id) } }
                    .filter { it.candidat.id !in dejaInscrits }
                    .filter { s?.autoEcoleId == null || it.candidat.autoEcoleId == s.autoEcoleId }
                    .filter { texte.isBlank() || "${it.candidat.nom} ${it.candidat.prenom}".contains(texte.trim(), ignoreCase = true) }
                    .sortedBy { it.candidat.nom }
                EtatInscriptions(
                    session = se,
                    libelleSession = "Permis ${categories.find { it.id == se.categorieId }?.code ?: "?"} — ${epreuves.find { it.id == se.typeEpreuveId }?.libelle ?: "?"} · ${centres.find { it.id == se.centreId }?.nom ?: "?"} · ${se.date} ${se.heureConvocation}",
                    creneaux = creneaux,
                    inscrits = inscriptions
                        .filter { s?.autoEcoleId == null || candidats.find { c -> c.id == it.candidatId }?.autoEcoleId == s.autoEcoleId }
                        .map { i ->
                            val c = candidats.find { it.id == i.candidatId }
                            InscritLigne(i, c?.let { "${it.nom} ${it.prenom}" } ?: "?", nomsAutoEcoles[c?.autoEcoleId] ?: "?", creneaux.find { it.id == i.creneauId })
                        },
                    placesRestantes = se.capacite - actives.size,
                    candidatsEligibles = eligibles,
                    recherche = texte, creneauChoisiId = creneauId, motif = m,
                    message = divers[0] as String?, erreur = divers[1] as String?,
                    peutInscrire = estAtt(s), peutDemander = s?.role == Role.AUTO_ECOLE && regleAutoEcole, estAtt = estAtt(s),
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatInscriptions())

    /** Rassemble les faits puis applique `ReglesInscription.verifier` ; enregistre inscription + présence + historique. */
    fun inscrire(candidatId: Int) {
        val utilisateur = session.value ?: return
        val sessionId = idSession.value
        viewModelScope.launch {
            val se = db.sessionDao().parId(sessionId) ?: return@launch
            val candidat = db.candidatDao().parId(candidatId) ?: return@launch
            val dossier = db.dossierDao().valideEnCours(candidatId, se.categorieId)
            val epreuve = db.typeEpreuveDao().parId(se.typeEpreuveId)
            val inscriptionsSession = db.inscriptionDao().listePourSession(sessionId)
            val inscriptionsCandidat = db.inscriptionDao().listePourCandidat(candidatId).mapNotNull { i -> db.sessionDao().parId(i.sessionId)?.let { i to it } }
            val tentatives = db.tentativeDao().listePourCandidatEtEpreuve(candidatId, se.typeEpreuveId)
            val theorie = db.typeEpreuveDao().parCategorieEtCode(se.categorieId, "THEORIE")
            val theorieRequise = epreuve?.code == "CONDUITE" && theorie != null && db.regleBooleen(ClesRegles.CONDUITE_APRES_THEORIE_REUSSIE, se.categorieId)
            val contexte = ReglesInscription.ContexteInscription(
                session = se, inscriptionsSession = inscriptionsSession, inscriptionsCandidat = inscriptionsCandidat,
                dossierValide = dossier != null,
                regionAutoEcole = db.autoEcoleDao().parId(candidat.autoEcoleId)?.regionId, regionCentre = db.centreDao().parId(se.centreId)?.regionId,
                examenDansRegion = db.regleBooleen(ClesRegles.EXAMEN_DANS_REGION_AUTO_ECOLE, se.categorieId),
                derniereTentative = tentatives.maxByOrNull { it.numero }?.dateHeure?.substringBefore('T'),
                delaiRepassageJours = db.regleEntier(ClesRegles.DELAI_REPASSAGE_JOURS, se.categorieId),
                nombreTentatives = tentatives.size, tentativesMax = db.regleEntier(ClesRegles.TENTATIVES_MAX, se.categorieId),
                theorieRequise = theorieRequise,
                theorieReussie = theorie != null && db.resultatDao().dernierReussi(candidatId, theorie.id) != null,
            )
            val refus = ReglesInscription.verifier(contexte)
            if (refus != null) return@launch run { erreur.value = refus; message.value = null }
            val creneaux = db.creneauDao().listePourSession(sessionId)
            val creneau = creneauChoisi.value?.let { id -> creneaux.find { it.id == id } }?.takeIf { c -> ReglesInscription.actives(inscriptionsSession).count { it.creneauId == c.id } < c.capacite }
                ?: ReglesInscription.choisirCreneau(creneaux, inscriptionsSession)
            if (creneau == null) return@launch run { erreur.value = "Aucun créneau disponible." }
            val statut = if (estAtt(utilisateur)) StatutInscription.INSCRIT else StatutInscription.DEMANDE
            val inscription = Inscription(
                candidatId = candidatId, dossierId = dossier?.id ?: 0, sessionId = sessionId, creneauId = creneau.id, statut = statut,
                dateInscription = dateDuJour(), heurePassageEstimee = creneau.heureDebut, numeroAnonymat = ReglesInscription.numeroAnonymat(inscriptionsSession),
            )
            db.withTransaction {
                val id = db.inscriptionDao().inserer(inscription).toInt()
                db.presenceDao().inserer(Presence(inscriptionId = id))
                db.tracer(EntitesHistorique.INSCRIPTION, id, ActionsHistorique.CREATION, utilisateur.id, nouvelleValeur = inscription.copy(id = id).resume())
                // La session devient COMPLETE quand la dernière place est prise.
                if (ReglesInscription.actives(inscriptionsSession).size + 1 >= se.capacite && se.statut == StatutSession.OUVERTE) {
                    db.sessionDao().modifier(se.copy(statut = StatutSession.COMPLETE))
                    db.tracer(EntitesHistorique.SESSION, se.id, ActionsHistorique.MODIFICATION, utilisateur.id, nouvelleValeur = "COMPLETE (dernière place prise)")
                }
            }
            erreur.value = null
            message.value = "${candidat.nom} ${candidat.prenom} : ${if (statut == StatutInscription.DEMANDE) "demande enregistrée" else "inscrit(e)"}, n° ${inscription.numeroAnonymat}, créneau ${creneau.heureDebut}."
        }
    }

    /** L'ATT confirme une demande d'auto-école. */
    fun confirmer(inscriptionId: Int) = changerStatut(inscriptionId, StatutInscription.INSCRIT, ActionsHistorique.VALIDATION, motifObligatoire = false)

    /** Annulation ou report (motif obligatoire) : la place est libérée, la session repasse OUVERTE si elle était COMPLETE. */
    fun annuler(inscriptionId: Int) = changerStatut(inscriptionId, StatutInscription.ANNULE, ActionsHistorique.ANNULATION, motifObligatoire = true)
    fun reporter(inscriptionId: Int) = changerStatut(inscriptionId, StatutInscription.REPORTE, "REPORT", motifObligatoire = true)

    private fun changerStatut(inscriptionId: Int, nouveau: StatutInscription, action: String, motifObligatoire: Boolean) {
        val utilisateur = session.value ?: return
        if (!estAtt(utilisateur)) return
        val texteMotif = motif.value.trim()
        if (motifObligatoire && texteMotif.isBlank()) return run { erreur.value = "Indiquez le motif." }
        viewModelScope.launch {
            db.withTransaction {
                val actuelle = db.inscriptionDao().parId(inscriptionId) ?: return@withTransaction
                val modifiee = actuelle.copy(statut = nouveau, motif = texteMotif.ifBlank { actuelle.motif })
                db.inscriptionDao().modifier(modifiee)
                db.tracer(EntitesHistorique.INSCRIPTION, inscriptionId, action, utilisateur.id, ancienneValeur = actuelle.resume(), nouvelleValeur = modifiee.resume(), motif = texteMotif.ifBlank { null })
                val se = db.sessionDao().parId(actuelle.sessionId)
                if (se != null && se.statut == StatutSession.COMPLETE && (nouveau == StatutInscription.ANNULE || nouveau == StatutInscription.REPORTE)) {
                    db.sessionDao().modifier(se.copy(statut = StatutSession.OUVERTE))
                    db.tracer(EntitesHistorique.SESSION, se.id, ActionsHistorique.MODIFICATION, utilisateur.id, nouvelleValeur = "OUVERTE (place libérée)")
                }
            }
            motif.value = ""
            erreur.value = null
            message.value = null
        }
    }
}
