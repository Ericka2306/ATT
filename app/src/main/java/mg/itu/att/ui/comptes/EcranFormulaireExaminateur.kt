package mg.itu.att.ui.comptes

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import mg.itu.att.metier.ValidationCompte
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.EncartInfo
import mg.itu.att.ui.communs.SelecteurRegion
import mg.itu.att.ui.communs.TexteErreur
import mg.itu.att.ui.communs.TitreSection

/**
 * Création d'un examinateur avec son compte (`examinateurId == null`), ou modification de sa fiche.
 * L'examinateur n'est jamais affecté à un candidat à l'avance (règle R5) : il se déclare à la saisie.
 */
@Composable
fun EcranFormulaireExaminateur(viewModel: ComptesViewModel, examinateurId: Int?, onEnregistre: () -> Unit, onRetour: () -> Unit) {
    viewModel.preparerExaminateur(examinateurId)
    val f by viewModel.formulaireExaminateur.collectAsState()
    val creation = examinateurId == null

    EcranStandard(titre = if (creation) "Nouvel examinateur" else "Modifier l'examinateur", onRetour = onRetour, defilant = true) {
        ChampTexte(f.nom, { v -> viewModel.modifierExaminateur { it.copy(nom = v) } }, "Nom *")
        ChampTexte(f.matricule, { v -> viewModel.modifierExaminateur { it.copy(matricule = v) } }, "Matricule (à confirmer)")
        SelecteurRegion(f.regions, f.regionId, { v -> viewModel.modifierExaminateur { it.copy(regionId = v) } }, avecToutes = true, verrouille = f.regionVerrouillee)
        if (creation) {
            TitreSection("Compte de connexion")
            EncartInfo("Communiquez l'identifiant et le mot de passe initial à l'examinateur ; il pourra le changer.")
            ChampTexte(f.identifiant, { v -> viewModel.modifierExaminateur { it.copy(identifiant = v) } }, "Identifiant *", aide = "${ValidationCompte.LONGUEUR_MIN_IDENTIFIANT} caractères minimum")
            ChampTexte(f.motDePasse, { v -> viewModel.modifierExaminateur { it.copy(motDePasse = v) } }, "Mot de passe initial *", motDePasse = true, aide = "${ValidationCompte.LONGUEUR_MIN_MOT_DE_PASSE} caractères minimum")
        }
        TexteErreur(f.erreur)
        BoutonPrincipal(if (creation) "Créer l'examinateur et son compte" else "Enregistrer", { viewModel.enregistrerExaminateur(onSucces = onEnregistre) }, actif = !f.enCours)
        if (!creation) {
            OutlinedButton(onClick = { viewModel.basculerExaminateur(f.id); onEnregistre() }) { Text(if (f.actif) "Désactiver l'examinateur et son compte" else "Réactiver l'examinateur et son compte") }
        }
    }
}
