package mg.itu.att.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** DAO des acteurs : utilisateurs, auto-écoles, candidats, examinateurs. */

@Dao
interface UtilisateurDao {
    @Query("SELECT * FROM utilisateurs ORDER BY nom ASC")
    fun tous(): Flow<List<Utilisateur>>

    @Query("SELECT * FROM utilisateurs WHERE identifiant = :identifiant")
    suspend fun parIdentifiant(identifiant: String): Utilisateur?

    @Query("SELECT * FROM utilisateurs WHERE id = :id")
    suspend fun parId(id: Int): Utilisateur?

    @Query("SELECT COUNT(*) FROM utilisateurs")
    suspend fun nombre(): Int

    @Query("SELECT COUNT(*) FROM utilisateurs")
    fun nombreEnDirect(): Flow<Int>

    @Insert
    suspend fun inserer(utilisateur: Utilisateur): Long

    @Update
    suspend fun modifier(utilisateur: Utilisateur)
}

@Dao
interface AutoEcoleDao {
    @Query("SELECT * FROM auto_ecoles ORDER BY nom ASC")
    fun toutes(): Flow<List<AutoEcole>>

    @Query("SELECT * FROM auto_ecoles WHERE regionId = :regionId ORDER BY nom ASC")
    fun parRegion(regionId: Int): Flow<List<AutoEcole>>

    @Query("SELECT * FROM auto_ecoles WHERE id = :id")
    suspend fun parId(id: Int): AutoEcole?

    @Insert
    suspend fun inserer(autoEcole: AutoEcole): Long

    @Update
    suspend fun modifier(autoEcole: AutoEcole)
}

@Dao
interface CandidatDao {
    @Query("SELECT * FROM candidats ORDER BY nom ASC, prenom ASC")
    fun tous(): Flow<List<Candidat>>

    /** Les candidats d'une auto-école : la seule vue autorisée pour ce rôle (instructions §7). */
    @Query("SELECT * FROM candidats WHERE autoEcoleId = :autoEcoleId ORDER BY nom ASC, prenom ASC")
    fun parAutoEcole(autoEcoleId: Int): Flow<List<Candidat>>

    @Query("SELECT * FROM candidats WHERE id = :id")
    suspend fun parId(id: Int): Candidat?

    @Query("SELECT * FROM candidats WHERE id = :id")
    fun parIdEnDirect(id: Int): Flow<Candidat?>

    /** Détection d'un doublon probable à la création (UC04). */
    @Query("SELECT * FROM candidats WHERE nom = :nom AND prenom = :prenom AND dateNaissance = :dateNaissance")
    suspend fun homonymes(nom: String, prenom: String, dateNaissance: String): List<Candidat>

    @Insert
    suspend fun inserer(candidat: Candidat): Long

    @Update
    suspend fun modifier(candidat: Candidat)
}

@Dao
interface ExaminateurDao {
    @Query("SELECT * FROM examinateurs ORDER BY nom ASC")
    fun tous(): Flow<List<Examinateur>>

    @Query("SELECT * FROM examinateurs WHERE id = :id")
    suspend fun parId(id: Int): Examinateur?

    @Insert
    suspend fun inserer(examinateur: Examinateur): Long

    @Update
    suspend fun modifier(examinateur: Examinateur)
}
