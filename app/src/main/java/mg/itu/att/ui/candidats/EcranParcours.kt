package mg.itu.att.ui.candidats

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import mg.itu.att.ui.communs.LigneInfo
import mg.itu.att.ui.communs.TitreSection

/**
 * « Mon parcours » (UC12) : ce que le candidat connecté voit de lui-même, en lecture seule —
 * identité, dossiers, inscriptions et convocations, passages d'épreuve. Même état que la fiche candidat,
 * limité par `ReglesConsultation.peutVoirCandidat` au candidat de la session.
 */
@Composable
fun EcranParcours(
    viewModel: CandidatsViewModel,
    candidatId: Int?,
    onOuvrirDossier: (Int) -> Unit,
    onRetour: () -> Unit,
) {
    if (candidatId != null) viewModel.afficherDetail(candidatId)
    val etat by viewModel.detail.collectAsState()
    val c = etat.candidat

    EcranStandard(titre = "Mon parcours", onRetour = onRetour) {
        if (candidatId == null || c == null) {
            Text("Aucun candidat n'est rattaché à ce compte. Adressez-vous à votre auto-école ou à l'ATT.", style = MaterialTheme.typography.bodyLarge)
            return@EcranStandard
        }
        LazyColumn {
            item {
                CarteFiche {
                    LigneInfo("Candidat", "${c.nom} ${c.prenom}")
                    LigneInfo("Naissance", formatDate(c.dateNaissance))
                    LigneInfo("Auto-école", etat.nomAutoEcole)
                }
                TitreSection("Dossiers")
                if (etat.dossiers.isEmpty()) Text("Aucun dossier déposé par votre auto-école.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(etat.dossiers) { ligne ->
                CarteIcone(
                    icone = Icons.AutoMirrored.Filled.List,
                    titre = "Permis ${ligne.codeCategorie} — dossier n° ${ligne.dossier.id}",
                    description = ligne.libelleCategorie + (ligne.dossier.motif?.let { " · $it" } ?: ""),
                    onClick = { onOuvrirDossier(ligne.dossier.id) },
                    complement = { PastilleDossier(ligne.dossier.statut) },
                )
            }
            sectionInscriptions(etat.inscriptions)
            sectionPassages(etat.passages)
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Les résultats sont communiqués après validation par l'ATT.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
