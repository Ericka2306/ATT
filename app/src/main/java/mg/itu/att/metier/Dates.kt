package mg.itu.att.metier

import java.util.Calendar

/**
 * Dates et heures en texte ISO, formatées à la main comme `heureCourante()` du cours
 * (pas de bibliothèque de dates : docs/HORS_COURS.md). Fonctions pures, sans Android.
 */

/** Date du jour "AAAA-MM-JJ". */
fun dateDuJour(): String {
    val c = Calendar.getInstance()
    return "%04d-%02d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}

/** Heure courante "HH:MM". */
fun heureCourante(): String {
    val c = Calendar.getInstance()
    return "%02d:%02d".format(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
}

/** Date et heure courantes "AAAA-MM-JJTHH:MM:SS" (format des lignes d'historique). */
fun maintenantIso(): String {
    val c = Calendar.getInstance()
    return "%04d-%02d-%02dT%02d:%02d:%02d".format(
        c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
        c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND),
    )
}

/** "2026-09-15" → "15/09/2026" ; "2026-09-15T08:30:00" → "15/09/2026 08:30" ; texte inattendu rendu tel quel. */
fun formatDate(iso: String): String {
    val date = iso.substringBefore('T')
    val heure = if ('T' in iso) " " + iso.substringAfter('T').take(5) else ""
    val parties = date.split('-')
    return if (parties.size == 3) "${parties[2]}/${parties[1]}/${parties[0]}$heure" else iso
}
