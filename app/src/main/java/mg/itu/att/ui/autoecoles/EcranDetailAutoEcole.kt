package mg.itu.att.ui.autoecoles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.communs.ContenuColonne
import mg.itu.att.ui.communs.EcranAvecBarre
import mg.itu.att.ui.communs.ouTiret

/**
 * Fiche d'une auto-école (UC03) : informations, comptes de connexion, historique des modifications.
 * L'écran reçoit l'identifiant par la route et se sert lui-même (règle S5).
 */
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

    EcranAvecBarre(
        titre = ligne?.autoEcole?.nom ?: "Auto-école",
        onRetour = onRetour,
        iconeAction = Icons.Filled.Edit,
        descriptionAction = "Modifier",
        onAction = onModifier,
    ) { marges ->
        ContenuColonne(marges) {
            // Un identifiant invalide n'affiche rien : il ne plante pas (cours S5).
            if (ligne == null) {
                Text("Auto-école introuvable.")
                return@ContenuColonne
            }
            val a = ligne.autoEcole

            LazyColumn {
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Ligne("Région", ligne.nomRegion)
                            Ligne("Adresse", a.adresse)
                            Ligne("Téléphone", a.telephone.ouTiret())
                            Ligne("Agrément", a.numeroAgrement.ouTiret() + "  (à confirmer)")
                            Ligne("Statut", if (a.actif) "Active" else "Inactive")
                            Spacer(Modifier.height(12.dp))
                            Row {
                                OutlinedButton(onClick = { viewModel.basculerActif(a.id) }) {
                                    Text(if (a.actif) "Désactiver" else "Réactiver")
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("Comptes de connexion", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                }

                if (etat.comptes.isEmpty()) {
                    item {
                        Text(
                            "Aucun compte : l'auto-école ne peut pas encore se connecter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(etat.comptes) { compte ->
                    Text(
                        "• ${compte.identifiant}" + if (compte.actif) "" else " (désactivé)",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onCreerCompte, enabled = a.actif) { Text("Créer un compte") }
                    Spacer(Modifier.height(20.dp))
                    Text("Historique", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                }
                items(etat.historique) { h ->
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text(
                            "${formatDate(h.dateHeure)} — ${h.action}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            h.nouvelleValeur ?: h.ancienneValeur ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/** Une ligne « libellé : valeur » de la fiche. */
@Composable
private fun Ligne(libelle: String, valeur: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text(
            libelle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp),
        )
        Text(valeur, style = MaterialTheme.typography.bodyLarge)
    }
}
