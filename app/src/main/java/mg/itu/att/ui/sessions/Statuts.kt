package mg.itu.att.ui.sessions

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import mg.itu.att.data.StatutSession
import mg.itu.att.ui.communs.PastilleStatut

fun StatutSession.libelle(): String = when (this) {
    StatutSession.PLANIFIEE -> "Planifiée"
    StatutSession.OUVERTE -> "Ouverte aux inscriptions"
    StatutSession.COMPLETE -> "Complète"
    StatutSession.EN_COURS -> "En cours"
    StatutSession.TERMINEE -> "Terminée"
    StatutSession.ANNULEE -> "Annulée"
}

@Composable
fun PastilleSession(statut: StatutSession) {
    val (fond, texte) = when (statut) {
        StatutSession.OUVERTE, StatutSession.EN_COURS -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        StatutSession.PLANIFIEE, StatutSession.COMPLETE -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        StatutSession.ANNULEE -> Color(0xFFF9DEDC) to MaterialTheme.colorScheme.error
        StatutSession.TERMINEE -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    PastilleStatut(statut.libelle(), fond, texte)
}
