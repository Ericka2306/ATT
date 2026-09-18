package mg.itu.att.ui.candidats

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.att.data.PieceDossier
import mg.itu.att.data.StatutDossier
import mg.itu.att.metier.PiecesJointes
import mg.itu.att.metier.PiecesJointes.Verification
import mg.itu.att.metier.ReglesDossier
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.communs.LigneActions
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.CarteFiche
import mg.itu.att.ui.communs.CaseACocher
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.HistoriqueVide
import mg.itu.att.ui.communs.LigneHistorique
import mg.itu.att.ui.communs.LigneInfo
import mg.itu.att.ui.communs.TexteErreur
import mg.itu.att.ui.communs.TitreSection
import mg.itu.att.ui.communs.ouTiret

/**
 * Un dossier (UC04 soumission, UC05 décision) : pièces à cocher, avec pour chacune un fichier joint facultatif
 * (photo ou PDF, D2b), soumission par l'auto-école ou l'ATT, décision de l'ATT (valider / incomplet / refuser
 * avec motif), historique.
 */
@Composable
fun EcranDossier(viewModel: CandidatsViewModel, dossierId: Int, onOuvrirPiece: (Int) -> Unit, onRetour: () -> Unit) {
    viewModel.afficherDossier(dossierId)
    val etat by viewModel.dossier.collectAsState()
    val saisie by viewModel.saisieDossier.collectAsState()
    val d = etat.dossier
    // Le téléphone répond plus tard (docs/HORS_COURS.md n° 28) : la réponse remonte au ViewModel, qui sait pour quelle pièce.
    val choixFichier = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { viewModel.fichierChoisi(it) }
    val prisePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { viewModel.photoPrise(it) }

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
            item {
                Text(
                    if (etat.peutJoindre) "Joindre une photo ou un PDF est facultatif : l'ATT vérifie alors la pièce sans attendre le dossier papier."
                    else "Une pièce sans fichier se vérifie sur le dossier papier apporté par l'auto-école.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(etat.pieces) { piece ->
                // Une pièce avec un fichier est fournie : pour la décocher, retirer d'abord le fichier.
                CaseACocher(piece.fournie, { viewModel.cocherPiece(piece, it) }, piece.typePiece, actif = etat.peutSoumettre && piece.fichier == null)
                LignePieceJointe(
                    piece = piece,
                    peutJoindre = etat.peutJoindre,
                    onChoisir = { viewModel.preparerFichier(piece.id); choixFichier.launch(arrayOf("image/*", "application/pdf")) },
                    onPhoto = { prisePhoto.launch(viewModel.preparerPhoto(piece.id)) },
                    onOuvrir = { onOuvrirPiece(piece.id) },
                    onRetirer = { viewModel.retirerFichier(piece.id) },
                )
            }
            item {
                TexteErreur(saisie.erreur)
                if (etat.peutSoumettre) {
                    BoutonPrincipal(
                        texte = if (d.statut == StatutDossier.INCOMPLET) "Soumettre à nouveau à l'ATT" else "Soumettre à l'ATT",
                        onClick = { viewModel.soumettre(d.id) },
                    )
                }
                if (etat.peutDecider) {
                    TitreSection("Décision de l'ATT")
                    ChampTexte(saisie.motif, viewModel::changerMotif, "Motif", uneLigne = false, aide = "obligatoire si incomplet ou refusé")
                    LigneActions {
                        Button(onClick = { viewModel.decider(d.id, StatutDossier.VALIDE) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("Valider") }
                        OutlinedButton(onClick = { viewModel.decider(d.id, StatutDossier.INCOMPLET) }) { Text("Incomplet") }
                        OutlinedButton(onClick = { viewModel.decider(d.id, StatutDossier.REFUSE) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Refuser") }
                    }
                }
                TitreSection("Historique")
                if (etat.historique.isEmpty()) HistoriqueVide()
            }
            items(etat.historique) { LigneHistorique(it) }
        }
    }
}

/** Sous une pièce : comment l'ATT la vérifiera, et les actions sur son fichier joint. */
@Composable
private fun LignePieceJointe(
    piece: PieceDossier,
    peutJoindre: Boolean,
    onChoisir: () -> Unit,
    onPhoto: () -> Unit,
    onOuvrir: () -> Unit,
    onRetirer: () -> Unit,
) {
    val verification = PiecesJointes.verification(piece)
    Column(Modifier.padding(start = 40.dp, bottom = 6.dp)) {
        val texte = when (verification) {
            Verification.FICHIER_JOINT -> if (piece.fichier?.let(PiecesJointes::estPdf) == true) "PDF joint" else "Photo jointe"
            Verification.SUR_PAPIER -> "Sans fichier : vérification sur le dossier papier"
            Verification.MANQUANTE -> "Pièce non fournie"
        }
        Text(
            texte,
            style = MaterialTheme.typography.bodySmall,
            color = if (verification == Verification.FICHIER_JOINT) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LigneActions {
            if (verification == Verification.FICHIER_JOINT) TextButton(onClick = onOuvrir) { Text("Ouvrir") }
            if (peutJoindre) {
                TextButton(onClick = onChoisir) { Text(if (piece.fichier == null) "Joindre un fichier" else "Remplacer") }
                TextButton(onClick = onPhoto) { Text("Prendre une photo") }
                if (piece.fichier != null) TextButton(onClick = onRetirer) { Text("Retirer", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
