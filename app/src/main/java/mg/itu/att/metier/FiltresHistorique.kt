package mg.itu.att.metier

import mg.itu.att.data.EntitesHistorique
import mg.itu.att.data.Historique
import mg.itu.att.data.Role
import java.util.Calendar

/**
 * Consultation de l'historique (UC14) : qui peut le lire, et le filtrage par objet, utilisateur et période.
 * Fonctions pures, sans Android ; la date du jour est passée en paramètre pour être testable.
 */
object FiltresHistorique {

    /** Périodes proposées à l'écran. */
    enum class Periode { TOUT, AUJOURD_HUI, SEPT_JOURS, TRENTE_JOURS }

    /** Seuls le Super Admin et l'Admin ATT lisent l'historique (docs/01 §6). */
    fun peutConsulter(role: Role): Boolean = role == Role.SUPER_ADMIN || role == Role.ADMIN_ATT

    /** "AAAA-MM-JJ" moins [jours] jours ; texte inattendu rendu tel quel. */
    fun joursAvant(dateIso: String, jours: Int): String {
        val parties = dateIso.split('-').mapNotNull { it.toIntOrNull() }
        if (parties.size != 3) return dateIso
        val c = Calendar.getInstance()
        c.clear()
        c.set(parties[0], parties[1] - 1, parties[2])
        c.add(Calendar.DAY_OF_MONTH, -jours)
        return "%04d-%02d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }

    /** Première date incluse dans la période, ou null pour « tout ». « 7 jours » = aujourd'hui et les 6 jours précédents. */
    fun dateDebut(periode: Periode, aujourdHui: String): String? = when (periode) {
        Periode.TOUT -> null
        Periode.AUJOURD_HUI -> aujourdHui
        Periode.SEPT_JOURS -> joursAvant(aujourdHui, 6)
        Periode.TRENTE_JOURS -> joursAvant(aujourdHui, 29)
    }

    /**
     * Les lignes qui correspondent aux filtres, dans l'ordre reçu (le DAO trie déjà du plus récent au plus ancien).
     * Un filtre null n'est pas appliqué.
     */
    fun filtrer(
        lignes: List<Historique>,
        entite: String?,
        utilisateurId: Int?,
        periode: Periode,
        aujourdHui: String,
    ): List<Historique> {
        val debut = dateDebut(periode, aujourdHui)
        return lignes.filter { h ->
            (entite == null || h.entite == entite) &&
                (utilisateurId == null || h.utilisateurId == utilisateurId) &&
                (debut == null || h.dateHeure.substringBefore('T') >= debut)
        }
    }

    /** Les objets présents dans l'historique, triés par libellé : les choix du filtre « Objet ». */
    fun entitesPresentes(lignes: List<Historique>): List<String> =
        lignes.map { it.entite }.distinct().sortedBy { libelleEntite(it) }

    /** Libellé affichable d'un nom d'entité de l'historique. */
    fun libelleEntite(entite: String): String = when (entite) {
        EntitesHistorique.AUTO_ECOLE -> "Auto-école"
        EntitesHistorique.UTILISATEUR -> "Compte"
        EntitesHistorique.EXAMINATEUR -> "Examinateur"
        EntitesHistorique.CANDIDAT -> "Candidat"
        EntitesHistorique.DOSSIER -> "Dossier"
        EntitesHistorique.SESSION -> "Session"
        EntitesHistorique.INSCRIPTION -> "Inscription"
        EntitesHistorique.PRESENCE -> "Présence"
        EntitesHistorique.TENTATIVE -> "Passage"
        EntitesHistorique.EVALUATION -> "Évaluation"
        EntitesHistorique.RESULTAT -> "Résultat"
        EntitesHistorique.REGLE_CONFIG -> "Règle"
        EntitesHistorique.CATEGORIE -> "Catégorie"
        EntitesHistorique.TYPE_EPREUVE -> "Épreuve"
        EntitesHistorique.BAREME -> "Barème"
        EntitesHistorique.QUESTION -> "Question"
        EntitesHistorique.CRITERE -> "Critère"
        EntitesHistorique.CENTRE -> "Centre"
        else -> entite
    }
}
