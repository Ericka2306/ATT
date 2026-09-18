package mg.itu.att.ui.configuration

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.EncartInfo
import mg.itu.att.ui.communs.TonEncart
import mg.itu.att.ui.communs.PastilleStatut

/** Menu de la configuration (UC02, Super Admin). Les questions et critères se gèrent depuis chaque épreuve. */
@Composable
fun EcranConfiguration(onCategories: () -> Unit, onRegles: () -> Unit, onCentres: () -> Unit, onRetour: () -> Unit) {
    EcranStandard(titre = "Configuration", onRetour = onRetour) {
        EncartInfo("Les valeurs marquées « à confirmer » sont des exemples : aucune n'est une règle officielle de l'ATT tant qu'elle n'a pas été validée.", TonEncart.AVERTISSEMENT)
        CarteIcone(Icons.Filled.List, "Catégories et épreuves", "Catégories de permis, épreuves, barèmes, questions, critères", onCategories)
        CarteIcone(Icons.Filled.Settings, "Règles", "Nombre de passages, délais, retards, capacités, pièces du dossier…", onRegles)
        CarteIcone(Icons.Filled.Place, "Centres d'examen", "Centres par région, capacité par défaut", onCentres)
    }
}

/** Badge « À confirmer » (ambre) affiché sur toute valeur d'exemple. */
@Composable
fun PastilleAConfirmer() = PastilleStatut("À confirmer", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
