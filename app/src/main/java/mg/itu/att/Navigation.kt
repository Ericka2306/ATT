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
import mg.itu.att.data.EntitesHistorique
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
import mg.itu.att.ui.candidats.EcranPieceJointe
import mg.itu.att.ui.candidats.PieceJointeViewModel
import mg.itu.att.ui.candidats.EcranDossiersATraiter
import mg.itu.att.ui.candidats.EcranFormulaireCompteCandidat
import mg.itu.att.ui.candidats.EcranParcours
import mg.itu.att.ui.candidats.EcranFormulaireCandidat
import mg.itu.att.ui.candidats.EcranListeCandidats
import mg.itu.att.ui.communs.EcranAVenir
import mg.itu.att.ui.communs.revenir
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
import mg.itu.att.ui.evaluation.EcranEvaluationConduite
import mg.itu.att.ui.evaluation.EcranEvaluationTheorie
import mg.itu.att.ui.evaluation.EvaluationConduiteViewModel
import mg.itu.att.ui.evaluation.EcranSessionsExaminateur
import mg.itu.att.ui.evaluation.EvaluationTheorieViewModel
import mg.itu.att.ui.evaluation.EcranTentatives
import mg.itu.att.ui.evaluation.TentativesViewModel
import mg.itu.att.ui.historique.EcranHistorique
import mg.itu.att.ui.historique.HistoriqueViewModel
import mg.itu.att.ui.appel.AppelViewModel
import mg.itu.att.ui.appel.EcranAppel
import mg.itu.att.ui.inscriptions.EcranInscriptions
import mg.itu.att.ui.inscriptions.EcranMesInscriptions
import mg.itu.att.ui.impression.EcranImpression
import mg.itu.att.ui.impression.ImpressionViewModel
import mg.itu.att.ui.impression.TypeDocument
import mg.itu.att.ui.inscriptions.MesInscriptionsViewModel
import mg.itu.att.ui.resultats.EcranDetailResultat
import mg.itu.att.ui.resultats.EcranResultats
import mg.itu.att.ui.resultats.ResultatsViewModel
import mg.itu.att.ui.inscriptions.InscriptionsViewModel
import mg.itu.att.ui.sessions.EcranDetailSession
import mg.itu.att.ui.synchronisation.EcranSynchronisation
import mg.itu.att.ui.synchronisation.SynchronisationViewModel
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
        graphHistorique(navController) { session }

        graphResultats(navController) { session }
        composable(Routes.SYNCHRONISATION) {
            val s = session ?: return@composable
            val vm: SynchronisationViewModel = viewModel()
            vm.definirSession(s)
            EcranSynchronisation(vm, onRetour = { navController.revenir() })
        }
        graphImpression(navController) { session }

        // ---------- FONCTIONNALITÉS À VENIR ----------
        composable(Routes.A_VENIR) { entree ->
            EcranAVenir(entree.arguments?.getString("libelle") ?: "À venir", onRetour = { navController.revenir() })
        }
    }
}

// ---------- IMPRESSION (UC13, étape C13) ----------

object RoutesImpression {
    const val CONVOCATION = "impression/convocation/{inscriptionId}"
    const val APPEL = "impression/appel/{sessionId}"
    const val ADMIS = "impression/admis/{sessionId}"
    const val RELEVE = "impression/releve/{resultatId}"
    fun convocation(id: Int) = "impression/convocation/$id"
    fun appel(id: Int) = "impression/appel/$id"
    fun admis(id: Int) = "impression/admis/$id"
    fun releve(id: Int) = "impression/releve/$id"
}

/**
 * Les quatre documents imprimables : un seul écran d'aperçu, le type et l'identifiant viennent de la route.
 * Le ViewModel vérifie les droits avant de construire la page (une auto-école n'imprime que ses candidats).
 */
private fun NavGraphBuilder.graphImpression(nav: NavHostController, session: () -> SessionUtilisateur?) {
    val retour: () -> Unit = { nav.revenir() }

    fun NavGraphBuilder.document(route: String, argument: String, type: TypeDocument) {
        composable(route) { entree ->
            val s = session() ?: return@composable
            val vm: ImpressionViewModel = viewModel()
            vm.definirSession(s)
            val id = entree.idArgument(argument) ?: return@composable
            EcranImpression(vm, type, id, onRetour = retour)
        }
    }

    document(RoutesImpression.CONVOCATION, "inscriptionId", TypeDocument.CONVOCATION)
    document(RoutesImpression.APPEL, "sessionId", TypeDocument.LISTE_APPEL)
    document(RoutesImpression.ADMIS, "sessionId", TypeDocument.LISTE_ADMIS)
    document(RoutesImpression.RELEVE, "resultatId", TypeDocument.RELEVE)
}

// ---------- RÉSULTATS (UC10, UC11, UC12, étape C11) ----------

object RoutesResultats {
    const val LISTE = Routes.RESULTATS
    const val DETAIL = "resultat/{resultatId}"
    fun detail(id: Int) = "resultat/$id"
}

/**
 * Résultats : la file « à valider » de l'ATT, les résultats validés de l'auto-école et du candidat,
 * les évaluations saisies par l'examinateur. Un seul écran, filtré par le ViewModel selon le rôle.
 */
private fun NavGraphBuilder.graphResultats(nav: NavHostController, session: () -> SessionUtilisateur?) {
    val retour: () -> Unit = { nav.revenir() }

    composable(RoutesResultats.LISTE) {
        val vm = viewModelDuSousParcours<ResultatsViewModel>(nav, RoutesResultats.LISTE, session()) ?: return@composable
        EcranResultats(vm, onOuvrir = { nav.navigate(RoutesResultats.detail(it)) }, onRetour = retour)
    }
    composable(RoutesResultats.DETAIL) { entree ->
        val vm = viewModelDuSousParcours<ResultatsViewModel>(nav, RoutesResultats.LISTE, session()) ?: return@composable
        val id = entree.idArgument("resultatId") ?: return@composable
        EcranDetailResultat(
            vm, id,
            onImprimer = { nav.navigate(RoutesImpression.releve(id)) },
            // Une correction crée un nouveau résultat : on l'ouvre à la place de l'ancien.
            onCorrige = { nouveau -> nav.navigate(RoutesResultats.detail(nouveau)) { popUpTo(RoutesResultats.LISTE) } },
            onRetour = retour,
        )
    }
}

// ---------- HISTORIQUE (UC14, étape C12) ----------

/**
 * Historique global (menu Super Admin et Admin ATT). Un clic sur une ligne ouvre la fiche de l'objet.
 * Les fiches candidat, session et auto-école vivent dans un sous-parcours dont la liste est la racine
 * (`viewModelDuSousParcours`) : on ouvre d'abord la liste, puis la fiche, pour que la racine soit dans la pile.
 */
private fun NavGraphBuilder.graphHistorique(nav: NavHostController, session: () -> SessionUtilisateur?) {
    val retour: () -> Unit = { nav.revenir() }

    fun peutOuvrir(entite: String) = entite in listOf(
        EntitesHistorique.CANDIDAT, EntitesHistorique.DOSSIER, EntitesHistorique.SESSION, EntitesHistorique.AUTO_ECOLE,
    )

    fun ouvrir(entite: String, id: Int) = when (entite) {
        EntitesHistorique.CANDIDAT -> { nav.navigate(RoutesCandidats.LISTE); nav.navigate(RoutesCandidats.detail(id)) }
        EntitesHistorique.DOSSIER -> nav.navigate(RoutesCandidats.dossier(id))
        EntitesHistorique.SESSION -> { nav.navigate(RoutesSessions.LISTE); nav.navigate(RoutesSessions.detail(id)) }
        EntitesHistorique.AUTO_ECOLE -> { nav.navigate(RoutesAutoEcoles.LISTE); nav.navigate(RoutesAutoEcoles.detail(id)) }
        else -> Unit
    }

    composable(Routes.HISTORIQUE) {
        val s = session() ?: return@composable
        val vm: HistoriqueViewModel = viewModel()
        vm.definirSession(s)
        EcranHistorique(vm, peutOuvrir = ::peutOuvrir, onOuvrir = ::ouvrir, onRetour = retour)
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
    val retour: () -> Unit = { nav.revenir() }

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
    const val TENTATIVES = "session/{sessionId}/tentatives"
    fun detail(id: Int) = "session/$id"
    fun inscrire(id: Int) = "session/$id/inscrire"
    fun appel(id: Int) = "session/$id/appel"
    fun tentatives(id: Int) = "session/$id/tentatives"
}

/** Routes de l'évaluation (UC09) : sessions à évaluer, tentatives d'une session, saisie par épreuve (C9, C10). */
object RoutesEvaluation {
    const val SESSIONS = Routes.EVALUATION
    const val THEORIE = "tentative/{tentativeId}/theorie"
    const val CONDUITE = "tentative/{tentativeId}/conduite"
    fun saisie(tentativeId: Int, codeEpreuve: String) = if (codeEpreuve == "THEORIE") "tentative/$tentativeId/theorie" else "tentative/$tentativeId/conduite"
}

private fun NavGraphBuilder.graphSessions(nav: NavHostController, session: () -> SessionUtilisateur?) {
    val retour: () -> Unit = { nav.revenir() }
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
        EcranDetailSession(
            v, id,
            onInscrire = { nav.navigate(RoutesSessions.inscrire(id)) },
            onAppel = { nav.navigate(RoutesSessions.appel(id)) },
            onTentatives = { nav.navigate(RoutesSessions.tentatives(id)) },
            onImprimerAppel = { nav.navigate(RoutesImpression.appel(id)) },
            onImprimerAdmis = { nav.navigate(RoutesImpression.admis(id)) },
            onRetour = retour,
        )
    }
    composable(RoutesSessions.INSCRIRE) { entree ->
        val s = session() ?: return@composable
        val id = entree.idArgument("sessionId") ?: return@composable
        val v: InscriptionsViewModel = viewModel()
        v.definirSession(s)
        EcranInscriptions(v, id, onImprimerConvocation = { nav.navigate(RoutesImpression.convocation(it)) }, onRetour = retour)
    }
    composable(RoutesSessions.APPEL) { entree ->
        val s = session() ?: return@composable
        val id = entree.idArgument("sessionId") ?: return@composable
        val v: AppelViewModel = viewModel()
        v.definirSession(s)
        EcranAppel(v, id, onRetour = retour)
    }
    composable(RoutesSessions.TENTATIVES) { entree ->
        val s = session() ?: return@composable
        val id = entree.idArgument("sessionId") ?: return@composable
        val v: TentativesViewModel = viewModel()
        v.definirSession(s)
        EcranTentatives(v, id, onTentative = { tentativeId, code -> nav.navigate(RoutesEvaluation.saisie(tentativeId, code)) }, onRetour = retour)
    }
    // ---------- ÉVALUATION (UC09, étape C8 ; théorie C9, conduite C10) ----------
    composable(RoutesEvaluation.SESSIONS) {
        val s = session() ?: return@composable
        val v: TentativesViewModel = viewModel()
        v.definirSession(s)
        EcranSessionsExaminateur(v, onOuvrir = { nav.navigate(RoutesSessions.tentatives(it)) }, onRetour = retour)
    }
    composable(RoutesEvaluation.THEORIE) { entree ->
        val s = session() ?: return@composable
        val id = entree.idArgument("tentativeId") ?: return@composable
        val v: EvaluationTheorieViewModel = viewModel()
        v.definirSession(s)
        EcranEvaluationTheorie(v, id, onTerminee = retour, onRetour = retour)
    }
    composable(RoutesEvaluation.CONDUITE) { entree ->
        val s = session() ?: return@composable
        val id = entree.idArgument("tentativeId") ?: return@composable
        val v: EvaluationConduiteViewModel = viewModel()
        v.definirSession(s)
        EcranEvaluationConduite(v, id, onTerminee = retour, onRetour = retour)
    }
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
    val retour: () -> Unit = { nav.revenir() }
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
        EcranEpreuve(
            v, id,
            // La catégorie vient de l'épreuve affichée : la lire ici dans le flux donnerait 0
            // tant que l'épreuve n'est pas chargée.
            onModifier = { categorieId -> nav.navigate(RoutesConfiguration.modifierEpreuve(id, categorieId)) },
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
    val retour: () -> Unit = { nav.revenir() }

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
    const val PIECE = "piece/{pieceId}"
    const val A_TRAITER = Routes.DOSSIERS
    const val COMPTE = "candidat/{candidatId}/compte"
    const val PARCOURS = Routes.PARCOURS
    const val MES_INSCRIPTIONS = Routes.MES_INSCRIPTIONS
    fun detail(id: Int) = "candidat/$id"
    fun modifier(id: Int) = "candidat/$id/modifier"
    fun dossier(id: Int) = "dossier/$id"
    fun piece(id: Int) = "piece/$id"
    fun compte(id: Int) = "candidat/$id/compte"
}

private fun NavGraphBuilder.graphCandidats(nav: NavHostController, session: () -> SessionUtilisateur?) {
    val retour: () -> Unit = { nav.revenir() }

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
        EcranDetailCandidat(vm, id, onModifier = { nav.navigate(RoutesCandidats.modifier(id)) }, onOuvrirDossier = { nav.navigate(RoutesCandidats.dossier(it)) }, onCreerCompte = { nav.navigate(RoutesCandidats.compte(id)) }, onRetour = retour)
    }
    composable(RoutesCandidats.COMPTE) { entree ->
        val vm = viewModelDuSousParcours<CandidatsViewModel>(nav, RoutesCandidats.LISTE, session()) ?: return@composable
        val id = entree.idArgument("candidatId") ?: return@composable
        EcranFormulaireCompteCandidat(vm, id, onCree = { nav.popBackStack() }, onRetour = retour)
    }
    // Le parcours du candidat connecté et les inscriptions de l'auto-école (UC12, étape C12) :
    // hors du sous-parcours candidats, avec leur propre ViewModel.
    composable(RoutesCandidats.PARCOURS) {
        val s = session() ?: return@composable
        val vm: CandidatsViewModel = viewModel()
        vm.definirSession(s)
        EcranParcours(vm, s.candidatId, onOuvrirDossier = { nav.navigate(RoutesCandidats.dossier(it)) }, onRetour = retour)
    }
    composable(RoutesCandidats.MES_INSCRIPTIONS) {
        val s = session() ?: return@composable
        val vm: MesInscriptionsViewModel = viewModel()
        vm.definirSession(s)
        EcranMesInscriptions(
            vm,
            onOuvrirCandidat = { nav.navigate(RoutesCandidats.LISTE); nav.navigate(RoutesCandidats.detail(it)) },
            onImprimerConvocation = { nav.navigate(RoutesImpression.convocation(it)) },
            onRetour = retour,
        )
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
        EcranDossier(vm, id, onOuvrirPiece = { nav.navigate(RoutesCandidats.piece(it)) }, onRetour = retour)
    }
    composable(RoutesCandidats.PIECE) { entree ->
        val s = session() ?: return@composable
        val vm: PieceJointeViewModel = viewModel()
        vm.definirSession(s)
        val id = entree.idArgument("pieceId") ?: return@composable
        EcranPieceJointe(vm, id, onRetour = retour)
    }
    composable(RoutesCandidats.A_TRAITER) {
        val s = session() ?: return@composable
        val vm: CandidatsViewModel = viewModel()
        vm.definirSession(s)
        EcranDossiersATraiter(vm, onOuvrir = { nav.navigate(RoutesCandidats.dossier(it)) }, onRetour = retour)
    }
}
