package mg.itu.att.ui.accueil

import mg.itu.att.data.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Le menu de chaque rôle respecte la matrice des permissions (docs/01 §6). */
class MenuParRoleTest {

    @Test
    fun `chaque role a au moins une entree et peut changer son mot de passe`() {
        for (role in Role.entries) {
            assertTrue("menu vide pour $role", menuPour(role).isNotEmpty())
            assertTrue("$role doit pouvoir changer son mot de passe", menuPour(role).any { it.route == Routes.MOT_DE_PASSE })
        }
    }

    @Test
    fun `seul le super admin voit la configuration et les comptes`() {
        assertTrue(menuPour(Role.SUPER_ADMIN).any { it.route == Routes.CONFIGURATION })
        assertTrue(menuPour(Role.SUPER_ADMIN).any { it.route == Routes.COMPTES })
        for (role in Role.entries.filter { it != Role.SUPER_ADMIN }) {
            val routes = menuPour(role).map { it.route }
            assertFalse("$role ne doit pas voir la configuration", routes.contains(Routes.CONFIGURATION))
            assertFalse("$role ne doit pas voir les comptes", routes.contains(Routes.COMPTES))
        }
    }

    @Test
    fun `seule l ATT gere les auto-ecoles et les examinateurs`() {
        for (role in listOf(Role.SUPER_ADMIN, Role.ADMIN_ATT)) {
            assertTrue(menuPour(role).any { it.route == Routes.EXAMINATEURS })
            assertTrue(menuPour(role).any { it.route == Routes.AUTO_ECOLES })
        }
        for (role in listOf(Role.AUTO_ECOLE, Role.EXAMINATEUR, Role.CANDIDAT)) {
            val routes = menuPour(role).map { it.route }
            assertFalse(routes.contains(Routes.EXAMINATEURS))
            assertFalse(routes.contains(Routes.AUTO_ECOLES))
            assertFalse(routes.contains(Routes.DOSSIERS))
            assertFalse(routes.contains(Routes.HISTORIQUE))
        }
    }

    @Test
    fun `le candidat ne voit que son parcours et son mot de passe`() {
        assertEquals(listOf(Routes.PARCOURS, Routes.MOT_DE_PASSE), menuPour(Role.CANDIDAT).map { it.route })
    }

    @Test
    fun `les routes du menu sont uniques par role`() {
        for (role in Role.entries) {
            val routes = menuPour(role).map { it.route }
            assertEquals("routes en doublon pour $role", routes.distinct().size, routes.size)
        }
    }
}
