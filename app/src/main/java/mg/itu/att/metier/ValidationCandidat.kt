package mg.itu.att.metier

/**
 * Validation de la fiche candidat (UC04). Fonction pure, testée par JUnit.
 * La date de naissance est saisie en texte ISO "AAAA-MM-JJ" (pas de sélecteur de date : hors cours).
 */
object ValidationCandidat {

    private val FORMAT_DATE = Regex("""\d{4}-\d{2}-\d{2}""")

    fun validerFiche(nom: String, prenom: String, dateNaissance: String, autoEcoleId: Int?): String? = when {
        nom.isBlank() -> "Le nom est obligatoire."
        prenom.isBlank() -> "Le prénom est obligatoire."
        !dateValide(dateNaissance) -> "La date de naissance doit être au format AAAA-MM-JJ (ex. 2004-05-17)."
        autoEcoleId == null -> "Choisissez l'auto-école."
        else -> null
    }

    /** Vrai si le texte est une date ISO plausible (format, mois 1-12, jour 1-31). */
    fun dateValide(iso: String): Boolean {
        if (!FORMAT_DATE.matches(iso)) return false
        val (annee, mois, jour) = iso.split('-').map { it.toInt() }
        return annee in 1900..2100 && mois in 1..12 && jour in 1..31
    }
}
