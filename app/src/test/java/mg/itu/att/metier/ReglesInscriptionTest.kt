package mg.itu.att.metier

import mg.itu.att.data.Creneau
import mg.itu.att.data.Inscription
import mg.itu.att.data.Session
import mg.itu.att.data.StatutInscription
import mg.itu.att.data.StatutSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglesInscriptionTest {

    private val session = Session(id = 1, categorieId = 3, typeEpreuveId = 5, centreId = 1, date = "2026-10-05", heureConvocation = "08:00", capacite = 2, dureeCreneauMin = 15, margeMin = 5, statut = StatutSession.OUVERTE, creeParId = 1)

    private fun inscription(id: Int, candidatId: Int, sessionId: Int = 1, creneauId: Int? = 1, statut: StatutInscription = StatutInscription.INSCRIT) =
        Inscription(id = id, candidatId = candidatId, dossierId = 1, sessionId = sessionId, creneauId = creneauId, statut = statut, dateInscription = "2026-09-16", numeroAnonymat = "%03d".format(id))

    private fun contexte(
        inscriptionsSession: List<Inscription> = emptyList(), inscriptionsCandidat: List<Pair<Inscription, Session>> = emptyList(),
        dossierValide: Boolean = true, regionAutoEcole: Int? = 1, regionCentre: Int? = 1, examenDansRegion: Boolean = false,
        derniereTentative: String? = null, delai: Int = 25, nombreTentatives: Int = 0, tentativesMax: Int = 0,
        theorieRequise: Boolean = false, theorieReussie: Boolean = false, s: Session = session,
    ) = ReglesInscription.ContexteInscription(s, inscriptionsSession, inscriptionsCandidat, dossierValide, regionAutoEcole, regionCentre, examenDansRegion, derniereTentative, delai, nombreTentatives, tentativesMax, theorieRequise, theorieReussie)

    @Test
    fun `candidat eligible`() {
        assertNull(ReglesInscription.verifier(contexte()))
    }

    @Test
    fun `session non ouverte ou dossier non valide`() {
        assertNotNull(ReglesInscription.verifier(contexte(s = session.copy(statut = StatutSession.PLANIFIEE))))
        assertNotNull(ReglesInscription.verifier(contexte(dossierValide = false)))
    }

    @Test
    fun `double inscription et conflit le meme jour`() {
        val deja = inscription(1, 7)
        assertNotNull(ReglesInscription.verifier(contexte(inscriptionsSession = listOf(deja), inscriptionsCandidat = listOf(deja to session))))
        val autreSession = session.copy(id = 2)
        assertNotNull(ReglesInscription.verifier(contexte(inscriptionsCandidat = listOf(inscription(2, 7, sessionId = 2) to autreSession))))
        // Une inscription annulée ne bloque pas.
        assertNull(ReglesInscription.verifier(contexte(inscriptionsCandidat = listOf(inscription(3, 7, sessionId = 2, statut = StatutInscription.ANNULE) to autreSession))))
    }

    @Test
    fun `capacite de la session respectee`() {
        val pleine = listOf(inscription(1, 8), inscription(2, 9))
        assertNotNull(ReglesInscription.verifier(contexte(inscriptionsSession = pleine)))
        val avecAnnulee = listOf(inscription(1, 8), inscription(2, 9, statut = StatutInscription.ANNULE))
        assertNull(ReglesInscription.verifier(contexte(inscriptionsSession = avecAnnulee)))
    }

    @Test
    fun `region delai tentatives theorie`() {
        assertNotNull(ReglesInscription.verifier(contexte(examenDansRegion = true, regionAutoEcole = 2, regionCentre = 1)))
        assertNull(ReglesInscription.verifier(contexte(examenDansRegion = false, regionAutoEcole = 2, regionCentre = 1)))
        assertNotNull(ReglesInscription.verifier(contexte(derniereTentative = "2026-09-20", delai = 25)))
        assertNull(ReglesInscription.verifier(contexte(derniereTentative = "2026-08-01", delai = 25)))
        assertNotNull(ReglesInscription.verifier(contexte(nombreTentatives = 3, tentativesMax = 3)))
        assertNull(ReglesInscription.verifier(contexte(nombreTentatives = 3, tentativesMax = 0))) // 0 = illimité
        assertNotNull(ReglesInscription.verifier(contexte(theorieRequise = true, theorieReussie = false)))
        assertNull(ReglesInscription.verifier(contexte(theorieRequise = true, theorieReussie = true)))
    }

    @Test
    fun `choix du creneau et numero d anonymat`() {
        val creneaux = listOf(Creneau(id = 1, sessionId = 1, ordre = 1, heureDebut = "08:00", heureFinEstimee = "08:15", capacite = 1), Creneau(id = 2, sessionId = 1, ordre = 2, heureDebut = "08:20", heureFinEstimee = "08:35", capacite = 1))
        assertEquals(1, ReglesInscription.choisirCreneau(creneaux, emptyList())?.id)
        assertEquals(2, ReglesInscription.choisirCreneau(creneaux, listOf(inscription(1, 8, creneauId = 1)))?.id)
        assertNull(ReglesInscription.choisirCreneau(creneaux, listOf(inscription(1, 8, creneauId = 1), inscription(2, 9, creneauId = 2))))
        assertEquals("001", ReglesInscription.numeroAnonymat(emptyList()))
        assertEquals("003", ReglesInscription.numeroAnonymat(listOf(inscription(1, 8), inscription(2, 9, statut = StatutInscription.ANNULE))))
    }

    @Test
    fun `jours entre deux dates`() {
        assertEquals(19, ReglesInscription.joursEntre("2026-09-16", "2026-10-05"))
        assertEquals(365, ReglesInscription.joursEntre("2025-09-16", "2026-09-16"))
        assertTrue(ReglesInscription.joursEntre("2026-10-05", "2026-09-16") < 0)
    }
}
