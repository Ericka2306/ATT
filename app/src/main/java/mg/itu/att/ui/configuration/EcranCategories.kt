package mg.itu.att.ui.configuration

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.KeyboardType
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.CaseACocher
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.TexteErreur

/** Les catégories de permis (A', A, B, C, D, E…). */
@Composable
fun EcranCategories(viewModel: ConfigurationViewModel, onNouvelle: () -> Unit, onOuvrir: (Int) -> Unit, onRetour: () -> Unit) {
    val categories by viewModel.categories.collectAsState()

    EcranStandard(titre = "Catégories de permis", onRetour = onRetour, iconeAction = Icons.Filled.Add, descriptionAction = "Nouvelle catégorie", onAction = onNouvelle) {
        LazyColumn {
            items(categories) { c ->
                CarteIcone(
                    icone = Icons.Filled.List,
                    titre = "${c.code} — ${c.libelle}" + if (c.actif) "" else " (inactive)",
                    description = "Âge minimum ${c.ageMinimum ?: "—"}" + (c.categoriePrealableCode?.let { " · préalable : $it" } ?: ""),
                    onClick = { onOuvrir(c.id) },
                    couleurPastille = if (c.actif) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    complement = if (c.aConfirmer) ({ PastilleAConfirmer() }) else null,
                )
            }
        }
    }
}

/** Création ou modification d'une catégorie. */
@Composable
fun EcranFormulaireCategorie(viewModel: ConfigurationViewModel, categorieId: Int?, onEnregistre: (Int) -> Unit, onRetour: () -> Unit) {
    viewModel.preparerCategorie(categorieId)
    val f by viewModel.formulaireCategorie.collectAsState()

    EcranStandard(titre = if (categorieId == null) "Nouvelle catégorie" else "Modifier la catégorie", onRetour = onRetour, defilant = true) {
        ChampTexte(f.code, { v -> viewModel.modifierCategorie { it.copy(code = v) } }, "Code * (ex. B)")
        ChampTexte(f.libelle, { v -> viewModel.modifierCategorie { it.copy(libelle = v) } }, "Libellé *")
        ChampTexte(f.ageMinimum, { v -> viewModel.modifierCategorie { it.copy(ageMinimum = v) } }, "Âge minimum (à confirmer)", clavier = KeyboardType.Number)
        ChampTexte(f.prealable, { v -> viewModel.modifierCategorie { it.copy(prealable = v) } }, "Catégorie préalable (code)", aide = "ex. B pour C, D, E")
        CaseACocher(f.aConfirmer, { v -> viewModel.modifierCategorie { it.copy(aConfirmer = v) } }, "Valeur à confirmer par l'ATT")
        CaseACocher(f.actif, { v -> viewModel.modifierCategorie { it.copy(actif = v) } }, "Catégorie active")
        TexteErreur(f.erreur)
        BoutonPrincipal(if (categorieId == null) "Créer la catégorie" else "Enregistrer", { viewModel.enregistrerCategorie(onSucces = onEnregistre) })
    }
}
