package mg.itu.att.metier

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DatesTest {

    @Test
    fun `formatDate rend une date lisible`() {
        assertEquals("15/09/2026", formatDate("2026-09-15"))
    }

    @Test
    fun `formatDate garde l heure sans les secondes`() {
        assertEquals("15/09/2026 08:30", formatDate("2026-09-15T08:30:00"))
    }

    @Test
    fun `formatDate rend tel quel un texte inattendu`() {
        assertEquals("bizarre", formatDate("bizarre"))
    }

    @Test
    fun `dateDuJour et maintenantIso ont le format ISO`() {
        assertTrue(dateDuJour().matches(Regex("""\d{4}-\d{2}-\d{2}""")))
        assertTrue(maintenantIso().matches(Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}""")))
    }
}
