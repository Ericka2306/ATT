package mg.itu.att.ui.appel

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
import mg.itu.att.data.Creneau
import mg.itu.att.data.EntitesHistorique
import mg.itu.att.data.Inscription
import mg.itu.att.data.Presence
import mg.itu.att.data.Role
import mg.itu.att.data.Session
import mg.itu.att.data.StatutInscription
import mg.itu.att.data.StatutPresence
import mg.itu.att.data.StatutSession
import mg.itu.att.data.TypeEpreuve
import mg.itu.att.data.regleEntier
import mg.itu.att.data.regleTexte
import mg.itu.att.data.resume
import mg.itu.att.data.tracer
import mg.itu.att.metier.ReglesInscription
import mg.itu.att.metier.ReglesPresence
import mg.itu.att.metier.formatDate
import mg.itu.att.metier.heureCourante
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.connexion.SessionUtilisateur

// ---------- ÉTATS ----------

data class LigneAppel(val inscription: Inscription, val presence: Presence?, val nomCandidat: String, val creneau: Creneau?)

data class EtatAppel(
    val session: Session? = null,
    val libelleSession: String = "",
    val toleranceMin: Int = 0,
    val regleAbsence: String = "",
    val lignes: List<LigneAppel> = emptyList(),
    val presents: Int = 0, val absents: Int = 0, val retards: Int = 0, val enAttente: Int = 0,
    /** ATT : peut marquer. Verrouillé si la session est terminée ou annulée. */
    val peutMarquer: Boolean = false,
    /** L'examinateur ne voit que les numéros d'appel (pratique ATT d'anonymisation, Q4). */
    val nomsVisibles: Boolean = true,
    val message: String? = null,
)

// ---------- LE VIEWMODEL ----------

/**
 * Appel et présence (UC08). Présent dans la tolérance, en retard au-delà (règle `TOLERANCE_RETARD_MIN`) ;
 * absent → report automatique ou nouvelle inscription selon `REGLE_ABSENCE`. Chaque changement est tracé.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppelViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private val session = MutableStateFlow<SessionUtilisateur?>(null)
    private val idSession = MutableStateFlow(0)
    private val message = MutableStateFlow<String?>(null)
    private val regles = MutableStateFlow(0 to "")

    override fun definirSession(session: SessionUtilisateur) {
        if (this.session.value != session) this.session.value = session
    }

    fun afficher(sessionId: Int) {
        if (idSession.value == sessionId) return
        idSession.value = sessionId
        viewModelScope.launch {
            val se = db.sessionDao().parId(sessionId)
            regles.value = db.regleEntier(ClesRegles.TOLERANCE_RETARD_MIN, se?.categorieId) to db.regleTexte(ClesRegles.REGLE_ABSENCE, se?.categorieId, ReglesPresence.NOUVELLE_INSCRIPTION)
        }
    }

    private fun estAtt(s: SessionUtilisateur?) = s?.role == Role.ADMIN_ATT || s?.role == Role.SUPER_ADMIN

    /** Regroupements intermédiaires pour rester sous la limite de cinq flux par `combine`. */
    private data class Donnees(val session: Session?, val creneaux: List<Creneau>, val inscriptions: List<Inscription>, val presences: List<Presence>)
    private data class Referentiels(val candidats: List<Candidat>, val categories: List<CategoriePermis>, val epreuves: List<TypeEpreuve>, val centres: List<Centre>)

    val etat: StateFlow<EtatAppel> =
        idSession.flatMapLatest { id ->
            combine(
                combine(db.sessionDao().parIdEnDirect(id), db.creneauDao().parSession(id), db.inscriptionDao().parSession(id), db.presenceDao().parSession(id)) { s, c, i, p -> Donnees(s, c, i, p) },
                combine(db.candidatDao().tous(), db.categoriePermisDao().toutes(), db.typeEpreuveDao().toutes(), db.centreDao().tous()) { c, ca, e, ce -> Referentiels(c, ca, e, ce) },
                combine(session, regles, message) { s, r, m -> Triple(s, r, m) },
            ) { (se, creneaux, inscriptions, presences), (candidats, categories, epreuves, centres), (s, r, m) ->
                if (se == null) return@combine EtatAppel()
                val lignes = ReglesInscription.actives(inscriptions)
                    .filter { it.statut != StatutInscription.DEMANDE }
                    .map { i ->
                        val c = candidats.find { it.id == i.candidatId }
                        LigneAppel(i, presences.find { it.inscriptionId == i.id }, c?.let { "${it.nom} ${it.prenom}" } ?: "?", creneaux.find { it.id == i.creneauId })
                    }
                    .sortedWith(compareBy({ it.creneau?.ordre ?: 0 }, { it.inscription.numeroAnonymat }))
                val statuts = lignes.map { it.presence?.statut ?: StatutPresence.EN_ATTENTE }
                EtatAppel(
                    session = se,
                    libelleSession = "Permis ${categories.find { it.id == se.categorieId }?.code ?: "?"} — ${epreuves.find { it.id == se.typeEpreuveId }?.libelle ?: "?"} · ${centres.find { it.id == se.centreId }?.nom ?: "?"} · ${formatDate(se.date)}, convocation ${se.heureConvocation}",
                    toleranceMin = r.first, regleAbsence = r.second, lignes = lignes,
                    presents = statuts.count { it == StatutPresence.PRESENT || it == StatutPresence.EN_COURS || it == StatutPresence.TERMINE },
                    absents = statuts.count { it == StatutPresence.ABSENT }, retards = statuts.count { it == StatutPresence.EN_RETARD }, enAttente = statuts.count { it == StatutPresence.EN_ATTENTE },
                    peutMarquer = estAtt(s) && se.statut != StatutSession.TERMINEE && se.statut != StatutSession.ANNULEE,
                    nomsVisibles = s?.role != Role.EXAMINATEUR,
                    message = m,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatAppel())

    /** Le candidat se présente : PRESENT dans la tolérance, EN_RETARD au-delà (à accepter ou refuser ensuite). */
    fun marquerArrivee(inscriptionId: Int) {
        val (tolerance, _) = regles.value
        viewModelScope.launch {
            val se = db.sessionDao().parId(idSession.value) ?: return@launch
            val heure = heureCourante()
            val statut = ReglesPresence.statutArrivee(se.heureConvocation, heure, tolerance)
            enregistrer(inscriptionId, statut, heureArrivee = heure, remarque = if (statut == StatutPresence.EN_RETARD) "retard de ${ReglesPresence.minutesDeRetard(se.heureConvocation, heure)} min" else null)
        }
    }

    /** Retardataire accepté : il passe (PRESENT, avec la remarque conservée). */
    fun accepterRetard(inscriptionId: Int) {
        viewModelScope.launch {
            val p = db.presenceDao().parInscription(inscriptionId)
            enregistrer(inscriptionId, StatutPresence.PRESENT, heureArrivee = p?.heureArrivee, remarque = (p?.remarque ?: "retard") + ", accepté")
        }
    }

    /** Absent (ou retardataire refusé) : selon la règle, report automatique de l'inscription ou nouvelle inscription à faire. */
    fun marquerAbsent(inscriptionId: Int) {
        viewModelScope.launch {
            val (_, regleAbsence) = regles.value
            enregistrer(inscriptionId, StatutPresence.ABSENT, heureArrivee = null, remarque = null)
            if (ReglesPresence.absentReporteAutomatiquement(regleAbsence)) reporterInscription(inscriptionId)
            else message.value = "Absent enregistré : le candidat devra être réinscrit à une autre session (règle REGLE_ABSENCE)."
        }
    }

    /** Correction (session non terminée) : retour en attente, tracé. */
    fun reinitialiser(inscriptionId: Int) {
        viewModelScope.launch { enregistrer(inscriptionId, StatutPresence.EN_ATTENTE, heureArrivee = null, remarque = "corrigé") }
    }

    private suspend fun enregistrer(inscriptionId: Int, statut: StatutPresence, heureArrivee: String?, remarque: String?) {
        val utilisateur = session.value ?: return
        if (!estAtt(utilisateur)) return
        db.withTransaction {
            // La présence est créée à l'inscription (C6) ; sans elle, rien à marquer.
            val actuelle = db.presenceDao().parInscription(inscriptionId) ?: return@withTransaction
            val modifiee = actuelle.copy(statut = statut, heureAppel = heureCourante(), heureArrivee = heureArrivee, remarque = remarque)
            db.presenceDao().modifier(modifiee)
            db.tracer(EntitesHistorique.PRESENCE, modifiee.id, ActionsHistorique.MODIFICATION, utilisateur.id, ancienneValeur = actuelle.resume(), nouvelleValeur = modifiee.resume())
        }
        message.value = null
    }

    private suspend fun reporterInscription(inscriptionId: Int) {
        val utilisateur = session.value ?: return
        db.withTransaction {
            val i = db.inscriptionDao().parId(inscriptionId) ?: return@withTransaction
            val se = db.sessionDao().parId(i.sessionId)
            val motif = "Absent à la session du ${se?.date?.let { formatDate(it) } ?: "?"} (report automatique, règle REGLE_ABSENCE)"
            val modifiee = i.copy(statut = StatutInscription.REPORTE, motif = motif)
            db.inscriptionDao().modifier(modifiee)
            db.tracer(EntitesHistorique.INSCRIPTION, i.id, "REPORT", utilisateur.id, ancienneValeur = i.resume(), nouvelleValeur = modifiee.resume(), motif = motif)
        }
        message.value = "Absent enregistré : inscription reportée automatiquement (règle REGLE_ABSENCE)."
    }
}
