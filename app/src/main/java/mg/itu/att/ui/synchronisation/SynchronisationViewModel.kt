package mg.itu.att.ui.synchronisation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mg.itu.att.data.AppDatabase
import mg.itu.att.data.FauxServeurATT
import mg.itu.att.data.Resultat
import mg.itu.att.data.StatutResultat
import mg.itu.att.data.Synchronisation
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.communs.formatPoints
import mg.itu.att.ui.connexion.SessionUtilisateur

/** Un résultat validé tel qu'affiché sur l'écran de synchronisation : lisible sans le nom du candidat. */
data class LigneSynchro(val resultat: Resultat, val libelle: String)

data class EtatSynchronisation(
    val enAttente: Int = 0,
    val reseau: Boolean = true,
    val syncEnCours: Boolean = false,
    val resultats: List<LigneSynchro> = emptyList(),
    val journalServeur: List<String> = emptyList(),
    val derniereSynchronisation: String? = null,
    val message: String? = null,
)

/**
 * Écran de synchronisation (cours S7, offline-first) : l'état de la file d'attente, l'interrupteur réseau de la
 * démonstration, le bouton « Synchroniser maintenant » et le panneau de ce que le serveur central a reçu.
 * La base locale reste la source de vérité : l'écran ne fait que remonter ce qui attend.
 */
class SynchronisationViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private val session = MutableStateFlow<SessionUtilisateur?>(null)
    private val reseau = MutableStateFlow(FauxServeurATT.reseauDisponible)
    private val syncEnCours = MutableStateFlow(false)
    private val journal = MutableStateFlow(FauxServeurATT.contenu())
    private val message = MutableStateFlow<String?>(null)

    override fun definirSession(session: SessionUtilisateur) {
        if (this.session.value != session) this.session.value = session
    }

    private data class Locaux(val reseau: Boolean, val sync: Boolean, val journal: List<String>, val message: String?)

    val etat: StateFlow<EtatSynchronisation> =
        combine(
            db.resultatDao().tous(), db.resultatDao().nombreEnAttenteDeSynchronisation(),
            db.tentativeDao().toutes(), db.inscriptionDao().toutes(),
            combine(reseau, syncEnCours, journal, message) { r, s, j, m -> Locaux(r, s, j, m) },
        ) { resultats, attente, tentatives, inscriptions, locaux ->
            EtatSynchronisation(
                enAttente = attente,
                reseau = locaux.reseau,
                syncEnCours = locaux.sync,
                resultats = resultats.filter { it.statut == StatutResultat.VALIDE_ATT }.map { r ->
                    val t = tentatives.find { it.id == r.tentativeId }
                    val i = inscriptions.find { it.id == t?.inscriptionId }
                    LigneSynchro(r, "n° ${i?.numeroAnonymat ?: "?"} · passage n° ${t?.numero ?: "?"} · ${formatPoints(r.noteObtenue)} / ${formatPoints(r.noteMax)}")
                },
                journalServeur = locaux.journal,
                derniereSynchronisation = FauxServeurATT.derniereSynchronisation,
                message = locaux.message,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatSynchronisation())

    /** L'interrupteur de la démonstration : simule la coupure du réseau sans toucher au mode avion. */
    fun basculerReseau(actif: Boolean) {
        reseau.value = actif
        FauxServeurATT.reseauDisponible = actif
        message.value = null
    }

    /** Remonte au serveur tout ce qui est en attente. Sans réseau : ne casse rien, on réessaiera. */
    fun synchroniser() {
        if (syncEnCours.value) return
        viewModelScope.launch {
            syncEnCours.value = true
            val envoyes = Synchronisation.synchroniserResultats(db)
            journal.value = FauxServeurATT.contenu()
            message.value = when {
                envoyes > 0 && FauxServeurATT.reseauDisponible -> "$envoyes résultat(s) remonté(s) au serveur de l'ATT."
                !FauxServeurATT.reseauDisponible -> "Réseau coupé : rien n'est perdu, les résultats restent en attente."
                else -> "Rien à synchroniser."
            }
            syncEnCours.value = false
        }
    }
}
