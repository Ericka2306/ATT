package mg.itu.att

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import mg.itu.att.ui.theme.ThemeATT

/**
 * Point d'entrée de l'application ATT.
 *
 * Squelette identique à celui des projets du cours : une seule Activity,
 * le thème Material (avec nos couleurs : [ThemeATT]), et toute la navigation dans [AppNavigation].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThemeATT {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}
