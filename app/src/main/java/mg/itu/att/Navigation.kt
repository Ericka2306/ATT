package mg.itu.att

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mg.itu.att.data.Role
import mg.itu.att.ui.accueil.EcranAccueil
import mg.itu.att.ui.accueil.Routes
import mg.itu.att.ui.accueil.menuPour
import mg.itu.att.ui.communs.EcranAVenir
import mg.itu.att.ui.connexion.ConnexionViewModel
import mg.itu.att.ui.connexion.EcranConnexion

/**
 * Graphe de navigation de l'application (cours S5) : routes en chaînes,
 * argument = identifiant ou libellé lu dans la route, écrans qui reçoivent des lambdas.
 * La liste complète des routes est dans docs/05_CAS_UTILISATION.md §4 et `Routes`.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // UN SEUL ViewModel de session, partagé par tous les écrans : créé au-dessus de la navigation,
    // il survit aux changements d'écran et à la rotation (cours S6).
    val connexionViewModel: ConnexionViewModel = viewModel()
    val session by connexionViewModel.session.collectAsState()

    /**
     * Déconnexion : retour à la connexion en vidant toute la pile, pour que le bouton « retour »
     * du téléphone ne ramène jamais dans un écran protégé (`popUpTo` : docs/HORS_COURS.md n° 11).
     */
    fun seDeconnecter() {
        connexionViewModel.seDeconnecter()
        navController.navigate(Routes.CONNEXION) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Routes.CONNEXION) {

        composable(Routes.CONNEXION) {
            EcranConnexion(
                viewModel = connexionViewModel,
                onConnecte = {
                    navController.navigate(Routes.ACCUEIL) {
                        popUpTo(Routes.CONNEXION) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.ACCUEIL) {
            val utilisateur = session
            if (utilisateur != null) {
                EcranAccueil(
                    session = utilisateur,
                    onNaviguer = { route -> navController.navigate(route) },
                    onDeconnexion = { seDeconnecter() },
                )
            } else {
                // Pas de session : on n'affiche rien de protégé, on remontre la connexion
                // (le null se gère jusque dans l'UI, comme le prix non fixé du cours).
                EcranConnexion(
                    viewModel = connexionViewModel,
                    onConnecte = { navController.navigate(Routes.ACCUEIL) { popUpTo(Routes.ACCUEIL) { inclusive = true } } },
                )
            }
        }

        // ---------- FONCTIONNALITÉS À VENIR ----------
        // Chaque étape du plan remplace l'une de ces lignes par son vrai écran.
        // Le titre affiché est le libellé de l'entrée de menu correspondante.
        val libelles = Role.entries.flatMap { menuPour(it) }.associate { it.route to it.libelle }
        for (route in listOf(
            Routes.CONFIGURATION, Routes.AUTO_ECOLES, Routes.CANDIDATS, Routes.DOSSIERS, Routes.SESSIONS,
            Routes.EVALUATION, Routes.RESULTATS, Routes.HISTORIQUE, Routes.PARCOURS,
        )) {
            composable(route) {
                EcranAVenir(libelle = libelles[route] ?: route, onRetour = { navController.popBackStack() })
            }
        }

        composable(Routes.A_VENIR) { backStackEntry ->
            val libelle = backStackEntry.arguments?.getString("libelle") ?: "À venir"
            EcranAVenir(libelle = libelle, onRetour = { navController.popBackStack() })
        }
    }
}
