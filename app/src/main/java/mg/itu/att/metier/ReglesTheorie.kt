package mg.itu.att.metier

import mg.itu.att.data.Bareme
import mg.itu.att.data.Question
import kotlin.random.Random

/** Valeurs possibles de la règle `MODE_THEORIE` (Q1 bis). */
object ModesTheorie {
    /** L'application tire le sujet au sort à l'ouverture et le fige. */
    const val TIRAGE = "TIRAGE"

    /** L'examinateur choisit les questions au fil de l'eau, parmi des propositions. */
    const val DIRECT = "DIRECT"
}

/** Une ligne de la feuille d'examen à contrôler : les points maximums de la question et les points saisis (texte). */
data class NoteQuestion(val pointsMax: Double, val pointsSaisis: String)

/**
 * Règles de l'épreuve théorique orale (UC09, étape C9) : tirage du sujet, propositions en mode direct,
 * contrôle des points attribués, contrôles d'ouverture et de fin. Fonctions pures ; le hasard est injecté
 * (`Random`) pour être testable. Aucun nombre de questions ni seuil ici : tout vient du barème configuré.
 */
object ReglesTheorie {

    /** Somme des points des questions posées. */
    fun pointsPoses(questions: List<Question>): Double = questions.sumOf { it.points }

    /**
     * Tire au sort un sujet parmi les questions actives : on mélange, puis on prend chaque question
     * tant qu'elle tient dans la note maximale du barème. Le total ne dépasse jamais `noteMax` ;
     * il l'atteint dès que la banque le permet. Jamais deux fois la même question.
     */
    fun tirerSujet(questionsActives: List<Question>, noteMax: Double, aleatoire: Random = Random.Default): List<Question> {
        val sujet = mutableListOf<Question>()
        var total = 0.0
        for (q in questionsActives.filter { it.actif && it.points > 0 }.shuffled(aleatoire)) {
            if (total >= noteMax) break
            if (total + q.points <= noteMax) {
                sujet += q
                total += q.points
            }
        }
        return sujet
    }

    /** Questions encore proposables en mode direct : actives, pas encore posées, et qui tiennent dans les points restants. */
    fun proposables(questionsActives: List<Question>, idsPosees: Set<Int>, pointsRestants: Double, pointsVoulus: Double? = null): List<Question> =
        questionsActives.filter {
            it.actif && it.id !in idsPosees && it.points > 0 && it.points <= pointsRestants && (pointsVoulus == null || it.points == pointsVoulus)
        }

    /** Une proposition au hasard parmi les [proposables], ou null s'il n'en reste aucune. */
    fun proposer(questionsActives: List<Question>, idsPosees: Set<Int>, pointsRestants: Double, pointsVoulus: Double? = null, aleatoire: Random = Random.Default): Question? =
        proposables(questionsActives, idsPosees, pointsRestants, pointsVoulus).let { if (it.isEmpty()) null else it[aleatoire.nextInt(it.size)] }

    /** Les valeurs de points encore disponibles parmi les proposables, pour filtrer les propositions. */
    fun pointsDisponibles(questionsActives: List<Question>, idsPosees: Set<Int>, pointsRestants: Double): List<Double> =
        proposables(questionsActives, idsPosees, pointsRestants).map { it.points }.distinct().sorted()

    /**
     * Peut-on ouvrir l'épreuve théorique ?
     * @return un message d'erreur qui renvoie à la configuration, ou null si tout est prêt.
     */
    fun verifierOuverture(bareme: Bareme?, questionsActives: List<Question>, mode: String): String? = when {
        bareme == null -> "Aucun barème courant pour cette épreuve : à créer dans Configuration → Catégories et épreuves."
        questionsActives.none { it.actif } -> "Aucune question active pour cette épreuve : à saisir dans Configuration → Catégories et épreuves."
        mode != ModesTheorie.TIRAGE && mode != ModesTheorie.DIRECT -> "Règle MODE_THEORIE inconnue (« $mode ») : attendu TIRAGE ou DIRECT, dans Configuration → Règles."
        else -> null
    }

    /** Points saisis pour une question, ou null si le texte est vide ou n'est pas un nombre entre 0 et le maximum. */
    fun pointsValides(pointsSaisis: String, pointsMax: Double): Double? =
        pointsSaisis.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 && it <= pointsMax }

    /** Total des points attribués sur les lignes correctement notées. */
    fun totalAttribue(notes: List<NoteQuestion>): Double = notes.sumOf { pointsValides(it.pointsSaisis, it.pointsMax) ?: 0.0 }

    /** Nombre de questions posées dont les points ne sont pas (ou mal) saisis. */
    fun sansNote(notes: List<NoteQuestion>): Int = notes.count { pointsValides(it.pointsSaisis, it.pointsMax) == null }

    /**
     * Contrôle avant « Terminer l'épreuve » : au moins une question posée, et des points valides sur chacune
     * (la feuille d'examen porte un nombre de points par question, 0 compris).
     * @return un message d'erreur, ou null si l'épreuve peut être close.
     */
    fun verifierFin(notes: List<NoteQuestion>): String? = when {
        notes.isEmpty() -> "Aucune question n'a été posée."
        sansNote(notes) > 0 -> "${sansNote(notes)} question(s) sans points : saisissez un nombre entre 0 et le maximum de la question (0 si la réponse est fausse)."
        else -> null
    }
}
