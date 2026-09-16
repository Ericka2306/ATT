package mg.itu.att.ui.configuration

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.KeyboardType
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.CarteFiche
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.CaseACocher
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.LigneInfo
import mg.itu.att.ui.communs.TexteErreur
import mg.itu.att.ui.communs.TitreSection

/** Une catégorie et ses épreuves, dans l'ordre de passage. */
@Composable
fun EcranDetailCategorie(viewModel: ConfigurationViewModel, categorieId: Int, onModifier: () -> Unit, onNouvelleEpreuve: () -> Unit, onOuvrirEpreuve: (Int) -> Unit, onRetour: () -> Unit) {
    viewModel.afficherCategorie(categorieId)
    val etat by viewModel.detailCategorie.collectAsState()
    val c = etat.categorie

    EcranStandard(titre = c?.let { "Permis ${it.code}" } ?: "Catégorie", onRetour = onRetour, iconeAction = Icons.Filled.Edit, descriptionAction = "Modifier", onAction = onModifier) {
        if (c == null) { Text("Catégorie introuvable."); return@EcranStandard }
        LazyColumn {
            item {
                CarteFiche {
                    LigneInfo("Libellé", c.libelle)
                    LigneInfo("Âge minimum", c.ageMinimum?.toString() ?: "—")
                    LigneInfo("Préalable", c.categoriePrealableCode ?: "—")
                    LigneInfo("Statut", (if (c.actif) "Active" else "Inactive") + if (c.aConfirmer) " · à confirmer" else "")
                }
                TitreSection("Épreuves (ordre de passage)")
                if (etat.epreuves.isEmpty()) Text("Aucune épreuve.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(etat.epreuves) { e ->
                CarteIcone(
                    icone = Icons.Filled.Star,
                    titre = "${e.ordre}. ${e.libelle}" + if (e.actif) "" else " (inactive)",
                    description = "${e.code} · durée ${e.dureeMinutes?.let { "$it min" } ?: "—"}" + if (e.obligatoire) " · obligatoire" else "",
                    onClick = { onOuvrirEpreuve(e.id) },
                    complement = if (e.aConfirmer) ({ PastilleAConfirmer() }) else null,
                )
            }
            item { Button(onClick = onNouvelleEpreuve) { Text("Ajouter une épreuve") } }
        }
    }
}

/** Création ou modification d'une épreuve d'une catégorie. */
@Composable
fun EcranFormulaireEpreuve(viewModel: ConfigurationViewModel, categorieId: Int, epreuveId: Int?, onEnregistre: () -> Unit, onRetour: () -> Unit) {
    viewModel.preparerEpreuve(categorieId, epreuveId)
    val f by viewModel.formulaireEpreuve.collectAsState()

    EcranStandard(titre = if (epreuveId == null) "Nouvelle épreuve" else "Modifier l'épreuve", onRetour = onRetour, defilant = true) {
        ChampTexte(f.code, { v -> viewModel.modifierEpreuve { it.copy(code = v) } }, "Code * (THEORIE, CONDUITE, MANOEUVRES…)")
        ChampTexte(f.libelle, { v -> viewModel.modifierEpreuve { it.copy(libelle = v) } }, "Libellé *")
        ChampTexte(f.ordre, { v -> viewModel.modifierEpreuve { it.copy(ordre = v) } }, "Ordre de passage *", clavier = KeyboardType.Number)
        ChampTexte(f.duree, { v -> viewModel.modifierEpreuve { it.copy(duree = v) } }, "Durée d'un passage en minutes (à confirmer)", clavier = KeyboardType.Number)
        CaseACocher(f.obligatoire, { v -> viewModel.modifierEpreuve { it.copy(obligatoire = v) } }, "Épreuve obligatoire")
        CaseACocher(f.aConfirmer, { v -> viewModel.modifierEpreuve { it.copy(aConfirmer = v) } }, "Valeur à confirmer par l'ATT")
        CaseACocher(f.actif, { v -> viewModel.modifierEpreuve { it.copy(actif = v) } }, "Épreuve active")
        TexteErreur(f.erreur)
        BoutonPrincipal(if (epreuveId == null) "Créer l'épreuve" else "Enregistrer", { viewModel.enregistrerEpreuve(onSucces = onEnregistre) })
    }
}
