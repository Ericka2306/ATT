package mg.itu.att.metier

import mg.itu.att.data.StatutPresence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglesPresenceTest {

    @Test
    fun `minutes de retard`() {
        assertEquals(10, ReglesPresence.minutesDeRetard("08:00", "08:10"))
        assertEquals(-5, ReglesPresence.minutesDeRetard("08:00", "07:55"))
        assertEquals(70, ReglesPresence.minutesDeRetard("07:30", "08:40"))
    }

    @Test
    fun `present dans la tolerance, en retard au-dela`() {
        assertEquals(StatutPresence.PRESENT, ReglesPresence.statutArrivee("08:00", "08:15", 15))
        assertEquals(StatutPresence.EN_RETARD, ReglesPresence.statutArrivee("08:00", "08:16", 15))
        assertEquals(StatutPresence.PRESENT, ReglesPresence.statutArrivee("08:00", "07:50", 15))
        assertEquals(StatutPresence.EN_RETARD, ReglesPresence.statutArrivee("08:00", "08:01", 0))
    }

    @Test
    fun `regle d absence`() {
        assertTrue(ReglesPresence.absentReporteAutomatiquement("REPORT_AUTO"))
        assertTrue(ReglesPresence.absentReporteAutomatiquement(" report_auto "))
        assertFalse(ReglesPresence.absentReporteAutomatiquement("NOUVELLE_INSCRIPTION"))
    }

    @Test
    fun `transitions`() {
        assertEquals(listOf(StatutPresence.PRESENT, StatutPresence.ABSENT), ReglesPresence.transitionsPossibles(StatutPresence.EN_ATTENTE))
        assertEquals(listOf(StatutPresence.EN_ATTENTE), ReglesPresence.transitionsPossibles(StatutPresence.PRESENT))
        assertTrue(ReglesPresence.transitionsPossibles(StatutPresence.TERMINE).isEmpty())
    }
}
