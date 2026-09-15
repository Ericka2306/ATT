package mg.itu.att.metier

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidationAutoEcoleTest {

    @Test
    fun `une fiche complete est acceptee`() {
        assertNull(ValidationAutoEcole.validerFiche("Auto-école Soarano", 1, "Gare de Soarano", emptyList()))
    }

    @Test
    fun `le nom la region et l adresse sont obligatoires`() {
        assertNotNull(ValidationAutoEcole.validerFiche("", 1, "Adresse", emptyList()))
        assertNotNull(ValidationAutoEcole.validerFiche("Nom", null, "Adresse", emptyList()))
        assertNotNull(ValidationAutoEcole.validerFiche("Nom", 1, "  ", emptyList()))
    }

    @Test
    fun `le doublon de nom dans la meme region est refuse sans tenir compte de la casse`() {
        val erreur = ValidationAutoEcole.validerFiche("auto-école soarano", 1, "Adresse", listOf("Auto-École Soarano"))
        assertEquals("Une auto-école porte déjà ce nom dans cette région.", erreur)
    }

    @Test
    fun `le meme nom dans une autre region est accepte`() {
        // La liste reçue ne contient que les noms de la région choisie : le ViewModel filtre avant.
        assertNull(ValidationAutoEcole.validerFiche("Auto-école Soarano", 2, "Adresse", emptyList()))
    }

    @Test
    fun `un compte valide est accepte`() {
        assertNull(ValidationAutoEcole.validerCompte("soarano", "MotDePasse1", listOf("admin")))
    }

    @Test
    fun `identifiant court avec espace ou deja pris est refuse`() {
        assertNotNull(ValidationAutoEcole.validerCompte("ab", "MotDePasse1", emptyList()))
        assertNotNull(ValidationAutoEcole.validerCompte("auto ecole", "MotDePasse1", emptyList()))
        assertNotNull(ValidationAutoEcole.validerCompte("Admin", "MotDePasse1", listOf("admin")))
    }

    @Test
    fun `mot de passe trop court refuse`() {
        assertNotNull(ValidationAutoEcole.validerCompte("soarano", "court", emptyList()))
    }
}
