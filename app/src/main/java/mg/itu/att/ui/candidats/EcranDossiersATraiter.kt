package mg.itu.att.ui.candidats

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.EcranStandard

/** Les dossiers soumis en attente d'une décision de l'ATT (UC05), du plus ancien au plus récent. */
@Composable
fun EcranDossiersATraiter(viewModel: CandidatsViewModel, onOuvrir: (Int) -> Unit, onRetour: () -> Unit) {
    val lignes by viewModel.aTraiter.collectAsState()

    EcranStandard(titre = "Dossiers à traiter", onRetour = onRetour) {
        Text("${lignes.size} dossier(s) soumis", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (lignes.isEmpty()) {
            Text("Aucun dossier en attente.", style = MaterialTheme.typography.bodyLarge)
        }
        LazyColumn {
            items(lignes) { l ->
                CarteIcone(
                    icone = Icons.Filled.List,
                    titre = "${l.nomCandidat} — permis ${l.codeCategorie}",
                    description = "${l.nomAutoEcole} · soumis le ${l.dossier.dateSoumission?.let { formatDate(it) } ?: "?"}",
                    onClick = { onOuvrir(l.dossier.id) },
                    couleurPastille = MaterialTheme.colorScheme.tertiaryContainer,
                    couleurIcone = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}
