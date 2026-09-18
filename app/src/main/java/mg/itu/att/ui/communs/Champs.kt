package mg.itu.att.ui.communs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Champs de formulaire factorisés : un champ = une ligne dans l'écran.
 * Chaque champ embarque son espacement, donc les formulaires ne contiennent plus de `Spacer`.
 * Aspect (refonte du 18/09/2026) : champs blancs à coins arrondis et bord fin, icône facultative,
 * bouton principal haut et arrondi. Les signatures existantes sont inchangées.
 */

/** Les couleurs communes des champs : fond blanc, bord fin gris, bord bleu quand le champ est actif. */
@Composable
fun couleursChamp(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
)

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
    icone: ImageVector? = null,
) {
    OutlinedTextField(
        value = valeur,
        onValueChange = onChange,
        label = { Text(libelle) },
        singleLine = uneLigne,
        minLines = if (uneLigne) 1 else 2,
        enabled = actif,
        visualTransformation = if (motDePasse) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = clavier),
        supportingText = aide?.let { { Text(it) } },
        leadingIcon = icone?.let { { Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } },
        shape = MaterialTheme.shapes.extraSmall,
        colors = couleursChamp(),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(14.dp))
}

/** Bouton principal pleine largeur, placé sous un formulaire ; icône facultative à gauche du texte. */
@Composable
fun BoutonPrincipal(texte: String, onClick: () -> Unit, actif: Boolean = true, icone: ImageVector? = null) {
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = onClick,
        enabled = actif,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        if (icone != null) {
            Icon(icone, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texte, style = MaterialTheme.typography.labelLarge)
    }
}

/** Bouton secondaire pleine largeur (contour), pour l'action de second rang sous un formulaire. */
@Composable
fun BoutonSecondaire(texte: String, onClick: () -> Unit, actif: Boolean = true, icone: ImageVector? = null) {
    Spacer(Modifier.height(8.dp))
    OutlinedButton(
        onClick = onClick,
        enabled = actif,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        if (icone != null) {
            Icon(icone, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texte, style = MaterialTheme.typography.labelLarge)
    }
}

/** Case à cocher avec son libellé (docs/HORS_COURS.md n° 20) ; toute la ligne est cliquable. */
@Composable
fun CaseACocher(coche: Boolean, onChange: (Boolean) -> Unit, libelle: String, actif: Boolean = true) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = actif) { onChange(!coche) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = coche, onCheckedChange = null, enabled = actif)
        Spacer(Modifier.width(4.dp))
        Text(libelle, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Une rangée d'actions (boutons) qui passe à la ligne quand l'écran est trop étroit (`FlowRow`,
 * docs/HORS_COURS.md n° 27) : sur un petit téléphone, trois boutons ne tiennent pas toujours côte à côte.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LigneActions(contenu: @Composable () -> Unit) {
    FlowRow(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) { contenu() }
}
