package mg.itu.att.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Identité visuelle de l'application (docs/HORS_COURS.md n° 13).
 *
 * `MaterialTheme` du cours, avec une palette fournie au lieu de la palette par défaut :
 * - bleu institutionnel pour l'administration (ATT),
 * - vert de Madagascar pour ce qui est validé / réussi,
 * - ambre de signalisation routière pour les badges « À confirmer » et les avertissements.
 * Un seul mode (clair) : l'application est un outil de guichet, lisible en plein jour.
 */

// ---------- COULEURS ----------

val BleuATT = Color(0xFF0F4C81)
val BleuATTFonce = Color(0xFF0B2E4F)
val BleuATTClair = Color(0xFFD6E4F5)
val VertMadagascar = Color(0xFF1E8449)
val VertClair = Color(0xFFD5F0DF)
val Ambre = Color(0xFFC77700)
val AmbreClair = Color(0xFFFFE6BF)
val RougeMadagascar = Color(0xFFB3261E)
val Fond = Color(0xFFF4F7FB)
val Surface = Color(0xFFFFFFFF)
val SurfaceVariante = Color(0xFFE6EDF5)
val TexteSombre = Color(0xFF1B2430)
val TexteSecondaire = Color(0xFF4F5B6B)

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
    background = Fond,
    onBackground = TexteSombre,
    surface = Surface,
    onSurface = TexteSombre,
    surfaceVariant = SurfaceVariante,
    onSurfaceVariant = TexteSecondaire,
    outline = Color(0xFFB7C3D1),
)

// ---------- FORMES ----------

private val FormesATT = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
)

// ---------- LE THÈME ----------

/** À utiliser à la place de `MaterialTheme { }` dans MainActivity : même chose, avec nos couleurs. */
@Composable
fun ThemeATT(contenu: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PaletteATT,
        shapes = FormesATT,
        content = contenu,
    )
}
