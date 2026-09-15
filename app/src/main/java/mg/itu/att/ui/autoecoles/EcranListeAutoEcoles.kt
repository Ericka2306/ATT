package mg.itu.att.ui.autoecoles

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.ContenuColonne
import mg.itu.att.ui.communs.EcranAvecBarre
import mg.itu.att.ui.communs.SelecteurRegion

/**
 * Liste des auto-écoles (UC03), filtrable par région. Le « + » de la barre ouvre le formulaire.
 * Comme la liste de produits du cours : `LazyColumn` + cartes + lambdas vers la navigation.
 */
@Composable
fun EcranListeAutoEcoles(
    viewModel: AutoEcolesViewModel,
    onNouvelle: () -> Unit,
    onOuvrir: (Int) -> Unit,
    onRetour: () -> Unit,
) {
    val etat by viewModel.liste.collectAsState()

    EcranAvecBarre(
        titre = "Auto-écoles",
        onRetour = onRetour,
        iconeAction = Icons.Filled.Add,
        descriptionAction = "Nouvelle auto-école",
        onAction = onNouvelle,
    ) { marges ->
        ContenuColonne(marges) {
            SelecteurRegion(
                regions = etat.regions,
                regionId = etat.regionFiltreId,
                onChoix = viewModel::filtrerParRegion,
                avecToutes = true,
                verrouille = etat.regionVerrouillee,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${etat.autoEcoles.size} auto-école(s)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))

            if (etat.autoEcoles.isEmpty()) {
                Text(
                    "Aucune auto-école pour l'instant. Utilisez le « + » pour en créer une.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp),
                )
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
}
