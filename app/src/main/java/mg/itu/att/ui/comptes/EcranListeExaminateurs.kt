package mg.itu.att.ui.comptes

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.EcranStandard

/** Liste des examinateurs de l'ATT (Admin ATT, filtrée par région pour un admin régional). */
@Composable
fun EcranListeExaminateurs(viewModel: ComptesViewModel, onNouveau: () -> Unit, onOuvrir: (Int) -> Unit, onRetour: () -> Unit) {
    val etat by viewModel.examinateurs.collectAsState()

    EcranStandard(titre = "Examinateurs", onRetour = onRetour, iconeAction = Icons.Filled.Add, descriptionAction = "Nouvel examinateur", onAction = onNouveau) {
        Text("${etat.examinateurs.size} examinateur(s)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (etat.examinateurs.isEmpty()) {
            Text("Aucun examinateur. Utilisez le « + » pour en créer un avec son compte.", style = MaterialTheme.typography.bodyLarge)
        }
        LazyColumn {
            items(etat.examinateurs) { ligne ->
                val e = ligne.examinateur
                CarteIcone(
                    icone = Icons.Filled.Face,
                    titre = e.nom + if (e.actif) "" else " (inactif)",
                    description = "${ligne.nomRegion} · compte ${ligne.identifiant ?: "—"}" + (e.matricule?.let { " · matricule $it" } ?: ""),
                    onClick = { onOuvrir(e.id) },
                    couleurPastille = if (e.actif) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    couleurIcone = if (e.actif) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
