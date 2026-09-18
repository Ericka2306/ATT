package mg.itu.att.metier

import mg.itu.att.data.PieceDossier
import mg.itu.att.data.Role
import mg.itu.att.data.StatutDossier
import mg.itu.att.metier.PiecesJointes.Verification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PiecesJointesTest {

    private fun piece(fournie: Boolean = false, fichier: String? = null) =
        PieceDossier(id = 7, dossierId = 1, typePiece = "Acte de naissance", fournie = fournie, fichier = fichier)

    @Test
    fun `le fichier est facultatif - une piece fournie sans fichier se verifie sur papier`() {
        assertEquals(Verification.SUR_PAPIER, PiecesJointes.verification(piece(fournie = true)))
        assertEquals(Verification.FICHIER_JOINT, PiecesJointes.verification(piece(fournie = true, fichier = "pieces/a.jpg")))
        assertEquals(Verification.MANQUANTE, PiecesJointes.verification(piece()))
        // Un dossier dont toutes les pièces sont cochées est complet, qu'elles aient un fichier ou non.
        val sansAucunFichier = listOf(piece(fournie = true), piece(fournie = true))
        assertTrue(ReglesDossier.dossierComplet(sansAucunFichier.map { it.fournie }))
    }

    @Test
    fun `joindre fournit la piece, retirer garde la piece fournie`() {
        val jointe = PiecesJointes.joindre(piece(), "pieces/piece_7_x.pdf")
        assertTrue(jointe.fournie)
        assertEquals("pieces/piece_7_x.pdf", jointe.fichier)
        val retiree = PiecesJointes.retirer(jointe)
        assertNull(retiree.fichier)
        assertTrue("l'original papier peut encore être apporté", retiree.fournie)
    }

    @Test
    fun `seuls l ATT et l auto-ecole joignent, et seulement pendant la preparation`() {
        assertTrue(PiecesJointes.peutJoindre(Role.AUTO_ECOLE, StatutDossier.BROUILLON))
        assertTrue(PiecesJointes.peutJoindre(Role.AUTO_ECOLE, StatutDossier.INCOMPLET))
        assertTrue(PiecesJointes.peutJoindre(Role.ADMIN_ATT, StatutDossier.BROUILLON))
        assertFalse("un dossier soumis est figé pendant la décision", PiecesJointes.peutJoindre(Role.AUTO_ECOLE, StatutDossier.SOUMIS))
        assertFalse(PiecesJointes.peutJoindre(Role.AUTO_ECOLE, StatutDossier.VALIDE))
        assertFalse(PiecesJointes.peutJoindre(Role.CANDIDAT, StatutDossier.BROUILLON))
        assertFalse(PiecesJointes.peutJoindre(Role.EXAMINATEUR, StatutDossier.BROUILLON))
    }

    @Test
    fun `photo ou PDF, pas vide, pas trop lourd`() {
        assertEquals("jpg", PiecesJointes.extensionPour("image/jpeg"))
        assertEquals("png", PiecesJointes.extensionPour("image/png"))
        assertEquals("pdf", PiecesJointes.extensionPour("application/pdf"))
        assertNull(PiecesJointes.extensionPour("application/msword"))
        assertNull(PiecesJointes.extensionPour(null))

        assertNull(PiecesJointes.verifierFichier("image/jpeg", 2_000_000))
        assertNotNull(PiecesJointes.verifierFichier("video/mp4", 2_000_000))
        assertNotNull(PiecesJointes.verifierFichier("application/pdf", 0))
        assertNotNull(PiecesJointes.verifierFichier("application/pdf", PiecesJointes.TAILLE_MAX_OCTETS + 1))
    }

    @Test
    fun `chaque ajout a un nom neuf dans le dossier des pieces`() {
        val chemin = PiecesJointes.cheminCopie(7, "2026-09-18T10:15:30", "jpg")
        assertEquals("pieces/piece_7_20260918T101530.jpg", chemin)
        assertTrue(PiecesJointes.estPdf("pieces/piece_7_x.PDF"))
        assertFalse(PiecesJointes.estPdf(chemin))
    }

    @Test
    fun `le candidat ne voit pas un dossier en brouillon`() {
        assertFalse(ReglesConsultation.dossierVisible(Role.CANDIDAT, StatutDossier.BROUILLON))
        assertTrue(ReglesConsultation.dossierVisible(Role.CANDIDAT, StatutDossier.SOUMIS))
        assertTrue(ReglesConsultation.dossierVisible(Role.CANDIDAT, StatutDossier.INCOMPLET))
        assertTrue(ReglesConsultation.dossierVisible(Role.AUTO_ECOLE, StatutDossier.BROUILLON))
        assertTrue(ReglesConsultation.dossierVisible(Role.ADMIN_ATT, StatutDossier.BROUILLON))
    }
}
