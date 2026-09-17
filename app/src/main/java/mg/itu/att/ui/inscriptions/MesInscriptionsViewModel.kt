package mg.itu.att.ui.inscriptions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import mg.itu.att.data.AppDatabase
import mg.itu.att.data.Inscription
import mg.itu.att.data.Presence
import mg.itu.att.data.Session
import mg.itu.att.metier.ReglesConsultation
import mg.itu.att.metier.dateDuJour
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.connexion.SessionUtilisateur

// ---------- ÉTATS ----------

/** Une inscription vue par l'auto-école : son candidat, la session, le créneau, la présence. */
data class MonInscriptionLigne(
    val inscription: Inscription,
    val candidatId: Int,
    val nomCandidat: String,
    val session: Session?,
    val libelleSession: String,
    val nomCentre: String,
    val heureCreneau: String?,
    val presence: Presence?,
    val aVenir: Boolean,
)

enum class FiltreMesInscriptions { A_VENIR, PASSEES, TOUTES }

data class EtatMesInscriptions(
    val lignes: List<MonInscriptionLigne> = emptyList(),
    val filtre: FiltreMesInscriptions = FiltreMesInscriptions.A_VENIR,
    val total: Int = 0,
)

// ---------- LE VIEWMODEL ----------

/**
 * « Mes inscriptions » (UC12) : les inscriptions des candidats visibles par l'utilisateur —
 * l'auto-école voit les siens, un administrateur régional sa région. Lecture seule ;
 * les actions (inscrire, reporter, annuler) restent sur l'écran des inscriptions d'une session (UC07).
 */
@OptIn(ExperimentalCoroutinesApi::class) // flatMapLatest (HORS_COURS n° 18)
class MesInscriptionsViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private val session = MutableStateFlow<SessionUtilisateur?>(null)
    private val filtre = MutableStateFlow(FiltreMesInscriptions.A_VENIR)

    override fun definirSession(session: SessionUtilisateur) {
        if (this.session.value != session) this.session.value = session
    }

    fun filtrer(f: FiltreMesInscriptions) { filtre.value = f }

    /** Les candidats visibles par l'utilisateur connecté (même règle que la liste des candidats). */
    private val candidatsVisibles = session.flatMapLatest { s ->
        when {
            s?.autoEcoleId != null -> db.candidatDao().parAutoEcole(s.autoEcoleId)
            s?.regionId != null -> db.candidatDao().parRegion(s.regionId)
            else -> db.candidatDao().tous()
        }
    }

    val uiState: StateFlow<EtatMesInscriptions> =
        combine(
            combine(candidatsVisibles, db.inscriptionDao().toutes(), db.presenceDao().toutes()) { c, i, p -> Triple(c, i, p) },
            combine(db.sessionDao().toutes(), db.creneauDao().tous(), db.centreDao().tous()) { s, c, ce -> Triple(s, c, ce) },
            combine(db.categoriePermisDao().toutes(), db.typeEpreuveDao().toutes()) { c, e -> c to e },
            filtre,
        ) { (candidats, inscriptions, presences), (sessions, creneaux, centres), (categories, epreuves), f ->
            val aujourdHui = dateDuJour()
            val noms = candidats.associate { it.id to "${it.nom} ${it.prenom}" }
            val lignes = inscriptions
                .filter { it.candidatId in noms }
                .map { i ->
                    val se = sessions.find { it.id == i.sessionId }
                    MonInscriptionLigne(
                        inscription = i, candidatId = i.candidatId, nomCandidat = noms[i.candidatId] ?: "?",
                        session = se,
                        libelleSession = se?.let {
                            ReglesConsultation.libelleSession(it.date, it.heureConvocation, categories.find { c -> c.id == it.categorieId }?.code ?: "?", epreuves.find { e -> e.id == it.typeEpreuveId }?.libelle ?: "?")
                        } ?: "Session inconnue",
                        nomCentre = centres.find { it.id == se?.centreId }?.nom ?: "?",
                        heureCreneau = creneaux.find { it.id == i.creneauId }?.heureDebut,
                        presence = presences.find { it.inscriptionId == i.id },
                        aVenir = se != null && ReglesConsultation.estAVenir(se.date, aujourdHui),
                    )
                }
            EtatMesInscriptions(
                lignes = when (f) {
                    FiltreMesInscriptions.A_VENIR -> lignes.filter { it.aVenir }.sortedBy { (it.session?.date ?: "") + (it.session?.heureConvocation ?: "") }
                    FiltreMesInscriptions.PASSEES -> lignes.filter { !it.aVenir }.sortedByDescending { it.session?.date ?: "" }
                    FiltreMesInscriptions.TOUTES -> lignes.sortedByDescending { it.session?.date ?: "" }
                },
                filtre = f,
                total = lignes.size,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatMesInscriptions())
}
