package mg.itu.att.ui.communs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import mg.itu.att.data.Region

/** Une option d'un sélecteur : un identifiant et son libellé. */
data class Option(val id: Int, val libelle: String)

/**
 * Sélecteur générique : un champ qui ressemble aux autres champs du formulaire (libellé, valeur, flèche)
 * et qui ouvre une liste déroulante (`DropdownMenu`, docs/HORS_COURS.md n° 17).
 * L'ouverture/fermeture est un état purement local au composable (`remember`, cours S4) ;
 * le choix lui-même remonte au ViewModel par [onChoix].
 *
 * @param avecTous ajoute une entrée « Tous » (valeur null), pour un filtre de liste.
 * @param verrouille vrai quand le choix est imposé par le rôle (administrateur régional, auto-école).
 */
@Composable
fun SelecteurChoix(
    prefixe: String,
    options: List<Option>,
    choixId: Int?,
    onChoix: (Int?) -> Unit,
    avecTous: Boolean = false,
    libelleTous: String = "Tous",
    libelleVide: String = "à choisir",
    verrouille: Boolean = false,
) {
    var ouvert by remember { mutableStateOf(false) }
    val libelle = options.find { it.id == choixId }?.libelle ?: if (avecTous) libelleTous else ""

    Box(Modifier.fillMaxWidth()) {
        // Le champ n'est pas éditable : il affiche le choix ; le `Box` transparent par-dessus reçoit le clic.
        OutlinedTextField(
            value = libelle,
            onValueChange = {},
            readOnly = true,
            enabled = !verrouille,
            label = { Text(prefixe) },
            placeholder = { Text(libelleVide) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true,
            shape = MaterialTheme.shapes.extraSmall,
            colors = couleursChamp(),
            modifier = Modifier.fillMaxWidth(),
        )
        if (!verrouille) {
            // La zone cliquable porte le nom du champ et son choix : un lecteur d'écran (et l'automate de test)
            // y voient un bouton « Région : Analamanga » au lieu d'une surface muette.
            Box(
                Modifier
                    .matchParentSize()
                    .semantics { contentDescription = if (libelle.isBlank()) prefixe else "$prefixe : $libelle"; role = Role.Button }
                    .clickable { ouvert = true },
            )
        }
        DropdownMenu(expanded = ouvert, onDismissRequest = { ouvert = false }) {
            if (avecTous) {
                DropdownMenuItem(text = { Text(libelleTous) }, onClick = { onChoix(null); ouvert = false })
            }
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option.libelle) }, onClick = { onChoix(option.id); ouvert = false })
            }
        }
    }
    Spacer(Modifier.height(14.dp))
}

/** Sélecteur de région, cas particulier du sélecteur générique. */
@Composable
fun SelecteurRegion(
    regions: List<Region>,
    regionId: Int?,
    onChoix: (Int?) -> Unit,
    avecToutes: Boolean = false,
    verrouille: Boolean = false,
    libelleVide: String = "à choisir *",
) = SelecteurChoix(
    prefixe = "Région",
    options = regions.map { Option(it.id, it.nom) },
    choixId = regionId,
    onChoix = onChoix,
    avecTous = avecToutes,
    libelleTous = "Toutes les régions",
    libelleVide = libelleVide,
    verrouille = verrouille,
)
