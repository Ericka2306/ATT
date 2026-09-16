package mg.itu.att.ui.communs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Composants communs à tous les écrans (docs/HORS_COURS.md n° 7 et n° 14).
 *
 * `Scaffold` + `TopAppBar` = un `Column` Material avec des emplacements nommés :
 * la barre de titre en haut (qui gère aussi la barre d'état du téléphone), le contenu dessous.
 * Tous les écrans passent par [EcranStandard] pour avoir le même aspect et le même code.
 */

/**
 * Cadre standard d'un écran : barre de titre bleue, flèche « retour » facultative,
 * icône d'action facultative à droite, puis le contenu dans une colonne avec marges de 16 dp.
 * @param defilant vrai pour un formulaire (le contenu défile quand le clavier s'ouvre) ; faux pour une `LazyColumn`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcranStandard(
    titre: String,
    onRetour: (() -> Unit)? = null,
    iconeAction: ImageVector? = null,
    descriptionAction: String? = null,
    onAction: (() -> Unit)? = null,
    defilant: Boolean = false,
    contenu: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(titre, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    if (onRetour != null) {
                        IconButton(onClick = onRetour) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    }
                },
                actions = {
                    if (iconeAction != null && onAction != null) {
                        IconButton(onClick = onAction) {
                            Icon(iconeAction, contentDescription = descriptionAction)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { marges ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(marges)
                .padding(16.dp)
                .let { if (defilant) it.verticalScroll(rememberScrollState()) else it },
            content = contenu,
        )
    }
}

/** Même cadre, mais le contenu gère lui-même ses marges (`PaddingValues`) : pour les bandeaux pleine largeur. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcranAvecBarre(
    titre: String,
    onRetour: (() -> Unit)? = null,
    iconeAction: ImageVector? = null,
    descriptionAction: String? = null,
    onAction: (() -> Unit)? = null,
    contenu: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(titre, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    if (onRetour != null) {
                        IconButton(onClick = onRetour) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") }
                    }
                },
                actions = {
                    if (iconeAction != null && onAction != null) {
                        IconButton(onClick = onAction) { Icon(iconeAction, contentDescription = descriptionAction) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        content = contenu,
    )
}

/** Icône de déconnexion, réutilisée par l'accueil. */
val IconeDeconnexion: ImageVector = Icons.AutoMirrored.Filled.ExitToApp

/** Message d'erreur en rouge, ou rien si `message` est null (le null se gère jusque dans l'UI, cours S5). */
@Composable
fun TexteErreur(message: String?) {
    if (message != null) {
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Titre de section dans un écran. */
@Composable
fun TitreSection(texte: String) {
    Spacer(Modifier.height(20.dp))
    Text(texte, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(6.dp))
}

/** Une ligne « libellé : valeur » d'une fiche. */
@Composable
fun LigneInfo(libelle: String, valeur: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text(
            libelle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp),
        )
        Text(valeur, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Carte blanche standard contenant une fiche. */
@Composable
fun CarteFiche(contenu: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), content = contenu)
    }
}

/** Petite pastille de statut (« Validé », « Soumis »…) colorée. */
@Composable
fun PastilleStatut(texte: String, couleurFond: Color, couleurTexte: Color) {
    Text(
        texte,
        style = MaterialTheme.typography.labelMedium,
        color = couleurTexte,
        modifier = Modifier
            .background(couleurFond, MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/**
 * Carte de menu ou de liste : une pastille colorée avec une icône, un titre, une description.
 * Même principe que la `ProduitCard` du cours, habillée.
 */
@Composable
fun CarteIcone(
    icone: ImageVector,
    titre: String,
    description: String,
    onClick: () -> Unit,
    couleurPastille: Color = MaterialTheme.colorScheme.primaryContainer,
    couleurIcone: Color = MaterialTheme.colorScheme.primary,
    complement: (@Composable () -> Unit)? = null,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .background(couleurPastille, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icone, contentDescription = null, tint = couleurIcone)
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(titre, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (complement != null) complement()
        }
    }
}

/** Écran générique pour une fonctionnalité pas encore développée : garde le menu navigable. */
@Composable
fun EcranAVenir(libelle: String, onRetour: () -> Unit) {
    EcranStandard(titre = libelle, onRetour = onRetour) {
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Build, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
        }
        Spacer(Modifier.height(16.dp))
        Text("En construction", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(
            "Cette fonctionnalité sera développée dans une prochaine étape du plan.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(onClick = onRetour, modifier = Modifier.padding(top = 24.dp)) { Text("Retour à l'accueil") }
    }
}
