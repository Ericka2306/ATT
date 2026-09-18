package mg.itu.att.metier

import mg.itu.att.metier.DocumentsImpression.Convocation
import mg.itu.att.metier.DocumentsImpression.LigneAdmis
import mg.itu.att.metier.DocumentsImpression.LigneAppel
import mg.itu.att.metier.DocumentsImpression.Releve
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentsImpressionTest {

    private val edition = "2026-09-17"

    @Test
    fun `le texte saisi est echappe`() {
        assertEquals("RAKOTO &amp; fils", DocumentsImpression.echapper("RAKOTO & fils"))
        assertEquals("&lt;script&gt;", DocumentsImpression.echapper("<script>"))
    }

    @Test
    fun `chaque document porte la mention et la date d edition`() {
        val html = DocumentsImpression.page("Titre", "<p>corps</p>", edition)
        assertTrue(html.contains(DocumentsImpression.MENTION))
        assertTrue(html.contains("Édité le 17/09/2026"))
        assertTrue(html.startsWith("<!DOCTYPE html>"))
    }

    @Test
    fun `la convocation porte tout ce qu il faut pour se presenter`() {
        val html = DocumentsImpression.convocation(
            Convocation(
                nomCandidat = "RAKOTO Jean", dateNaissance = "2004-05-17", nomAutoEcole = "Auto-ecole Lalana",
                codeCategorie = "B", libelleEpreuve = "Épreuve théorique",
                nomCentre = "Centre ATT Soarano", adresseCentre = "Soarano Antananarivo",
                dateSession = "2026-09-17", heureConvocation = "08:00", heurePassageEstimee = "08:20",
                numeroAnonymat = "001", pieces = listOf("Acte de naissance", "5 photos d'identité"),
            ),
            edition,
        )
        assertTrue(html.contains("RAKOTO Jean"))
        assertTrue(html.contains("001"))
        assertTrue(html.contains("permis B — Épreuve théorique"))
        assertTrue(html.contains("Centre ATT Soarano, Soarano Antananarivo"))
        assertTrue(html.contains("17/09/2026"))
        assertTrue(html.contains("08:20"))
        assertTrue(html.contains("Acte de naissance"))
    }

    @Test
    fun `sans heure estimee, la convocation le dit`() {
        val html = DocumentsImpression.convocation(
            Convocation("RAKOTO Jean", "2004-05-17", "Lalana", "B", "Théorie", "Centre", "Adresse", "2026-09-17", "08:00", null, "001", emptyList()),
            edition,
        )
        assertTrue(html.contains("communiquée à l'appel"))
        assertTrue(html.contains("Aucune pièce particulière"))
    }

    @Test
    fun `la liste d appel ATT porte les noms, celle de l examinateur non`() {
        val lignes = listOf(LigneAppel("001", "RAKOTO Jean", "08:00"), LigneAppel("002", "RASOA Marie", null))
        val att = DocumentsImpression.listeAppel(lignes, "B", "Épreuve théorique", "Centre ATT Soarano", "2026-09-17", "08:00", avecNoms = true, dateEdition = edition)
        val examinateur = DocumentsImpression.listeAppel(lignes, "B", "Épreuve théorique", "Centre ATT Soarano", "2026-09-17", "08:00", avecNoms = false, dateEdition = edition)

        assertTrue(att.contains("RAKOTO Jean"))
        assertTrue(att.contains("<th>Candidat</th>"))
        assertFalse("la version examinateur ne doit pas nommer les candidats", examinateur.contains("RAKOTO Jean"))
        assertTrue(examinateur.contains("001"))
        assertTrue(examinateur.contains("Version examinateur"))
        // Colonnes à cocher pour l'appel papier.
        assertTrue(att.contains("<th>Présent</th>"))
        assertTrue(att.contains("<th>Absent</th>"))
    }

    @Test
    fun `la liste des admis compte les reussites`() {
        val lignes = listOf(
            LigneAdmis("001", "RAKOTO Jean", "20 / 30", true),
            LigneAdmis("002", "RASOA Marie", "12 / 30", false),
        )
        val html = DocumentsImpression.listeAdmis(lignes, "B", "Épreuve théorique", "Centre", "2026-09-17", "08:00", edition)
        assertTrue(html.contains("1 sur 2 résultat(s) validé(s)"))
        assertTrue(html.contains("Admis"))
        assertTrue(html.contains("Non admis"))
    }

    @Test
    fun `liste des admis vide`() {
        val html = DocumentsImpression.listeAdmis(emptyList(), "B", "Épreuve théorique", "Centre", "2026-09-17", "08:00", edition)
        assertTrue(html.contains("Aucun résultat validé"))
    }

    @Test
    fun `le releve porte la note, le resultat et la date de validation`() {
        val html = DocumentsImpression.releve(
            Releve("RAKOTO Jean", "2004-05-17", "Lalana", "B", "Épreuve théorique", 1, "20 / 30 (seuil 20)", true, "2026-09-17"),
            edition,
        )
        assertTrue(html.contains("20 / 30 (seuil 20)"))
        assertTrue(html.contains("Réussi"))
        assertTrue(html.contains("17/09/2026"))
        assertTrue(html.contains(DocumentsImpression.MENTION))
    }

    @Test
    fun `un releve non valide le signale`() {
        val html = DocumentsImpression.releve(
            Releve("RAKOTO Jean", "2004-05-17", "Lalana", "B", "Épreuve théorique", 2, "8 / 30 (seuil 20)", false, null),
            edition,
        )
        assertTrue(html.contains("en attente de validation"))
        assertTrue(html.contains("Échec"))
    }
}
