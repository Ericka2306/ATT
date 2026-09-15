package mg.itu.att.ui.communs

import java.util.Calendar
import mg.itu.att.data.Role

/**
 * Fonctions de formatage, à la main comme `heureCourante()` et `formatAriary()` du cours
 * (pas de bibliothèque de dates : docs/HORS_COURS.md, dates en texte ISO).
 */

/** Date du jour au format ISO "AAAA-MM-JJ". */
fun dateDuJour(): String {
    val c = Calendar.getInstance()
    return "%04d-%02d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}

/** Heure courante "HH:MM". */
fun heureCourante(): String {
    val c = Calendar.getInstance()
    return "%02d:%02d".format(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
}

/** Date et heure courantes "AAAA-MM-JJTHH:MM:SS" (format de l'historique). */
fun maintenantIso(): String {
    val c = Calendar.getInstance()
    return "%04d-%02d-%02dT%02d:%02d:%02d".format(
        c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
        c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND),
    )
}

/** "2026-09-15" → "15/09/2026" pour l'affichage ; une date mal formée est rendue telle quelle. */
fun formatDate(iso: String): String {
    val parties = iso.split('-')
    return if (parties.size == 3) "${parties[2]}/${parties[1]}/${parties[0]}" else iso
}

/** Libellé affichable d'un rôle. */
fun Role.libelle(): String = when (this) {
    Role.SUPER_ADMIN -> "Super administrateur"
    Role.ADMIN_ATT -> "Administrateur ATT"
    Role.AUTO_ECOLE -> "Auto-école"
    Role.EXAMINATEUR -> "Examinateur"
    Role.CANDIDAT -> "Candidat"
}
