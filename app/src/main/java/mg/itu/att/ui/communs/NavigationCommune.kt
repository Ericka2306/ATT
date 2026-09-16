package mg.itu.att.ui.communs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import mg.itu.att.ui.connexion.SessionUtilisateur

/** Un ViewModel qui a besoin de savoir qui est connecté (filtrage par rôle, signature de l'historique). */
interface ViewModelAvecSession {
    fun definirSession(session: SessionUtilisateur)
}

/**
 * Le ViewModel partagé d'un sous-parcours (liste → fiche → formulaire), obtenu sur l'entrée de pile
 * de la route racine : c'est le « ViewModel au-dessus de la navigation » du cours S6, limité à un
 * sous-parcours. Rend null si personne n'est connecté (l'écran n'affiche alors rien de protégé).
 */
@Composable
inline fun <reified VM> viewModelDuSousParcours(
    navController: NavHostController,
    routeRacine: String,
    session: SessionUtilisateur?,
): VM? where VM : ViewModel, VM : ViewModelAvecSession {
    if (session == null) return null
    val entree = remember { navController.getBackStackEntry(routeRacine) }
    val vm: VM = viewModel(entree)
    vm.definirSession(session)
    return vm
}

/** Lit un identifiant entier dans les arguments d'une route ("{xxxId}") ; null si absent ou invalide (cours S5). */
fun androidx.navigation.NavBackStackEntry.idArgument(nom: String): Int? = arguments?.getString(nom)?.toIntOrNull()
