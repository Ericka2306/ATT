package mg.itu.att.ui.autoecoles

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import mg.itu.att.metier.ValidationAutoEcole
import mg.itu.att.ui.communs.ContenuColonne
import mg.itu.att.ui.communs.EcranAvecBarre
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

    EcranAvecBarre(titre = "Nouveau compte", onRetour = onRetour) { marges ->
        ContenuColonne(marges) {
            Text(
                "Ce compte permettra à l'auto-école de gérer ses propres candidats. " +
                    "Communiquez-lui l'identifiant et le mot de passe initial.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = c.identifiant,
                onValueChange = viewModel::changerIdentifiantCompte,
                label = { Text("Identifiant (${ValidationAutoEcole.LONGUEUR_MIN_IDENTIFIANT} caractères min.)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = c.motDePasse,
                onValueChange = viewModel::changerMotDePasseCompte,
                label = { Text("Mot de passe initial (${ValidationAutoEcole.LONGUEUR_MIN_MOT_DE_PASSE} caractères min.)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            TexteErreur(c.erreur)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.creerCompte(autoEcoleId, onSucces = onCree) },
                enabled = !c.enCours,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Créer le compte")
            }
        }
    }
}
