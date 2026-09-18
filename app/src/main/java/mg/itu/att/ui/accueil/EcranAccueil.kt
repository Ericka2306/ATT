package mg.itu.att.ui.accueil

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mg.itu.att.R
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.EcranAvecBarre
import mg.itu.att.ui.communs.IconeDeconnexion
import mg.itu.att.metier.dateDuJour
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.communs.libelle
import mg.itu.att.ui.connexion.SessionUtilisateur

/**
 * Accueil : le menu du rôle connecté (UC12). Un bandeau bleu avec le nom et le rôle,
 * puis les cartes du menu (une icône par route). L'écran signale la route choisie par [onNaviguer].
 */
@Composable
fun EcranAccueil(
    session: SessionUtilisateur,
    onNaviguer: (String) -> Unit,
    onDeconnexion: () -> Unit,
) {
    EcranAvecBarre(
        titre = "ATT",
        iconeAction = IconeDeconnexion,
        descriptionAction = "Déconnexion",
        onAction = onDeconnexion,
    ) { marges ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(marges),
        ) {
            // ---------- BANDEAU ----------
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_logo_att),
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                )
                Spacer(Modifier.size(14.dp))
                Column {
                    Text(
                        session.nom,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        session.role.libelle(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    )
                    Text(
                        formatDate(dateDuJour()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    )
                }
            }

            // ---------- MENU ----------
            LazyColumn(Modifier.padding(horizontal = 16.dp)) {
                item { Spacer(Modifier.height(12.dp)) }
                items(menuPour(session.role)) { entree ->
                    CarteIcone(
                        icone = iconePour(entree.route),
                        titre = entree.libelle,
                        description = entree.description,
                        onClick = { onNaviguer(entree.route) },
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

/** L'icône d'une route du menu (jeu d'icônes de base de Material, HORS_COURS n° 14). */
fun iconePour(route: String): ImageVector = when (route) {
    Routes.CONFIGURATION -> Icons.Filled.Settings
    Routes.COMPTES -> Icons.Filled.AccountCircle
    Routes.EXAMINATEURS -> Icons.Filled.Face
    Routes.MOT_DE_PASSE -> Icons.Filled.Lock
    Routes.AUTO_ECOLES -> Icons.Filled.Place
    Routes.CANDIDATS -> Icons.Filled.Person
    Routes.DOSSIERS -> Icons.AutoMirrored.Filled.List
    Routes.SESSIONS -> Icons.Filled.DateRange
    Routes.EVALUATION -> Icons.Filled.Build
    Routes.RESULTATS -> Icons.Filled.Done
    Routes.HISTORIQUE -> Icons.Filled.Info
    Routes.PARCOURS -> Icons.Filled.AccountCircle
    else -> Icons.Filled.Star
}
