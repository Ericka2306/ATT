package mg.itu.att.metier

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidationCompteTest {

    @Test
    fun `creation valide`() {
        assertNull(ValidationCompte.validerCreation("examinateur1", "MotDePasse1", listOf("admin")))
    }

    @Test
    fun `changement valide`() {
        assertNull(ValidationCompte.validerChangement("admin2026", "NouveauMdp2026", "NouveauMdp2026"))
    }

    @Test
    fun `changement refuse si confirmation differente identique a l ancien ou trop court`() {
        assertNotNull(ValidationCompte.validerChangement("admin2026", "NouveauMdp2026", "autre"))
        assertNotNull(ValidationCompte.validerChangement("admin2026", "admin2026", "admin2026"))
        assertNotNull(ValidationCompte.validerChangement("admin2026", "court", "court"))
        assertNotNull(ValidationCompte.validerChangement("", "NouveauMdp2026", "NouveauMdp2026"))
    }
}
