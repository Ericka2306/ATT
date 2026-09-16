package mg.itu.att.ui.candidats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.KeyboardType
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.Option
import mg.itu.att.ui.communs.SelecteurChoix
import mg.itu.att.ui.communs.TexteErreur

/** Création (`candidatId == null`) ou modification d'un candidat (UC04). */
@Composable
fun EcranFormulaireCandidat(
    viewModel: CandidatsViewModel,
    candidatId: Int?,
    onEnregistre: (Int) -> Unit,
    onRetour: () -> Unit,
) {
    viewModel.preparerFormulaire(candidatId)
    val f by viewModel.formulaire.collectAsState()

    EcranStandard(titre = if (candidatId == null) "Nouveau candidat" else "Modifier le candidat", onRetour = onRetour, defilant = true) {
        ChampTexte(f.nom, { v -> viewModel.modifierFormulaire { it.copy(nom = v) } }, "Nom *")
        ChampTexte(f.prenom, { v -> viewModel.modifierFormulaire { it.copy(prenom = v) } }, "Prénom *")
        ChampTexte(f.dateNaissance, { v -> viewModel.modifierFormulaire { it.copy(dateNaissance = v) } }, "Date de naissance *", clavier = KeyboardType.Number, aide = "Format AAAA-MM-JJ, ex. 2004-05-17")
        SelecteurChoix("Auto-école", f.autoEcoles.map { Option(it.id, it.nom) }, f.autoEcoleId, { v -> viewModel.modifierFormulaire { it.copy(autoEcoleId = v) } }, libelleVide = "à choisir *", verrouille = f.autoEcoleVerrouillee)
        ChampTexte(f.cin, { v -> viewModel.modifierFormulaire { it.copy(cin = v) } }, "Numéro de CIN (à confirmer)", clavier = KeyboardType.Number)
        ChampTexte(f.telephone, { v -> viewModel.modifierFormulaire { it.copy(telephone = v) } }, "Téléphone", clavier = KeyboardType.Phone)
        ChampTexte(f.adresse, { v -> viewModel.modifierFormulaire { it.copy(adresse = v) } }, "Adresse", uneLigne = false)
        TexteErreur(f.erreur)
        BoutonPrincipal(
            texte = if (candidatId == null) "Créer le candidat" else "Enregistrer les modifications",
            onClick = { viewModel.enregistrer(onSucces = onEnregistre) },
            actif = !f.enCours,
        )
    }
}
