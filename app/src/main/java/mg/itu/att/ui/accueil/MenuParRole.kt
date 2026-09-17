package mg.itu.att.ui.accueil

import mg.itu.att.data.Role

/**
 * Une entrée du menu d'accueil : un libellé et la route vers laquelle elle mène
 * (docs/05_CAS_UTILISATION.md §5). Fonction pure, testée par JUnit.
 */
data class EntreeMenu(val libelle: String, val route: String, val description: String)

/** Routes des fonctionnalités, telles que listées dans docs/05 §4. */
object Routes {
    const val CONNEXION = "connexion"
    const val ACCUEIL = "accueil"
    const val CONFIGURATION = "config"
    const val AUTO_ECOLES = "autoecoles"
    const val EXAMINATEURS = "examinateurs"
    const val COMPTES = "comptes"
    const val CANDIDATS = "candidats"
    const val DOSSIERS = "dossiers"
    const val SESSIONS = "sessions"
    const val EVALUATION = "evaluation"
    const val RESULTATS = "resultats"
    const val HISTORIQUE = "historique"
    const val PARCOURS = "parcours"
    const val MES_INSCRIPTIONS = "mes-inscriptions"
    const val MOT_DE_PASSE = "mot-de-passe"

    /** Écran générique « à venir » : `avenir/{libelle}`. */
    const val A_VENIR = "avenir/{libelle}"
    fun aVenir(libelle: String) = "avenir/$libelle"
}

private val entreeMotDePasse = EntreeMenu("Mon mot de passe", Routes.MOT_DE_PASSE, "Changer mon mot de passe")

/** Le menu d'un rôle : uniquement ce que ce rôle a le droit de faire (instructions §7). */
fun menuPour(role: Role): List<EntreeMenu> = when (role) {
    Role.SUPER_ADMIN -> listOf(
        EntreeMenu("Configuration", Routes.CONFIGURATION, "Catégories, épreuves, barèmes, règles, centres"),
        EntreeMenu("Comptes", Routes.COMPTES, "Tous les comptes, administrateurs ATT"),
        EntreeMenu("Auto-écoles", Routes.AUTO_ECOLES, "Auto-écoles agréées et leurs comptes"),
        EntreeMenu("Examinateurs", Routes.EXAMINATEURS, "Examinateurs de l'ATT et leurs comptes"),
        EntreeMenu("Candidats", Routes.CANDIDATS, "Tous les candidats"),
        EntreeMenu("Dossiers à traiter", Routes.DOSSIERS, "Dossiers soumis par les auto-écoles"),
        EntreeMenu("Sessions", Routes.SESSIONS, "Sessions, créneaux, inscriptions, appel"),
        EntreeMenu("Résultats à valider", Routes.RESULTATS, "Résultats calculés en attente de validation"),
        EntreeMenu("Historique", Routes.HISTORIQUE, "Journal des modifications"),
    )
    Role.ADMIN_ATT -> listOf(
        EntreeMenu("Auto-écoles", Routes.AUTO_ECOLES, "Auto-écoles agréées et leurs comptes"),
        EntreeMenu("Examinateurs", Routes.EXAMINATEURS, "Examinateurs de l'ATT et leurs comptes"),
        EntreeMenu("Candidats", Routes.CANDIDATS, "Tous les candidats"),
        EntreeMenu("Dossiers à traiter", Routes.DOSSIERS, "Dossiers soumis par les auto-écoles"),
        EntreeMenu("Sessions", Routes.SESSIONS, "Créer, inscrire, faire l'appel, imprimer"),
        EntreeMenu("Résultats à valider", Routes.RESULTATS, "Résultats calculés en attente de validation"),
        EntreeMenu("Historique", Routes.HISTORIQUE, "Journal des modifications"),
    )
    Role.AUTO_ECOLE -> listOf(
        EntreeMenu("Mes candidats", Routes.CANDIDATS, "Créer un candidat, constituer et soumettre un dossier"),
        EntreeMenu("Mes inscriptions", Routes.MES_INSCRIPTIONS, "Convocations et présence de mes candidats"),
        EntreeMenu("Résultats", Routes.RESULTATS, "Résultats validés de mes candidats"),
    )
    Role.EXAMINATEUR -> listOf(
        EntreeMenu("Sessions du jour", Routes.EVALUATION, "Candidats présents à évaluer"),
        EntreeMenu("Mes évaluations", Routes.RESULTATS, "Évaluations que j'ai saisies"),
    )
    Role.CANDIDAT -> listOf(
        EntreeMenu("Mon parcours", Routes.PARCOURS, "Dossier, convocation, résultats"),
    )
} + entreeMotDePasse
