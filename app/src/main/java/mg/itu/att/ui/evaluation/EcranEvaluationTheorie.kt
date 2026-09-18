package mg.itu.att.ui.evaluation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import mg.itu.att.data.StatutTentative
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.CarteFiche
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.LigneInfo
import mg.itu.att.ui.communs.Option
import mg.itu.att.ui.communs.SelecteurChoix
import mg.itu.att.ui.communs.TexteErreur
import mg.itu.att.ui.communs.TitreSection

/** "12.0" → "12", "12.5" → "12,5" : les points s'affichent sans décimale inutile. */
fun formatPoints(points: Double): String = if (points == points.toLong().toDouble()) points.toLong().toString() else points.toString().replace('.', ',')

/**
 * Épreuve théorique orale d'un passage (UC09, étape C9) : la feuille d'examen à l'écran. Sujet tiré au sort (TIRAGE)
 * ou construit question par question (DIRECT) selon la règle `MODE_THEORIE` ; pour chaque question, l'examinateur
 * écrit la réponse du candidat et les points attribués. « Terminer l'épreuve » fige tout ; le calcul est en C11.
 */
@Composable
fun EcranEvaluationTheorie(viewModel: EvaluationTheorieViewModel, tentativeId: Int, onTerminee: () -> Unit, onRetour: () -> Unit) {
    viewModel.afficher(tentativeId)
    val e by viewModel.etat.collectAsState()
    val saisies by viewModel.saisies.collectAsState()
    val observations by viewModel.observationsSaisies.collectAsState()

    EcranStandard(titre = "Épreuve théorique", onRetour = onRetour) {
        val tentative = e.tentative
        if (tentative == null) { Text("Passage introuvable."); return@EcranStandard }
        LazyColumn {
            item {
                CarteFiche {
                    Text(e.entete, style = MaterialTheme.typography.titleMedium)
                    LigneInfo("Sujet", if (e.modeDirect) "choisi par l'examinateur" else "tiré au sort par l'application")
                    LigneInfo("Points posés", "${formatPoints(e.pointsPoses)} / ${formatPoints(e.noteMax)}")
                    LigneInfo("Total", "${formatPoints(e.totalAttribue)} / ${formatPoints(e.noteMax)}")
                    if (e.dureeMinutes > 0) LigneInfo("Durée", "${e.dureeMinutes} min, ouvert à ${tentative.dateHeure.substringAfter('T')}")
                    if (!e.enCours) LigneInfo("Statut", "passage ${tentative.statut.libelle()}")
                }
                TexteErreur(e.erreur)
            }

            if (e.enCours && e.modeDirect) {
                item { Proposition(viewModel, e) }
            }
            item { TitreSection("Questions posées (${e.questions.size})") }
            if (e.questions.isEmpty()) {
                item { Text(if (e.modeDirect) "Aucune question posée pour l'instant." else "Aucune question dans le sujet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            itemsIndexed(e.questions) { index, q ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${index + 1}. ${q.question.enonce}", style = MaterialTheme.typography.bodyLarge)
                        Text("${formatPoints(q.question.points)} pt" + if (q.question.aConfirmer) " · à confirmer" else "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        q.question.reponseAttendue?.let {
                            Text("Attendu : $it", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(8.dp))
                        val saisie = saisies[q.question.id] ?: q.saisie // la frappe en cours, sinon ce qui est en base
                        ChampTexte(saisie.reponse, { v -> viewModel.saisir(q.question.id) { it.copy(reponse = v) } }, "Réponse du candidat", uneLigne = false, actif = e.enCours)
                        ChampTexte(saisie.points, { v -> viewModel.saisir(q.question.id) { it.copy(points = v) } }, "Points attribués (0 à ${formatPoints(q.question.points)}) *", clavier = KeyboardType.Decimal, actif = e.enCours)
                    }
                }
            }

            item {
                TitreSection("Observations")
                ChampTexte(observations, viewModel::changerObservations, "Observations de l'examinateur", uneLigne = false, actif = e.enCours)
                if (e.enCours) {
                    if (e.sansNote > 0) {
                        Text("${e.sansNote} question(s) sans points : à noter avant de terminer (0 si la réponse est fausse).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                    }
                    BoutonPrincipal("Terminer l'épreuve", { viewModel.terminer(onTerminee) })
                    Text("Une fois terminée, l'épreuve ne se modifie plus : le résultat sera calculé puis validé par l'ATT.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                } else if (tentative.statut != StatutTentative.EN_COURS) {
                    Text("Épreuve close le ${formatDate(tentative.dateHeure)} : lecture seule.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Passage encore en cours mais non saisissable : la cause est déjà affichée plus haut
                // (épreuve non configurée, rôle sans droit de saisie). Ne pas dire « close ».
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/** Mode DIRECT : la question proposée, un filtre par points, « Une autre » et « Poser cette question ». */
@Composable
private fun Proposition(viewModel: EvaluationTheorieViewModel, e: EtatTheorie) {
    TitreSection("Prochaine question (${formatPoints(e.pointsRestants)} pt restants)")
    val proposition = e.proposition
    if (e.pointsDisponibles.size > 1) {
        SelecteurChoix(
            "Points", e.pointsDisponibles.map { Option((it * 100).toInt(), "${formatPoints(it)} pt") },
            e.pointsVoulus?.let { (it * 100).toInt() }, { id -> viewModel.choisirPoints(id?.let { it / 100.0 }) },
            avecTous = true, libelleTous = "tous",
        )
    }
    if (proposition == null) {
        Text(
            if (e.pointsRestants <= 0) "Sujet complet : la note maximale est atteinte." else "Plus aucune question proposable pour les points restants.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(12.dp)) {
            Text(proposition.enonce, style = MaterialTheme.typography.bodyLarge)
            Text("${formatPoints(proposition.points)} pt", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.autreProposition() }, modifier = Modifier.weight(1f)) { Text("Une autre") }
                Button(onClick = { viewModel.poserQuestion(proposition) }, modifier = Modifier.weight(1f)) { Text("Poser cette question") }
            }
        }
    }
}
