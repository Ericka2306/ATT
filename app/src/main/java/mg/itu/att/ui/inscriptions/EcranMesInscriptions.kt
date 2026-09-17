package mg.itu.att.ui.inscriptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.att.ui.appel.PastillePresence
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.EcranStandard

/**
 * « Mes inscriptions » (UC12) : les convocations des candidats de l'auto-école, à venir / passées / toutes
 * (`FilterChip`, cours S7). Un clic ouvre la fiche du candidat, où se trouve tout son parcours.
 */
@Composable
fun EcranMesInscriptions(
    viewModel: MesInscriptionsViewModel,
    onOuvrirCandidat: (Int) -> Unit,
    onImprimerConvocation: (Int) -> Unit,
    onRetour: () -> Unit,
) {
    val etat by viewModel.uiState.collectAsState()

    EcranStandard(titre = "Mes inscriptions", onRetour = onRetour) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FiltreMesInscriptions.entries.forEach { f ->
                FilterChip(
                    selected = etat.filtre == f, onClick = { viewModel.filtrer(f) },
                    label = { Text(when (f) { FiltreMesInscriptions.A_VENIR -> "À venir"; FiltreMesInscriptions.PASSEES -> "Passées"; FiltreMesInscriptions.TOUTES -> "Toutes" }) },
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("${etat.lignes.size} inscription(s) sur ${etat.total}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (etat.lignes.isEmpty()) Text("Aucune inscription. L'ATT inscrit vos candidats dont le dossier est validé.", style = MaterialTheme.typography.bodyLarge)
        LazyColumn {
            items(etat.lignes) { l ->
                val i = l.inscription
                CarteIcone(
                    icone = Icons.Filled.DateRange,
                    titre = "${l.nomCandidat} — n° ${i.numeroAnonymat}",
                    description = l.libelleSession + "\n" + listOfNotNull(
                        l.nomCentre,
                        l.heureCreneau?.let { "créneau $it" },
                        i.heurePassageEstimee?.let { "passage estimé $it" },
                        i.motif?.let { "motif : $it" },
                    ).joinToString(" · "),
                    onClick = { onOuvrirCandidat(l.candidatId) },
                    complement = {
                        val p = l.presence
                        if (p != null && !l.aVenir) PastillePresence(p.statut) else PastilleInscription(i.statut)
                    },
                )
                // La convocation s'imprime tant que la session est à venir (cadrage §10).
                if (l.aVenir) {
                    TextButton(onClick = { onImprimerConvocation(i.id) }, modifier = Modifier.padding(start = 8.dp)) { Text("Imprimer la convocation") }
                }
            }
        }
    }
}
