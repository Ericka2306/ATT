package mg.itu.att.ui.socle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mg.itu.att.data.AppDatabase
import mg.itu.att.data.DonneesInitiales

/** État de l'écran provisoire : quelques compteurs lus en direct dans la base. */
data class EtatSocle(
    val regions: Int = 0,
    val categories: Int = 0,
    val regles: Int = 0,
    val utilisateurs: Int = 0,
    val donneesInserees: Boolean = false,
)

/**
 * ViewModel PROVISOIRE de l'étape B1 (remplacé par ConnexionViewModel à l'étape C1).
 * Il prouve que la base se crée et se remplit au premier lancement :
 * même structure que ProduitsViewModel de listedetailv3 (init + combine + stateIn).
 */
class SocleViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.obtenir(application)

    init {
        // Premier lancement : on remplit la base si elle est vide.
        viewModelScope.launch { DonneesInitiales.insererSiVide(db) }
    }

    val uiState: StateFlow<EtatSocle> =
        combine(
            db.regionDao().nombreEnDirect(),
            db.categoriePermisDao().nombreEnDirect(),
            db.regleConfigDao().nombreEnDirect(),
            db.utilisateurDao().nombreEnDirect(),
        ) { regions, categories, regles, utilisateurs ->
            EtatSocle(
                regions = regions,
                categories = categories,
                regles = regles,
                utilisateurs = utilisateurs,
                donneesInserees = regions > 0,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EtatSocle(),
        )
}
