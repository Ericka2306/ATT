package mg.itu.att.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** DAO de l'historique : on ajoute, on ne modifie ni ne supprime jamais. */
@Dao
interface HistoriqueDao {
    @Query("SELECT * FROM historique ORDER BY dateHeure DESC, id DESC")
    fun tout(): Flow<List<Historique>>

    @Query("SELECT * FROM historique WHERE entite = :entite AND entiteId = :entiteId ORDER BY dateHeure DESC, id DESC")
    fun pourObjet(entite: String, entiteId: Int): Flow<List<Historique>>

    @Query("SELECT COUNT(*) FROM historique")
    fun nombreEnDirect(): Flow<Int>

    @Insert
    suspend fun inserer(entree: Historique): Long
}
