package mg.itu.att.metier

import mg.itu.att.data.Bareme
import mg.itu.att.data.CriterePratique

/**
 * Une ligne de la grille de conduite à contrôler : le critère (points max, éliminatoire ou non),
 * les points saisis (texte) et la faute éliminatoire cochée.
 */
data class NoteCritere(val pointsMax: Double, val pointsSaisis: String, val eliminatoire: Boolean, val fauteCochee: Boolean) {
    /** Une faute éliminatoire vaut 0 point sur le critère, quoi qu'ait tapé l'examinateur. */
    val points: Double? get() = if (fauteCochee) 0.0 else pointsValides(pointsSaisis, pointsMax)
}

/**
 * Règles de l'épreuve de conduite (UC09, étape C10) : structure seulement, la grille (critères, points, fautes)
 * vient de la configuration et reste « à confirmer » (Q2). Fonctions pures, testées par JUnit.
 */
object ReglesConduite {

    /** Somme des points maximums des critères de la grille. */
    fun pointsGrille(criteres: List<CriterePratique>): Double = criteres.sumOf { it.points }

    /**
     * Peut-on ouvrir l'épreuve de conduite ?
     * @return un message d'erreur qui renvoie à la configuration, ou null si tout est prêt.
     */
    fun verifierOuverture(bareme: Bareme?, criteresActifs: List<CriterePratique>): String? = when {
        bareme == null -> "Aucun barème courant pour cette épreuve : à créer dans Configuration → Catégories et épreuves."
        criteresActifs.none { it.actif } -> "Aucun critère de conduite actif : la grille est à saisir dans Configuration → Catégories et épreuves (Q2)."
        else -> null
    }

    /** Total des points attribués sur les critères correctement notés. */
    fun totalAttribue(notes: List<NoteCritere>): Double = notes.sumOf { it.points ?: 0.0 }

    /** Au moins une faute éliminatoire cochée : l'épreuve est perdue quel que soit le total (docs/03 §invariants). */
    fun fauteEliminatoire(notes: List<NoteCritere>): Boolean = notes.any { it.eliminatoire && it.fauteCochee }

    /** Nombre de critères sans points valides (une faute éliminatoire cochée compte comme notée à 0). */
    fun sansNote(notes: List<NoteCritere>): Int = notes.count { it.points == null }

    /**
     * Contrôle avant « Terminer l'épreuve » : au moins un critère, et des points valides sur chacun.
     * @return un message d'erreur, ou null si l'épreuve peut être close.
     */
    fun verifierFin(notes: List<NoteCritere>): String? = when {
        notes.isEmpty() -> "Aucun critère dans la grille."
        sansNote(notes) > 0 -> "${sansNote(notes)} critère(s) sans points : saisissez un nombre entre 0 et le maximum du critère."
        else -> null
    }
}
