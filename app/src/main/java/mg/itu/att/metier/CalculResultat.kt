package mg.itu.att.metier

import mg.itu.att.data.Bareme
import mg.itu.att.data.StatutTentative
import kotlin.math.round

/**
 * Calcul du résultat d'un passage (UC10, cadrage §7) : la note vient de la saisie de l'examinateur,
 * le seuil et la note maximale viennent du **barème** (jamais codés en dur, R3).
 * Fonctions pures, sans Android, testées par JUnit.
 */
object CalculResultat {

    /** Une ligne notée de la feuille de théorie : les points attribués (null = non notée) et le maximum de la question. */
    data class LigneTheorie(val pointsAttribues: Double?, val pointsQuestion: Double)

    /** Une ligne de la grille de conduite : la note, le maximum du critère, et la faute éliminatoire éventuelle. */
    data class LigneConduite(val note: Double?, val pointsCritere: Double, val critereEliminatoire: Boolean, val fauteCochee: Boolean)

    /** Le calcul, avant écriture en base : la note rapportée au barème et la réussite. */
    data class Calcul(
        val noteObtenue: Double,
        val noteMax: Double,
        val seuil: Double,
        val reussi: Boolean,
        val fauteEliminatoire: Boolean,
        /** Total brut des points attribués et total des points proposés (sujet tiré ou grille), avant mise à l'échelle. */
        val totalBrut: Double,
        val totalPropose: Double,
    )

    /** Arrondi à deux décimales : une note affichée reste lisible (12.67 et non 12.666666). */
    fun arrondir(valeur: Double): Double = round(valeur * 100) / 100

    /**
     * Ramène un total à la note maximale du barème.
     * Le sujet de théorie ou la grille de conduite ne totalise pas forcément la note maximale
     * (décision C10 n° 1, Q2) : on applique une règle de trois. Un total proposé nul donne 0.
     */
    fun rapporterAuBareme(totalBrut: Double, totalPropose: Double, noteMax: Double): Double = when {
        totalPropose <= 0.0 -> 0.0
        totalPropose == noteMax -> arrondir(totalBrut)
        else -> arrondir(totalBrut * noteMax / totalPropose)
    }

    /** Théorie : somme des points attribués, les questions non notées comptant pour 0. */
    fun totalTheorie(lignes: List<LigneTheorie>): Double = arrondir(lignes.sumOf { it.pointsAttribues ?: 0.0 })

    /** Théorie : total des points posés (le sujet), qui sert de base au rapport au barème. */
    fun totalPoseTheorie(lignes: List<LigneTheorie>): Double = arrondir(lignes.sumOf { it.pointsQuestion })

    /** Conduite : une faute éliminatoire cochée met le critère à 0 (invariant docs/03). */
    fun totalConduite(lignes: List<LigneConduite>): Double =
        arrondir(lignes.sumOf { if (it.fauteCochee) 0.0 else (it.note ?: 0.0) })

    /** Conduite : total des points de la grille. */
    fun totalGrilleConduite(lignes: List<LigneConduite>): Double = arrondir(lignes.sumOf { it.pointsCritere })

    /** Vrai si au moins un critère éliminatoire a sa faute cochée : l'épreuve est perdue quel que soit le total. */
    fun fauteEliminatoire(lignes: List<LigneConduite>): Boolean = lignes.any { it.critereEliminatoire && it.fauteCochee }

    /**
     * Le calcul commun aux deux épreuves : note rapportée au barème, réussite si le seuil est atteint
     * **et** qu'aucune faute éliminatoire n'a été commise.
     */
    fun calculer(totalBrut: Double, totalPropose: Double, bareme: Bareme, fauteEliminatoire: Boolean = false): Calcul {
        val note = rapporterAuBareme(totalBrut, totalPropose, bareme.noteMax)
        return Calcul(
            noteObtenue = note,
            noteMax = bareme.noteMax,
            seuil = bareme.seuilReussite,
            reussi = !fauteEliminatoire && note >= bareme.seuilReussite,
            fauteEliminatoire = fauteEliminatoire,
            totalBrut = arrondir(totalBrut),
            totalPropose = arrondir(totalPropose),
        )
    }

    /** Calcul d'une épreuve théorique, à partir de la feuille d'examen. */
    fun calculerTheorie(lignes: List<LigneTheorie>, bareme: Bareme): Calcul =
        calculer(totalTheorie(lignes), totalPoseTheorie(lignes), bareme)

    /** Calcul d'une épreuve de conduite, à partir de la grille. */
    fun calculerConduite(lignes: List<LigneConduite>, bareme: Bareme): Calcul =
        calculer(totalConduite(lignes), totalGrilleConduite(lignes), bareme, fauteEliminatoire(lignes))

    /**
     * Peut-on calculer le résultat de ce passage ?
     * @return un message d'erreur, ou null si le calcul est possible (UC10 : barème présent, passage terminé).
     */
    fun verifierCalcul(statutTentative: StatutTentative?, bareme: Bareme?, lignesSaisies: Int): String? = when {
        statutTentative == null -> "Passage introuvable."
        statutTentative == StatutTentative.EN_COURS -> "L'épreuve n'est pas terminée : le résultat ne peut pas être calculé."
        statutTentative == StatutTentative.ANNULEE -> "Passage annulé : aucun résultat à calculer."
        bareme == null -> "Le barème de l'épreuve est introuvable : le résultat ne peut pas être calculé."
        bareme.seuilReussite <= 0.0 -> "Le barème n'a pas de seuil de réussite : à corriger dans Configuration → Catégories et épreuves."
        lignesSaisies == 0 -> "Aucune ligne saisie pour cette épreuve."
        else -> null
    }

    /** Une correction doit porter un motif et ne peut viser qu'un résultat encore vivant (R6 : on n'écrase jamais). */
    fun verifierCorrection(motif: String, statutResultat: mg.itu.att.data.StatutResultat?): String? = when {
        statutResultat == null -> "Résultat introuvable."
        statutResultat == mg.itu.att.data.StatutResultat.ANNULE -> "Ce résultat a déjà été remplacé par une correction."
        motif.isBlank() -> "Indiquez le motif de la correction (erreur de notation, réclamation…)."
        else -> null
    }
}
