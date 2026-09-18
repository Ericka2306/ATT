package mg.itu.att.ui.candidats

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.TexteErreur

/** Le fichier joint à une pièce du dossier : la photo, ou les pages du PDF l'une sous l'autre (D2b). */
@Composable
fun EcranPieceJointe(viewModel: PieceJointeViewModel, pieceId: Int, onRetour: () -> Unit) {
    viewModel.afficher(pieceId)
    val e by viewModel.uiState.collectAsState()

    EcranStandard(titre = e.titre, onRetour = onRetour) {
        when {
            e.erreur != null -> TexteErreur(e.erreur)
            e.chargement -> Text("Ouverture du fichier…", style = MaterialTheme.typography.bodyMedium)
            else -> LazyColumn {
                item {
                    Text(
                        e.candidat + " · " + if (e.estPdf) "PDF, ${e.pages.size} page(s)" else "photo",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                itemsIndexed(e.pages) { numero, page ->
                    Image(
                        bitmap = page,
                        contentDescription = if (e.estPdf) "Page ${numero + 1}" else e.titre,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    )
                }
            }
        }
    }
}
