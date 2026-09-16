package mg.itu.att.ui.comptes

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import mg.itu.att.metier.ValidationCompte
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.TexteErreur

/** Changement de son propre mot de passe (tous les rôles) : l'ancien est vérifié, le nouveau est haché. */
@Composable
fun EcranMotDePasse(viewModel: ComptesViewModel, onRetour: () -> Unit) {
    val f by viewModel.motDePasse.collectAsState()

    EcranStandard(titre = "Mon mot de passe", onRetour = onRetour, defilant = true) {
        ChampTexte(f.ancien, { v -> viewModel.modifierMotDePasse { it.copy(ancien = v) } }, "Mot de passe actuel", motDePasse = true)
        ChampTexte(f.nouveau, { v -> viewModel.modifierMotDePasse { it.copy(nouveau = v) } }, "Nouveau mot de passe (${ValidationCompte.LONGUEUR_MIN_MOT_DE_PASSE} caractères min.)", motDePasse = true)
        ChampTexte(f.confirmation, { v -> viewModel.modifierMotDePasse { it.copy(confirmation = v) } }, "Confirmer le nouveau mot de passe", motDePasse = true)
        TexteErreur(f.erreur)
        if (f.reussi) {
            Text("Mot de passe changé.", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyLarge)
        }
        BoutonPrincipal("Changer le mot de passe", { viewModel.changerMotDePasse() })
    }
}
