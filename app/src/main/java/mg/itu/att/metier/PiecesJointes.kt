package mg.itu.att.metier

import mg.itu.att.data.PieceDossier
import mg.itu.att.data.Role
import mg.itu.att.data.StatutDossier

/**
 * Pièces jointes du dossier (étape D2b, Q7) : l'auto-école peut joindre une photo ou un PDF à chaque pièce,
 * pour que l'ATT vérifie le dossier avant la convocation au lieu de le découvrir incomplet le jour de l'appel.
 * Joindre est **facultatif** : une pièce fournie sans fichier se vérifie sur le dossier papier.
 * Fonctions pures, sans Android : la copie du fichier et son affichage sont dans `ui/candidats/`.
 */
object PiecesJointes {

    /** Taille maximale d'un fichier joint : une photo de téléphone ou un PDF de quelques pages (limite technique). */
    const val TAILLE_MAX_OCTETS = 10L * 1024 * 1024

    /** Dossier, dans `filesDir`, où sont rangées les copies. */
    const val DOSSIER = "pieces"

    /** Comment l'ATT vérifie une pièce. */
    enum class Verification { FICHIER_JOINT, SUR_PAPIER, MANQUANTE }

    /** Un fichier joint suffit ; sinon une pièce cochée « fournie » se vérifie sur le papier ; sinon elle manque. */
    fun verification(piece: PieceDossier): Verification = when {
        piece.fichier != null -> Verification.FICHIER_JOINT
        piece.fournie -> Verification.SUR_PAPIER
        else -> Verification.MANQUANTE
    }

    /** Joindre ou retirer un fichier : seulement tant que le dossier se prépare (brouillon ou renvoyé incomplet). */
    fun peutJoindre(role: Role, statut: StatutDossier): Boolean =
        ReglesConsultation.peutGererCandidat(role) && (statut == StatutDossier.BROUILLON || statut == StatutDossier.INCOMPLET)

    /** Extension du fichier copié selon son type ; null si le type n'est pas accepté (photo JPEG/PNG ou PDF). */
    fun extensionPour(typeMime: String?): String? = when (typeMime?.lowercase()) {
        "image/jpeg", "image/jpg" -> "jpg"
        "image/png" -> "png"
        "application/pdf" -> "pdf"
        else -> null
    }

    /** Message d'erreur si le fichier ne peut pas être joint, null sinon. */
    fun verifierFichier(typeMime: String?, tailleOctets: Long): String? = when {
        extensionPour(typeMime) == null -> "Format non accepté : une photo (JPEG, PNG) ou un PDF."
        tailleOctets <= 0 -> "Le fichier est vide."
        tailleOctets > TAILLE_MAX_OCTETS -> "Fichier trop lourd : ${TAILLE_MAX_OCTETS / (1024 * 1024)} Mo au maximum."
        else -> null
    }

    /** Chemin de la copie, relatif à `filesDir` : un nom neuf à chaque ajout, pour ne jamais réécrire un fichier. */
    fun cheminCopie(pieceId: Int, horodatage: String, extension: String): String =
        "$DOSSIER/piece_${pieceId}_${horodatage.filter { it.isLetterOrDigit() }}.$extension"

    fun estPdf(chemin: String): Boolean = chemin.endsWith(".pdf", ignoreCase = true)

    /** Joindre un fichier, c'est fournir la pièce. */
    fun joindre(piece: PieceDossier, chemin: String): PieceDossier = piece.copy(fichier = chemin, fournie = true)

    /** Retirer le fichier ne décoche pas la pièce : l'original papier peut toujours être apporté. */
    fun retirer(piece: PieceDossier): PieceDossier = piece.copy(fichier = null)

    /** Résumé pour l'historique : « Acte de naissance : fichier joint (PDF) ». */
    fun resume(piece: PieceDossier): String =
        "${piece.typePiece} : " + (piece.fichier?.let { "fichier joint (" + (if (estPdf(it)) "PDF" else "photo") + ")" } ?: "sans fichier")
}
