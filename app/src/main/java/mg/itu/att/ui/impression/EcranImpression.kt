package mg.itu.att.ui.impression

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import mg.itu.att.ui.communs.BoutonPrincipal
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.EncartInfo
import mg.itu.att.ui.communs.TexteErreur

/**
 * Aperçu et impression d'un document (UC13) : la page HTML préparée par le ViewModel est affichée dans une
 * `WebView` (docs/HORS_COURS.md n° 24), puis confiée au service d'impression d'Android
 * (`PrintManager`, n° 10), qui propose l'imprimante ou l'enregistrement en PDF. Fonctionne sans réseau.
 */
@Composable
fun EcranImpression(viewModel: ImpressionViewModel, type: TypeDocument, id: Int, onRetour: () -> Unit) {
    viewModel.preparer(type, id)
    val etat by viewModel.uiState.collectAsState()
    val contexte = LocalContext.current
    // La WebView est créée une fois et réutilisée : c'est elle qui fournit l'adaptateur d'impression.
    val vue = remember { WebView(contexte).apply { webViewClient = WebViewClient() } }

    EcranStandard(titre = etat.titre, onRetour = onRetour) {
        TexteErreur(etat.erreur)
        val html = etat.html
        if (html == null) {
            if (etat.erreur == null) Text("Préparation du document…", style = MaterialTheme.typography.bodyLarge)
            return@EcranStandard
        }
        EncartInfo("Aperçu du document. « Imprimer » ouvre le service d'impression d'Android : imprimante ou enregistrement en PDF.")
        Spacer(Modifier.height(4.dp))
        // L'aperçu occupe la hauteur disponible sous le bouton (le cadre standard est une colonne).
        AndroidView(
            factory = { vue },
            modifier = Modifier.fillMaxWidth().height(460.dp),
            update = { it.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null) },
        )
        Spacer(Modifier.height(8.dp))
        BoutonPrincipal("Imprimer", { imprimer(contexte, vue, etat.titre) })
        Spacer(Modifier.height(8.dp))
    }
}

/** Confie la page affichée au service d'impression du téléphone. */
private fun imprimer(contexte: Context, vue: WebView, titre: String) {
    val service = contexte.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
    val nom = "ATT — $titre"
    service.print(nom, vue.createPrintDocumentAdapter(nom), PrintAttributes.Builder().build())
}
