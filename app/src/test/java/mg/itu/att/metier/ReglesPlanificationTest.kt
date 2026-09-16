package mg.itu.att.metier

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglesPlanificationTest {

    @Test
    fun `ajout de minutes`() {
        assertEquals("09:15", ReglesPlanification.ajouterMinutes("08:30", 45))
        assertEquals("23:59", ReglesPlanification.ajouterMinutes("23:50", 15)) // borné à la fin de journée
    }

    @Test
    fun `generation des creneaux, capacite decoupee, dernier creneau avec le reste`() {
        val creneaux = ReglesPlanification.genererCreneaux("08:00", capacite = 12, capaciteCreneau = 5, dureeMin = 15, margeMin = 5)
        assertEquals(3, creneaux.size)
        assertEquals("08:00", creneaux[0].heureDebut); assertEquals("08:15", creneaux[0].heureFinEstimee); assertEquals(5, creneaux[0].capacite)
        assertEquals("08:20", creneaux[1].heureDebut)
        assertEquals("08:40", creneaux[2].heureDebut); assertEquals(2, creneaux[2].capacite)
        assertEquals(12, creneaux.sumOf { it.capacite })
    }

    @Test
    fun `creneaux individuels quand la capacite d un creneau vaut 1`() {
        val creneaux = ReglesPlanification.genererCreneaux("08:00", capacite = 3, capaciteCreneau = 1, dureeMin = 20, margeMin = 0)
        assertEquals(3, creneaux.size)
        assertTrue(creneaux.all { it.capacite == 1 })
        assertEquals("08:40", creneaux[2].heureDebut)
    }

    @Test
    fun `parametres invalides, aucun creneau`() {
        assertTrue(ReglesPlanification.genererCreneaux("8h", 10, 5, 15, 5).isEmpty())
        assertTrue(ReglesPlanification.genererCreneaux("08:00", 0, 5, 15, 5).isEmpty())
    }

    @Test
    fun `validation de session`() {
        assertNull(ReglesPlanification.validerSession(1, 1, 1, "2026-10-01", "08:00", "30", "5", "15", "5", "2026-09-16"))
        assertNotNull(ReglesPlanification.validerSession(1, 1, 1, "2026-09-01", "08:00", "30", "5", "15", "5", "2026-09-16")) // passée
        assertNotNull(ReglesPlanification.validerSession(1, 1, null, "2026-10-01", "08:00", "30", "5", "15", "5", "2026-09-16"))
        assertNotNull(ReglesPlanification.validerSession(1, 1, 1, "2026-10-01", "8h", "30", "5", "15", "5", "2026-09-16"))
        assertNotNull(ReglesPlanification.validerSession(1, 1, 1, "2026-10-01", "08:00", "0", "5", "15", "5", "2026-09-16"))
    }

    @Test
    fun `avertissement de conflit`() {
        assertNull(ReglesPlanification.avertissementConflit(0))
        assertNotNull(ReglesPlanification.avertissementConflit(2))
    }
}
