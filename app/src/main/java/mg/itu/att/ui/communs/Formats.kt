package mg.itu.att.ui.communs

import mg.itu.att.data.Role

/** Libellés affichables. Les dates sont dans `metier/Dates.kt`. */

/** Libellé affichable d'un rôle. */
fun Role.libelle(): String = when (this) {
    Role.SUPER_ADMIN -> "Super administrateur"
    Role.ADMIN_ATT -> "Administrateur ATT"
    Role.AUTO_ECOLE -> "Auto-école"
    Role.EXAMINATEUR -> "Examinateur"
    Role.CANDIDAT -> "Candidat"
}

/** Un texte facultatif affiché avec un tiret quand il est vide (le null va jusqu'à l'UI, cours S5). */
fun String?.ouTiret(): String = if (this.isNullOrBlank()) "—" else this

/** "12.0" → "12", "12.5" → "12,5" : les points et notes s'affichent sans décimale inutile. */
fun formatPoints(points: Double): String = if (points == points.toLong().toDouble()) points.toLong().toString() else points.toString().replace('.', ',')
