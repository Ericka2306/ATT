package mg.itu.att.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mg.itu.att.R

/**
 * Identité visuelle de l'application (docs/HORS_COURS.md n° 13 et n° 26).
 *
 * `MaterialTheme` du cours, avec une palette, des formes et une typographie fournies au lieu des valeurs par défaut :
 * - bleu institutionnel pour l'administration (ATT),
 * - vert de Madagascar pour ce qui est validé / réussi,
 * - ambre de signalisation routière pour les badges « À confirmer » et les avertissements,
 * - police « Plus Jakarta Sans » (licence OFL, crédit dans LISEZMOI.md), plus moderne que la police système.
 * Un seul mode (clair) : l'application est un outil de guichet, lisible en plein jour.
 *
 * Refonte visuelle du 18/09/2026 (dev 1 + Claude) : barre de titre claire, coins bien arrondis, marges généreuses,
 * titres affirmés. Les écrans n'ont pas changé : ils passent tous par les composants de `ui/communs`.
 */

// ---------- COULEURS ----------

val BleuATT = Color(0xFF0F4C81)
val BleuATTFonce = Color(0xFF0B2E4F)
val BleuATTClair = Color(0xFFDCE8F7)
val VertMadagascar = Color(0xFF1E8449)
val VertClair = Color(0xFFD5F0DF)
val Ambre = Color(0xFFC77700)
val AmbreClair = Color(0xFFFFE6BF)
val RougeMadagascar = Color(0xFFB3261E)
val RougeClair = Color(0xFFF9DEDC)
val Fond = Color(0xFFF4F6FA)
val Surface = Color(0xFFFFFFFF)
val SurfaceVariante = Color(0xFFEAEFF5)
val Contour = Color(0xFFCBD5E1)
val ContourLeger = Color(0xFFE6EBF2)
val TexteSombre = Color(0xFF141C26)
val TexteSecondaire = Color(0xFF5E6B7C)

/** Pastels des tuiles du menu et des icônes de liste (un ton par famille d'écrans). */
val PastelBleu = Color(0xFFDCE8F7)
val PastelVert = Color(0xFFD8F1E3)
val PastelAmbre = Color(0xFFFFEBCB)
val PastelViolet = Color(0xFFE7E0F8)
val PastelRose = Color(0xFFFBDDE6)
val PastelTurquoise = Color(0xFFD5F0F0)
val EncreViolet = Color(0xFF5B3FA6)
val EncreRose = Color(0xFFB0305B)
val EncreTurquoise = Color(0xFF0E7C7B)

private val PaletteATT = lightColorScheme(
    primary = BleuATT,
    onPrimary = Color.White,
    primaryContainer = BleuATTClair,
    onPrimaryContainer = BleuATTFonce,
    secondary = VertMadagascar,
    onSecondary = Color.White,
    secondaryContainer = VertClair,
    onSecondaryContainer = Color(0xFF0B3D1F),
    tertiary = Ambre,
    onTertiary = Color.White,
    tertiaryContainer = AmbreClair,
    onTertiaryContainer = Color(0xFF4A2C00),
    error = RougeMadagascar,
    onError = Color.White,
    errorContainer = RougeClair,
    onErrorContainer = Color(0xFF5C1410),
    background = Fond,
    onBackground = TexteSombre,
    surface = Surface,
    onSurface = TexteSombre,
    surfaceVariant = SurfaceVariante,
    onSurfaceVariant = TexteSecondaire,
    outline = Contour,
    outlineVariant = ContourLeger,
)

// ---------- FORMES ----------

/**
 * Les formes sont lues par les composants Material eux-mêmes : `extraSmall` pour les champs de texte,
 * `medium` pour les cartes, `large` pour les grandes surfaces. Tout s'arrondit d'un seul coup.
 */
private val FormesATT = Shapes(
    extraSmall = RoundedCornerShape(14.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

// ---------- TYPOGRAPHIE ----------

/**
 * Police variable « Plus Jakarta Sans » : un seul fichier, chaque graisse est demandée par `FontVariation`
 * (minSdk 26 le permet). Tailles un peu en dessous de Material par défaut : le dev 1 veut des textes compacts, l'écran de démo est petit.
 */
private fun police(poids: FontWeight) = Font(R.font.plus_jakarta_sans, weight = poids, variationSettings = FontVariation.Settings(poids, FontStyle.Normal))

val PoliceATT = FontFamily(
    police(FontWeight.Normal),
    police(FontWeight.Medium),
    police(FontWeight.SemiBold),
    police(FontWeight.Bold),
    police(FontWeight.ExtraBold),
)

private fun style(poids: FontWeight, taille: Float, interligne: Int, espacement: Float = 0f) =
    TextStyle(fontFamily = PoliceATT, fontWeight = poids, fontSize = taille.sp, lineHeight = interligne.sp, letterSpacing = espacement.sp)

private val TypographieATT = Typography(
    headlineLarge = style(FontWeight.ExtraBold, 28f, 34, -0.5f),
    headlineMedium = style(FontWeight.Bold, 23f, 29, -0.3f),
    headlineSmall = style(FontWeight.Bold, 19f, 25),
    titleLarge = style(FontWeight.Bold, 18f, 24),
    titleMedium = style(FontWeight.SemiBold, 14f, 20, 0.1f),
    titleSmall = style(FontWeight.SemiBold, 13f, 18, 0.1f),
    bodyLarge = style(FontWeight.Normal, 14f, 20),
    bodyMedium = style(FontWeight.Normal, 12.5f, 18),
    bodySmall = style(FontWeight.Normal, 11.5f, 15),
    labelLarge = style(FontWeight.SemiBold, 14f, 19, 0.1f),
    labelMedium = style(FontWeight.SemiBold, 11.5f, 15, 0.2f),
    labelSmall = style(FontWeight.Medium, 10.5f, 14, 0.3f),
)

// ---------- LE THÈME ----------

/** À utiliser à la place de `MaterialTheme { }` dans MainActivity : même chose, avec nos couleurs, formes et polices. */
@Composable
fun ThemeATT(contenu: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PaletteATT,
        shapes = FormesATT,
        typography = TypographieATT,
        content = contenu,
    )
}
