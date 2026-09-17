package mg.itu.att.metier

import mg.itu.att.data.StatutPresence

/**
 * Règles de l'appel (UC08, cadrage §6 : présent, absent, en retard, réorganisation configurable).
 * Fonctions pures ; la tolérance et la règle d'absence viennent de la configuration.
 */
object ReglesPresence {

    /** Valeurs possibles de la règle `REGLE_ABSENCE`. */
    const val REPORT_AUTO = "REPORT_AUTO"
    const val NOUVELLE_INSCRIPTION = "NOUVELLE_INSCRIPTION"

    /** Minutes écoulées entre l'heure de convocation et l'heure d'arrivée (négatif si en avance). */
    fun minutesDeRetard(heureConvocation: String, heureArrivee: String): Int {
        fun minutes(h: String): Int = h.split(':').let { it[0].toInt() * 60 + it[1].toInt() }
        return minutes(heureArrivee) - minutes(heureConvocation)
    }

    /** Le statut à enregistrer quand un candidat se présente : PRESENT dans la tolérance, EN_RETARD au-delà. */
    fun statutArrivee(heureConvocation: String, heureArrivee: String, toleranceMin: Int): StatutPresence =
        if (minutesDeRetard(heureConvocation, heureArrivee) > toleranceMin) StatutPresence.EN_RETARD else StatutPresence.PRESENT

    /** Vrai si, selon la règle configurée, un absent est automatiquement reporté (sinon il doit se réinscrire). */
    fun absentReporteAutomatiquement(regleAbsence: String): Boolean = regleAbsence.trim().equals(REPORT_AUTO, ignoreCase = true)

    /** Les transitions autorisées depuis un statut, pour l'ATT pendant la session. */
    fun transitionsPossibles(statut: StatutPresence): List<StatutPresence> = when (statut) {
        StatutPresence.EN_ATTENTE -> listOf(StatutPresence.PRESENT, StatutPresence.ABSENT)
        StatutPresence.EN_RETARD -> listOf(StatutPresence.PRESENT, StatutPresence.ABSENT)
        StatutPresence.PRESENT, StatutPresence.ABSENT -> listOf(StatutPresence.EN_ATTENTE)
        StatutPresence.EN_COURS, StatutPresence.TERMINE -> emptyList() // gérés par l'évaluation (C8, C9)
    }
}
