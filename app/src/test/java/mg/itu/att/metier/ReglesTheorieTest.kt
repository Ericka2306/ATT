package mg.itu.att.metier

import mg.itu.att.data.Bareme
import mg.itu.att.data.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ReglesTheorieTest {

    private fun question(id: Int, points: Double = 1.0, actif: Boolean = true) =
        Question(id = id, typeEpreuveId = 1, enonce = "Question $id", points = points, actif = actif)

    private val bareme = Bareme(id = 1, typeEpreuveId = 1, version = 1, noteMax = 20.0, seuilReussite = 12.0, dateDebutValidite = "2026-09-15")

    @Test
    fun `tirage atteint la note max sans la depasser ni repeter`() {
        val banque = (1..50).map { question(it, points = if (it % 3 == 0) 3.0 else 2.0) }
        val sujet = ReglesTheorie.tirerSujet(banque, 20.0, Random(42))
        assertEquals(20.0, ReglesTheorie.pointsPoses(sujet), 0.0)
        assertEquals(sujet.size, sujet.map { it.id }.distinct().size)
    }

    @Test
    fun `tirage prend tout quand la banque est trop petite`() {
        val banque = listOf(question(1, 10.0), question(2, 5.0), question(3, 3.0))
        val sujet = ReglesTheorie.tirerSujet(banque, 20.0, Random(1))
        assertEquals(3, sujet.size)
        assertEquals(18.0, ReglesTheorie.pointsPoses(sujet), 0.0)
    }

    @Test
    fun `tirage ignore les questions inactives et celles qui ne tiennent pas`() {
        val banque = listOf(question(1, 15.0), question(2, 15.0), question(3, 5.0), question(4, 2.0, actif = false))
        val sujet = ReglesTheorie.tirerSujet(banque, 20.0, Random(7))
        assertTrue(ReglesTheorie.pointsPoses(sujet) <= 20.0)
        assertTrue(sujet.none { it.id == 4 })
    }

    @Test
    fun `tirage reproductible avec la meme graine`() {
        val banque = (1..40).map { question(it) }
        assertEquals(ReglesTheorie.tirerSujet(banque, 10.0, Random(3)).map { it.id }, ReglesTheorie.tirerSujet(banque, 10.0, Random(3)).map { it.id })
    }

    @Test
    fun `proposition exclut les questions posees et trop grosses`() {
        val banque = listOf(question(1, 5.0), question(2, 10.0), question(3, 10.0), question(4, 2.0))
        val proposables = ReglesTheorie.proposables(banque, idsPosees = setOf(2), pointsRestants = 6.0)
        assertEquals(listOf(1, 4), proposables.map { it.id })
        assertEquals(listOf(2.0, 5.0), ReglesTheorie.pointsDisponibles(banque, setOf(2), 6.0))
        assertEquals(4, ReglesTheorie.proposer(banque, setOf(2), 6.0, pointsVoulus = 2.0, aleatoire = Random(0))?.id)
        assertNull(ReglesTheorie.proposer(banque, setOf(1, 2, 3, 4), 30.0))
    }

    @Test
    fun `ouverture refusee sans bareme, sans question ou avec un mode inconnu`() {
        val banque = listOf(question(1))
        assertNotNull(ReglesTheorie.verifierOuverture(null, banque, ModesTheorie.TIRAGE))
        assertNotNull(ReglesTheorie.verifierOuverture(bareme, emptyList(), ModesTheorie.TIRAGE))
        assertNotNull(ReglesTheorie.verifierOuverture(bareme, listOf(question(1, actif = false)), ModesTheorie.DIRECT))
        assertNotNull(ReglesTheorie.verifierOuverture(bareme, banque, "AUTRE"))
        assertNull(ReglesTheorie.verifierOuverture(bareme, banque, ModesTheorie.DIRECT))
    }

    @Test
    fun `points attribues entre 0 et le maximum de la question`() {
        assertEquals(2.5, ReglesTheorie.pointsValides("2,5", 3.0))
        assertEquals(0.0, ReglesTheorie.pointsValides("0", 3.0))
        assertNull(ReglesTheorie.pointsValides("4", 3.0))
        assertNull(ReglesTheorie.pointsValides("-1", 3.0))
        assertNull(ReglesTheorie.pointsValides("", 3.0))
    }

    @Test
    fun `fin d epreuve, toutes les questions posees doivent etre notees`() {
        val notees = listOf(NoteQuestion(3.0, "3"), NoteQuestion(2.0, "0"), NoteQuestion(5.0, "2,5"))
        assertNull(ReglesTheorie.verifierFin(notees))
        assertEquals(5.5, ReglesTheorie.totalAttribue(notees), 0.0)
        assertNotNull(ReglesTheorie.verifierFin(emptyList()))
        val incomplet = notees + NoteQuestion(2.0, "")
        assertEquals(1, ReglesTheorie.sansNote(incomplet))
        assertNotNull(ReglesTheorie.verifierFin(incomplet))
        assertNotNull(ReglesTheorie.verifierFin(listOf(NoteQuestion(2.0, "3"))))
    }
}
