package mg.itu.att.ui.evaluation

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.att.data.StatutPresence
import mg.itu.att.data.StatutTentative
import mg.itu.att.ui.appel.PastillePresence
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.TexteErreur
import mg.itu.att.ui.communs.TitreSection

fun StatutTentative.libelle(): String = when (this) {
    StatutTentative.EN_COURS -> "en cours"
    StatutTentative.TERMINEE -> "terminée"
    StatutTentative.VALIDEE -> "validée"
    StatutTentative.ANNULEE -> "annulée"
}

/** Candidats d'une session à évaluer (UC09) : ouvrir ou reprendre une tentative pour un candidat présent. */
@Composable
fun EcranTentatives(viewModel: TentativesViewModel, sessionId: Int, onTentative: (Int, String) -> Unit, onRetour: () -> Unit) {
    viewModel.afficher(sessionId)
    val e by viewModel.etat.collectAsState()

    EcranStandard(titre = "Passages", onRetour = onRetour) {
        if (e.session == null) { Text("Session introuvable."); return@EcranStandard }
        LazyColumn {
            item {
                Text(e.libelleSession, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TexteErreur(e.erreur)
                TitreSection("Candidats (${e.lignes.size})")
                if (e.lignes.isEmpty()) Text("Aucun inscrit actif.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(e.lignes) { l ->
                val statut = l.presence?.statut ?: StatutPresence.EN_ATTENTE
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("n° ${l.inscription.numeroAnonymat}" + if (e.nomsVisibles) " — ${l.nomCandidat}" else "", style = MaterialTheme.typography.titleMedium)
                            PastillePresence(statut)
                        }
                        Text(
                            l.tentative?.let { "passage n° ${it.numero} ${it.statut.libelle()} (${it.dateHeure.replace('T', ' ')})" } ?: "prochain passage : n° ${l.prochainNumero}",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        val tentative = l.tentative
                        Spacer(Modifier.height(6.dp))
                        when {
                            // Passage clos : consultation en lecture seule (l'écran d'épreuve se verrouille tout seul).
                            tentative != null && tentative.statut != StatutTentative.EN_COURS ->
                                OutlinedButton(onClick = { onTentative(tentative.id, e.codeEpreuve) }) { Text("Consulter l'épreuve") }
                            !e.peutOuvrir -> {}
                            tentative != null ->
                                Button(onClick = { viewModel.ouvrirTentative(l.inscription.id) { id -> onTentative(id, e.codeEpreuve) } }) { Text("Continuer l'évaluation") }
                            l.refus == null ->
                                Button(onClick = { viewModel.ouvrirTentative(l.inscription.id) { id -> onTentative(id, e.codeEpreuve) } }) { Text("Ouvrir le passage n° ${l.prochainNumero}") }
                            else -> Text(l.refus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
