package mg.itu.att.metier

/**
 * Un nombre tel qu'il a été tapé : espaces autour, virgule ou point décimal.
 * Le clavier décimal d'Android produit une virgule en français : tous les champs numériques
 * du projet passent par ici pour l'accepter de la même façon.
 */
fun nombreSaisi(texte: String): Double? = texte.trim().replace(',', '.').toDoubleOrNull()

/**
 * Points saisis par l'examinateur (théorie et conduite) : texte tel que tapé, virgule acceptée.
 * @return la valeur si c'est un nombre entre 0 et [pointsMax], sinon null (vide, non numérique, hors bornes).
 */
fun pointsValides(pointsSaisis: String, pointsMax: Double): Double? =
    nombreSaisi(pointsSaisis)?.takeIf { it >= 0 && it <= pointsMax }
