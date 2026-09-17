package mg.itu.att.ui.historique

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import mg.itu.att.data.AppDatabase
import mg.itu.att.data.Historique
import mg.itu.att.metier.FiltresHistorique
import mg.itu.att.metier.FiltresHistorique.Periode
import mg.itu.att.metier.dateDuJour
import mg.itu.att.ui.communs.Option
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.connexion.SessionUtilisateur

// ---------- ÉTATS ----------

/** Une ligne d'historique avec les libellés utiles à l'affichage (jointure faite dans le ViewModel). */
data class LigneHistoriqueComplete(val historique: Historique, val libelleEntite: String, val nomUtilisateur: String)

/** Les filtres choisis à l'écran ; null = pas de filtre. */
data class FiltresChoisis(val entite: String? = null, val utilisateurId: Int? = null, val periode: Periode = Periode.TOUT)

data class EtatHistorique(
    val lignes: List<LigneHistoriqueComplete> = emptyList(),
    val filtres: FiltresChoisis = FiltresChoisis(),
    /** Les objets présents dans l'historique : l'identifiant de l'option est sa position dans cette liste. */
    val entites: List<String> = emptyList(),
    val utilisateurs: List<Option> = emptyList(),
    /** Faux pour un rôle qui n'a pas le droit de lire l'historique : l'écran n'affiche rien. */
    val autorise: Boolean = false,
    val total: Int = 0,
)

// ---------- LE VIEWMODEL ----------

/**
 * Historique des modifications (UC14) : consultation seule, jamais d'écriture.
 * Le filtrage est délégué à la fonction pure `FiltresHistorique.filtrer` ; l'écran ne fait que signaler les choix.
 */
class HistoriqueViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private val session = MutableStateFlow<SessionUtilisateur?>(null)
    private val filtres = MutableStateFlow(FiltresChoisis())

    override fun definirSession(session: SessionUtilisateur) {
        if (this.session.value != session) this.session.value = session
    }

    fun choisirEntite(entite: String?) = filtres.update { it.copy(entite = entite) }
    fun choisirUtilisateur(utilisateurId: Int?) = filtres.update { it.copy(utilisateurId = utilisateurId) }
    fun choisirPeriode(periode: Periode) = filtres.update { it.copy(periode = periode) }

    val uiState: StateFlow<EtatHistorique> =
        combine(db.historiqueDao().tout(), db.utilisateurDao().tous(), filtres, session) { tout, utilisateurs, f, s ->
            if (s == null || !FiltresHistorique.peutConsulter(s.role)) return@combine EtatHistorique()
            val noms = utilisateurs.associate { it.id to it.nom }
            EtatHistorique(
                lignes = FiltresHistorique.filtrer(tout, f.entite, f.utilisateurId, f.periode, dateDuJour())
                    .map { LigneHistoriqueComplete(it, FiltresHistorique.libelleEntite(it.entite), noms[it.utilisateurId] ?: "?") },
                filtres = f,
                entites = FiltresHistorique.entitesPresentes(tout),
                // Seuls les utilisateurs qui apparaissent dans l'historique sont proposés.
                utilisateurs = tout.map { it.utilisateurId }.distinct()
                    .map { Option(it, noms[it] ?: "?") }
                    .sortedBy { it.libelle },
                autorise = true,
                total = tout.size,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatHistorique())
}

