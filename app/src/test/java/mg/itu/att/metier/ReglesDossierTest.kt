package mg.itu.att.metier

import mg.itu.att.data.CategoriePermis
import mg.itu.att.data.Dossier
import mg.itu.att.data.StatutDossier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglesDossierTest {

    private val categorieB = CategoriePermis(id = 3, code = "B", libelle = "Véhicules légers", ageMinimum = 18)

    @Test
    fun `age revolu calcule avant et apres l anniversaire`() {
        assertEquals(18, ReglesDossier.ageA("2008-09-15", "2026-09-15")) // le jour même
        assertEquals(17, ReglesDossier.ageA("2008-09-16", "2026-09-15")) // la veille
        assertEquals(22, ReglesDossier.ageA("2004-05-17", "2026-09-15"))
        assertNull(ReglesDossier.ageA("n'importe quoi", "2026-09-15"))
    }

    @Test
    fun `eligibilite selon l age minimum de la categorie`() {
        assertNull(ReglesDossier.verifierEligibilite("2004-05-17", categorieB, "2026-09-15"))
        assertNotNull(ReglesDossier.verifierEligibilite("2010-01-01", categorieB, "2026-09-15"))
    }

    @Test
    fun `un age minimum non renseigne ne bloque pas`() {
        val sansAge = categorieB.copy(ageMinimum = null)
        assertNull(ReglesDossier.verifierEligibilite("2015-01-01", sansAge, "2026-09-15"))
    }

    @Test
    fun `un seul dossier en cours par categorie`() {
        val enCours = Dossier(id = 1, candidatId = 1, categorieId = 3, statut = StatutDossier.SOUMIS)
        val refuse = Dossier(id = 2, candidatId = 1, categorieId = 3, statut = StatutDossier.REFUSE)
        val valide = Dossier(id = 3, candidatId = 1, categorieId = 3, statut = StatutDossier.VALIDE)
        assertNotNull(ReglesDossier.peutOuvrirDossier(listOf(enCours), 3))
        assertNull(ReglesDossier.peutOuvrirDossier(listOf(refuse), 3))
        assertNotNull(ReglesDossier.peutOuvrirDossier(listOf(valide), 3))
        assertNull(ReglesDossier.peutOuvrirDossier(listOf(enCours), 4)) // autre catégorie
    }

    @Test
    fun `dossier complet et pieces depuis la regle`() {
        assertTrue(ReglesDossier.dossierComplet(listOf(true, true)))
        assertFalse(ReglesDossier.dossierComplet(listOf(true, false)))
        assertFalse(ReglesDossier.dossierComplet(emptyList()))
        assertEquals(listOf("CIN", "Photos"), ReglesDossier.piecesDepuisRegle(" CIN ; Photos ;", ";"))
        assertEquals(emptyList<String>(), ReglesDossier.piecesDepuisRegle(null, ";"))
    }
}
