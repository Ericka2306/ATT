package mg.itu.att.metier

import mg.itu.att.data.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglesConsultationTest {

    // Candidat n° 7 de l'auto-école n° 3, elle-même dans la région n° 2.
    private fun voit(role: Role, region: Int? = null, autoEcole: Int? = null, candidat: Int? = null) =
        ReglesConsultation.peutVoirCandidat(role, region, autoEcole, candidat, candidatId = 7, autoEcoleIdCandidat = 3, regionIdAutoEcole = 2)

    @Test
    fun `le super admin et l admin national voient tout`() {
        assertTrue(voit(Role.SUPER_ADMIN))
        assertTrue(voit(Role.ADMIN_ATT))
    }

    @Test
    fun `l admin regional ne voit que sa region`() {
        assertTrue(voit(Role.ADMIN_ATT, region = 2))
        assertFalse(voit(Role.ADMIN_ATT, region = 5))
    }

    @Test
    fun `l auto-ecole ne voit que ses candidats`() {
        assertTrue(voit(Role.AUTO_ECOLE, autoEcole = 3))
        assertFalse(voit(Role.AUTO_ECOLE, autoEcole = 4))
        assertFalse(voit(Role.AUTO_ECOLE))
    }

    @Test
    fun `le candidat ne voit que lui-meme, l examinateur aucune fiche`() {
        assertTrue(voit(Role.CANDIDAT, candidat = 7))
        assertFalse(voit(Role.CANDIDAT, candidat = 8))
        assertFalse(voit(Role.EXAMINATEUR, region = 2, autoEcole = 3, candidat = 7))
    }

    @Test
    fun `seuls l ATT et l auto-ecole gerent la fiche et creent le compte`() {
        for (role in listOf(Role.SUPER_ADMIN, Role.ADMIN_ATT, Role.AUTO_ECOLE)) {
            assertTrue(ReglesConsultation.peutGererCandidat(role))
            assertTrue(ReglesConsultation.peutCreerCompteCandidat(role))
        }
        for (role in listOf(Role.CANDIDAT, Role.EXAMINATEUR)) {
            assertFalse(ReglesConsultation.peutGererCandidat(role))
            assertFalse(ReglesConsultation.peutCreerCompteCandidat(role))
        }
    }

    @Test
    fun `la configuration est reservee au Super Admin, les comptes a l ATT`() {
        assertTrue(ReglesConsultation.peutConfigurer(Role.SUPER_ADMIN))
        for (role in listOf(Role.ADMIN_ATT, Role.AUTO_ECOLE, Role.EXAMINATEUR, Role.CANDIDAT)) {
            assertFalse("l'Admin ATT et les autres ne font que lire les référentiels", ReglesConsultation.peutConfigurer(role))
        }
        for (role in listOf(Role.SUPER_ADMIN, Role.ADMIN_ATT)) {
            assertTrue(ReglesConsultation.peutGererComptes(role))
        }
        for (role in listOf(Role.AUTO_ECOLE, Role.EXAMINATEUR, Role.CANDIDAT)) {
            assertFalse(ReglesConsultation.peutGererComptes(role))
        }
    }

    @Test
    fun `a venir et libelle de session`() {
        assertTrue(ReglesConsultation.estAVenir("2026-09-17", "2026-09-17"))
        assertTrue(ReglesConsultation.estAVenir("2026-10-01", "2026-09-17"))
        assertFalse(ReglesConsultation.estAVenir("2026-09-16", "2026-09-17"))
        assertEquals("17/09/2026 à 08:00 — permis B, Épreuve théorique", ReglesConsultation.libelleSession("2026-09-17", "08:00", "B", "Épreuve théorique"))
    }
}
