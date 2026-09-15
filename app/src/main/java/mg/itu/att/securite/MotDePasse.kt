package mg.itu.att.securite

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hachage des mots de passe (docs/HORS_COURS.md n° 8).
 *
 * Un mot de passe n'est jamais stocké : on garde une empreinte PBKDF2 (fonction à sens unique,
 * volontairement lente) avec un sel aléatoire propre à chaque utilisateur. Pour vérifier,
 * on recalcule l'empreinte et on la compare. Bibliothèque Java standard, aucune dépendance.
 *
 * Format stocké : "pbkdf2$<iterations>$<sel base64>$<empreinte base64>".
 */
object MotDePasse {

    private const val ALGORITHME = "PBKDF2WithHmacSHA256" // disponible depuis Android 8.0 (minSdk 26)
    private const val ITERATIONS = 10_000 // compromis sécurité / fluidité sur mobile (décision T3)
    private const val TAILLE_SEL_OCTETS = 16
    private const val TAILLE_CLE_BITS = 256

    /** Produit l'empreinte à stocker dans `Utilisateur.motDePasseHash`. */
    fun hacher(motDePasse: String): String {
        val sel = ByteArray(TAILLE_SEL_OCTETS).also { SecureRandom().nextBytes(it) }
        val empreinte = calculer(motDePasse, sel, ITERATIONS)
        val b64 = Base64.getEncoder()
        return listOf("pbkdf2", ITERATIONS.toString(), b64.encodeToString(sel), b64.encodeToString(empreinte))
            .joinToString("$")
    }

    /** Vrai si le mot de passe correspond à l'empreinte stockée. */
    fun verifier(motDePasse: String, empreinteStockee: String): Boolean {
        val parties = empreinteStockee.split('$')
        if (parties.size != 4 || parties[0] != "pbkdf2") return false
        val iterations = parties[1].toIntOrNull() ?: return false
        val b64 = Base64.getDecoder()
        val sel = b64.decode(parties[2])
        val attendu = b64.decode(parties[3])
        val calcule = calculer(motDePasse, sel, iterations)
        // Comparaison en temps constant : ne révèle pas à quel octet la différence apparaît.
        return MessageDigest.isEqual(attendu, calcule)
    }

    private fun calculer(motDePasse: String, sel: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(motDePasse.toCharArray(), sel, iterations, TAILLE_CLE_BITS)
        return SecretKeyFactory.getInstance(ALGORITHME).generateSecret(spec).encoded
    }
}
