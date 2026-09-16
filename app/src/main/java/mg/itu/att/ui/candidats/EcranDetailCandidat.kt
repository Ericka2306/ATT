package mg.itu.att.ui.candidats

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.communs.CarteFiche
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.LigneHistorique
import mg.itu.att.ui.communs.LigneInfo
import mg.itu.att.ui.communs.Option
import mg.itu.att.ui.communs.SelecteurChoix
import mg.itu.att.ui.communs.TexteErreur
import mg.itu.att.ui.communs.TitreSection
import mg.itu.att.ui.communs.ouTiret

/** Fiche d'un candidat (UC04, UC12) : identité, dossiers par catégorie, ouverture d'un dossier, historique. */
@Composable
fun EcranDetailCandidat(
    viewModel: CandidatsViewModel,
    candidatId: Int,
    onModifier: () -> Unit,
    onOuvrirDossier: (Int) -> Unit,
    onRetour: () -> Unit,
) {
    viewModel.afficherDetail(candidatId)
    val etat by viewModel.detail.collectAsState()
    val c = etat.candidat

    EcranStandard(
        titre = c?.let { "${it.nom} ${it.prenom}" } ?: "Candidat",
        onRetour = onRetour,
        iconeAction = Icons.Filled.Edit,
        descriptionAction = "Modifier",
        onAction = onModifier,
    ) {
        if (c == null) {
            Text("Candidat introuvable.")
            return@EcranStandard
        }
        LazyColumn {
            item {
                CarteFiche {
                    LigneInfo("Naissance", formatDate(c.dateNaissance))
                    LigneInfo("Auto-école", etat.nomAutoEcole)
                    LigneInfo("CIN", c.cin.ouTiret())
                    LigneInfo("Téléphone", c.telephone.ouTiret())
                    LigneInfo("Adresse", c.adresse.ouTiret())
                }
                TitreSection("Dossiers")
                if (etat.dossiers.isEmpty()) {
                    Text("Aucun dossier. Choisissez une catégorie puis ouvrez un dossier.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(etat.dossiers) { ligne ->
                CarteIcone(
                    icone = Icons.Filled.List,
                    titre = "Permis ${ligne.codeCategorie} — dossier n° ${ligne.dossier.id}",
                    description = ligne.libelleCategorie + (ligne.dossier.motif?.let { " · $it" } ?: ""),
                    onClick = { onOuvrirDossier(ligne.dossier.id) },
                    complement = { PastilleDossier(ligne.dossier.statut) },
                )
            }
            item {
                Spacer(Modifier.height(12.dp))
                SelecteurChoix("Catégorie", etat.categories.map { Option(it.id, "${it.code} — ${it.libelle}") }, etat.categorieChoisieId, viewModel::choisirCategorie, libelleVide = "à choisir")
                TexteErreur(etat.erreur)
                Button(onClick = { viewModel.ouvrirDossier(c.id, onSucces = onOuvrirDossier) }) { Text("Ouvrir un dossier") }
                TitreSection("Historique")
            }
            items(etat.historique) { LigneHistorique(it) }
        }
    }
}
