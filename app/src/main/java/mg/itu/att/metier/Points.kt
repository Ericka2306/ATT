package mg.itu.att.metier

/**
 * Points saisis par l'examinateur (théorie et conduite) : texte tel que tapé, virgule acceptée.
 * @return la valeur si c'est un nombre entre 0 et [pointsMax], sinon null (vide, non numérique, hors bornes).
 */
fun pointsValides(pointsSaisis: String, pointsMax: Double): Double? =
    pointsSaisis.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 && it <= pointsMax }
