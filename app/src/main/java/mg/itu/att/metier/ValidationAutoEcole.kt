package mg.itu.att.metier

/**
 * Règles de validation des auto-écoles (UC03). Fonctions pures, testées par JUnit.
 * Les règles de compte sont dans [ValidationCompte], communes à tous les rôles.
 */
object ValidationAutoEcole {

    const val LONGUEUR_MIN_MOT_DE_PASSE = ValidationCompte.LONGUEUR_MIN_MOT_DE_PASSE
    const val LONGUEUR_MIN_IDENTIFIANT = ValidationCompte.LONGUEUR_MIN_IDENTIFIANT

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

    /** Valide la création d'un compte de connexion (délégué aux règles communes). */
    fun validerCompte(identifiant: String, motDePasse: String, identifiantsExistants: List<String>): String? =
        ValidationCompte.validerCreation(identifiant, motDePasse, identifiantsExistants)
}
