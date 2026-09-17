package mg.itu.att.ui.candidats

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.att.metier.ValidationCompte
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.TexteErreur

/**
 * Création du compte de connexion d'un candidat (UC12, cadrage §10 : accès numérique facultatif).
 * Même formulaire que le compte d'une auto-école ; le mot de passe est haché, jamais stocké en clair.
 */
@Composable
fun EcranFormulaireCompteCandidat(
    viewModel: CandidatsViewModel,
    candidatId: Int,
    onCree: () -> Unit,
    onRetour: () -> Unit,
) {
    val c by viewModel.compte.collectAsState()

    EcranStandard(titre = "Compte du candidat", onRetour = onRetour, defilant = true) {
        Text(
            "Ce compte permettra au candidat de consulter son parcours (dossier, convocations, passages) depuis l'application. " +
                "Il est facultatif : sans compte, l'auto-école et l'ATT lui communiquent les informations. Communiquez-lui l'identifiant et le mot de passe initial.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        ChampTexte(c.identifiant, { v -> viewModel.modifierCompte { it.copy(identifiant = v) } }, "Identifiant (${ValidationCompte.LONGUEUR_MIN_IDENTIFIANT} caractères min.)")
        ChampTexte(c.motDePasse, { v -> viewModel.modifierCompte { it.copy(motDePasse = v) } }, "Mot de passe initial (${ValidationCompte.LONGUEUR_MIN_MOT_DE_PASSE} caractères min.)", motDePasse = true)
        TexteErreur(c.erreur)
        BoutonPrincipal("Créer le compte", { viewModel.creerCompte(candidatId, onSucces = onCree) }, actif = !c.enCours)
    }
}
