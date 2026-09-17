package mg.itu.att.metier

import mg.itu.att.data.TypeValeur
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidationConfigurationTest {

    @Test
    fun `categorie valide et doublon de code refuse`() {
        assertNull(ValidationConfiguration.validerCategorie("F", "Tracteurs", "18", listOf("A", "B")))
        assertNotNull(ValidationConfiguration.validerCategorie("b", "Doublon", "", listOf("A", "B")))
        assertNotNull(ValidationConfiguration.validerCategorie("F", "Tracteurs", "abc", emptyList()))
    }

    @Test
    fun `bareme, seuil borne par la note max`() {
        assertNull(ValidationConfiguration.validerBareme("30", "20"))
        assertNotNull(ValidationConfiguration.validerBareme("30", "31"))
        assertNotNull(ValidationConfiguration.validerBareme("0", "0"))
        assertNotNull(ValidationConfiguration.validerBareme("30", "x"))
    }

    @Test
    fun `regle, la valeur respecte le type`() {
        assertNull(ValidationConfiguration.validerRegle("25", TypeValeur.ENTIER))
        assertNotNull(ValidationConfiguration.validerRegle("25.5", TypeValeur.ENTIER))
        assertNull(ValidationConfiguration.validerRegle("true", TypeValeur.BOOLEEN))
        assertNotNull(ValidationConfiguration.validerRegle("oui", TypeValeur.BOOLEEN))
        assertNotNull(ValidationConfiguration.validerRegle("  ", TypeValeur.TEXTE))
    }

    @Test
    fun `question orale, enonce et points positifs`() {
        assertNull(ValidationConfiguration.validerQuestion("Q ?", "1"))
        assertNull(ValidationConfiguration.validerQuestion("Q ?", "2,5"))
        assertNotNull(ValidationConfiguration.validerQuestion("", "1"))
        assertNotNull(ValidationConfiguration.validerQuestion("Q ?", "0"))
        assertNotNull(ValidationConfiguration.validerQuestion("Q ?", "abc"))
    }

    @Test
    fun `epreuve critere centre`() {
        assertNull(ValidationConfiguration.validerEpreuve("MANOEUVRES", "Manœuvres", "2", "", listOf("THEORIE")))
        assertNotNull(ValidationConfiguration.validerEpreuve("theorie", "x", "1", "", listOf("THEORIE")))
        assertNull(ValidationConfiguration.validerCritere("Démarrage en côte", "2"))
        assertNotNull(ValidationConfiguration.validerCritere("", "2"))
        assertNull(ValidationConfiguration.validerCentre("Centre Soarano", 1, "Gare de Soarano", "40"))
        assertNotNull(ValidationConfiguration.validerCentre("Centre", null, "Adresse", ""))
    }
}
