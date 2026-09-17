package mg.itu.att.metier

/**
 * Après un échec (UC11, cadrage §9) : quelles épreuves le candidat doit repasser, et à partir de quand.
 * Les délais et la durée de conservation d'une épreuve réussie viennent de la configuration (R3),
 * jamais du code. Fonctions pures, testées par JUnit.
 */
object ReglesRepassage {

    /** Ce que l'on sait d'une épreuve pour un candidat : sa dernière réussite, et la date du dernier passage. */
    data class EtatEpreuve(
        val typeEpreuveId: Int,
        val libelle: String,
        /** Date ISO du résultat réussi le plus récent, null si l'épreuve n'a jamais été réussie. */
        val dateReussite: String?,
        /** Date ISO du dernier passage, réussi ou non. */
        val dateDernierPassage: String?,
    )

    /** Une épreuve à repasser, avec la raison et la date à partir de laquelle une nouvelle inscription est possible. */
    data class ARepasser(val typeEpreuveId: Int, val libelle: String, val raison: String, val inscriptibleLe: String?)

    /**
     * Une épreuve réussie reste acquise pendant `conservationJours` (0 = sans limite).
     * @return vrai si la réussite est encore valable à la date [aujourdHui].
     */
    fun reussiteEncoreValable(dateReussite: String?, conservationJours: Int, aujourdHui: String): Boolean = when {
        dateReussite == null -> false
        conservationJours <= 0 -> true
        else -> ReglesInscription.joursEntre(dateReussite, aujourdHui) < conservationJours
    }

    /** Date à partir de laquelle le candidat peut se réinscrire après un passage (délai configurable). */
    fun inscriptibleLe(dateDernierPassage: String?, delaiJours: Int): String? = when {
        dateDernierPassage == null -> null
        delaiJours <= 0 -> dateDernierPassage
        else -> FiltresHistorique.joursAvant(dateDernierPassage, -delaiJours)
    }

    /**
     * Les épreuves que le candidat doit repasser : celles jamais réussies, et celles dont la réussite
     * n'est plus valable (conservation expirée).
     */
    fun epreuvesARepasser(
        epreuves: List<EtatEpreuve>,
        conservationJours: Int,
        delaiRepassageJours: Int,
        aujourdHui: String,
    ): List<ARepasser> = epreuves.mapNotNull { e ->
        when {
            reussiteEncoreValable(e.dateReussite, conservationJours, aujourdHui) -> null
            e.dateReussite != null -> ARepasser(
                e.typeEpreuveId, e.libelle,
                "réussite du ${formatDate(e.dateReussite)} expirée (conservation $conservationJours jours)",
                inscriptibleLe(e.dateDernierPassage, delaiRepassageJours),
            )
            else -> ARepasser(
                e.typeEpreuveId, e.libelle,
                if (e.dateDernierPassage == null) "épreuve jamais passée" else "échec du ${formatDate(e.dateDernierPassage)}",
                inscriptibleLe(e.dateDernierPassage, delaiRepassageJours),
            )
        }
    }
}
