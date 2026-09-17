package mg.itu.att.metier

import mg.itu.att.data.StatutPresence
import mg.itu.att.data.StatutTentative
import mg.itu.att.data.Tentative
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ReglesTentativesTest {

    private fun tentative(numero: Int, statut: StatutTentative = StatutTentative.TERMINEE) =
        Tentative(id = numero, candidatId = 1, inscriptionId = numero, typeEpreuveId = 5, numero = numero, statut = statut, dateHeure = "2026-09-16T08:00")

    @Test
    fun `numero suivant jamais reutilise`() {
        assertEquals(1, ReglesTentatives.numeroSuivant(emptyList()))
        assertEquals(3, ReglesTentatives.numeroSuivant(listOf(tentative(1), tentative(2))))
        assertEquals(6, ReglesTentatives.numeroSuivant(listOf(tentative(5, StatutTentative.ANNULEE))))
    }

    @Test
    fun `ouverture reservee aux presents`() {
        assertNull(ReglesTentatives.peutOuvrir(StatutPresence.PRESENT, null, 0, 0))
        assertNotNull(ReglesTentatives.peutOuvrir(StatutPresence.EN_ATTENTE, null, 0, 0))
        assertNotNull(ReglesTentatives.peutOuvrir(StatutPresence.ABSENT, null, 0, 0))
        assertNotNull(ReglesTentatives.peutOuvrir(null, null, 0, 0))
    }

    @Test
    fun `reprise d une tentative en cours, refus si terminee`() {
        assertNull(ReglesTentatives.peutOuvrir(StatutPresence.EN_COURS, tentative(1, StatutTentative.EN_COURS), 1, 0))
        assertNotNull(ReglesTentatives.peutOuvrir(StatutPresence.PRESENT, tentative(1, StatutTentative.TERMINEE), 1, 0))
    }

    @Test
    fun `nombre maximal de passages`() {
        assertNotNull(ReglesTentatives.peutOuvrir(StatutPresence.PRESENT, null, 3, 3))
        assertNull(ReglesTentatives.peutOuvrir(StatutPresence.PRESENT, null, 3, 0)) // 0 = illimité
    }
}
