package mg.itu.att.ui.autoecoles

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.SelecteurRegion

/** Liste des auto-écoles (UC03), filtrable par région. Le « + » de la barre ouvre le formulaire. */
@Composable
fun EcranListeAutoEcoles(
    viewModel: AutoEcolesViewModel,
    onNouvelle: () -> Unit,
    onOuvrir: (Int) -> Unit,
    onRetour: () -> Unit,
) {
    val etat by viewModel.liste.collectAsState()

    EcranStandard(
        titre = "Auto-écoles",
        onRetour = onRetour,
        iconeAction = Icons.Filled.Add,
        descriptionAction = "Nouvelle auto-école",
        onAction = onNouvelle,
    ) {
        SelecteurRegion(etat.regions, etat.regionFiltreId, viewModel::filtrerParRegion, avecToutes = true, verrouille = etat.regionVerrouillee)
        Text("${etat.autoEcoles.size} auto-école(s)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (etat.autoEcoles.isEmpty()) {
            Text("Aucune auto-école pour l'instant. Utilisez le « + » pour en créer une.", style = MaterialTheme.typography.bodyLarge)
        }
        LazyColumn {
            items(etat.autoEcoles) { ligne ->
                val a = ligne.autoEcole
                CarteIcone(
                    icone = Icons.Filled.Place,
                    titre = a.nom + if (a.actif) "" else " (inactive)",
                    description = "${ligne.nomRegion} · ${a.adresse}",
                    onClick = { onOuvrir(a.id) },
                    couleurPastille = if (a.actif) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    couleurIcone = if (a.actif) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
