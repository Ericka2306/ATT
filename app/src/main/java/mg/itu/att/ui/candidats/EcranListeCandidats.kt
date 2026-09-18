package mg.itu.att.ui.candidats

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.Option
import mg.itu.att.ui.communs.SelecteurChoix

/** Liste des candidats (UC04, UC12) : recherche par nom, filtre par auto-école (ATT), statut du dernier dossier. */
@Composable
fun EcranListeCandidats(
    viewModel: CandidatsViewModel,
    onNouveau: () -> Unit,
    onOuvrir: (Int) -> Unit,
    onRetour: () -> Unit,
) {
    val etat by viewModel.liste.collectAsState()

    EcranStandard(
        titre = if (etat.autoEcoleVerrouillee) "Mes candidats" else "Candidats",
        onRetour = onRetour,
        iconeAction = Icons.Filled.Add,
        descriptionAction = "Nouveau candidat",
        onAction = onNouveau,
    ) {
        val texteCherche by viewModel.texteRecherche.collectAsState()
        ChampTexte(texteCherche, viewModel::rechercher, "Rechercher un nom")
        if (!etat.autoEcoleVerrouillee) {
            SelecteurChoix("Auto-école", etat.autoEcoles.map { Option(it.id, it.nom) }, etat.autoEcoleFiltreId, viewModel::filtrerParAutoEcole, avecTous = true, libelleTous = "Toutes")
        }
        Text("${etat.candidats.size} candidat(s)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (etat.candidats.isEmpty()) {
            Text("Aucun candidat. Utilisez le « + » pour en créer un.", style = MaterialTheme.typography.bodyLarge)
        }
        LazyColumn {
            items(etat.candidats) { ligne ->
                val c = ligne.candidat
                CarteIcone(
                    icone = Icons.Filled.Person,
                    titre = "${c.nom} ${c.prenom}",
                    description = "${ligne.nomAutoEcole} · né(e) le ${formatDate(c.dateNaissance)}" +
                        (ligne.codeCategorie?.let { " · permis $it" } ?: ""),
                    onClick = { onOuvrir(c.id) },
                    complement = ligne.dernierDossier?.let { { PastilleDossier(it.statut) } },
                )
            }
        }
    }
}
