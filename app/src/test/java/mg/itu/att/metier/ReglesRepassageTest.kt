package mg.itu.att.metier

import mg.itu.att.metier.ReglesRepassage.EtatEpreuve
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglesRepassageTest {

    private val aujourdHui = "2026-09-17"

    @Test
    fun `une reussite recente reste valable, une trop ancienne expire`() {
        assertTrue(ReglesRepassage.reussiteEncoreValable("2026-06-01", conservationJours = 365, aujourdHui))
        assertFalse(ReglesRepassage.reussiteEncoreValable("2025-06-01", conservationJours = 365, aujourdHui))
        // 0 = conservation sans limite (règle configurable).
        assertTrue(ReglesRepassage.reussiteEncoreValable("2020-01-01", conservationJours = 0, aujourdHui))
        assertFalse(ReglesRepassage.reussiteEncoreValable(null, conservationJours = 365, aujourdHui))
    }

    @Test
    fun `la date de reinscription suit le delai de repassage`() {
        assertEquals("2026-10-12", ReglesRepassage.inscriptibleLe("2026-09-17", delaiJours = 25))
        assertEquals("2026-09-17", ReglesRepassage.inscriptibleLe("2026-09-17", delaiJours = 0))
        assertNull(ReglesRepassage.inscriptibleLe(null, delaiJours = 25))
    }

    @Test
    fun `epreuves a repasser - echec, jamais passee, reussite expiree`() {
        val epreuves = listOf(
            EtatEpreuve(1, "Épreuve théorique", dateReussite = "2026-09-01", dateDernierPassage = "2026-09-01"),
            EtatEpreuve(2, "Épreuve de conduite", dateReussite = null, dateDernierPassage = "2026-09-17"),
            EtatEpreuve(3, "Épreuve ancienne", dateReussite = "2024-01-01", dateDernierPassage = "2024-01-01"),
        )
        val aRepasser = ReglesRepassage.epreuvesARepasser(epreuves, conservationJours = 365, delaiRepassageJours = 25, aujourdHui)

        // La théorie réussie il y a peu n'est pas à repasser.
        assertEquals(listOf(2, 3), aRepasser.map { it.typeEpreuveId })
        assertEquals("échec du 17/09/2026", aRepasser[0].raison)
        assertEquals("2026-10-12", aRepasser[0].inscriptibleLe)
        assertTrue(aRepasser[1].raison.startsWith("réussite du 01/01/2024 expirée"))
    }

    @Test
    fun `une epreuve jamais passee est a repasser sans date`() {
        val aRepasser = ReglesRepassage.epreuvesARepasser(
            listOf(EtatEpreuve(1, "Épreuve théorique", null, null)),
            conservationJours = 365, delaiRepassageJours = 25, aujourdHui,
        )
        assertEquals("épreuve jamais passée", aRepasser.single().raison)
        assertNull(aRepasser.single().inscriptibleLe)
    }
}
