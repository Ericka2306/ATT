package mg.itu.att.metier

/**
 * Règles de planification des sessions (UC06, cadrage §6). Fonctions pures, testées par JUnit.
 * Les durées, marges et capacités par défaut viennent des règles configurables ; ici on ne fait que calculer.
 */
object ReglesPlanification {

    private val FORMAT_HEURE = Regex("""\d{2}:\d{2}""")

    /** Un créneau calculé, avant insertion en base. */
    data class CreneauCalcule(val ordre: Int, val heureDebut: String, val heureFinEstimee: String, val capacite: Int)

    fun heureValide(heure: String): Boolean {
        if (!FORMAT_HEURE.matches(heure)) return false
        val (h, m) = heure.split(':').map { it.toInt() }
        return h in 0..23 && m in 0..59
    }

    /** "08:30" + 45 min → "09:15" (borné à 23:59). */
    fun ajouterMinutes(heure: String, minutes: Int): String {
        val (h, m) = heure.split(':').map { it.toInt() }
        val total = (h * 60 + m + minutes).coerceIn(0, 23 * 60 + 59)
        return "%02d:%02d".format(total / 60, total % 60)
    }

    /**
     * Découpe la capacité de la session en créneaux successifs à partir de l'heure de convocation.
     * Chaque créneau accueille `capaciteCreneau` candidats pendant `dureeMin` minutes, puis une marge de `margeMin`.
     * Le dernier créneau reçoit le reste. Capacité 1 = créneaux individuels (à confirmer, Q5).
     */
    fun genererCreneaux(heureConvocation: String, capacite: Int, capaciteCreneau: Int, dureeMin: Int, margeMin: Int): List<CreneauCalcule> {
        if (capacite < 1 || capaciteCreneau < 1 || dureeMin < 1 || !heureValide(heureConvocation)) return emptyList()
        val nombre = (capacite + capaciteCreneau - 1) / capaciteCreneau
        return (0 until nombre).map { i ->
            val debut = ajouterMinutes(heureConvocation, i * (dureeMin + margeMin))
            val places = if (i == nombre - 1) capacite - i * capaciteCreneau else capaciteCreneau
            CreneauCalcule(ordre = i + 1, heureDebut = debut, heureFinEstimee = ajouterMinutes(debut, dureeMin), capacite = places)
        }
    }

    /** Valide la saisie d'une session ; `dateDuJour` permet de refuser une date passée. */
    fun validerSession(
        categorieId: Int?, typeEpreuveId: Int?, centreId: Int?, date: String, heureConvocation: String,
        capacite: String, capaciteCreneau: String, dureeMin: String, margeMin: String, dateDuJour: String,
    ): String? = when {
        categorieId == null -> "Choisissez la catégorie."
        typeEpreuveId == null -> "Choisissez l'épreuve."
        centreId == null -> "Choisissez le centre."
        !ValidationCandidat.dateValide(date) -> "La date doit être au format AAAA-MM-JJ."
        date < dateDuJour -> "La date est déjà passée."
        !heureValide(heureConvocation) -> "L'heure de convocation doit être au format HH:MM."
        (capacite.toIntOrNull() ?: 0) < 1 -> "La capacité doit être un entier ≥ 1."
        (capaciteCreneau.toIntOrNull() ?: 0) < 1 -> "La capacité d'un créneau doit être un entier ≥ 1."
        (dureeMin.toIntOrNull() ?: 0) < 1 -> "La durée d'un créneau doit être un entier ≥ 1."
        (margeMin.toIntOrNull() ?: -1) < 0 -> "La marge doit être un entier ≥ 0."
        else -> null
    }

    /** Message d'avertissement si d'autres sessions ont lieu au même centre le même jour (conflit possible, §11). */
    fun avertissementConflit(nombreSessionsMemeJour: Int): String? =
        if (nombreSessionsMemeJour > 0) "Attention : $nombreSessionsMemeJour autre(s) session(s) déjà prévue(s) dans ce centre ce jour-là." else null
}
