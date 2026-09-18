package mg.itu.att.ui.comptes

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.EncartInfo
import mg.itu.att.ui.communs.TonEncart
import mg.itu.att.ui.communs.SelecteurRegion
import mg.itu.att.ui.communs.TexteErreur
import mg.itu.att.ui.communs.libelle

/** Tous les comptes de connexion (Super Admin) : rôle, région, actif ; désactivation directe (sauf le sien). */
@Composable
fun EcranComptes(viewModel: ComptesViewModel, onNouvelAdmin: () -> Unit, onRetour: () -> Unit) {
    val etat by viewModel.comptes.collectAsState()

    EcranStandard(titre = "Comptes", onRetour = onRetour, iconeAction = Icons.Filled.Add, descriptionAction = "Nouvel administrateur ATT", onAction = onNouvelAdmin) {
        Text("${etat.comptes.size} compte(s). Les comptes auto-école et examinateur se créent depuis leurs fiches.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyColumn {
            items(etat.comptes) { ligne ->
                val u = ligne.utilisateur
                CarteIcone(
                    icone = Icons.Filled.AccountCircle,
                    titre = u.identifiant + if (u.actif) "" else " (désactivé)",
                    description = "${u.nom} · ${u.role.libelle()} · ${ligne.nomRegion ?: "national"}",
                    onClick = {},
                    couleurPastille = if (u.actif) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    couleurIcone = if (u.actif) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    complement = if (u.id == etat.idCourant) null else {
                        { TextButton(onClick = { viewModel.basculerCompte(u.id) }) { Text(if (u.actif) "Désactiver" else "Réactiver") } }
                    },
                )
            }
        }
    }
}

/** Création d'un compte Administrateur ATT, national (aucune région) ou régional (Super Admin). */
@Composable
fun EcranFormulaireAdmin(viewModel: ComptesViewModel, onCree: () -> Unit, onRetour: () -> Unit) {
    viewModel.preparerAdmin()
    val f by viewModel.formulaireAdmin.collectAsState()

    EcranStandard(titre = "Nouvel administrateur ATT", onRetour = onRetour, defilant = true) {
        ChampTexte(f.nom, { v -> viewModel.modifierAdmin { it.copy(nom = v) } }, "Nom *")
        ChampTexte(f.identifiant, { v -> viewModel.modifierAdmin { it.copy(identifiant = v) } }, "Identifiant *")
        ChampTexte(f.motDePasse, { v -> viewModel.modifierAdmin { it.copy(motDePasse = v) } }, "Mot de passe initial *", motDePasse = true)
        SelecteurRegion(f.regions, f.regionId, { v -> viewModel.modifierAdmin { it.copy(regionId = v) } }, avecToutes = true)
        EncartInfo("« Toutes les régions » = administrateur national. Une région = administrateur régional (à confirmer, Q11).", TonEncart.AVERTISSEMENT)
        TexteErreur(f.erreur)
        BoutonPrincipal("Créer le compte", { viewModel.creerAdmin(onSucces = onCree) }, actif = !f.enCours)
    }
}
