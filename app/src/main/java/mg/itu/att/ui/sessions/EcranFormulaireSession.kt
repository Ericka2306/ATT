package mg.itu.att.ui.sessions

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import mg.itu.att.ui.communs.TitreSection

/**
 * Création d'une session (UC06). Durée, marge et capacités sont préremplies depuis les règles configurables ;
 * l'aperçu des créneaux se recalcule à chaque saisie (fonction pure `genererCreneaux`).
 */
@Composable
fun EcranFormulaireSession(viewModel: SessionsViewModel, onCree: (Int) -> Unit, onRetour: () -> Unit) {
    viewModel.preparerFormulaire()
    val f by viewModel.formulaire.collectAsState()

    EcranStandard(titre = "Nouvelle session", onRetour = onRetour, defilant = true) {
        SelecteurChoix("Catégorie", f.categories.map { Option(it.id, "${it.code} — ${it.libelle}") }, f.categorieId, viewModel::choisirCategorie, libelleVide = "à choisir *")
        SelecteurChoix("Épreuve", f.epreuves.map { Option(it.id, it.libelle) }, f.typeEpreuveId, { v -> viewModel.modifierFormulaire { it.copy(typeEpreuveId = v) } }, libelleVide = if (f.categorieId == null) "choisir d'abord la catégorie" else "à choisir *")
        SelecteurChoix("Centre", f.centres.map { Option(it.id, it.nom) }, f.centreId, viewModel::choisirCentre, libelleVide = if (f.centres.isEmpty()) "aucun centre actif (Configuration → Centres)" else "à choisir *")
        ChampTexte(f.date, viewModel::changerDate, "Date * (AAAA-MM-JJ)", clavier = KeyboardType.Number)
        ChampTexte(f.heureConvocation, { v -> viewModel.modifierFormulaire { it.copy(heureConvocation = v) } }, "Heure de convocation * (HH:MM)")
        ChampTexte(f.capacite, { v -> viewModel.modifierFormulaire { it.copy(capacite = v) } }, "Capacité de la session *", clavier = KeyboardType.Number)
        ChampTexte(f.capaciteCreneau, { v -> viewModel.modifierFormulaire { it.copy(capaciteCreneau = v) } }, "Candidats par créneau *", clavier = KeyboardType.Number, aide = "1 = créneau individuel")
        ChampTexte(f.dureeMin, { v -> viewModel.modifierFormulaire { it.copy(dureeMin = v) } }, "Durée d'un créneau (min) *", clavier = KeyboardType.Number)
        ChampTexte(f.margeMin, { v -> viewModel.modifierFormulaire { it.copy(margeMin = v) } }, "Marge entre créneaux (min) *", clavier = KeyboardType.Number)
        f.avertissement?.let { Text(it, color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium) }

        val apercu = f.apercuCreneaux
        if (apercu.isNotEmpty()) {
            TitreSection("Aperçu : ${apercu.size} créneau(x), de ${apercu.first().heureDebut} à ${apercu.last().heureFinEstimee}")
            apercu.take(6).forEach { c -> Text("${c.ordre}. ${c.heureDebut} – ${c.heureFinEstimee} : ${c.capacite} place(s)", style = MaterialTheme.typography.bodyMedium) }
            if (apercu.size > 6) Text("… et ${apercu.size - 6} autre(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TexteErreur(f.erreur)
        BoutonPrincipal("Créer la session et ses créneaux", { viewModel.enregistrer(onSucces = onCree) }, actif = !f.enCours)
    }
}
