package mg.itu.att.ui.configuration

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import mg.itu.att.ui.communs.Option
import mg.itu.att.ui.communs.SelecteurChoix
import mg.itu.att.ui.communs.TexteErreur
import mg.itu.att.ui.communs.TitreSection

/** Une épreuve : ses barèmes (versions), ses questions (théorie), ses critères (conduite). */
@Composable
fun EcranEpreuve(
    viewModel: ConfigurationViewModel, epreuveId: Int,
    onModifier: () -> Unit, onNouveauBareme: () -> Unit, onNouvelleQuestion: () -> Unit, onNouveauCritere: () -> Unit, onRetour: () -> Unit,
) {
    viewModel.afficherEpreuve(epreuveId)
    val etat by viewModel.epreuve.collectAsState()
    val e = etat.epreuve

    EcranStandard(titre = e?.libelle ?: "Épreuve", onRetour = onRetour, iconeAction = Icons.Filled.Edit, descriptionAction = "Modifier", onAction = onModifier) {
        if (e == null) { Text("Épreuve introuvable."); return@EcranStandard }
        val courant = etat.baremes.firstOrNull { it.dateFinValidite == null }
        LazyColumn {
            item {
                CarteFiche {
                    LigneInfo("Catégorie", etat.categorie?.let { "${it.code} — ${it.libelle}" } ?: "?")
                    LigneInfo("Code", e.code)
                    LigneInfo("Barème courant", courant?.let { "note max ${it.noteMax}, seuil ${it.seuilReussite} (v${it.version})" } ?: "aucun")
                    if (courant?.aConfirmer == true) Row { PastilleAConfirmer() }
                }
                TitreSection("Versions du barème")
            }
            items(etat.baremes) { b ->
                Text(
                    "v${b.version} : note max ${b.noteMax}, seuil ${b.seuilReussite} — du ${formatDate(b.dateDebutValidite)}" + (b.dateFinValidite?.let { " au ${formatDate(it)}" } ?: " (courant)"),
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp),
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onNouveauBareme) { Text("Nouvelle version du barème") }
                TitreSection("Questions (${etat.questions.count { it.actif }} actives)")
                if (etat.questions.isEmpty()) Text("Aucune question. Nécessaire pour évaluer une épreuve théorique.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(etat.questions) { q ->
                Row {
                    Text(
                        "${q.ordre}. ${q.enonce} (${q.points} pt)" + (q.reponseAttendue?.let { " — attendu : $it" } ?: "") + if (q.actif) "" else " — inactive",
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                        color = if (q.actif) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = { viewModel.basculerQuestion(q) }) { Text(if (q.actif) "Désactiver" else "Réactiver") }
                }
            }
            item {
                Button(onClick = onNouvelleQuestion) { Text("Ajouter une question") }
                TitreSection("Critères de conduite (${etat.criteres.count { it.actif }} actifs)")
                if (etat.criteres.isEmpty()) Text("Aucun critère : la grille de conduite est à confirmer par l'ATT (Q2).", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(etat.criteres) { c ->
                Row {
                    Text(
                        "${c.libelle} (${c.points} pt${if (c.eliminatoire) ", éliminatoire" else ""})" + if (c.actif) "" else " — inactif",
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                    )
                    TextButton(onClick = { viewModel.basculerCritere(c) }) { Text(if (c.actif) "Désactiver" else "Réactiver") }
                }
            }
            item {
                Button(onClick = onNouveauCritere) { Text("Ajouter un critère") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/** Nouvelle version du barème : l'ancienne est conservée et fermée, jamais modifiée. */
@Composable
fun EcranFormulaireBareme(viewModel: ConfigurationViewModel, epreuveId: Int, onCree: () -> Unit, onRetour: () -> Unit) {
    val f by viewModel.formulaireBareme.collectAsState()
    EcranStandard(titre = "Nouvelle version du barème", onRetour = onRetour, defilant = true) {
        EncartInfo("Les résultats déjà calculés gardent leur version de barème. La nouvelle version s'applique aux évaluations à venir.")
        Spacer(Modifier.height(12.dp))
        ChampTexte(f.noteMax, { v -> viewModel.modifierBareme { it.copy(noteMax = v) } }, "Note maximale *", clavier = KeyboardType.Decimal)
        ChampTexte(f.seuil, { v -> viewModel.modifierBareme { it.copy(seuil = v) } }, "Seuil de réussite *", clavier = KeyboardType.Decimal)
        CaseACocher(f.aConfirmer, { v -> viewModel.modifierBareme { it.copy(aConfirmer = v) } }, "Valeur à confirmer par l'ATT")
        TexteErreur(f.erreur)
        BoutonPrincipal("Créer la version", { viewModel.creerVersionBareme(epreuveId, onSucces = onCree) })
    }
}

/** Nouvelle question orale : énoncé, points, réponse attendue facultative (vue seulement par l'examinateur). */
@Composable
fun EcranFormulaireQuestion(viewModel: ConfigurationViewModel, epreuveId: Int, onCree: () -> Unit, onRetour: () -> Unit) {
    val f by viewModel.formulaireQuestion.collectAsState()
    EcranStandard(titre = "Nouvelle question", onRetour = onRetour, defilant = true) {
        ChampTexte(f.enonce, { v -> viewModel.modifierQuestion { it.copy(enonce = v) } }, "Énoncé *", uneLigne = false)
        ChampTexte(f.points, { v -> viewModel.modifierQuestion { it.copy(points = v) } }, "Points *", clavier = KeyboardType.Decimal)
        ChampTexte(f.reponseAttendue, { v -> viewModel.modifierQuestion { it.copy(reponseAttendue = v) } }, "Réponse attendue", uneLigne = false, aide = "Aide-mémoire pour l'examinateur ; le candidat répond à l'oral")
        CaseACocher(f.aConfirmer, { v -> viewModel.modifierQuestion { it.copy(aConfirmer = v) } }, "Question à confirmer par l'ATT")
        TexteErreur(f.erreur)
        BoutonPrincipal("Créer la question", { viewModel.creerQuestion(epreuveId, onSucces = onCree) })
    }
}

/** Nouveau critère de conduite (structure seulement, grille à confirmer). */
@Composable
fun EcranFormulaireCritere(viewModel: ConfigurationViewModel, epreuveId: Int, onCree: () -> Unit, onRetour: () -> Unit) {
    val f by viewModel.formulaireCritere.collectAsState()
    EcranStandard(titre = "Nouveau critère", onRetour = onRetour, defilant = true) {
        ChampTexte(f.libelle, { v -> viewModel.modifierCritere { it.copy(libelle = v) } }, "Libellé *")
        ChampTexte(f.points, { v -> viewModel.modifierCritere { it.copy(points = v) } }, "Points *", clavier = KeyboardType.Decimal)
        CaseACocher(f.eliminatoire, { v -> viewModel.modifierCritere { it.copy(eliminatoire = v) } }, "Faute éliminatoire")
        CaseACocher(f.aConfirmer, { v -> viewModel.modifierCritere { it.copy(aConfirmer = v) } }, "Critère à confirmer par l'ATT")
        TexteErreur(f.erreur)
        BoutonPrincipal("Créer le critère", { viewModel.creerCritere(epreuveId, onSucces = onCree) })
    }
}
