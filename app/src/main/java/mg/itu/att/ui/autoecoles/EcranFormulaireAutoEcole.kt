package mg.itu.att.ui.autoecoles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.KeyboardType
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.SelecteurRegion
import mg.itu.att.ui.communs.TexteErreur

/** Création (`autoEcoleId == null`) ou modification d'une auto-école (UC03). L'état de saisie vit dans le ViewModel. */
@Composable
fun EcranFormulaireAutoEcole(
    viewModel: AutoEcolesViewModel,
    autoEcoleId: Int?,
    onEnregistre: (Int) -> Unit,
    onRetour: () -> Unit,
) {
    viewModel.preparerFormulaire(autoEcoleId)
    val f by viewModel.formulaire.collectAsState()

    EcranStandard(
        titre = if (autoEcoleId == null) "Nouvelle auto-école" else "Modifier l'auto-école",
        onRetour = onRetour,
        defilant = true,
    ) {
        ChampTexte(f.nom, { v -> viewModel.modifierFormulaire { it.copy(nom = v) } }, "Nom de l'auto-école *")
        SelecteurRegion(f.regions, f.regionId, { v -> viewModel.modifierFormulaire { it.copy(regionId = v) } }, verrouille = f.regionVerrouillee)
        ChampTexte(f.adresse, { v -> viewModel.modifierFormulaire { it.copy(adresse = v) } }, "Adresse *", uneLigne = false)
        ChampTexte(f.numeroAgrement, { v -> viewModel.modifierFormulaire { it.copy(numeroAgrement = v) } }, "Numéro d'agrément (à confirmer)")
        ChampTexte(f.telephone, { v -> viewModel.modifierFormulaire { it.copy(telephone = v) } }, "Téléphone", clavier = KeyboardType.Phone)
        TexteErreur(f.erreur)
        BoutonPrincipal(
            texte = if (autoEcoleId == null) "Créer l'auto-école" else "Enregistrer les modifications",
            onClick = { viewModel.enregistrer(onSucces = onEnregistre) },
            actif = !f.enCours,
        )
    }
}
