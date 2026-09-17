package mg.itu.att.metier

import mg.itu.att.data.EntitesHistorique
import mg.itu.att.data.Historique
import mg.itu.att.data.Role
import mg.itu.att.metier.FiltresHistorique.Periode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FiltresHistoriqueTest {

    private fun ligne(id: Int, entite: String, utilisateurId: Int, dateHeure: String) =
        Historique(id = id, entite = entite, entiteId = id, action = "CREATION", utilisateurId = utilisateurId, dateHeure = dateHeure)

    private val lignes = listOf(
        ligne(1, EntitesHistorique.SESSION, 2, "2026-09-17T09:00:00"),
        ligne(2, EntitesHistorique.CANDIDAT, 3, "2026-09-15T14:30:00"),
        ligne(3, EntitesHistorique.CANDIDAT, 2, "2026-09-11T08:00:00"),
        ligne(4, EntitesHistorique.DOSSIER, 2, "2026-08-10T10:00:00"),
    )

    @Test
    fun `seuls le super admin et l admin ATT consultent l historique`() {
        assertTrue(FiltresHistorique.peutConsulter(Role.SUPER_ADMIN))
        assertTrue(FiltresHistorique.peutConsulter(Role.ADMIN_ATT))
        assertFalse(FiltresHistorique.peutConsulter(Role.AUTO_ECOLE))
        assertFalse(FiltresHistorique.peutConsulter(Role.EXAMINATEUR))
        assertFalse(FiltresHistorique.peutConsulter(Role.CANDIDAT))
    }

    @Test
    fun `jours avant, y compris en changeant de mois et d annee`() {
        assertEquals("2026-09-11", FiltresHistorique.joursAvant("2026-09-17", 6))
        assertEquals("2026-08-19", FiltresHistorique.joursAvant("2026-09-17", 29))
        assertEquals("2025-12-31", FiltresHistorique.joursAvant("2026-01-01", 1))
        assertEquals("pas une date", FiltresHistorique.joursAvant("pas une date", 3))
    }

    @Test
    fun `debut de periode`() {
        assertNull(FiltresHistorique.dateDebut(Periode.TOUT, "2026-09-17"))
        assertEquals("2026-09-17", FiltresHistorique.dateDebut(Periode.AUJOURD_HUI, "2026-09-17"))
        assertEquals("2026-09-11", FiltresHistorique.dateDebut(Periode.SEPT_JOURS, "2026-09-17"))
    }

    @Test
    fun `sans filtre, tout est garde dans l ordre`() {
        assertEquals(listOf(1, 2, 3, 4), FiltresHistorique.filtrer(lignes, null, null, Periode.TOUT, "2026-09-17").map { it.id })
    }

    @Test
    fun `filtres par objet, utilisateur et periode, combinables`() {
        assertEquals(listOf(2, 3), FiltresHistorique.filtrer(lignes, EntitesHistorique.CANDIDAT, null, Periode.TOUT, "2026-09-17").map { it.id })
        assertEquals(listOf(1, 3, 4), FiltresHistorique.filtrer(lignes, null, 2, Periode.TOUT, "2026-09-17").map { it.id })
        assertEquals(listOf(1), FiltresHistorique.filtrer(lignes, null, null, Periode.AUJOURD_HUI, "2026-09-17").map { it.id })
        // 7 jours : le 11 septembre est la première date incluse.
        assertEquals(listOf(1, 2, 3), FiltresHistorique.filtrer(lignes, null, null, Periode.SEPT_JOURS, "2026-09-17").map { it.id })
        assertEquals(listOf(3), FiltresHistorique.filtrer(lignes, EntitesHistorique.CANDIDAT, 2, Periode.SEPT_JOURS, "2026-09-17").map { it.id })
    }

    @Test
    fun `objets presents, sans doublon, avec leur libelle`() {
        assertEquals(listOf(EntitesHistorique.CANDIDAT, EntitesHistorique.DOSSIER, EntitesHistorique.SESSION), FiltresHistorique.entitesPresentes(lignes))
        assertEquals("Auto-école", FiltresHistorique.libelleEntite(EntitesHistorique.AUTO_ECOLE))
        assertEquals("Passage", FiltresHistorique.libelleEntite(EntitesHistorique.TENTATIVE))
        assertEquals("Inconnue", FiltresHistorique.libelleEntite("Inconnue"))
    }
}
