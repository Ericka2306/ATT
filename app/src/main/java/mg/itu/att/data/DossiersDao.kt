package mg.itu.att.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** DAO des dossiers et de leurs pièces. */

@Dao
interface DossierDao {
    @Query("SELECT * FROM dossiers WHERE candidatId = :candidatId ORDER BY id DESC")
    fun parCandidat(candidatId: Int): Flow<List<Dossier>>

    /** Les dossiers à traiter par l'ATT (UC05). */
    @Query("SELECT * FROM dossiers WHERE statut = :statut ORDER BY dateSoumission ASC, id ASC")
    fun parStatut(statut: StatutDossier): Flow<List<Dossier>>

    @Query("SELECT * FROM dossiers WHERE id = :id")
    suspend fun parId(id: Int): Dossier?

    @Query("SELECT * FROM dossiers WHERE id = :id")
    fun parIdEnDirect(id: Int): Flow<Dossier?>

    /** Le dossier validé d'un candidat pour une catégorie, s'il existe (précondition d'inscription). */
    @Query("SELECT * FROM dossiers WHERE candidatId = :candidatId AND categorieId = :categorieId AND statut = 'VALIDE' LIMIT 1")
    suspend fun valideEnCours(candidatId: Int, categorieId: Int): Dossier?

    @Insert
    suspend fun inserer(dossier: Dossier): Long

    @Update
    suspend fun modifier(dossier: Dossier)
}

@Dao
interface PieceDossierDao {
    @Query("SELECT * FROM pieces_dossier WHERE dossierId = :dossierId ORDER BY id ASC")
    fun parDossier(dossierId: Int): Flow<List<PieceDossier>>

    @Query("SELECT * FROM pieces_dossier WHERE dossierId = :dossierId ORDER BY id ASC")
    suspend fun listePourDossier(dossierId: Int): List<PieceDossier>

    @Insert
    suspend fun insererToutes(pieces: List<PieceDossier>)

    @Update
    suspend fun modifier(piece: PieceDossier)
}
