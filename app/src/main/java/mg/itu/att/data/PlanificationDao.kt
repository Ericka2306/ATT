package mg.itu.att.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** DAO de la planification : sessions, créneaux, inscriptions, présences. */

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY date DESC, heureConvocation ASC")
    fun toutes(): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE date >= :dateIso ORDER BY date ASC, heureConvocation ASC")
    fun aVenir(dateIso: String): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE date = :dateIso ORDER BY heureConvocation ASC")
    fun duJour(dateIso: String): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE centreId = :centreId ORDER BY date DESC")
    fun parCentre(centreId: Int): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun parId(id: Int): Session?

    @Query("SELECT * FROM sessions WHERE id = :id")
    fun parIdEnDirect(id: Int): Flow<Session?>

    /** Sessions du même centre le même jour : détection de conflit (UC06). */
    @Query("SELECT * FROM sessions WHERE centreId = :centreId AND date = :dateIso AND statut != 'ANNULEE'")
    suspend fun memeCentreMemeJour(centreId: Int, dateIso: String): List<Session>

    @Insert
    suspend fun inserer(session: Session): Long

    @Update
    suspend fun modifier(session: Session)
}

@Dao
interface CreneauDao {
    @Query("SELECT * FROM creneaux WHERE sessionId = :sessionId ORDER BY ordre ASC")
    fun parSession(sessionId: Int): Flow<List<Creneau>>

    @Query("SELECT * FROM creneaux WHERE sessionId = :sessionId ORDER BY ordre ASC")
    suspend fun listePourSession(sessionId: Int): List<Creneau>

    @Query("SELECT * FROM creneaux WHERE id = :id")
    suspend fun parId(id: Int): Creneau?

    @Insert
    suspend fun insererTous(creneaux: List<Creneau>)

    @Update
    suspend fun modifier(creneau: Creneau)
}

@Dao
interface InscriptionDao {
    @Query("SELECT * FROM inscriptions ORDER BY id ASC")
    fun toutes(): Flow<List<Inscription>>

    @Query("SELECT * FROM inscriptions WHERE sessionId = :sessionId ORDER BY creneauId ASC, numeroAnonymat ASC")
    fun parSession(sessionId: Int): Flow<List<Inscription>>

    @Query("SELECT * FROM inscriptions WHERE sessionId = :sessionId ORDER BY id ASC")
    suspend fun listePourSession(sessionId: Int): List<Inscription>

    @Query("SELECT * FROM inscriptions WHERE candidatId = :candidatId ORDER BY dateInscription DESC")
    fun parCandidat(candidatId: Int): Flow<List<Inscription>>

    @Query("SELECT * FROM inscriptions WHERE candidatId = :candidatId ORDER BY id ASC")
    suspend fun listePourCandidat(candidatId: Int): List<Inscription>

    @Query("SELECT * FROM inscriptions WHERE id = :id")
    suspend fun parId(id: Int): Inscription?

    /** Nombre d'inscriptions qui comptent dans la capacité (UC07). */
    @Query("SELECT COUNT(*) FROM inscriptions WHERE sessionId = :sessionId AND statut IN ('DEMANDE', 'INSCRIT', 'CONFIRME')")
    suspend fun nombreActivesPourSession(sessionId: Int): Int

    @Query("SELECT COUNT(*) FROM inscriptions WHERE creneauId = :creneauId AND statut IN ('DEMANDE', 'INSCRIT', 'CONFIRME')")
    suspend fun nombreActivesPourCreneau(creneauId: Int): Int

    @Query("SELECT * FROM inscriptions WHERE candidatId = :candidatId AND sessionId = :sessionId LIMIT 1")
    suspend fun pourCandidatEtSession(candidatId: Int, sessionId: Int): Inscription?

    @Insert
    suspend fun inserer(inscription: Inscription): Long

    @Update
    suspend fun modifier(inscription: Inscription)
}

@Dao
interface PresenceDao {
    @Query("SELECT * FROM presences ORDER BY id ASC")
    fun toutes(): Flow<List<Presence>>

    @Query("SELECT * FROM presences WHERE inscriptionId = :inscriptionId LIMIT 1")
    suspend fun parInscription(inscriptionId: Int): Presence?

    @Query("SELECT * FROM presences WHERE inscriptionId IN (SELECT id FROM inscriptions WHERE sessionId = :sessionId)")
    fun parSession(sessionId: Int): Flow<List<Presence>>

    @Insert
    suspend fun inserer(presence: Presence): Long

    @Update
    suspend fun modifier(presence: Presence)
}
