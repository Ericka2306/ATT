package mg.itu.att

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mg.itu.att.ui.socle.SocleViewModel

// ---------- LES ROUTES ----------
// Routes en chaînes de caractères, comme dans listedetail (cours S5).
// Un écran qui a besoin d'un identifiant le reçoit dans la route : "candidat/{candidatId}".
// La liste complète est dans docs/05_CAS_UTILISATION.md §4.

/** Écran d'accueil provisoire, remplacé par l'écran de connexion à l'étape C1. */
const val ROUTE_ACCUEIL = "accueil"

// ---------- LA NAVIGATION ----------

/**
 * Graphe de navigation de l'application.
 *
 * Les écrans ne connaissent pas le navController : ils reçoivent des lambdas.
 * L'écran signale, la navigation décide.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_ACCUEIL) {
        composable(ROUTE_ACCUEIL) {
            val viewModel: SocleViewModel = viewModel()
            EcranAccueilProvisoire(viewModel)
        }
    }
}

// ---------- L'ÉCRAN PROVISOIRE ----------

/** Vérifie que le socle (Compose + Navigation + Room) fonctionne. Supprimé à l'étape C1. */
@Composable
fun EcranAccueilProvisoire(viewModel: SocleViewModel) {
    val etat by viewModel.uiState.collectAsState()

    Column(Modifier.padding(24.dp)) {
        Text("ATT", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Gestion des examens du permis de conduire",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (etat.donneesInserees) "Base de données prête (étape B1)." else "Création de la base…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text("Régions : ${etat.regions}")
        Text("Catégories de permis : ${etat.categories}")
        Text("Règles configurables : ${etat.regles}")
        Text("Utilisateurs : ${etat.utilisateurs}")
    }
}
