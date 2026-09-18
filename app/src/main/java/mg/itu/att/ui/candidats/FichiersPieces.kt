package mg.itu.att.ui.candidats

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mg.itu.att.metier.PiecesJointes
import java.io.File
import java.io.IOException

/*
 * Le côté Android des pièces jointes (étape D2b) : copier un fichier choisi dans le stockage privé,
 * préparer l'adresse où l'appareil photo écrit, relire une photo ou un PDF pour l'afficher.
 * Les décisions (types acceptés, taille, qui peut joindre) sont dans `metier/PiecesJointes`.
 * Notions hors cours : docs/HORS_COURS.md n° 28, 29 et 30.
 */

/** Nom de l'autorité du `FileProvider` déclaré dans le manifeste. */
private fun autorite(contexte: Context) = "${contexte.packageName}.fichiers"

/** Nombre maximal de pages d'un PDF dessinées à l'écran : une pièce d'identité ou un certificat tient en quelques pages. */
private const val PAGES_MAX = 10

/**
 * Une adresse neuve où l'appareil photo enregistrera la photo : un fichier du cache, exposé par le `FileProvider`
 * (l'appareil photo est une autre application, il ne peut pas écrire directement dans nos dossiers).
 */
fun nouvelleAdressePhoto(contexte: Context): Uri {
    val dossier = File(contexte.cacheDir, "photos").apply { mkdirs() }
    val fichier = File(dossier, "photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(contexte, autorite(contexte), fichier)
}

/**
 * Copie le contenu de [source] dans `filesDir/[chemin]`.
 * S'arrête dès que [tailleMax] est dépassée : inutile de copier un fichier qui sera refusé.
 * @return le nombre d'octets lus, ou null si le fichier ne peut pas être lu.
 */
suspend fun copierDansStockage(contexte: Context, source: Uri, chemin: String, tailleMax: Long): Long? = withContext(Dispatchers.IO) {
    val cible = File(contexte.filesDir, chemin)
    cible.parentFile?.mkdirs()
    try {
        val entree = contexte.contentResolver.openInputStream(source) ?: return@withContext null
        var total = 0L
        entree.use { lecture ->
            cible.outputStream().use { ecriture ->
                val tampon = ByteArray(8 * 1024)
                while (total <= tailleMax) {
                    val lus = lecture.read(tampon)
                    if (lus < 0) break
                    ecriture.write(tampon, 0, lus)
                    total += lus
                }
            }
        }
        total
    } catch (e: IOException) {
        cible.delete()
        null
    } catch (e: SecurityException) {
        cible.delete()
        null
    }
}

/** Efface une copie devenue inutile (fichier remplacé, retiré ou refusé). */
suspend fun supprimerDuStockage(contexte: Context, chemin: String) {
    withContext(Dispatchers.IO) { File(contexte.filesDir, chemin).delete() }
}

/**
 * Relit une pièce jointe en images affichables : la photo, réduite à [largeurMax] pixels environ pour ne pas
 * saturer la mémoire, ou chaque page du PDF dessinée par `PdfRenderer`.
 * @return les pages, ou null si le fichier manque ou ne peut pas être lu.
 */
suspend fun lirePieceJointe(contexte: Context, chemin: String, largeurMax: Int = 1400): List<ImageBitmap>? = withContext(Dispatchers.IO) {
    val fichier = File(contexte.filesDir, chemin)
    if (!fichier.exists()) return@withContext null
    try {
        if (PiecesJointes.estPdf(chemin)) lirePdf(fichier, largeurMax) else lirePhoto(fichier, largeurMax)?.let { listOf(it) }
    } catch (e: IOException) {
        null
    } catch (e: SecurityException) {
        null
    }
}

private fun lirePhoto(fichier: File, largeurMax: Int): ImageBitmap? {
    // Premier passage : lire seulement les dimensions, pour choisir la réduction avant de charger l'image.
    val dimensions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(fichier.path, dimensions)
    var reduction = 1
    while (dimensions.outWidth / (reduction * 2) >= largeurMax) reduction *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = reduction }
    return BitmapFactory.decodeFile(fichier.path, options)?.asImageBitmap()
}

private fun lirePdf(fichier: File, largeurMax: Int): List<ImageBitmap> =
    ParcelFileDescriptor.open(fichier, ParcelFileDescriptor.MODE_READ_ONLY).use { descripteur ->
        PdfRenderer(descripteur).use { rendu ->
            (0 until minOf(rendu.pageCount, PAGES_MAX)).map { numero ->
                rendu.openPage(numero).use { page ->
                    val hauteur = (page.height * largeurMax.toFloat() / page.width).toInt()
                    val image = Bitmap.createBitmap(largeurMax, hauteur, Bitmap.Config.ARGB_8888)
                    image.eraseColor(Color.WHITE) // une page PDF n'a pas de fond : sans blanc, elle s'afficherait transparente
                    page.render(image, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    image.asImageBitmap()
                }
            }
        }
    }
