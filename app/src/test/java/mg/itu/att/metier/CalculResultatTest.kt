package mg.itu.att.metier

import mg.itu.att.data.Bareme
import mg.itu.att.data.StatutResultat
import mg.itu.att.data.StatutTentative
import mg.itu.att.metier.CalculResultat.LigneConduite
import mg.itu.att.metier.CalculResultat.LigneTheorie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculResultatTest {

    /** Barème d'exemple : 20 points, seuil 12 (valeurs « à confirmer » de la configuration). */
    private val bareme = Bareme(id = 1, typeEpreuveId = 1, version = 1, noteMax = 20.0, seuilReussite = 12.0, dateDebutValidite = "2026-01-01")

    @Test
    fun `theorie - somme des points, les questions non notees valent zero`() {
        val lignes = listOf(
            LigneTheorie(10.0, 10.0),
            LigneTheorie(5.0, 5.0),
            LigneTheorie(null, 3.0),
            LigneTheorie(1.0, 2.0),
        )
        assertEquals(16.0, CalculResultat.totalTheorie(lignes), 0.001)
        assertEquals(20.0, CalculResultat.totalPoseTheorie(lignes), 0.001)
    }

    @Test
    fun `theorie - sujet egal a la note max, la note est le total et le seuil est atteint`() {
        val lignes = listOf(LigneTheorie(10.0, 10.0), LigneTheorie(5.0, 5.0), LigneTheorie(0.0, 3.0), LigneTheorie(1.0, 2.0))
        val c = CalculResultat.calculerTheorie(lignes, bareme)
        assertEquals(16.0, c.noteObtenue, 0.001)
        assertEquals(20.0, c.noteMax, 0.001)
        assertEquals(12.0, c.seuil, 0.001)
        assertTrue(c.reussi)
    }

    @Test
    fun `theorie - sous le seuil, echec`() {
        val lignes = listOf(LigneTheorie(6.0, 10.0), LigneTheorie(2.0, 10.0))
        val c = CalculResultat.calculerTheorie(lignes, bareme)
        assertEquals(8.0, c.noteObtenue, 0.001)
        assertFalse(c.reussi)
    }

    @Test
    fun `un sujet qui ne totalise pas la note max est rapporte au bareme`() {
        // 9 points obtenus sur 18 posés → 10 sur 20.
        assertEquals(10.0, CalculResultat.rapporterAuBareme(9.0, 18.0, 20.0), 0.001)
        // 12 sur 18 → 13.33 sur 20, arrondi à deux décimales.
        assertEquals(13.33, CalculResultat.rapporterAuBareme(12.0, 18.0, 20.0), 0.001)
        // Aucun point proposé : note nulle, pas de division par zéro.
        assertEquals(0.0, CalculResultat.rapporterAuBareme(0.0, 0.0, 20.0), 0.001)
    }

    @Test
    fun `conduite - la faute eliminatoire met le critere a zero et fait echouer`() {
        val lignes = listOf(
            LigneConduite(5.0, 5.0, critereEliminatoire = false, fauteCochee = false),
            LigneConduite(3.0, 3.0, critereEliminatoire = false, fauteCochee = false),
            LigneConduite(10.0, 10.0, critereEliminatoire = true, fauteCochee = true),
        )
        assertEquals(8.0, CalculResultat.totalConduite(lignes), 0.001)
        assertEquals(18.0, CalculResultat.totalGrilleConduite(lignes), 0.001)
        assertTrue(CalculResultat.fauteEliminatoire(lignes))

        val c = CalculResultat.calculerConduite(lignes, bareme)
        // 8 sur 18 rapportés à 20 → 8.89, et l'épreuve est perdue malgré tout.
        assertEquals(8.89, c.noteObtenue, 0.001)
        assertTrue(c.fauteEliminatoire)
        assertFalse(c.reussi)
    }

    @Test
    fun `conduite - sans faute, le seuil decide`() {
        val lignes = listOf(
            LigneConduite(5.0, 5.0, critereEliminatoire = false, fauteCochee = false),
            LigneConduite(3.0, 3.0, critereEliminatoire = false, fauteCochee = false),
            LigneConduite(8.0, 10.0, critereEliminatoire = true, fauteCochee = false),
        )
        val c = CalculResultat.calculerConduite(lignes, bareme)
        assertEquals(17.78, c.noteObtenue, 0.001) // 16 / 18 × 20
        assertFalse(c.fauteEliminatoire)
        assertTrue(c.reussi)
    }

    @Test
    fun `la note egale au seuil est une reussite`() {
        val c = CalculResultat.calculer(totalBrut = 12.0, totalPropose = 20.0, bareme = bareme)
        assertEquals(12.0, c.noteObtenue, 0.001)
        assertTrue(c.reussi)
    }

    @Test
    fun `on ne calcule pas un passage en cours, annule, ou sans bareme`() {
        assertNotNull(CalculResultat.verifierCalcul(StatutTentative.EN_COURS, bareme, 3))
        assertNotNull(CalculResultat.verifierCalcul(StatutTentative.ANNULEE, bareme, 3))
        assertNotNull(CalculResultat.verifierCalcul(StatutTentative.TERMINEE, null, 3))
        assertNotNull(CalculResultat.verifierCalcul(StatutTentative.TERMINEE, bareme.copy(seuilReussite = 0.0), 3))
        assertNotNull(CalculResultat.verifierCalcul(StatutTentative.TERMINEE, bareme, 0))
        assertNull(CalculResultat.verifierCalcul(StatutTentative.TERMINEE, bareme, 3))
    }

    @Test
    fun `une correction exige un motif et un resultat vivant`() {
        assertNotNull(CalculResultat.verifierCorrection("", StatutResultat.VALIDE_ATT))
        assertNotNull(CalculResultat.verifierCorrection("   ", StatutResultat.CALCULE))
        assertNotNull(CalculResultat.verifierCorrection("erreur de saisie", StatutResultat.ANNULE))
        assertNull(CalculResultat.verifierCorrection("erreur de saisie", StatutResultat.VALIDE_ATT))
    }
}
