package mg.itu.att.ui.communs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.att.data.Historique
import mg.itu.att.metier.formatDate

/** Une ligne d'historique telle qu'affichée sur toutes les fiches (UC14). */
@Composable
fun LigneHistorique(h: Historique) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text("${formatDate(h.dateHeure)} — ${h.action}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        val detail = listOfNotNull(h.nouvelleValeur ?: h.ancienneValeur, h.motif?.let { "motif : $it" }).joinToString(" · ")
        if (detail.isNotBlank()) {
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Section d'historique encore vide : une phrase plutôt qu'un titre suivi de rien. */
@Composable
fun HistoriqueVide() {
    Text("Aucune modification enregistrée pour l'instant.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
