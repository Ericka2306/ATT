package mg.itu.att

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mg.itu.att.data.Role
import mg.itu.att.ui.accueil.EcranAccueil
import mg.itu.att.ui.accueil.Routes
import mg.itu.att.ui.accueil.menuPour
import mg.itu.att.ui.autoecoles.AutoEcolesViewModel
import mg.itu.att.ui.autoecoles.EcranDetailAutoEcole
import mg.itu.att.ui.autoecoles.EcranFormulaireAutoEcole
import mg.itu.att.ui.autoecoles.EcranFormulaireCompte
import mg.itu.att.ui.autoecoles.EcranListeAutoEcoles
import mg.itu.att.ui.communs.EcranAVenir
import mg.itu.att.ui.connexion.ConnexionViewModel
import mg.itu.att.ui.connexion.EcranConnexion
import mg.itu.att.ui.connexion.SessionUtilisateur

/**
 * Graphe de navigation de l'application (cours S5) : routes en chaînes,
 * argument = identifiant lu dans la route, écrans qui reçoivent des lambdas.
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

        // ---------- AUTO-ÉCOLES (UC03, étape C2) ----------
        // Le ViewModel est partagé par les quatre écrans du sous-parcours : la saisie survit aux allers-retours.
        graphAutoEcoles(navController, sessionCourante = { session })

        // ---------- FONCTIONNALITÉS À VENIR ----------
        // Chaque étape du plan remplace l'une de ces lignes par son vrai écran.
        // Le titre affiché est le libellé de l'entrée de menu correspondante.
        val libelles = Role.entries.flatMap { menuPour(it) }.associate { it.route to it.libelle }
        for (route in listOf(
            Routes.CONFIGURATION, Routes.CANDIDATS, Routes.DOSSIERS, Routes.SESSIONS,
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

/** Routes du sous-parcours auto-écoles (docs/05 §4.3). */
object RoutesAutoEcoles {
    const val LISTE = Routes.AUTO_ECOLES
    const val NOUVELLE = "autoecole/nouvelle"
    const val DETAIL = "autoecole/{autoEcoleId}"
    const val MODIFIER = "autoecole/{autoEcoleId}/modifier"
    const val COMPTE = "autoecole/{autoEcoleId}/compte"
    fun detail(id: Int) = "autoecole/$id"
    fun modifier(id: Int) = "autoecole/$id/modifier"
    fun compte(id: Int) = "autoecole/$id/compte"
}

/**
 * Les écrans des auto-écoles. Chaque écran obtient le ViewModel avec `viewModel()` sur l'entrée
 * de la liste (`getBackStackEntry`), donc le même pour tout le sous-parcours.
 */
private fun androidx.navigation.NavGraphBuilder.graphAutoEcoles(
    navController: NavHostController,
    sessionCourante: () -> SessionUtilisateur?,
) {
    /** Le ViewModel partagé, initialisé avec la session ; null si personne n'est connecté. */
    @Composable
    fun viewModelPartage(): AutoEcolesViewModel? {
        val session = sessionCourante() ?: return null
        val entreeListe = remember { navController.getBackStackEntry(RoutesAutoEcoles.LISTE) }
        val vm: AutoEcolesViewModel = viewModel(entreeListe)
        vm.definirSession(session)
        return vm
    }

    composable(RoutesAutoEcoles.LISTE) {
        val vm = viewModelPartage() ?: return@composable
        EcranListeAutoEcoles(
            viewModel = vm,
            onNouvelle = { navController.navigate(RoutesAutoEcoles.NOUVELLE) },
            onOuvrir = { id -> navController.navigate(RoutesAutoEcoles.detail(id)) },
            onRetour = { navController.popBackStack() },
        )
    }
    composable(RoutesAutoEcoles.NOUVELLE) {
        val vm = viewModelPartage() ?: return@composable
        EcranFormulaireAutoEcole(
            viewModel = vm,
            autoEcoleId = null,
            onEnregistre = { id ->
                navController.navigate(RoutesAutoEcoles.detail(id)) {
                    popUpTo(RoutesAutoEcoles.LISTE)
                }
            },
            onRetour = { navController.popBackStack() },
        )
    }
    composable(RoutesAutoEcoles.DETAIL) { entree ->
        val vm = viewModelPartage() ?: return@composable
        val id = entree.arguments?.getString("autoEcoleId")?.toIntOrNull() ?: return@composable
        EcranDetailAutoEcole(
            viewModel = vm,
            autoEcoleId = id,
            onModifier = { navController.navigate(RoutesAutoEcoles.modifier(id)) },
            onCreerCompte = { navController.navigate(RoutesAutoEcoles.compte(id)) },
            onRetour = { navController.popBackStack() },
        )
    }
    composable(RoutesAutoEcoles.MODIFIER) { entree ->
        val vm = viewModelPartage() ?: return@composable
        val id = entree.arguments?.getString("autoEcoleId")?.toIntOrNull() ?: return@composable
        EcranFormulaireAutoEcole(
            viewModel = vm,
            autoEcoleId = id,
            onEnregistre = { navController.popBackStack() },
            onRetour = { navController.popBackStack() },
        )
    }
    composable(RoutesAutoEcoles.COMPTE) { entree ->
        val vm = viewModelPartage() ?: return@composable
        val id = entree.arguments?.getString("autoEcoleId")?.toIntOrNull() ?: return@composable
        EcranFormulaireCompte(
            viewModel = vm,
            autoEcoleId = id,
            onCree = { navController.popBackStack() },
            onRetour = { navController.popBackStack() },
        )
    }
}
