package mg.itu.att.ui.evaluation

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
import kotlinx.coroutines.launch
import mg.itu.att.data.ActionsHistorique
import mg.itu.att.data.AppDatabase
import mg.itu.att.data.Candidat
import mg.itu.att.data.CategoriePermis
import mg.itu.att.data.Centre
import mg.itu.att.data.ClesRegles
import mg.itu.att.data.EntitesHistorique
import mg.itu.att.data.Inscription
import mg.itu.att.data.Presence
import mg.itu.att.data.Role
import mg.itu.att.data.Session
import mg.itu.att.data.StatutPresence
import mg.itu.att.data.StatutSession
import mg.itu.att.data.StatutTentative
import mg.itu.att.data.Tentative
import mg.itu.att.data.TypeEpreuve
import mg.itu.att.data.regleEntier
import mg.itu.att.data.resume
import mg.itu.att.data.tracer
import mg.itu.att.metier.ReglesInscription
import mg.itu.att.metier.ReglesTentatives
import mg.itu.att.metier.dateDuJour
import mg.itu.att.metier.formatDate
import mg.itu.att.metier.maintenantIso
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.connexion.SessionUtilisateur

// ---------- ÉTATS ----------

data class SessionAEvaluer(val session: Session, val libelle: String, val codeEpreuve: String, val presents: Int, val tentatives: Int)

data class LigneTentative(
    val inscription: Inscription, val presence: Presence?, val nomCandidat: String,
    val tentative: Tentative?, val prochainNumero: Int, val refus: String?,
)

data class EtatTentatives(
    val session: Session? = null,
    val libelleSession: String = "",
    val codeEpreuve: String = "",
    val lignes: List<LigneTentative> = emptyList(),
    val nomsVisibles: Boolean = true,
    val peutOuvrir: Boolean = false,
    val erreur: String? = null,
)

// ---------- LE VIEWMODEL ----------

/**
 * Tentatives (UC09, étape C8). L'examinateur choisit une session puis un candidat présent (numéro d'appel) ;
 * ouvrir une tentative crée la n-ième (jamais réutilisée) avec l'examinateur courant et passe la présence EN_COURS.
 * Aucune affectation préalable : n'importe quel examinateur connecté peut ouvrir (règle R5).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TentativesViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private val session = MutableStateFlow<SessionUtilisateur?>(null)
    private val idSession = MutableStateFlow(0)
    private val erreur = MutableStateFlow<String?>(null)
    private val tentativesMax = MutableStateFlow(0)

    override fun definirSession(session: SessionUtilisateur) {
        if (this.session.value != session) this.session.value = session
    }

    // ----- Sessions à évaluer (examinateur : du jour et à venir, non annulées) -----

    private data class Referentiels(val categories: List<CategoriePermis>, val epreuves: List<TypeEpreuve>, val centres: List<Centre>)

    val sessionsAEvaluer: StateFlow<List<SessionAEvaluer>> =
        combine(
            db.sessionDao().toutes(), db.presenceDao().toutes(), db.inscriptionDao().toutes(),
            combine(db.categoriePermisDao().toutes(), db.typeEpreuveDao().toutes(), db.centreDao().tous()) { c, e, ce -> Referentiels(c, e, ce) },
            combine(db.tentativeDao().toutes(), session) { t, s -> t to s },
        ) { sessions, presences, inscriptions, (categories, epreuves, centres), (tentatives, s) ->
            val aujourdHui = dateDuJour()
            sessions
                .filter { it.statut != StatutSession.ANNULEE && it.statut != StatutSession.PLANIFIEE && it.date >= aujourdHui }
                .filter { s?.regionId == null || centres.find { c -> c.id == it.centreId }?.regionId == s.regionId }
                .sortedBy { it.date + it.heureConvocation }
                .map { se ->
                    val ids = inscriptions.filter { it.sessionId == se.id }.map { it.id }.toSet()
                    val epreuve = epreuves.find { it.id == se.typeEpreuveId }
                    SessionAEvaluer(
                        session = se,
                        libelle = "${formatDate(se.date)} ${se.heureConvocation} — permis ${categories.find { it.id == se.categorieId }?.code ?: "?"}, ${epreuve?.libelle ?: "?"} · ${centres.find { it.id == se.centreId }?.nom ?: "?"}",
                        codeEpreuve = epreuve?.code ?: "",
                        presents = presences.count { it.inscriptionId in ids && (it.statut == StatutPresence.PRESENT || it.statut == StatutPresence.EN_COURS || it.statut == StatutPresence.TERMINE) },
                        tentatives = tentatives.count { it.inscriptionId in ids },
                    )
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ----- Candidats d'une session -----

    fun afficher(sessionId: Int) {
        if (idSession.value == sessionId) return
        idSession.value = sessionId
        erreur.value = null
        viewModelScope.launch {
            val se = db.sessionDao().parId(sessionId)
            tentativesMax.value = db.regleEntier(ClesRegles.TENTATIVES_MAX, se?.categorieId)
        }
    }

    private data class Donnees(val session: Session?, val inscriptions: List<Inscription>, val presences: List<Presence>, val tentativesSession: List<Tentative>)

    val etat: StateFlow<EtatTentatives> =
        idSession.flatMapLatest { id ->
            combine(
                combine(db.sessionDao().parIdEnDirect(id), db.inscriptionDao().parSession(id), db.presenceDao().parSession(id), db.tentativeDao().parSession(id)) { s, i, p, t -> Donnees(s, i, p, t) },
                combine(db.candidatDao().tous(), db.tentativeDao().toutes()) { c, t -> c to t },
                combine(db.categoriePermisDao().toutes(), db.typeEpreuveDao().toutes(), db.centreDao().tous()) { c, e, ce -> Referentiels(c, e, ce) },
                combine(session, erreur, tentativesMax) { s, e, m -> Triple(s, e, m) },
            ) { (se, inscriptions, presences, tentativesSession), (candidats, toutesTentatives), (categories, epreuves, centres), (s, e, max) ->
                if (se == null) return@combine EtatTentatives()
                val epreuve = epreuves.find { it.id == se.typeEpreuveId }
                EtatTentatives(
                    session = se,
                    libelleSession = "Permis ${categories.find { it.id == se.categorieId }?.code ?: "?"} — ${epreuve?.libelle ?: "?"} · ${centres.find { it.id == se.centreId }?.nom ?: "?"} · ${formatDate(se.date)}",
                    codeEpreuve = epreuve?.code ?: "",
                    lignes = ReglesInscription.actives(inscriptions)
                        .map { i ->
                            val presence = presences.find { it.inscriptionId == i.id }
                            val tentative = tentativesSession.find { it.inscriptionId == i.id }
                            val existantes = toutesTentatives.filter { it.candidatId == i.candidatId && it.typeEpreuveId == se.typeEpreuveId }
                            LigneTentative(
                                inscription = i, presence = presence,
                                nomCandidat = candidats.find { it.id == i.candidatId }?.let { "${it.nom} ${it.prenom}" } ?: "?",
                                tentative = tentative, prochainNumero = ReglesTentatives.numeroSuivant(existantes),
                                refus = ReglesTentatives.peutOuvrir(presence?.statut, tentative, existantes.size, max),
                            )
                        }
                        .sortedBy { it.inscription.numeroAnonymat },
                    nomsVisibles = s?.role != Role.EXAMINATEUR,
                    peutOuvrir = s.peutEvaluer() && se.statut != StatutSession.TERMINEE && se.statut != StatutSession.ANNULEE,
                    erreur = e,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatTentatives())

    /**
     * Ouvre (ou reprend) la tentative d'un candidat présent, puis appelle [onOuverte] avec son id.
     * La présence passe EN_COURS ; l'examinateur connecté est enregistré sur la tentative.
     */
    fun ouvrirTentative(inscriptionId: Int, onOuverte: (Int) -> Unit) {
        val utilisateur = session.value ?: return
        if (!utilisateur.peutEvaluer()) return
        viewModelScope.launch {
            val i = db.inscriptionDao().parId(inscriptionId) ?: return@launch
            val se = db.sessionDao().parId(i.sessionId) ?: return@launch
            val presence = db.presenceDao().parInscription(inscriptionId)
            val existantes = db.tentativeDao().listePourCandidatEtEpreuve(i.candidatId, se.typeEpreuveId)
            val deCetteInscription = db.tentativeDao().parInscription(inscriptionId).firstOrNull()
            val refus = ReglesTentatives.peutOuvrir(presence?.statut, deCetteInscription, existantes.size, tentativesMax.value)
            if (refus != null) return@launch run { erreur.value = refus }
            if (deCetteInscription != null) return@launch onOuverte(deCetteInscription.id) // reprise
            val tentative = Tentative(
                candidatId = i.candidatId, inscriptionId = inscriptionId, typeEpreuveId = se.typeEpreuveId,
                numero = ReglesTentatives.numeroSuivant(existantes), examinateurId = utilisateur.examinateurId, dateHeure = maintenantIso().take(16),
            )
            val id = db.withTransaction {
                val id = db.tentativeDao().inserer(tentative).toInt()
                db.tracer(EntitesHistorique.TENTATIVE, id, ActionsHistorique.CREATION, utilisateur.id, nouvelleValeur = tentative.copy(id = id).resume())
                if (presence != null) {
                    val modifiee = presence.copy(statut = StatutPresence.EN_COURS)
                    db.presenceDao().modifier(modifiee)
                    db.tracer(EntitesHistorique.PRESENCE, presence.id, ActionsHistorique.MODIFICATION, utilisateur.id, ancienneValeur = presence.resume(), nouvelleValeur = modifiee.resume())
                }
                id
            }
            erreur.value = null
            onOuverte(id)
        }
    }
}
