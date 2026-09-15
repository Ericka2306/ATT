package mg.itu.att.ui.accueil

import mg.itu.att.data.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Le menu de chaque rôle respecte la matrice des permissions (docs/01 §6). */
class MenuParRoleTest {

    @Test
    fun `chaque role a au moins une entree`() {
        for (role in Role.entries) {
            assertTrue("menu vide pour $role", menuPour(role).isNotEmpty())
        }
    }

    @Test
    fun `seul le super admin voit la configuration`() {
        assertTrue(menuPour(Role.SUPER_ADMIN).any { it.route == Routes.CONFIGURATION })
        for (role in Role.entries.filter { it != Role.SUPER_ADMIN }) {
            assertFalse("$role ne doit pas voir la configuration", menuPour(role).any { it.route == Routes.CONFIGURATION })
        }
    }

    @Test
    fun `l auto-ecole et le candidat ne voient ni les dossiers a traiter ni l historique`() {
        for (role in listOf(Role.AUTO_ECOLE, Role.CANDIDAT)) {
            val routes = menuPour(role).map { it.route }
            assertFalse(routes.contains(Routes.DOSSIERS))
            assertFalse(routes.contains(Routes.HISTORIQUE))
            assertFalse(routes.contains(Routes.AUTO_ECOLES))
        }
    }

    @Test
    fun `le candidat ne voit que son parcours`() {
        assertEquals(listOf(Routes.PARCOURS), menuPour(Role.CANDIDAT).map { it.route })
    }

    @Test
    fun `les routes du menu sont uniques par role`() {
        for (role in Role.entries) {
            val routes = menuPour(role).map { it.route }
            assertEquals("routes en doublon pour $role", routes.distinct().size, routes.size)
        }
    }
}
