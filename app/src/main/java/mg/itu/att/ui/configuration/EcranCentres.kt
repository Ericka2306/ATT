package mg.itu.att.ui.configuration

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.KeyboardType
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.CaseACocher
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.SelecteurRegion
import mg.itu.att.ui.communs.TexteErreur

/** Les centres d'examen, toujours rattachés à une région. */
@Composable
fun EcranCentres(viewModel: ConfigurationViewModel, onNouveau: () -> Unit, onOuvrir: (Int) -> Unit, onRetour: () -> Unit) {
    val centres by viewModel.centres.collectAsState()
    EcranStandard(titre = "Centres d'examen", onRetour = onRetour, iconeAction = Icons.Filled.Add, descriptionAction = "Nouveau centre", onAction = onNouveau) {
        Text("${centres.size} centre(s)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (centres.isEmpty()) Text("Aucun centre. Il en faut au moins un pour créer une session.", style = MaterialTheme.typography.bodyLarge)
        LazyColumn {
            items(centres) { l ->
                val c = l.centre
                CarteIcone(
                    icone = Icons.Filled.Place,
                    titre = c.nom + if (c.actif) "" else " (inactif)",
                    description = "${l.nomRegion} · ${c.adresse}" + (c.capaciteParDefaut?.let { " · capacité $it" } ?: ""),
                    onClick = { onOuvrir(c.id) },
                    couleurPastille = if (c.actif) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

/** Création ou modification d'un centre. */
@Composable
fun EcranFormulaireCentre(viewModel: ConfigurationViewModel, centreId: Int?, onEnregistre: () -> Unit, onRetour: () -> Unit) {
    viewModel.preparerCentre(centreId)
    val f by viewModel.formulaireCentre.collectAsState()
    EcranStandard(titre = if (centreId == null) "Nouveau centre" else "Modifier le centre", onRetour = onRetour, defilant = true) {
        ChampTexte(f.nom, { v -> viewModel.modifierCentre { it.copy(nom = v) } }, "Nom *")
        SelecteurRegion(f.regions, f.regionId, { v -> viewModel.modifierCentre { it.copy(regionId = v) } })
        ChampTexte(f.adresse, { v -> viewModel.modifierCentre { it.copy(adresse = v) } }, "Adresse *", uneLigne = false)
        ChampTexte(f.capacite, { v -> viewModel.modifierCentre { it.copy(capacite = v) } }, "Capacité par défaut d'une session", clavier = KeyboardType.Number)
        CaseACocher(f.actif, { v -> viewModel.modifierCentre { it.copy(actif = v) } }, "Centre actif")
        TexteErreur(f.erreur)
        BoutonPrincipal(if (centreId == null) "Créer le centre" else "Enregistrer", { viewModel.enregistrerCentre(onSucces = onEnregistre) })
    }
}
