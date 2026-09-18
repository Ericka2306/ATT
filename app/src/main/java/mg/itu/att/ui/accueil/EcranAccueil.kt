package mg.itu.att.ui.accueil

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mg.itu.att.R
import mg.itu.att.metier.dateDuJour
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.communs.EcranAvecBarre
import mg.itu.att.ui.communs.IconeDeconnexion
import mg.itu.att.ui.communs.bordCarte
import mg.itu.att.ui.communs.libelle
import mg.itu.att.ui.connexion.SessionUtilisateur
import mg.itu.att.ui.theme.Ambre
import mg.itu.att.ui.theme.BleuATT
import mg.itu.att.ui.theme.BleuATTFonce
import mg.itu.att.ui.theme.EncreRose
import mg.itu.att.ui.theme.EncreTurquoise
import mg.itu.att.ui.theme.EncreViolet
import mg.itu.att.ui.theme.PastelAmbre
import mg.itu.att.ui.theme.PastelBleu
import mg.itu.att.ui.theme.PastelRose
import mg.itu.att.ui.theme.PastelTurquoise
import mg.itu.att.ui.theme.PastelVert
import mg.itu.att.ui.theme.PastelViolet
import mg.itu.att.ui.theme.VertMadagascar

/**
 * Accueil : le menu du rôle connecté (UC12). Une carte d'en-tête bleue (logo, nom, rôle, date), puis les
 * entrées du menu en grille de deux colonnes (`LazyVerticalGrid`, docs/HORS_COURS.md n° 25).
 * L'écran signale la route choisie par [onNaviguer].
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(marges),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(span = { GridItemSpan(2) }) { CarteBienvenue(session) }
            item(span = { GridItemSpan(2) }) {
                Text(
                    "Que voulez-vous faire ?",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 10.dp, start = 2.dp),
                )
            }
            items(menuPour(session.role)) { entree ->
                val (fond, encre) = couleursPour(entree.route)
                TuileMenu(iconePour(entree.route), entree.libelle, entree.description, fond, encre, onClick = { onNaviguer(entree.route) })
            }
        }
    }
}

/** L'en-tête de l'accueil : dégradé bleu, logo, salutation, rôle et date. */
@Composable
private fun CarteBienvenue(session: SessionUtilisateur) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(BleuATT, BleuATTFonce)), MaterialTheme.shapes.large)
            .padding(24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(R.drawable.ic_logo_att), contentDescription = null, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Bonjour,", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                Text(session.nom, style = MaterialTheme.typography.titleLarge, color = Color.White, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Text(
                    session.role.libelle(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.18f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                )
                Spacer(Modifier.height(6.dp))
                Text(formatDate(dateDuJour()), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f), maxLines = 1)
            }
        }
    }
}

/** Une tuile du menu : icône sur un carré pastel, titre, courte description. */
@Composable
private fun TuileMenu(icone: ImageVector, titre: String, description: String, fond: Color, encre: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = bordCarte(),
    ) {
        Column(Modifier.padding(16.dp).height(132.dp)) {
            Box(
                Modifier
                    .size(46.dp)
                    .background(fond, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icone, contentDescription = null, tint = encre, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(titre, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** La couleur d'une tuile du menu (fond pastel, encre) : une teinte par famille d'écrans. */
fun couleursPour(route: String): Pair<Color, Color> = when (route) {
    Routes.AUTO_ECOLES, Routes.CANDIDATS, Routes.DOSSIERS -> PastelBleu to BleuATT
    Routes.SESSIONS, Routes.EVALUATION -> PastelViolet to EncreViolet
    Routes.RESULTATS -> PastelVert to VertMadagascar
    Routes.CONFIGURATION, Routes.COMPTES, Routes.EXAMINATEURS -> PastelAmbre to Ambre
    Routes.HISTORIQUE, Routes.SYNCHRONISATION -> PastelTurquoise to EncreTurquoise
    Routes.PARCOURS, Routes.MOT_DE_PASSE -> PastelRose to EncreRose
    else -> PastelBleu to BleuATT
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
    Routes.SYNCHRONISATION -> Icons.Filled.Refresh
    Routes.PARCOURS -> Icons.Filled.AccountCircle
    else -> Icons.Filled.Star
}
