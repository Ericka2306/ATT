package mg.itu.att.ui.historique

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.att.metier.FiltresHistorique
import mg.itu.att.metier.FiltresHistorique.Periode
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.Option
import mg.itu.att.ui.communs.SelecteurChoix

/**
 * Historique des modifications (UC14) : filtres par période (`FilterChip`, cours S7), par objet et par utilisateur ;
 * un clic sur une ligne ouvre la fiche de l'objet quand elle existe ([onOuvrir] rend faux sinon).
 */
@Composable
fun EcranHistorique(
    viewModel: HistoriqueViewModel,
    peutOuvrir: (entite: String) -> Boolean,
    onOuvrir: (entite: String, entiteId: Int) -> Unit,
    onRetour: () -> Unit,
) {
    val etat by viewModel.uiState.collectAsState()

    EcranStandard(titre = "Historique", onRetour = onRetour) {
        if (!etat.autorise) {
            Text("L'historique est réservé à l'administration ATT.", style = MaterialTheme.typography.bodyLarge)
            return@EcranStandard
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Periode.entries.forEach { p ->
                FilterChip(
                    selected = etat.filtres.periode == p, onClick = { viewModel.choisirPeriode(p) },
                    label = { Text(when (p) { Periode.TOUT -> "Tout"; Periode.AUJOURD_HUI -> "Aujourd'hui"; Periode.SEPT_JOURS -> "7 jours"; Periode.TRENTE_JOURS -> "30 jours" }) },
                )
            }
        }
        // Le filtre « Objet » porte sur un nom d'entité : l'identifiant de l'option est sa position dans la liste.
        SelecteurChoix(
            prefixe = "Objet",
            options = etat.entites.mapIndexed { i, e -> Option(i, FiltresHistorique.libelleEntite(e)) },
            choixId = etat.entites.indexOf(etat.filtres.entite).takeIf { it >= 0 },
            onChoix = { i -> viewModel.choisirEntite(i?.let { etat.entites.getOrNull(it) }) },
            avecTous = true,
        )
        SelecteurChoix("Utilisateur", etat.utilisateurs, etat.filtres.utilisateurId, viewModel::choisirUtilisateur, avecTous = true)
        Text("${etat.lignes.size} modification(s) sur ${etat.total}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        if (etat.lignes.isEmpty()) Text("Aucune modification pour ces filtres.", style = MaterialTheme.typography.bodyLarge)
        LazyColumn {
            items(etat.lignes) { l ->
                val h = l.historique
                val detail = listOfNotNull(h.nouvelleValeur ?: h.ancienneValeur, h.motif?.let { "motif : $it" }).joinToString(" · ")
                CarteIcone(
                    icone = Icons.Filled.Info,
                    titre = "${formatDate(h.dateHeure)} — ${h.action}",
                    description = "${l.libelleEntite} n° ${h.entiteId} · par ${l.nomUtilisateur}" + if (detail.isNotBlank()) "\n$detail" else "",
                    onClick = { if (peutOuvrir(h.entite)) onOuvrir(h.entite, h.entiteId) },
                )
            }
        }
    }
}
