package mg.itu.att.securite

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests JUnit du hachage : fonction pure, sans Android (docs/HORS_COURS.md n° 9). */
class MotDePasseTest {

    @Test
    fun `le bon mot de passe est accepte`() {
        val empreinte = MotDePasse.hacher("ChangezMoi2026")
        assertTrue(MotDePasse.verifier("ChangezMoi2026", empreinte))
    }

    @Test
    fun `un mauvais mot de passe est refuse`() {
        val empreinte = MotDePasse.hacher("ChangezMoi2026")
        assertFalse(MotDePasse.verifier("changezmoi2026", empreinte))
        assertFalse(MotDePasse.verifier("", empreinte))
    }

    @Test
    fun `deux empreintes du meme mot de passe sont differentes grace au sel`() {
        assertNotEquals(MotDePasse.hacher("secret"), MotDePasse.hacher("secret"))
    }

    @Test
    fun `l empreinte n est jamais le mot de passe en clair`() {
        val empreinte = MotDePasse.hacher("secret")
        assertFalse(empreinte.contains("secret"))
        assertTrue(empreinte.startsWith("pbkdf2$"))
    }

    @Test
    fun `une empreinte mal formee est refusee sans planter`() {
        assertFalse(MotDePasse.verifier("secret", "n'importe quoi"))
        assertFalse(MotDePasse.verifier("secret", ""))
    }
}
