package mg.itu.att.ui.inscriptions

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mg.itu.att.data.StatutInscription
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.Option
import mg.itu.att.ui.communs.PastilleStatut
import mg.itu.att.ui.communs.SelecteurChoix
import mg.itu.att.ui.communs.TexteErreur
import mg.itu.att.ui.communs.TitreSection

fun StatutInscription.libelle(): String = when (this) {
    StatutInscription.DEMANDE -> "Demande"
    StatutInscription.INSCRIT -> "Inscrit"
    StatutInscription.CONFIRME -> "Confirmé"
    StatutInscription.REPORTE -> "Reporté"
    StatutInscription.ANNULE -> "Annulé"
}

@Composable
fun PastilleInscription(statut: StatutInscription) {
    val (fond, texte) = when (statut) {
        StatutInscription.INSCRIT, StatutInscription.CONFIRME -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        StatutInscription.DEMANDE -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        StatutInscription.REPORTE -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        StatutInscription.ANNULE -> Color(0xFFF9DEDC) to MaterialTheme.colorScheme.error
    }
    PastilleStatut(statut.libelle(), fond, texte)
}

/**
 * Inscriptions d'une session (UC07) : les inscrits (numéro d'appel, créneau, statut, actions ATT),
 * puis la recherche d'un candidat éligible à inscrire. L'auto-école ne voit que ses candidats.
 */
@Composable
fun EcranInscriptions(viewModel: InscriptionsViewModel, sessionId: Int, onImprimerConvocation: (Int) -> Unit, onRetour: () -> Unit) {
    viewModel.afficher(sessionId)
    val e by viewModel.etat.collectAsState()

    EcranStandard(titre = "Inscriptions", onRetour = onRetour) {
        val se = e.session
        if (se == null) { Text("Session introuvable."); return@EcranStandard }
        LazyColumn {
            item {
                Text(e.libelleSession, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${e.placesRestantes} place(s) restante(s) sur ${se.capacite}", style = MaterialTheme.typography.titleMedium)
                TitreSection("Inscrits (${e.inscrits.size})")
                if (e.inscrits.isEmpty()) Text("Aucun inscrit.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(e.inscrits) { l ->
                val i = l.inscription
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("n° ${i.numeroAnonymat} — ${l.nomCandidat}", style = MaterialTheme.typography.titleMedium)
                            PastilleInscription(i.statut)
                        }
                        Text("${l.nomAutoEcole} · créneau ${l.creneau?.heureDebut ?: "—"}" + (i.motif?.let { " · $it" } ?: ""), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (e.estAtt && (i.statut == StatutInscription.DEMANDE || i.statut == StatutInscription.INSCRIT || i.statut == StatutInscription.CONFIRME)) {
                            Row {
                                if (i.statut == StatutInscription.DEMANDE) TextButton(onClick = { viewModel.confirmer(i.id) }) { Text("Confirmer") }
                                TextButton(onClick = { viewModel.reporter(i.id) }) { Text("Reporter") }
                                TextButton(onClick = { viewModel.annuler(i.id) }) { Text("Annuler") }
                                TextButton(onClick = { onImprimerConvocation(i.id) }) { Text("Convocation") }
                            }
                        }
                    }
                }
            }
            if (e.estAtt && e.inscrits.any { it.inscription.statut != StatutInscription.ANNULE && it.inscription.statut != StatutInscription.REPORTE }) {
                item { ChampTexte(e.motif, viewModel::changerMotif, "Motif (obligatoire pour reporter ou annuler)") }
            }
            if (e.peutInscrire || e.peutDemander) {
                item {
                    TitreSection(if (e.peutInscrire) "Inscrire un candidat" else "Demander une inscription")
                    Text("Seuls les candidats ayant un dossier validé pour cette catégorie sont proposés.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    ChampTexte(e.recherche, viewModel::rechercher, "Rechercher un candidat")
                    SelecteurChoix("Créneau", e.creneaux.map { Option(it.id, "${it.heureDebut} – ${it.heureFinEstimee} (${it.capacite} pl.)") }, e.creneauChoisiId, viewModel::choisirCreneau, avecTous = true, libelleTous = "premier disponible")
                    e.message?.let { Text(it, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium) }
                    TexteErreur(e.erreur)
                    if (e.candidatsEligibles.isEmpty()) Text("Aucun candidat éligible (ou déjà inscrit).", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                }
                items(e.candidatsEligibles) { c ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("${c.candidat.nom} ${c.candidat.prenom}", style = MaterialTheme.typography.bodyLarge)
                            Text(c.nomAutoEcole, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(onClick = { viewModel.inscrire(c.candidat.id) }) { Text(if (e.peutInscrire) "Inscrire" else "Demander") }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
