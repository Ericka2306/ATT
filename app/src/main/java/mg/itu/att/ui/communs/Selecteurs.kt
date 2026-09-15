package mg.itu.att.ui.communs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import mg.itu.att.data.Region

/**
 * Sélecteur de région : un bouton qui ouvre une liste déroulante (`DropdownMenu`, docs/HORS_COURS.md n° 17).
 * L'ouverture/fermeture du menu est un état purement local au composable (`remember`, cours S4) ;
 * le choix lui-même remonte au ViewModel par [onChoix].
 *
 * @param avecToutes ajoute une entrée « Toutes les régions » (valeur null), pour un filtre de liste.
 * @param verrouille vrai quand l'utilisateur est un administrateur régional : la région ne se change pas.
 */
@Composable
fun SelecteurRegion(
    regions: List<Region>,
    regionId: Int?,
    onChoix: (Int?) -> Unit,
    avecToutes: Boolean = false,
    verrouille: Boolean = false,
    libelleVide: String = "Choisir la région",
) {
    var ouvert by remember { mutableStateOf(false) }
    val libelle = regions.find { it.id == regionId }?.nom
        ?: if (avecToutes) "Toutes les régions" else libelleVide

    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { ouvert = true },
            enabled = !verrouille,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Région : $libelle")
        }
        DropdownMenu(expanded = ouvert, onDismissRequest = { ouvert = false }) {
            if (avecToutes) {
                DropdownMenuItem(
                    text = { Text("Toutes les régions") },
                    onClick = { onChoix(null); ouvert = false },
                )
            }
            regions.forEach { region ->
                DropdownMenuItem(
                    text = { Text(region.nom) },
                    onClick = { onChoix(region.id); ouvert = false },
                )
            }
        }
    }
}
