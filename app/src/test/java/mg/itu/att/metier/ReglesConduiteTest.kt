package mg.itu.att.metier

import mg.itu.att.data.Bareme
import mg.itu.att.data.CriterePratique
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglesConduiteTest {

    private fun critere(id: Int, points: Double, eliminatoire: Boolean = false, actif: Boolean = true) =
        CriterePratique(id = id, typeEpreuveId = 2, libelle = "Critère $id", points = points, eliminatoire = eliminatoire, actif = actif)

    private val bareme = Bareme(id = 2, typeEpreuveId = 2, version = 1, noteMax = 20.0, seuilReussite = 12.0, dateDebutValidite = "2026-09-15")

    @Test
    fun `ouverture refusee sans bareme ou sans critere actif`() {
        assertNotNull(ReglesConduite.verifierOuverture(null, listOf(critere(1, 5.0))))
        assertNotNull(ReglesConduite.verifierOuverture(bareme, emptyList()))
        assertNotNull(ReglesConduite.verifierOuverture(bareme, listOf(critere(1, 5.0, actif = false))))
        assertNull(ReglesConduite.verifierOuverture(bareme, listOf(critere(1, 5.0))))
        assertEquals(15.0, ReglesConduite.pointsGrille(listOf(critere(1, 5.0), critere(2, 10.0))), 0.0)
    }

    @Test
    fun `faute eliminatoire vaut zero et fait perdre l epreuve`() {
        val notes = listOf(
            NoteCritere(10.0, "8", eliminatoire = true, fauteCochee = true),
            NoteCritere(5.0, "4", eliminatoire = false, fauteCochee = false),
        )
        assertEquals(4.0, ReglesConduite.totalAttribue(notes), 0.0)
        assertTrue(ReglesConduite.fauteEliminatoire(notes))
        assertNull(ReglesConduite.verifierFin(notes))
    }

    @Test
    fun `fin d epreuve, tous les criteres doivent etre notes`() {
        val notes = listOf(NoteCritere(10.0, "7,5", false, false), NoteCritere(5.0, "0", false, false))
        assertNull(ReglesConduite.verifierFin(notes))
        assertFalse(ReglesConduite.fauteEliminatoire(notes))
        assertEquals(7.5, ReglesConduite.totalAttribue(notes), 0.0)
        assertNotNull(ReglesConduite.verifierFin(emptyList()))
        val incomplet = notes + NoteCritere(5.0, "", false, false)
        assertEquals(1, ReglesConduite.sansNote(incomplet))
        assertNotNull(ReglesConduite.verifierFin(incomplet))
        assertNotNull(ReglesConduite.verifierFin(listOf(NoteCritere(5.0, "6", false, false))))
    }
}
