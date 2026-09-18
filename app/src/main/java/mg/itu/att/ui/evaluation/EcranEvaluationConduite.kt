package mg.itu.att.ui.evaluation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.CarteFiche
import mg.itu.att.ui.communs.CaseACocher
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.EncartInfo
import mg.itu.att.ui.communs.LigneInfo
import mg.itu.att.ui.communs.TexteErreur
import mg.itu.att.ui.communs.TitreSection

/**
 * Épreuve de conduite d'un passage (UC09, étape C10) : la grille à l'écran, un critère par carte (points attribués,
 * faute éliminatoire si le critère l'admet, observation), observations générales, « Terminer l'épreuve ».
 * La grille elle-même vient de la configuration et reste « à confirmer » (Q2) ; le calcul est en C11.
 */
@Composable
fun EcranEvaluationConduite(viewModel: EvaluationConduiteViewModel, tentativeId: Int, onTerminee: () -> Unit, onRetour: () -> Unit) {
    viewModel.afficher(tentativeId)
    val e by viewModel.etat.collectAsState()
    val saisies by viewModel.saisies.collectAsState()
    val observations by viewModel.observationsSaisies.collectAsState()

    EcranStandard(titre = "Épreuve de conduite", onRetour = onRetour) {
        val tentative = e.tentative
        if (tentative == null) { Text("Passage introuvable."); return@EcranStandard }
        LazyColumn {
            item {
                CarteFiche {
                    Text(e.entete, style = MaterialTheme.typography.titleMedium)
                    LigneInfo("Grille", "${e.criteres.size} critère(s), ${formatPoints(e.pointsGrille)} pt")
                    LigneInfo("Total", "${formatPoints(e.totalAttribue)} / ${formatPoints(e.noteMax)}")
                    if (e.fauteEliminatoire) Text("Faute éliminatoire : épreuve perdue quel que soit le total.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    LigneInfo("Ouvert à", tentative.dateHeure.substringAfter('T'))
                    if (!e.enCours) LigneInfo("Statut", "passage ${tentative.statut.libelle()}")
                }
                TexteErreur(e.erreur)
                TitreSection("Critères (${e.criteres.size})")
                if (e.criteres.isEmpty() && e.erreur == null) Text("Aucun critère dans la grille.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            itemsIndexed(e.criteres) { index, c ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(12.dp)) {
                        val saisie = saisies[c.critere.id] ?: c.saisie
                        Text("${index + 1}. ${c.critere.libelle}", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${formatPoints(c.critere.points)} pt" + (if (c.critere.eliminatoire) " · faute éliminatoire possible" else "") + if (c.critere.aConfirmer) " · à confirmer" else "",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        if (c.critere.eliminatoire) {
                            CaseACocher(saisie.faute, { v -> viewModel.saisir(c.critere.id) { it.copy(faute = v) } }, "Faute éliminatoire commise", actif = e.enCours)
                        }
                        if (!saisie.faute) {
                            ChampTexte(saisie.points, { v -> viewModel.saisir(c.critere.id) { it.copy(points = v) } }, "Points (0 à ${formatPoints(c.critere.points)}) *", clavier = KeyboardType.Decimal, actif = e.enCours)
                        }
                        ChampTexte(saisie.observation, { v -> viewModel.saisir(c.critere.id) { it.copy(observation = v) } }, "Observation", uneLigne = false, actif = e.enCours)
                    }
                }
            }
            item {
                TitreSection("Observations générales")
                ChampTexte(observations, viewModel::changerObservations, "Observations de l'examinateur", uneLigne = false, actif = e.enCours)
                if (e.enCours) {
                    if (e.sansNote > 0) {
                        Text("${e.sansNote} critère(s) sans points : à noter avant de terminer.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                    }
                    BoutonPrincipal("Terminer l'épreuve", { viewModel.terminer(onTerminee) })
                    Spacer(Modifier.height(8.dp))
                    EncartInfo("Une fois terminée, l'épreuve ne se modifie plus : le résultat sera calculé puis validé par l'ATT.")
                } else {
                    Text("Épreuve close le ${formatDate(tentative.dateHeure)} : lecture seule.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
