package mg.itu.att.ui.candidats

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.att.data.StatutDossier
import mg.itu.att.metier.ReglesDossier
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.CarteFiche
import mg.itu.att.ui.communs.CaseACocher
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.LigneHistorique
import mg.itu.att.ui.communs.LigneInfo
import mg.itu.att.ui.communs.TexteErreur
import mg.itu.att.ui.communs.TitreSection
import mg.itu.att.ui.communs.ouTiret

/**
 * Un dossier (UC04 soumission, UC05 décision) : pièces à cocher, soumission par l'auto-école ou l'ATT,
 * décision de l'ATT (valider / incomplet / refuser avec motif), historique.
 */
@Composable
fun EcranDossier(viewModel: CandidatsViewModel, dossierId: Int, onRetour: () -> Unit) {
    viewModel.afficherDossier(dossierId)
    val etat by viewModel.dossier.collectAsState()
    val d = etat.dossier

    EcranStandard(titre = etat.categorie?.let { "Dossier permis ${it.code}" } ?: "Dossier", onRetour = onRetour) {
        if (d == null) {
            Text("Dossier introuvable.")
            return@EcranStandard
        }
        LazyColumn {
            item {
                CarteFiche {
                    Row { PastilleDossier(d.statut) }
                    Spacer(Modifier.height(8.dp))
                    LigneInfo("Candidat", etat.candidat?.let { "${it.nom} ${it.prenom}" } ?: "?")
                    LigneInfo("Naissance", etat.candidat?.dateNaissance?.let { formatDate(it) } ?: "?")
                    LigneInfo("Catégorie", etat.categorie?.let { "${it.code} — ${it.libelle}" } ?: "?")
                    LigneInfo("Soumis le", d.dateSoumission?.let { formatDate(it) }.ouTiret())
                    LigneInfo("Décision le", d.dateDecision?.let { formatDate(it) }.ouTiret())
                    if (d.motif != null) LigneInfo("Motif", d.motif)
                }
                TitreSection("Pièces du dossier" + if (ReglesDossier.dossierComplet(etat.pieces.map { it.fournie })) " — complet" else "")
            }
            items(etat.pieces) { piece ->
                CaseACocher(piece.fournie, { viewModel.cocherPiece(piece, it) }, piece.typePiece, actif = etat.peutSoumettre)
            }
            item {
                TexteErreur(etat.erreur)
                if (etat.peutSoumettre) {
                    BoutonPrincipal(
                        texte = if (d.statut == StatutDossier.INCOMPLET) "Soumettre à nouveau à l'ATT" else "Soumettre à l'ATT",
                        onClick = { viewModel.soumettre(d.id) },
                    )
                }
                if (etat.peutDecider) {
                    TitreSection("Décision de l'ATT")
                    ChampTexte(etat.motif, viewModel::changerMotif, "Motif (obligatoire si incomplet ou refusé)", uneLigne = false)
                    Row {
                        Button(onClick = { viewModel.decider(d.id, StatutDossier.VALIDE) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("Valider") }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = { viewModel.decider(d.id, StatutDossier.INCOMPLET) }) { Text("Incomplet") }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = { viewModel.decider(d.id, StatutDossier.REFUSE) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Refuser") }
                    }
                }
                TitreSection("Historique")
            }
            items(etat.historique) { LigneHistorique(it) }
        }
    }
}
