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

    /** Les comptes d'une auto-école (UC03). */
    @Query("SELECT * FROM utilisateurs WHERE autoEcoleId = :autoEcoleId ORDER BY identifiant ASC")
    fun parAutoEcole(autoEcoleId: Int): Flow<List<Utilisateur>>

    /** Les comptes d'un candidat (facultatif, en principe un seul — UC12). */
    @Query("SELECT * FROM utilisateurs WHERE candidatId = :candidatId ORDER BY identifiant ASC")
    fun parCandidat(candidatId: Int): Flow<List<Utilisateur>>

    /** Les comptes d'un examinateur (en principe un seul). */
    @Query("SELECT * FROM utilisateurs WHERE examinateurId = :examinateurId")
    suspend fun parExaminateur(examinateurId: Int): List<Utilisateur>

    /** Tous les identifiants existants, pour refuser un doublon à la création d'un compte. */
    @Query("SELECT identifiant FROM utilisateurs")
    suspend fun tousLesIdentifiants(): List<String>

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

    @Query("SELECT * FROM auto_ecoles WHERE actif = 1 ORDER BY nom ASC")
    suspend fun listeActives(): List<AutoEcole>

    @Query("SELECT * FROM auto_ecoles WHERE id = :id")
    suspend fun parId(id: Int): AutoEcole?

    @Query("SELECT * FROM auto_ecoles WHERE id = :id")
    fun parIdEnDirect(id: Int): Flow<AutoEcole?>

    /** Noms des autres auto-écoles d'une région (détection de doublon à la saisie). */
    @Query("SELECT nom FROM auto_ecoles WHERE regionId = :regionId AND id != :saufId")
    suspend fun nomsDansRegion(regionId: Int, saufId: Int): List<String>

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

    /** Les candidats des auto-écoles d'une région (administrateur régional). */
    @Query("SELECT candidats.* FROM candidats JOIN auto_ecoles ON auto_ecoles.id = candidats.autoEcoleId WHERE auto_ecoles.regionId = :regionId ORDER BY candidats.nom ASC, candidats.prenom ASC")
    fun parRegion(regionId: Int): Flow<List<Candidat>>

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
