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
import mg.itu.att.ui.configuration.ConfigurationViewModel
import mg.itu.att.ui.configuration.EcranCategories
import mg.itu.att.ui.configuration.EcranCentres
import mg.itu.att.ui.configuration.EcranConfiguration
import mg.itu.att.ui.configuration.EcranDetailCategorie
import mg.itu.att.ui.configuration.EcranEpreuve
import mg.itu.att.ui.configuration.EcranFormulaireBareme
import mg.itu.att.ui.configuration.EcranFormulaireCategorie
import mg.itu.att.ui.configuration.EcranFormulaireCentre
import mg.itu.att.ui.configuration.EcranFormulaireCritere
import mg.itu.att.ui.configuration.EcranFormulaireEpreuve
import mg.itu.att.ui.configuration.EcranFormulaireQuestion
import mg.itu.att.ui.configuration.EcranFormulaireRegle
import mg.itu.att.ui.configuration.EcranRegles
import mg.itu.att.ui.sessions.EcranDetailSession
import mg.itu.att.ui.sessions.EcranFormulaireSession
import mg.itu.att.ui.sessions.EcranListeSessions
import mg.itu.att.ui.sessions.SessionsViewModel
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
        graphConfiguration(navController) { session }
        graphSessions(navController) { session }

        // ---------- FONCTIONNALITÉS À VENIR ----------
        val libelles = Role.entries.flatMap { menuPour(it) }.associate { it.route to it.libelle }
        for (route in listOf(Routes.EVALUATION, Routes.RESULTATS, Routes.HISTORIQUE, Routes.PARCOURS)) {
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

// ---------- SESSIONS ET CRÉNEAUX (UC06, étape C5) ----------

object RoutesSessions {
    const val LISTE = Routes.SESSIONS
    const val NOUVELLE = "session/nouvelle"
    const val DETAIL = "session/{sessionId}"
    const val INSCRIRE = "session/{sessionId}/inscrire"
    const val APPEL = "session/{sessionId}/appel"
    fun detail(id: Int) = "session/$id"
    fun inscrire(id: Int) = "session/$id/inscrire"
    fun appel(id: Int) = "session/$id/appel"
}

private fun NavGraphBuilder.graphSessions(nav: NavHostController, session: () -> SessionUtilisateur?) {
    val retour: () -> Unit = { nav.popBackStack() }
    @Composable
    fun vm(): SessionsViewModel? = viewModelDuSousParcours(nav, RoutesSessions.LISTE, session())

    composable(RoutesSessions.LISTE) {
        val v = vm() ?: return@composable
        EcranListeSessions(v, onNouvelle = { nav.navigate(RoutesSessions.NOUVELLE) }, onOuvrir = { nav.navigate(RoutesSessions.detail(it)) }, onRetour = retour)
    }
    composable(RoutesSessions.NOUVELLE) {
        val v = vm() ?: return@composable
        EcranFormulaireSession(v, onCree = { nav.navigate(RoutesSessions.detail(it)) { popUpTo(RoutesSessions.LISTE) } }, onRetour = retour)
    }
    composable(RoutesSessions.DETAIL) { entree ->
        val v = vm() ?: return@composable
        val id = entree.idArgument("sessionId") ?: return@composable
        EcranDetailSession(v, id, onInscrire = { nav.navigate(RoutesSessions.inscrire(id)) }, onAppel = { nav.navigate(RoutesSessions.appel(id)) }, onRetour = retour)
    }
    // Inscriptions (C6) et appel (C7) : écrans « à venir » pour l'instant.
    composable(RoutesSessions.INSCRIRE) { EcranAVenir("Inscriptions (étape C6)", onRetour = retour) }
    composable(RoutesSessions.APPEL) { EcranAVenir("Appel (étape C7)", onRetour = retour) }
}

// ---------- CONFIGURATION (UC02, étape C4) ----------

object RoutesConfiguration {
    const val MENU = Routes.CONFIGURATION
    const val CATEGORIES = "config/categories"
    const val NOUVELLE_CATEGORIE = "config/categorie/nouvelle"
    const val CATEGORIE = "config/categorie/{categorieId}"
    const val MODIFIER_CATEGORIE = "config/categorie/{categorieId}/modifier"
    const val NOUVELLE_EPREUVE = "config/categorie/{categorieId}/epreuve/nouvelle"
    const val EPREUVE = "config/epreuve/{epreuveId}"
    const val MODIFIER_EPREUVE = "config/epreuve/{epreuveId}/modifier/{categorieId}"
    const val BAREME = "config/epreuve/{epreuveId}/bareme"
    const val QUESTION = "config/epreuve/{epreuveId}/question"
    const val CRITERE = "config/epreuve/{epreuveId}/critere"
    const val REGLES = "config/regles"
    const val REGLE = "config/regle/{regleId}"
    const val CENTRES = "config/centres"
    const val NOUVEAU_CENTRE = "config/centre/nouveau"
    const val CENTRE = "config/centre/{centreId}"
    fun categorie(id: Int) = "config/categorie/$id"
    fun modifierCategorie(id: Int) = "config/categorie/$id/modifier"
    fun nouvelleEpreuve(categorieId: Int) = "config/categorie/$categorieId/epreuve/nouvelle"
    fun epreuve(id: Int) = "config/epreuve/$id"
    fun modifierEpreuve(id: Int, categorieId: Int) = "config/epreuve/$id/modifier/$categorieId"
    fun bareme(epreuveId: Int) = "config/epreuve/$epreuveId/bareme"
    fun question(epreuveId: Int) = "config/epreuve/$epreuveId/question"
    fun critere(epreuveId: Int) = "config/epreuve/$epreuveId/critere"
    fun regle(id: Int) = "config/regle/$id"
    fun centre(id: Int) = "config/centre/$id"
}

private fun NavGraphBuilder.graphConfiguration(nav: NavHostController, session: () -> SessionUtilisateur?) {
    val retour: () -> Unit = { nav.popBackStack() }
    /** Le ViewModel partagé de tout le sous-parcours, ancré sur le menu de configuration. */
    @Composable
    fun vm(): ConfigurationViewModel? = viewModelDuSousParcours(nav, RoutesConfiguration.MENU, session())

    composable(RoutesConfiguration.MENU) {
        vm() ?: return@composable
        EcranConfiguration(onCategories = { nav.navigate(RoutesConfiguration.CATEGORIES) }, onRegles = { nav.navigate(RoutesConfiguration.REGLES) }, onCentres = { nav.navigate(RoutesConfiguration.CENTRES) }, onRetour = retour)
    }
    composable(RoutesConfiguration.CATEGORIES) {
        val v = vm() ?: return@composable
        EcranCategories(v, onNouvelle = { nav.navigate(RoutesConfiguration.NOUVELLE_CATEGORIE) }, onOuvrir = { nav.navigate(RoutesConfiguration.categorie(it)) }, onRetour = retour)
    }
    composable(RoutesConfiguration.NOUVELLE_CATEGORIE) {
        val v = vm() ?: return@composable
        EcranFormulaireCategorie(v, null, onEnregistre = { nav.navigate(RoutesConfiguration.categorie(it)) { popUpTo(RoutesConfiguration.CATEGORIES) } }, onRetour = retour)
    }
    composable(RoutesConfiguration.CATEGORIE) { entree ->
        val v = vm() ?: return@composable
        val id = entree.idArgument("categorieId") ?: return@composable
        EcranDetailCategorie(v, id, onModifier = { nav.navigate(RoutesConfiguration.modifierCategorie(id)) }, onNouvelleEpreuve = { nav.navigate(RoutesConfiguration.nouvelleEpreuve(id)) }, onOuvrirEpreuve = { nav.navigate(RoutesConfiguration.epreuve(it)) }, onRetour = retour)
    }
    composable(RoutesConfiguration.MODIFIER_CATEGORIE) { entree ->
        val v = vm() ?: return@composable
        val id = entree.idArgument("categorieId") ?: return@composable
        EcranFormulaireCategorie(v, id, onEnregistre = { nav.popBackStack() }, onRetour = retour)
    }
    composable(RoutesConfiguration.NOUVELLE_EPREUVE) { entree ->
        val v = vm() ?: return@composable
        val categorieId = entree.idArgument("categorieId") ?: return@composable
        EcranFormulaireEpreuve(v, categorieId, null, onEnregistre = retour, onRetour = retour)
    }
    composable(RoutesConfiguration.EPREUVE) { entree ->
        val v = vm() ?: return@composable
        val id = entree.idArgument("epreuveId") ?: return@composable
        val categorieId = v.epreuve.value.epreuve?.categorieId ?: 0
        EcranEpreuve(
            v, id,
            onModifier = { nav.navigate(RoutesConfiguration.modifierEpreuve(id, categorieId)) },
            onNouveauBareme = { nav.navigate(RoutesConfiguration.bareme(id)) },
            onNouvelleQuestion = { nav.navigate(RoutesConfiguration.question(id)) },
            onNouveauCritere = { nav.navigate(RoutesConfiguration.critere(id)) },
            onRetour = retour,
        )
    }
    composable(RoutesConfiguration.MODIFIER_EPREUVE) { entree ->
        val v = vm() ?: return@composable
        val id = entree.idArgument("epreuveId") ?: return@composable
        val categorieId = entree.idArgument("categorieId") ?: return@composable
        EcranFormulaireEpreuve(v, categorieId, id, onEnregistre = retour, onRetour = retour)
    }
    composable(RoutesConfiguration.BAREME) { entree ->
        val v = vm() ?: return@composable
        val id = entree.idArgument("epreuveId") ?: return@composable
        EcranFormulaireBareme(v, id, onCree = retour, onRetour = retour)
    }
    composable(RoutesConfiguration.QUESTION) { entree ->
        val v = vm() ?: return@composable
        val id = entree.idArgument("epreuveId") ?: return@composable
        EcranFormulaireQuestion(v, id, onCree = retour, onRetour = retour)
    }
    composable(RoutesConfiguration.CRITERE) { entree ->
        val v = vm() ?: return@composable
        val id = entree.idArgument("epreuveId") ?: return@composable
        EcranFormulaireCritere(v, id, onCree = retour, onRetour = retour)
    }
    composable(RoutesConfiguration.REGLES) {
        val v = vm() ?: return@composable
        EcranRegles(v, onOuvrir = { nav.navigate(RoutesConfiguration.regle(it)) }, onRetour = retour)
    }
    composable(RoutesConfiguration.REGLE) { entree ->
        val v = vm() ?: return@composable
        val id = entree.idArgument("regleId") ?: return@composable
        EcranFormulaireRegle(v, id, onEnregistre = retour, onRetour = retour)
    }
    composable(RoutesConfiguration.CENTRES) {
        val v = vm() ?: return@composable
        EcranCentres(v, onNouveau = { nav.navigate(RoutesConfiguration.NOUVEAU_CENTRE) }, onOuvrir = { nav.navigate(RoutesConfiguration.centre(it)) }, onRetour = retour)
    }
    composable(RoutesConfiguration.NOUVEAU_CENTRE) {
        val v = vm() ?: return@composable
        EcranFormulaireCentre(v, null, onEnregistre = retour, onRetour = retour)
    }
    composable(RoutesConfiguration.CENTRE) { entree ->
        val v = vm() ?: return@composable
        val id = entree.idArgument("centreId") ?: return@composable
        EcranFormulaireCentre(v, id, onEnregistre = retour, onRetour = retour)
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
