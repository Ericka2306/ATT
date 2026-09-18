package mg.itu.att.ui.configuration

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import mg.itu.att.data.TypeValeur
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.CarteFiche
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.CaseACocher
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.EncartInfo
import mg.itu.att.ui.communs.LigneInfo
import mg.itu.att.ui.communs.TexteErreur

/** Les règles configurables (tentatives, délais, retards, capacités, pièces…), globales ou par catégorie. */
@Composable
fun EcranRegles(viewModel: ConfigurationViewModel, onOuvrir: (Int) -> Unit, onRetour: () -> Unit) {
    val regles by viewModel.regles.collectAsState()
    EcranStandard(titre = "Règles configurables", onRetour = onRetour) {
        EncartInfo("Le code lit ces valeurs au moment du calcul ; rien n'est codé en dur.")
        if (regles.isEmpty()) Text("Aucune règle enregistrée.", style = MaterialTheme.typography.bodyLarge)
        LazyColumn {
            items(regles) { l ->
                val r = l.regle
                CarteIcone(
                    icone = Icons.Filled.Settings,
                    titre = r.cle + (l.codeCategorie?.let { " (catégorie $it)" } ?: ""),
                    description = "= ${r.valeur.take(60)}${if (r.valeur.length > 60) "…" else ""} · ${r.description}",
                    onClick = { onOuvrir(r.id) },
                    complement = if (r.aConfirmer) ({ PastilleAConfirmer() }) else null,
                )
            }
        }
    }
}

/** Modification de la valeur d'une règle (la clé et le type ne changent pas). */
@Composable
fun EcranFormulaireRegle(viewModel: ConfigurationViewModel, regleId: Int, onEnregistre: () -> Unit, onRetour: () -> Unit) {
    viewModel.preparerRegle(regleId)
    val f by viewModel.formulaireRegle.collectAsState()
    val r = f.regle

    EcranStandard(titre = r?.cle ?: "Règle", onRetour = onRetour, defilant = true) {
        if (r == null) { Text("Règle introuvable."); return@EcranStandard }
        CarteFiche {
            LigneInfo("Type", when (r.typeValeur) { TypeValeur.ENTIER -> "nombre entier"; TypeValeur.DECIMAL -> "nombre"; TypeValeur.BOOLEEN -> "true / false"; TypeValeur.TEXTE -> "texte" })
            LigneInfo("Portée", if (r.categorieId == null) "toutes les catégories" else "catégorie n° ${r.categorieId}")
        }
        ChampTexte(f.valeur, { v -> viewModel.modifierRegle { it.copy(valeur = v) } }, "Valeur *", uneLigne = r.typeValeur != TypeValeur.TEXTE)
        ChampTexte(f.description, { v -> viewModel.modifierRegle { it.copy(description = v) } }, "Description", uneLigne = false)
        CaseACocher(f.aConfirmer, { v -> viewModel.modifierRegle { it.copy(aConfirmer = v) } }, "Valeur à confirmer par l'ATT")
        TexteErreur(f.erreur)
        BoutonPrincipal("Enregistrer", { viewModel.enregistrerRegle(onSucces = onEnregistre) })
    }
}
