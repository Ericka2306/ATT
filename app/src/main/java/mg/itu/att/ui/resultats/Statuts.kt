package mg.itu.att.ui.resultats

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import mg.itu.att.data.Resultat
import mg.itu.att.data.StatutResultat
import mg.itu.att.ui.communs.PastilleStatut

/** Libellé affichable d'un statut de résultat. */
fun StatutResultat.libelle(): String = when (this) {
    StatutResultat.CALCULE -> "À valider"
    StatutResultat.VALIDE_ATT -> "Validé par l'ATT"
    StatutResultat.CORRIGE -> "Corrigé"
    StatutResultat.ANNULE -> "Remplacé"
}

/** Pastille du statut administratif du résultat (validation), à ne pas confondre avec la réussite. */
@Composable
fun PastilleResultat(statut: StatutResultat) {
    val (fond, texte) = when (statut) {
        StatutResultat.VALIDE_ATT -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        StatutResultat.CALCULE, StatutResultat.CORRIGE -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        StatutResultat.ANNULE -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    PastilleStatut(statut.libelle(), fond, texte)
}

/** Pastille « Réussi » / « Échec » : la décision du barème (seuil et faute éliminatoire). */
@Composable
fun PastilleReussite(reussi: Boolean) {
    if (reussi) {
        PastilleStatut("Réussi", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
    } else {
        PastilleStatut("Échec", Color(0xFFF9DEDC), MaterialTheme.colorScheme.error)
    }
}

/** « 16 / 20 (seuil 12) » — les nombres entiers s'affichent sans décimale inutile. */
fun Resultat.noteLisible(): String = "${formatNote(noteObtenue)} / ${formatNote(noteMax)} (seuil ${formatNote(seuil)})"

/** 16.0 → « 16 » ; 13.33 → « 13,33 ». */
fun formatNote(valeur: Double): String =
    // Virgule décimale, comme les points de la feuille d'examen (`formatPoints`) : « 26,34 », pas « 26.34 ».
    if (valeur % 1.0 == 0.0) valeur.toInt().toString() else valeur.toString().replace('.', ',')
