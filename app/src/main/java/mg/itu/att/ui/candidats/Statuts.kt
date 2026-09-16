package mg.itu.att.ui.candidats

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import mg.itu.att.data.StatutDossier
import mg.itu.att.ui.communs.PastilleStatut

/** Libellé affichable d'un statut de dossier. */
fun StatutDossier.libelle(): String = when (this) {
    StatutDossier.BROUILLON -> "Brouillon"
    StatutDossier.SOUMIS -> "Soumis à l'ATT"
    StatutDossier.INCOMPLET -> "Incomplet"
    StatutDossier.VALIDE -> "Validé"
    StatutDossier.REFUSE -> "Refusé"
}

/** Pastille colorée d'un statut de dossier : vert validé, ambre en attente, rouge refusé. */
@Composable
fun PastilleDossier(statut: StatutDossier) {
    val (fond, texte) = when (statut) {
        StatutDossier.VALIDE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        StatutDossier.SOUMIS, StatutDossier.INCOMPLET -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        StatutDossier.REFUSE -> Color(0xFFF9DEDC) to MaterialTheme.colorScheme.error
        StatutDossier.BROUILLON -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    PastilleStatut(statut.libelle(), fond, texte)
}
