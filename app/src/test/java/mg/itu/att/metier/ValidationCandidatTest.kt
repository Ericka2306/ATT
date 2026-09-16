package mg.itu.att.metier

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationCandidatTest {

    @Test
    fun `une fiche complete est acceptee`() {
        assertNull(ValidationCandidat.validerFiche("RAKOTO", "Hery", "2004-05-17", 1))
    }

    @Test
    fun `nom prenom auto-ecole obligatoires`() {
        assertNotNull(ValidationCandidat.validerFiche("", "Hery", "2004-05-17", 1))
        assertNotNull(ValidationCandidat.validerFiche("RAKOTO", " ", "2004-05-17", 1))
        assertNotNull(ValidationCandidat.validerFiche("RAKOTO", "Hery", "2004-05-17", null))
    }

    @Test
    fun `date de naissance au format ISO`() {
        assertTrue(ValidationCandidat.dateValide("2004-05-17"))
        assertFalse(ValidationCandidat.dateValide("17/05/2004"))
        assertFalse(ValidationCandidat.dateValide("2004-13-01"))
        assertFalse(ValidationCandidat.dateValide("2004-05-32"))
        assertFalse(ValidationCandidat.dateValide(""))
        assertNotNull(ValidationCandidat.validerFiche("RAKOTO", "Hery", "17/05/2004", 1))
    }
}
