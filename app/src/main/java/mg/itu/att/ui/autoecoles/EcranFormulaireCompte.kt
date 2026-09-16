package mg.itu.att.ui.autoecoles

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.att.metier.ValidationAutoEcole
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.TexteErreur

/** Création du compte de connexion d'une auto-école (UC03). Le mot de passe est haché, jamais stocké en clair. */
@Composable
fun EcranFormulaireCompte(
    viewModel: AutoEcolesViewModel,
    autoEcoleId: Int,
    onCree: () -> Unit,
    onRetour: () -> Unit,
) {
    val c by viewModel.compte.collectAsState()

    EcranStandard(titre = "Nouveau compte", onRetour = onRetour, defilant = true) {
        Text(
            "Ce compte permettra à l'auto-école de gérer ses propres candidats. Communiquez-lui l'identifiant et le mot de passe initial.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        ChampTexte(c.identifiant, { v -> viewModel.modifierCompte { it.copy(identifiant = v) } }, "Identifiant (${ValidationAutoEcole.LONGUEUR_MIN_IDENTIFIANT} caractères min.)")
        ChampTexte(c.motDePasse, { v -> viewModel.modifierCompte { it.copy(motDePasse = v) } }, "Mot de passe initial (${ValidationAutoEcole.LONGUEUR_MIN_MOT_DE_PASSE} caractères min.)", motDePasse = true)
        TexteErreur(c.erreur)
        BoutonPrincipal("Créer le compte", { viewModel.creerCompte(autoEcoleId, onSucces = onCree) }, actif = !c.enCours)
    }
}
