package mg.itu.att.ui.appel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mg.itu.att.data.StatutPresence
import mg.itu.att.ui.communs.LigneActions
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.EncartInfo
import mg.itu.att.ui.communs.PastilleStatut
import mg.itu.att.ui.communs.TitreSection

fun StatutPresence.libelle(): String = when (this) {
    StatutPresence.EN_ATTENTE -> "En attente"
    StatutPresence.PRESENT -> "Présent"
    StatutPresence.ABSENT -> "Absent"
    StatutPresence.EN_RETARD -> "En retard"
    StatutPresence.EN_COURS -> "En cours d'examen"
    StatutPresence.TERMINE -> "Terminé"
}

@Composable
fun PastillePresence(statut: StatutPresence) {
    val (fond, texte) = when (statut) {
        StatutPresence.PRESENT, StatutPresence.EN_COURS, StatutPresence.TERMINE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        StatutPresence.EN_RETARD -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        StatutPresence.ABSENT -> Color(0xFFF9DEDC) to MaterialTheme.colorScheme.error
        StatutPresence.EN_ATTENTE -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    PastilleStatut(statut.libelle(), fond, texte)
}

/** Liste d'appel d'une session (UC08), par créneau puis numéro d'appel ; boutons Présent / Absent, retard à accepter ou refuser. */
@Composable
fun EcranAppel(viewModel: AppelViewModel, sessionId: Int, onRetour: () -> Unit) {
    viewModel.afficher(sessionId)
    val e by viewModel.etat.collectAsState()

    EcranStandard(titre = "Appel", onRetour = onRetour) {
        if (e.session == null) { Text("Session introuvable."); return@EcranStandard }
        LazyColumn {
            item {
                Text(e.libelleSession, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Présents ${e.presents} · Retards ${e.retards} · Absents ${e.absents} · En attente ${e.enAttente}", style = MaterialTheme.typography.titleMedium)
                EncartInfo("Tolérance de retard : ${e.toleranceMin} min · absent = ${if (e.regleAbsence.equals("REPORT_AUTO", true)) "report automatique" else "nouvelle inscription"} (règles à confirmer)")
                e.message?.let { Spacer(Modifier.height(6.dp)); Text(it, color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium) }
                TitreSection("Candidats convoqués (${e.lignes.size})")
                if (e.lignes.isEmpty()) Text("Aucun candidat inscrit.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(e.lignes) { l ->
                val statut = l.presence?.statut ?: StatutPresence.EN_ATTENTE
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("n° ${l.inscription.numeroAnonymat}" + if (e.nomsVisibles) " — ${l.nomCandidat}" else "", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            PastillePresence(statut)
                        }
                        Text(
                            "créneau ${l.creneau?.heureDebut ?: "—"}" + (l.presence?.heureArrivee?.let { " · arrivé à $it" } ?: "") + (l.presence?.remarque?.let { " · $it" } ?: ""),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (e.peutMarquer) {
                            LigneActions {
                                when (statut) {
                                    StatutPresence.EN_ATTENTE -> {
                                        Button(onClick = { viewModel.marquerArrivee(l.inscription.id) }) { Text("Présent") }
                                        OutlinedButton(onClick = { viewModel.marquerAbsent(l.inscription.id) }) { Text("Absent") }
                                    }
                                    StatutPresence.EN_RETARD -> {
                                        Button(onClick = { viewModel.accepterRetard(l.inscription.id) }) { Text("Accepter le retard") }
                                        OutlinedButton(onClick = { viewModel.marquerAbsent(l.inscription.id) }) { Text("Refuser") }
                                    }
                                    StatutPresence.PRESENT, StatutPresence.ABSENT -> TextButton(onClick = { viewModel.reinitialiser(l.inscription.id) }) { Text("Corriger") }
                                    StatutPresence.EN_COURS, StatutPresence.TERMINE -> {}
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
