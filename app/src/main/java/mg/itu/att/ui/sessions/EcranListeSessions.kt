package mg.itu.att.ui.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
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

/** Liste des sessions (UC06, UC12) : filtres à venir / du jour / passées / toutes par `FilterChip` (cours S7). */
@Composable
fun EcranListeSessions(viewModel: SessionsViewModel, onNouvelle: () -> Unit, onOuvrir: (Int) -> Unit, onRetour: () -> Unit) {
    val etat by viewModel.liste.collectAsState()

    EcranStandard(
        titre = "Sessions d'examen", onRetour = onRetour,
        iconeAction = if (etat.peutGerer) Icons.Filled.Add else null, descriptionAction = "Nouvelle session", onAction = if (etat.peutGerer) onNouvelle else null,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FiltreSessions.entries.forEach { f ->
                FilterChip(
                    selected = etat.filtre == f, onClick = { viewModel.filtrer(f) },
                    label = { Text(when (f) { FiltreSessions.A_VENIR -> "À venir"; FiltreSessions.DU_JOUR -> "Du jour"; FiltreSessions.PASSEES -> "Passées"; FiltreSessions.TOUTES -> "Toutes" }) },
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("${etat.sessions.size} session(s)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (etat.sessions.isEmpty()) Text(if (etat.peutGerer) "Aucune session. Utilisez le « + » pour en planifier une." else "Aucune session.", style = MaterialTheme.typography.bodyLarge)
        LazyColumn {
            items(etat.sessions) { l ->
                val s = l.session
                CarteIcone(
                    icone = Icons.Filled.DateRange,
                    titre = "${formatDate(s.date)} à ${s.heureConvocation} — permis ${l.codeCategorie}, ${l.libelleEpreuve}",
                    description = "${l.nomCentre} (${l.nomRegion}) · ${l.inscrits}/${s.capacite} inscrits",
                    onClick = { onOuvrir(s.id) },
                    complement = { PastilleSession(s.statut) },
                )
            }
        }
    }
}
