package mg.itu.att.ui.resultats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.EcranStandard

/**
 * Liste des résultats (UC10, UC12). L'ATT y trouve sa file « à valider » ; l'auto-école, le candidat
 * et l'examinateur n'y voient que ce qui les concerne, filtré par le ViewModel.
 */
@Composable
fun EcranResultats(viewModel: ResultatsViewModel, onOuvrir: (Int) -> Unit, onRetour: () -> Unit) {
    val etat by viewModel.liste.collectAsState()

    EcranStandard(titre = etat.titre, onRetour = onRetour) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FiltreResultats.entries.forEach { f ->
                // La file « à valider » n'a de sens que pour l'ATT.
                if (f == FiltreResultats.A_VALIDER && !etat.peutValider) return@forEach
                FilterChip(
                    selected = etat.filtre == f, onClick = { viewModel.filtrer(f) },
                    label = { Text(when (f) { FiltreResultats.A_VALIDER -> "À valider"; FiltreResultats.VALIDES -> "Validés"; FiltreResultats.TOUS -> "Tous" }) },
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("${etat.lignes.size} résultat(s) sur ${etat.total}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (etat.lignes.isEmpty()) {
            Text(
                if (etat.peutValider) "Aucun résultat. Ils sont calculés automatiquement à la fin de chaque épreuve." else "Aucun résultat validé pour le moment.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        LazyColumn {
            items(etat.lignes) { l ->
                val r = l.resultat
                CarteIcone(
                    icone = Icons.Filled.List,
                    titre = "${l.nomCandidat} — permis ${l.codeCategorie}, ${l.libelleEpreuve}",
                    description = "passage n° ${l.tentative?.numero ?: "?"} · ${r.noteLisible()} · ${formatDate(r.dateCalcul)}" +
                        (if (etat.peutValider) "\n${l.nomAutoEcole} · ${r.statut.libelle()}" else "\n${r.statut.libelle()}"),
                    onClick = { onOuvrir(r.id) },
                    complement = { PastilleReussite(r.reussi) },
                )
            }
        }
    }
}
