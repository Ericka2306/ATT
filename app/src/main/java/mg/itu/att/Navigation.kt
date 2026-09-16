package mg.itu.att

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
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
import mg.itu.att.ui.candidats.CandidatsViewModel
import mg.itu.att.ui.candidats.EcranDetailCandidat
import mg.itu.att.ui.candidats.EcranDossier
import mg.itu.att.ui.candidats.EcranDossiersATraiter
import mg.itu.att.ui.candidats.EcranFormulaireCandidat
import mg.itu.att.ui.candidats.EcranListeCandidats
import mg.itu.att.ui.communs.EcranAVenir
import mg.itu.att.ui.comptes.ComptesViewModel
import mg.itu.att.ui.comptes.EcranComptes
import mg.itu.att.ui.comptes.EcranFormulaireAdmin
import mg.itu.att.ui.comptes.EcranFormulaireExaminateur
import mg.itu.att.ui.comptes.EcranListeExaminateurs
import mg.itu.att.ui.comptes.EcranMotDePasse
import mg.itu.att.ui.communs.idArgument
import mg.itu.att.ui.communs.viewModelDuSousParcours
import mg.itu.att.ui.connexion.ConnexionViewModel
import mg.itu.att.ui.connexion.EcranConnexion
import mg.itu.att.ui.connexion.SessionUtilisateur

/**
 * Graphe de navigation de l'application (cours S5) : routes en chaînes,
 * argument = identifiant lu dans la route, écrans qui reçoivent des lambdas.
 * La liste complète des routes est dans docs/05_CAS_UTILISATION.md §4.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // UN SEUL ViewModel de session, partagé par tous les écrans : créé au-dessus de la navigation,
    // il survit aux changements d'écran et à la rotation (cours S6).
    val connexionViewModel: ConnexionViewModel = viewModel()
    val session by connexionViewModel.session.collectAsState()

    /** Déconnexion : retour à la connexion en vidant toute la pile (`popUpTo`, HORS_COURS n° 11). */
    fun seDeconnecter() {
        connexionViewModel.seDeconnecter()
        navController.navigate(Routes.CONNEXION) { popUpTo(navController.graph.id) { inclusive = true } }
    }

    NavHost(navController = navController, startDestination = Routes.CONNEXION) {

        composable(Routes.CONNEXION) {
            EcranConnexion(connexionViewModel, onConnecte = {
                navController.navigate(Routes.ACCUEIL) { popUpTo(Routes.CONNEXION) { inclusive = true } }
            })
        }

        composable(Routes.ACCUEIL) {
            val utilisateur = session
            if (utilisateur != null) {
                EcranAccueil(utilisateur, onNaviguer = { navController.navigate(it) }, onDeconnexion = { seDeconnecter() })
            } else {
                // Pas de session : rien de protégé, on remontre la connexion (le null va jusqu'à l'UI, cours S5).
                EcranConnexion(connexionViewModel, onConnecte = {
                    navController.navigate(Routes.ACCUEIL) { popUpTo(Routes.ACCUEIL) { inclusive = true } }
                })
            }
        }

        graphAutoEcoles(navController) { session }
        graphCandidats(navController) { session }
        graphComptes(navController) { session }

        // ---------- FONCTIONNALITÉS À VENIR ----------
        val libelles = Role.entries.flatMap { menuPour(it) }.associate { it.route to it.libelle }
        for (route in listOf(Routes.CONFIGURATION, Routes.SESSIONS, Routes.EVALUATION, Routes.RESULTATS, Routes.HISTORIQUE, Routes.PARCOURS)) {
            // (les comptes, examinateurs et le mot de passe sont dans graphComptes)
            composable(route) { EcranAVenir(libelles[route] ?: route, onRetour = { navController.popBackStack() }) }
        }
        composable(Routes.A_VENIR) { entree ->
            EcranAVenir(entree.arguments?.getString("libelle") ?: "À venir", onRetour = { navController.popBackStack() })
        }
    }
}

// ---------- AUTO-ÉCOLES (UC03, étape C2) ----------

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

private fun NavGraphBuilder.graphAutoEcoles(nav: NavHostController, session: () -> SessionUtilisateur?) {
    val retour: () -> Unit = { nav.popBackStack() }

    composable(RoutesAutoEcoles.LISTE) {
        val vm = viewModelDuSousParcours<AutoEcolesViewModel>(nav, RoutesAutoEcoles.LISTE, session()) ?: return@composable
        EcranListeAutoEcoles(vm, onNouvelle = { nav.navigate(RoutesAutoEcoles.NOUVELLE) }, onOuvrir = { nav.navigate(RoutesAutoEcoles.detail(it)) }, onRetour = retour)
    }
    composable(RoutesAutoEcoles.NOUVELLE) {
        val vm = viewModelDuSousParcours<AutoEcolesViewModel>(nav, RoutesAutoEcoles.LISTE, session()) ?: return@composable
        EcranFormulaireAutoEcole(vm, null, onEnregistre = { nav.navigate(RoutesAutoEcoles.detail(it)) { popUpTo(RoutesAutoEcoles.LISTE) } }, onRetour = retour)
    }
    composable(RoutesAutoEcoles.DETAIL) { entree ->
        val vm = viewModelDuSousParcours<AutoEcolesViewModel>(nav, RoutesAutoEcoles.LISTE, session()) ?: return@composable
        val id = entree.idArgument("autoEcoleId") ?: return@composable
        EcranDetailAutoEcole(vm, id, onModifier = { nav.navigate(RoutesAutoEcoles.modifier(id)) }, onCreerCompte = { nav.navigate(RoutesAutoEcoles.compte(id)) }, onRetour = retour)
    }
    composable(RoutesAutoEcoles.MODIFIER) { entree ->
        val vm = viewModelDuSousParcours<AutoEcolesViewModel>(nav, RoutesAutoEcoles.LISTE, session()) ?: return@composable
        val id = entree.idArgument("autoEcoleId") ?: return@composable
        EcranFormulaireAutoEcole(vm, id, onEnregistre = { nav.popBackStack() }, onRetour = retour)
    }
    composable(RoutesAutoEcoles.COMPTE) { entree ->
        val vm = viewModelDuSousParcours<AutoEcolesViewModel>(nav, RoutesAutoEcoles.LISTE, session()) ?: return@composable
        val id = entree.idArgument("autoEcoleId") ?: return@composable
        EcranFormulaireCompte(vm, id, onCree = { nav.popBackStack() }, onRetour = retour)
    }
}

// ---------- COMPTES : EXAMINATEURS, ADMINISTRATEURS, MOT DE PASSE (étape C1b) ----------

object RoutesComptes {
    const val EXAMINATEURS = Routes.EXAMINATEURS
    const val NOUVEL_EXAMINATEUR = "examinateur/nouveau"
    const val EXAMINATEUR = "examinateur/{examinateurId}"
    const val COMPTES = Routes.COMPTES
    const val NOUVEL_ADMIN = "compte/nouvel-admin"
    const val MOT_DE_PASSE = Routes.MOT_DE_PASSE
    fun examinateur(id: Int) = "examinateur/$id"
}

private fun NavGraphBuilder.graphComptes(nav: NavHostController, session: () -> SessionUtilisateur?) {
    val retour: () -> Unit = { nav.popBackStack() }

    composable(RoutesComptes.EXAMINATEURS) {
        val vm = viewModelDuSousParcours<ComptesViewModel>(nav, RoutesComptes.EXAMINATEURS, session()) ?: return@composable
        EcranListeExaminateurs(vm, onNouveau = { nav.navigate(RoutesComptes.NOUVEL_EXAMINATEUR) }, onOuvrir = { nav.navigate(RoutesComptes.examinateur(it)) }, onRetour = retour)
    }
    composable(RoutesComptes.NOUVEL_EXAMINATEUR) {
        val vm = viewModelDuSousParcours<ComptesViewModel>(nav, RoutesComptes.EXAMINATEURS, session()) ?: return@composable
        EcranFormulaireExaminateur(vm, null, onEnregistre = retour, onRetour = retour)
    }
    composable(RoutesComptes.EXAMINATEUR) { entree ->
        val vm = viewModelDuSousParcours<ComptesViewModel>(nav, RoutesComptes.EXAMINATEURS, session()) ?: return@composable
        val id = entree.idArgument("examinateurId") ?: return@composable
        EcranFormulaireExaminateur(vm, id, onEnregistre = retour, onRetour = retour)
    }
    composable(RoutesComptes.COMPTES) {
        val vm = viewModelDuSousParcours<ComptesViewModel>(nav, RoutesComptes.COMPTES, session()) ?: return@composable
        EcranComptes(vm, onNouvelAdmin = { nav.navigate(RoutesComptes.NOUVEL_ADMIN) }, onRetour = retour)
    }
    composable(RoutesComptes.NOUVEL_ADMIN) {
        val vm = viewModelDuSousParcours<ComptesViewModel>(nav, RoutesComptes.COMPTES, session()) ?: return@composable
        EcranFormulaireAdmin(vm, onCree = retour, onRetour = retour)
    }
    composable(RoutesComptes.MOT_DE_PASSE) {
        val s = session() ?: return@composable
        val vm: ComptesViewModel = viewModel()
        vm.definirSession(s)
        EcranMotDePasse(vm, onRetour = retour)
    }
}

// ---------- CANDIDATS ET DOSSIERS (UC04, UC05, étape C3) ----------

object RoutesCandidats {
    const val LISTE = Routes.CANDIDATS
    const val NOUVEAU = "candidat/nouveau"
    const val DETAIL = "candidat/{candidatId}"
    const val MODIFIER = "candidat/{candidatId}/modifier"
    const val DOSSIER = "dossier/{dossierId}"
    const val A_TRAITER = Routes.DOSSIERS
    fun detail(id: Int) = "candidat/$id"
    fun modifier(id: Int) = "candidat/$id/modifier"
    fun dossier(id: Int) = "dossier/$id"
}

private fun NavGraphBuilder.graphCandidats(nav: NavHostController, session: () -> SessionUtilisateur?) {
    val retour: () -> Unit = { nav.popBackStack() }

    composable(RoutesCandidats.LISTE) {
        val vm = viewModelDuSousParcours<CandidatsViewModel>(nav, RoutesCandidats.LISTE, session()) ?: return@composable
        EcranListeCandidats(vm, onNouveau = { nav.navigate(RoutesCandidats.NOUVEAU) }, onOuvrir = { nav.navigate(RoutesCandidats.detail(it)) }, onRetour = retour)
    }
    composable(RoutesCandidats.NOUVEAU) {
        val vm = viewModelDuSousParcours<CandidatsViewModel>(nav, RoutesCandidats.LISTE, session()) ?: return@composable
        EcranFormulaireCandidat(vm, null, onEnregistre = { nav.navigate(RoutesCandidats.detail(it)) { popUpTo(RoutesCandidats.LISTE) } }, onRetour = retour)
    }
    composable(RoutesCandidats.DETAIL) { entree ->
        val vm = viewModelDuSousParcours<CandidatsViewModel>(nav, RoutesCandidats.LISTE, session()) ?: return@composable
        val id = entree.idArgument("candidatId") ?: return@composable
        EcranDetailCandidat(vm, id, onModifier = { nav.navigate(RoutesCandidats.modifier(id)) }, onOuvrirDossier = { nav.navigate(RoutesCandidats.dossier(it)) }, onRetour = retour)
    }
    composable(RoutesCandidats.MODIFIER) { entree ->
        val vm = viewModelDuSousParcours<CandidatsViewModel>(nav, RoutesCandidats.LISTE, session()) ?: return@composable
        val id = entree.idArgument("candidatId") ?: return@composable
        EcranFormulaireCandidat(vm, id, onEnregistre = { nav.popBackStack() }, onRetour = retour)
    }
    // Le dossier et la liste « à traiter » sont accessibles hors du sous-parcours candidats :
    // ils ont leur propre ViewModel, chargé par identifiant.
    composable(RoutesCandidats.DOSSIER) { entree ->
        val s = session() ?: return@composable
        val vm: CandidatsViewModel = viewModel()
        vm.definirSession(s)
        val id = entree.idArgument("dossierId") ?: return@composable
        EcranDossier(vm, id, onRetour = retour)
    }
    composable(RoutesCandidats.A_TRAITER) {
        val s = session() ?: return@composable
        val vm: CandidatsViewModel = viewModel()
        vm.definirSession(s)
        EcranDossiersATraiter(vm, onOuvrir = { nav.navigate(RoutesCandidats.dossier(it)) }, onRetour = retour)
    }
}
