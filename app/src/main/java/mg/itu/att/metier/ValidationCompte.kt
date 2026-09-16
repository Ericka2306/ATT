package mg.itu.att.metier

/**
 * Règles communes à tous les comptes de connexion (auto-écoles, examinateurs, administrateurs)
 * et au changement de mot de passe. Fonctions pures, testées par JUnit.
 */
object ValidationCompte {

    const val LONGUEUR_MIN_MOT_DE_PASSE = 8
    const val LONGUEUR_MIN_IDENTIFIANT = 4

    /** Valide un identifiant et un mot de passe initial à la création d'un compte. */
    fun validerCreation(identifiant: String, motDePasse: String, identifiantsExistants: List<String>): String? = when {
        identifiant.isBlank() -> "L'identifiant est obligatoire."
        identifiant.trim().length < LONGUEUR_MIN_IDENTIFIANT -> "L'identifiant doit avoir au moins $LONGUEUR_MIN_IDENTIFIANT caractères."
        identifiant.trim().any { it.isWhitespace() } -> "L'identifiant ne doit pas contenir d'espace."
        identifiantsExistants.any { it.equals(identifiant.trim(), ignoreCase = true) } -> "Cet identifiant est déjà utilisé."
        else -> validerMotDePasse(motDePasse)
    }

    /** Valide un nouveau mot de passe (création ou changement). */
    fun validerMotDePasse(motDePasse: String): String? = when {
        motDePasse.length < LONGUEUR_MIN_MOT_DE_PASSE -> "Le mot de passe doit avoir au moins $LONGUEUR_MIN_MOT_DE_PASSE caractères."
        motDePasse.isBlank() -> "Le mot de passe ne peut pas être vide."
        else -> null
    }

    /** Valide un changement de mot de passe : nouveau conforme, confirmation identique, différent de l'ancien. */
    fun validerChangement(ancien: String, nouveau: String, confirmation: String): String? = when {
        ancien.isBlank() -> "Saisissez votre mot de passe actuel."
        validerMotDePasse(nouveau) != null -> validerMotDePasse(nouveau)
        nouveau != confirmation -> "La confirmation ne correspond pas au nouveau mot de passe."
        nouveau == ancien -> "Le nouveau mot de passe doit être différent de l'ancien."
        else -> null
    }
}
