package mg.itu.att.ui.autoecoles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import mg.itu.att.ui.communs.ContenuColonne
import mg.itu.att.ui.communs.EcranAvecBarre
import mg.itu.att.ui.communs.SelecteurRegion
import mg.itu.att.ui.communs.TexteErreur

/**
 * Création (`autoEcoleId == null`) ou modification d'une auto-école (UC03).
 * Tout l'état de saisie vit dans le ViewModel (règle S6) ; l'écran signale, la navigation décide.
 */
@Composable
fun EcranFormulaireAutoEcole(
    viewModel: AutoEcolesViewModel,
    autoEcoleId: Int?,
    onEnregistre: (Int) -> Unit,
    onRetour: () -> Unit,
) {
    viewModel.preparerFormulaire(autoEcoleId)
    val f by viewModel.formulaire.collectAsState()

    EcranAvecBarre(
        titre = if (autoEcoleId == null) "Nouvelle auto-école" else "Modifier l'auto-école",
        onRetour = onRetour,
    ) { marges ->
        ContenuColonne(marges) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = f.nom,
                    onValueChange = viewModel::changerNom,
                    label = { Text("Nom de l'auto-école *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                SelecteurRegion(
                    regions = f.regions,
                    regionId = f.regionId,
                    onChoix = viewModel::changerRegion,
                    verrouille = f.regionVerrouillee,
                    libelleVide = "à choisir *",
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = f.adresse,
                    onValueChange = viewModel::changerAdresse,
                    label = { Text("Adresse *") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = f.numeroAgrement,
                    onValueChange = viewModel::changerAgrement,
                    label = { Text("Numéro d'agrément (à confirmer)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = f.telephone,
                    onValueChange = viewModel::changerTelephone,
                    label = { Text("Téléphone") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                TexteErreur(f.erreur)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.enregistrer(onSucces = onEnregistre) },
                    enabled = !f.enCours,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (autoEcoleId == null) "Créer l'auto-école" else "Enregistrer les modifications")
                }
            }
        }
    }
}
