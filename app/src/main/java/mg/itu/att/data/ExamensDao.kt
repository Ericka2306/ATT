package mg.itu.att.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO des examens. Aucun @Delete ici, et aucun @Update sur Resultat :
 * une tentative ou un résultat ne s'écrase jamais (cadrage §9).
 */

@Dao
interface TentativeDao {
    @Query("SELECT * FROM tentatives ORDER BY id ASC")
    fun toutes(): Flow<List<Tentative>>

    @Query("SELECT * FROM tentatives WHERE candidatId = :candidatId ORDER BY dateHeure DESC")
    fun parCandidat(candidatId: Int): Flow<List<Tentative>>

    @Query("SELECT * FROM tentatives WHERE inscriptionId = :inscriptionId ORDER BY id ASC")
    suspend fun parInscription(inscriptionId: Int): List<Tentative>

    /** Les tentatives ouvertes dans une session (jointure sur les inscriptions). */
    @Query("SELECT tentatives.* FROM tentatives JOIN inscriptions ON inscriptions.id = tentatives.inscriptionId WHERE inscriptions.sessionId = :sessionId ORDER BY tentatives.id ASC")
    fun parSession(sessionId: Int): Flow<List<Tentative>>

    @Query("SELECT * FROM tentatives WHERE id = :id")
    fun parIdEnDirect(id: Int): Flow<Tentative?>

    @Query("SELECT * FROM tentatives WHERE candidatId = :candidatId AND typeEpreuveId = :typeEpreuveId ORDER BY numero ASC")
    suspend fun listePourCandidatEtEpreuve(candidatId: Int, typeEpreuveId: Int): List<Tentative>

    @Query("SELECT * FROM tentatives WHERE id = :id")
    suspend fun parId(id: Int): Tentative?

    /** Numéro de la dernière tentative du candidat pour cette épreuve (0 si aucune). */
    @Query("SELECT COALESCE(MAX(numero), 0) FROM tentatives WHERE candidatId = :candidatId AND typeEpreuveId = :typeEpreuveId")
    suspend fun dernierNumero(candidatId: Int, typeEpreuveId: Int): Int

    @Insert
    suspend fun inserer(tentative: Tentative): Long

    @Update
    suspend fun modifier(tentative: Tentative)
}

@Dao
interface EvaluationDao {
    @Query("SELECT * FROM evaluations WHERE tentativeId = :tentativeId LIMIT 1")
    suspend fun parTentative(tentativeId: Int): Evaluation?

    @Query("SELECT * FROM evaluations WHERE tentativeId = :tentativeId LIMIT 1")
    fun parTentativeEnDirect(tentativeId: Int): Flow<Evaluation?>

    @Query("SELECT * FROM evaluations WHERE id = :id")
    suspend fun parId(id: Int): Evaluation?

    @Insert
    suspend fun inserer(evaluation: Evaluation): Long

    @Update
    suspend fun modifier(evaluation: Evaluation)
}

@Dao
interface ReponseCandidatDao {
    @Query("SELECT * FROM reponses_candidat WHERE evaluationId = :evaluationId ORDER BY questionId ASC")
    fun parEvaluation(evaluationId: Int): Flow<List<ReponseCandidat>>

    @Query("SELECT * FROM reponses_candidat WHERE evaluationId = :evaluationId ORDER BY questionId ASC")
    suspend fun listePourEvaluation(evaluationId: Int): List<ReponseCandidat>

    /** Les lignes dans l'ordre où les questions ont été posées (l'ordre du sujet). */
    @Query("SELECT * FROM reponses_candidat WHERE evaluationId = :evaluationId ORDER BY id ASC")
    fun parEvaluationDansLOrdre(evaluationId: Int): Flow<List<ReponseCandidat>>

    @Query("SELECT * FROM reponses_candidat WHERE evaluationId = :evaluationId AND questionId = :questionId LIMIT 1")
    suspend fun pourQuestion(evaluationId: Int, questionId: Int): ReponseCandidat?

    @Insert
    suspend fun inserer(reponse: ReponseCandidat): Long

    @Insert
    suspend fun insererToutes(reponses: List<ReponseCandidat>)

    @Update
    suspend fun modifier(reponse: ReponseCandidat)
}

@Dao
interface EvaluationCritereDao {
    @Query("SELECT * FROM evaluations_critere WHERE evaluationId = :evaluationId ORDER BY critereId ASC")
    fun parEvaluation(evaluationId: Int): Flow<List<EvaluationCritere>>

    @Query("SELECT * FROM evaluations_critere WHERE evaluationId = :evaluationId ORDER BY critereId ASC")
    suspend fun listePourEvaluation(evaluationId: Int): List<EvaluationCritere>

    @Query("SELECT * FROM evaluations_critere WHERE evaluationId = :evaluationId AND critereId = :critereId LIMIT 1")
    suspend fun pourCritere(evaluationId: Int, critereId: Int): EvaluationCritere?

    @Insert
    suspend fun inserer(evaluationCritere: EvaluationCritere): Long

    @Insert
    suspend fun insererToutes(lignes: List<EvaluationCritere>)

    @Update
    suspend fun modifier(evaluationCritere: EvaluationCritere)
}

@Dao
interface ResultatDao {
    /** Tous les résultats d'une tentative, du plus récent au plus ancien : l'historique des corrections. */
    @Query("SELECT * FROM resultats WHERE tentativeId = :tentativeId ORDER BY id DESC")
    fun parTentative(tentativeId: Int): Flow<List<Resultat>>

    /** Le résultat courant d'une tentative = le plus récent non annulé. */
    @Query("SELECT * FROM resultats WHERE tentativeId = :tentativeId AND statut != 'ANNULE' ORDER BY id DESC LIMIT 1")
    suspend fun courantPourTentative(tentativeId: Int): Resultat?

    @Query("SELECT * FROM resultats WHERE statut = :statut ORDER BY dateCalcul ASC")
    fun parStatut(statut: StatutResultat): Flow<List<Resultat>>

    @Query("SELECT * FROM resultats WHERE id = :id")
    suspend fun parId(id: Int): Resultat?

    /** Le dernier résultat réussi (non annulé) d'un candidat pour une épreuve : sert à « théorie réussie avant conduite ». */
    @Query(
        "SELECT resultats.* FROM resultats JOIN tentatives ON tentatives.id = resultats.tentativeId " +
            "WHERE tentatives.candidatId = :candidatId AND tentatives.typeEpreuveId = :typeEpreuveId " +
            "AND resultats.reussi = 1 AND resultats.statut != 'ANNULE' ORDER BY resultats.id DESC LIMIT 1",
    )
    suspend fun dernierReussi(candidatId: Int, typeEpreuveId: Int): Resultat?

    /** Seule écriture autorisée : on ajoute, on ne modifie jamais. */
    @Insert
    suspend fun inserer(resultat: Resultat): Long
}
