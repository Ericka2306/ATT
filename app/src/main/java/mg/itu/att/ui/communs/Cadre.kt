package mg.itu.att.ui.communs

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Composants communs à tous les écrans (docs/HORS_COURS.md n° 7 et n° 14).
 *
 * `Scaffold` + `TopAppBar` = un `Column` Material avec des emplacements nommés :
 * la barre de titre en haut (qui gère aussi la barre d'état du téléphone), le contenu dessous.
 * Tous les écrans passent par [EcranStandard] pour avoir le même aspect et le même code.
 *
 * Refonte du 18/09/2026 : barre claire (titre sombre, icônes bleues), cartes à bord fin et coins arrondis,
 * chevron sur les cartes cliquables, états vides illustrés. Aucune signature n'a changé : les écrans sont intacts.
 */

/** La barre de titre commune : fond clair, titre affirmé, flèche retour et action à droite en bleu. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarreTitre(titre: String, onRetour: (() -> Unit)?, iconeAction: ImageVector?, descriptionAction: String?, onAction: (() -> Unit)?) {
    TopAppBar(
        title = { Text(titre, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

/**
 * Cadre standard d'un écran : barre de titre, flèche « retour » facultative,
 * icône d'action facultative à droite, puis le contenu dans une colonne avec marges de 16 dp.
 * @param defilant vrai pour un formulaire (le contenu défile quand le clavier s'ouvre) ; faux pour une `LazyColumn`.
 */
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
        topBar = { BarreTitre(titre, onRetour, iconeAction, descriptionAction, onAction) },
    ) { marges ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(marges)
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .let { if (defilant) it.verticalScroll(rememberScrollState()) else it },
            content = contenu,
        )
    }
}

/** Même cadre, mais le contenu gère lui-même ses marges (`PaddingValues`) : pour les bandeaux pleine largeur. */
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
        topBar = { BarreTitre(titre, onRetour, iconeAction, descriptionAction, onAction) },
        content = contenu,
    )
}

/** Icône de déconnexion, réutilisée par l'accueil. */
val IconeDeconnexion: ImageVector = Icons.AutoMirrored.Filled.ExitToApp

/** Message d'erreur dans un encart rouge pâle, ou rien si `message` est null (le null se gère jusque dans l'UI, cours S5). */
@Composable
fun TexteErreur(message: String?) {
    if (message != null) {
        Text(
            message,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.small)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        )
    }
}

/** Ton d'un encart d'information : neutre (bleu pâle), avertissement (ambre), succès (vert). */
enum class TonEncart { INFO, AVERTISSEMENT, SUCCES }

/**
 * Encart d'information : une phrase d'explication dans un cadre pastel arrondi, avec une icône à gauche.
 * Remplace les paragraphes gris posés à même l'écran (notices, rappels de règles, aides).
 */
@Composable
fun EncartInfo(texte: String, ton: TonEncart = TonEncart.INFO, icone: ImageVector = Icons.Filled.Info) {
    val (fond, encre) = when (ton) {
        TonEncart.INFO -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        TonEncart.AVERTISSEMENT -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        TonEncart.SUCCES -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(fond, MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icone, contentDescription = null, tint = encre, modifier = Modifier.size(22.dp).padding(top = 1.dp))
        Spacer(Modifier.width(12.dp))
        Text(texte, style = MaterialTheme.typography.bodyMedium, color = encre, modifier = Modifier.weight(1f))
    }
}

/** Titre de section dans un écran. */
@Composable
fun TitreSection(texte: String) {
    Spacer(Modifier.height(22.dp))
    Text(texte, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 2.dp))
    Spacer(Modifier.height(10.dp))
}

/** Une ligne « libellé / valeur » d'une fiche : libellé discret à gauche, valeur lisible à droite. */
@Composable
fun LigneInfo(libelle: String, valeur: String) {
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
        Text(
            libelle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(116.dp).padding(top = 1.dp),
        )
        Text(valeur, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

/** Le bord fin commun aux cartes : plus léger qu'une ombre, plus net sur un fond clair. */
@Composable
fun bordCarte() = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

/** Carte blanche standard contenant une fiche. */
@Composable
fun CarteFiche(contenu: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = bordCarte(),
    ) {
        Column(Modifier.padding(18.dp), content = contenu)
    }
}

/** Petite pastille de statut (« Validé », « Soumis »…) colorée, avec un point de couleur. */
@Composable
fun PastilleStatut(texte: String, couleurFond: Color, couleurTexte: Color) {
    Row(
        Modifier
            .background(couleurFond, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).background(couleurTexte, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(texte, style = MaterialTheme.typography.labelMedium, color = couleurTexte, maxLines = 1)
    }
}

/**
 * Carte de menu ou de liste : une pastille colorée avec une icône, un titre, une description, un chevron.
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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = bordCarte(),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(48.dp)
                    .background(couleurPastille, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icone, contentDescription = null, tint = couleurIcone, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(titre, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (description.isNotBlank()) {
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                // La pastille de statut vient sous le texte : elle ne rogne jamais le titre.
                if (complement != null) {
                    Spacer(Modifier.height(8.dp))
                    complement()
                }
            }
            Spacer(Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

/** État vide d'une liste : une icône dans un rond, un titre, une phrase d'aide. Centré, discret. */
@Composable
fun EtatVide(icone: ImageVector, titre: String, texte: String? = null) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(titre, style = MaterialTheme.typography.titleMedium)
        if (texte != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                texte,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}

/** Écran générique pour une fonctionnalité pas encore développée : garde le menu navigable. */
@Composable
fun EcranAVenir(libelle: String, onRetour: () -> Unit) {
    EcranStandard(titre = libelle, onRetour = onRetour) {
        EtatVide(Icons.Filled.Build, "En construction", "Cette fonctionnalité sera développée dans une prochaine étape du plan.")
        Button(onClick = onRetour, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Retour à l'accueil") }
    }
}
