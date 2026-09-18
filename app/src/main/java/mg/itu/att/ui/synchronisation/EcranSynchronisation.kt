package mg.itu.att.ui.synchronisation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.EncartInfo
import mg.itu.att.ui.communs.EtatVide
import mg.itu.att.ui.communs.PastilleStatut
import mg.itu.att.ui.communs.TitreSection
import mg.itu.att.ui.communs.bordCarte
import mg.itu.att.ui.theme.BleuATTFonce

/**
 * Synchronisation avec le serveur central de l'ATT (cours S7, « la base d'abord, le réseau ensuite »).
 * En haut l'état du réseau (interrupteur de démonstration) et la file d'attente ; au milieu les résultats validés
 * avec leur marque « envoyé » ou « en attente » ; en bas ce que le serveur a reçu.
 */
@Composable
fun EcranSynchronisation(viewModel: SynchronisationViewModel, onRetour: () -> Unit) {
    val e by viewModel.etat.collectAsState()

    EcranStandard(titre = "Synchronisation", onRetour = onRetour) {
        LazyColumn {
            item {
                // ---- L'état du réseau et de la file d'attente ----
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (e.reseau) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(if (e.reseau) "Réseau disponible" else "Réseau coupé", style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (e.enAttente == 0) "Tout est synchronisé" else "${e.enAttente} résultat(s) en attente d'envoi",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            e.derniereSynchronisation?.let { Text("Dernière synchronisation : ${formatDate(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        Switch(checked = e.reseau, onCheckedChange = viewModel::basculerReseau, modifier = Modifier.semantics { contentDescription = "Interrupteur réseau" })
                    }
                }
                Spacer(Modifier.height(8.dp))
                EncartInfo("La base locale est la source de vérité : un résultat validé est acquis même sans réseau. Le serveur central de l'ATT est simulé ici ; l'interrupteur coupe le réseau pour la démonstration.")
                BoutonPrincipal(
                    if (e.syncEnCours) "Synchronisation…" else "Synchroniser maintenant",
                    { viewModel.synchroniser() },
                    actif = !e.syncEnCours && e.enAttente > 0,
                    icone = Icons.Filled.Refresh,
                )
                e.message?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp)) }

                // ---- La base locale ----
                TitreSection("Résultats validés (${e.resultats.size})")
                if (e.resultats.isEmpty()) EtatVide(Icons.Filled.Refresh, "Aucun résultat validé", "Les résultats validés par l'ATT apparaissent ici avec leur état d'envoi.")
            }
            items(e.resultats) { l ->
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = bordCarte(),
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(l.libelle, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (l.resultat.synchronisee) {
                            PastilleStatut("Envoyé", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                        } else {
                            PastilleStatut("En attente", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }
            }
            item {
                // ---- Le serveur ----
                TitreSection("Serveur central de l'ATT (ce qui est remonté)")
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(BleuATTFonce, MaterialTheme.shapes.medium)
                        .padding(14.dp),
                ) {
                    if (e.journalServeur.isEmpty()) {
                        Text("(vide)", color = MaterialTheme.colorScheme.outline, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    } else {
                        e.journalServeur.takeLast(6).forEach {
                            Text(it, color = MaterialTheme.colorScheme.secondaryContainer, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
