package mg.itu.att.ui.communs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Champs de formulaire factorisés : un champ = une ligne dans l'écran.
 * Chaque champ embarque son espacement, donc les formulaires ne contiennent plus de `Spacer`.
 */

/** Champ texte standard, pleine largeur, avec son espacement en dessous. */
@Composable
fun ChampTexte(
    valeur: String,
    onChange: (String) -> Unit,
    libelle: String,
    uneLigne: Boolean = true,
    motDePasse: Boolean = false,
    clavier: KeyboardType = KeyboardType.Text,
    actif: Boolean = true,
    aide: String? = null,
) {
    OutlinedTextField(
        value = valeur,
        onValueChange = onChange,
        label = { Text(libelle) },
        singleLine = uneLigne,
        enabled = actif,
        visualTransformation = if (motDePasse) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = clavier),
        supportingText = aide?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(12.dp))
}

/** Bouton principal pleine largeur, placé sous un formulaire. */
@Composable
fun BoutonPrincipal(texte: String, onClick: () -> Unit, actif: Boolean = true) {
    Spacer(Modifier.height(4.dp))
    Button(onClick = onClick, enabled = actif, modifier = Modifier.fillMaxWidth().height(50.dp)) {
        Text(texte)
    }
}

/** Case à cocher avec son libellé (docs/HORS_COURS.md n° 20) ; toute la ligne est cliquable. */
@Composable
fun CaseACocher(coche: Boolean, onChange: (Boolean) -> Unit, libelle: String, actif: Boolean = true) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = actif) { onChange(!coche) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = coche, onCheckedChange = null, enabled = actif)
        Text(libelle)
    }
}
