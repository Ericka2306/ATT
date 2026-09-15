package mg.itu.att.metier

/**
 * Règles de validation des auto-écoles et de leurs comptes (UC03).
 * Fonctions pures : elles reçoivent ce qu'il faut et rendent un message d'erreur, ou null si tout va bien.
 * Testées par JUnit sans Android (docs/HORS_COURS.md n° 9).
 */
object ValidationAutoEcole {

    const val LONGUEUR_MIN_MOT_DE_PASSE = 8
    const val LONGUEUR_MIN_IDENTIFIANT = 4

    /**
     * Valide la fiche d'une auto-école.
     * @param nomsExistantsDansRegion noms des autres auto-écoles de la même région (sans celle en cours de modification).
     */
    fun validerFiche(nom: String, regionId: Int?, adresse: String, nomsExistantsDansRegion: List<String>): String? = when {
        nom.isBlank() -> "Le nom est obligatoire."
        regionId == null -> "Choisissez la région."
        adresse.isBlank() -> "L'adresse est obligatoire."
        nomsExistantsDansRegion.any { it.equals(nom.trim(), ignoreCase = true) } ->
            "Une auto-école porte déjà ce nom dans cette région."
        else -> null
    }

    /** Valide la création d'un compte de connexion. */
    fun validerCompte(identifiant: String, motDePasse: String, identifiantsExistants: List<String>): String? = when {
        identifiant.isBlank() -> "L'identifiant est obligatoire."
        identifiant.trim().length < LONGUEUR_MIN_IDENTIFIANT -> "L'identifiant doit avoir au moins $LONGUEUR_MIN_IDENTIFIANT caractères."
        identifiant.trim().any { it.isWhitespace() } -> "L'identifiant ne doit pas contenir d'espace."
        identifiantsExistants.any { it.equals(identifiant.trim(), ignoreCase = true) } -> "Cet identifiant est déjà utilisé."
        motDePasse.length < LONGUEUR_MIN_MOT_DE_PASSE -> "Le mot de passe doit avoir au moins $LONGUEUR_MIN_MOT_DE_PASSE caractères."
        else -> null
    }
}
