package mg.itu.att.ui.autoecoles

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.att.ui.communs.CarteFiche
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.LigneInfo
import mg.itu.att.ui.communs.LigneHistorique
import mg.itu.att.ui.communs.TitreSection
import mg.itu.att.ui.communs.ouTiret

/** Fiche d'une auto-école (UC03) : informations, comptes de connexion, historique. */
@Composable
fun EcranDetailAutoEcole(
    viewModel: AutoEcolesViewModel,
    autoEcoleId: Int,
    onModifier: () -> Unit,
    onCreerCompte: () -> Unit,
    onRetour: () -> Unit,
) {
    viewModel.afficherDetail(autoEcoleId)
    val etat by viewModel.detail.collectAsState()
    val ligne = etat.autoEcole

    EcranStandard(
        titre = ligne?.autoEcole?.nom ?: "Auto-école",
        onRetour = onRetour,
        iconeAction = Icons.Filled.Edit,
        descriptionAction = "Modifier",
        onAction = onModifier,
    ) {
        // Un identifiant invalide n'affiche rien : il ne plante pas (cours S5).
        if (ligne == null) {
            Text("Auto-école introuvable.")
            return@EcranStandard
        }
        val a = ligne.autoEcole

        LazyColumn {
            item {
                CarteFiche {
                    LigneInfo("Région", ligne.nomRegion)
                    LigneInfo("Adresse", a.adresse)
                    LigneInfo("Téléphone", a.telephone.ouTiret())
                    LigneInfo("Agrément", a.numeroAgrement.ouTiret() + "  (à confirmer)")
                    LigneInfo("Statut", if (a.actif) "Active" else "Inactive")
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.basculerActif(a.id) }) { Text(if (a.actif) "Désactiver" else "Réactiver") }
                }
                TitreSection("Comptes de connexion")
                if (etat.comptes.isEmpty()) {
                    Text("Aucun compte : l'auto-école ne peut pas encore se connecter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(etat.comptes) { compte ->
                Text("• ${compte.identifiant}" + if (compte.actif) "" else " (désactivé)", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 2.dp))
            }
            item {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onCreerCompte, enabled = a.actif) { Text("Créer un compte") }
                TitreSection("Historique")
            }
            items(etat.historique) { LigneHistorique(it) }
        }
    }
}
