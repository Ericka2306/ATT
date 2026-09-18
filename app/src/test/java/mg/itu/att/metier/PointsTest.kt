package mg.itu.att.metier

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Points saisis par l'examinateur (théorie et conduite) : ce qui est accepté, ce qui ne l'est pas. */
class PointsTest {

    @Test
    fun `un nombre entre zero et le maximum est accepte`() {
        assertEquals(0.0, pointsValides("0", 10.0))
        assertEquals(7.0, pointsValides("7", 10.0))
        assertEquals(10.0, pointsValides("10", 10.0))
    }

    @Test
    fun `la virgule est acceptee, les espaces sont ignores`() {
        assertEquals(2.5, pointsValides("2,5", 5.0))
        assertEquals(2.5, pointsValides(" 2.5 ", 5.0))
    }

    @Test
    fun `hors bornes, vide ou non numerique, rien n est retenu`() {
        assertNull(pointsValides("11", 10.0))
        assertNull(pointsValides("-1", 10.0))
        assertNull(pointsValides("", 10.0))
        assertNull(pointsValides("abc", 10.0))
    }
}
