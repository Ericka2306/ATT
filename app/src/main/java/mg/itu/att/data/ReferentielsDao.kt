package mg.itu.att.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO des référentiels (cours S7). Règle du cours :
 * « je veux être tenu au courant » → Flow ; « question ponctuelle » ou écriture → suspend.
 */

@Dao
interface RegionDao {
    /** Toutes les régions, actives ou non, par nom. */
    @Query("SELECT * FROM regions ORDER BY nom ASC")
    fun toutes(): Flow<List<Region>>

    @Query("SELECT * FROM regions WHERE actif = 1 ORDER BY nom ASC")
    fun actives(): Flow<List<Region>>

    /** Même liste, en une fois (pour remplir un formulaire). */
    @Query("SELECT * FROM regions WHERE actif = 1 ORDER BY nom ASC")
    suspend fun listeActives(): List<Region>

    @Query("SELECT * FROM regions WHERE id = :id")
    suspend fun parId(id: Int): Region?

    @Query("SELECT COUNT(*) FROM regions")
    suspend fun nombre(): Int

    @Query("SELECT COUNT(*) FROM regions")
    fun nombreEnDirect(): Flow<Int>

    @Insert
    suspend fun insererToutes(regions: List<Region>)

    @Update
    suspend fun modifier(region: Region)
}

@Dao
interface CategoriePermisDao {
    @Query("SELECT * FROM categories_permis ORDER BY code ASC")
    fun toutes(): Flow<List<CategoriePermis>>

    @Query("SELECT * FROM categories_permis WHERE actif = 1 ORDER BY code ASC")
    fun actives(): Flow<List<CategoriePermis>>

    @Query("SELECT * FROM categories_permis WHERE actif = 1 ORDER BY code ASC")
    suspend fun listeActives(): List<CategoriePermis>

    @Query("SELECT * FROM categories_permis WHERE id = :id")
    suspend fun parId(id: Int): CategoriePermis?

    @Query("SELECT * FROM categories_permis WHERE id = :id")
    fun parIdEnDirect(id: Int): Flow<CategoriePermis?>

    @Query("SELECT * FROM categories_permis WHERE code = :code")
    suspend fun parCode(code: String): CategoriePermis?

    @Query("SELECT COUNT(*) FROM categories_permis")
    fun nombreEnDirect(): Flow<Int>

    @Insert
    suspend fun inserer(categorie: CategoriePermis): Long

    @Update
    suspend fun modifier(categorie: CategoriePermis)
}

@Dao
interface TypeEpreuveDao {
    @Query("SELECT * FROM types_epreuve WHERE categorieId = :categorieId ORDER BY ordre ASC")
    fun parCategorie(categorieId: Int): Flow<List<TypeEpreuve>>

    @Query("SELECT * FROM types_epreuve WHERE categorieId = :categorieId AND actif = 1 ORDER BY ordre ASC")
    suspend fun listeActivesPourCategorie(categorieId: Int): List<TypeEpreuve>

    @Query("SELECT * FROM types_epreuve WHERE id = :id")
    suspend fun parId(id: Int): TypeEpreuve?

    @Query("SELECT * FROM types_epreuve WHERE id = :id")
    fun parIdEnDirect(id: Int): Flow<TypeEpreuve?>

    @Query("SELECT * FROM types_epreuve ORDER BY categorieId ASC, ordre ASC")
    fun toutes(): Flow<List<TypeEpreuve>>

    @Query("SELECT * FROM types_epreuve WHERE categorieId = :categorieId AND code = :code")
    suspend fun parCategorieEtCode(categorieId: Int, code: String): TypeEpreuve?

    @Insert
    suspend fun inserer(typeEpreuve: TypeEpreuve): Long

    @Update
    suspend fun modifier(typeEpreuve: TypeEpreuve)
}

@Dao
interface BaremeDao {
    @Query("SELECT * FROM baremes WHERE typeEpreuveId = :typeEpreuveId ORDER BY version DESC")
    fun parEpreuve(typeEpreuveId: Int): Flow<List<Bareme>>

    /** Le barème courant = celui sans date de fin, version la plus haute. */
    @Query("SELECT * FROM baremes WHERE typeEpreuveId = :typeEpreuveId AND dateFinValidite IS NULL ORDER BY version DESC LIMIT 1")
    suspend fun courant(typeEpreuveId: Int): Bareme?

    @Query("SELECT * FROM baremes WHERE id = :id")
    suspend fun parId(id: Int): Bareme?

    @Insert
    suspend fun inserer(bareme: Bareme): Long

    @Update
    suspend fun modifier(bareme: Bareme)
}

@Dao
interface RegleConfigDao {
    @Query("SELECT * FROM regles_config ORDER BY cle ASC, categorieId ASC")
    fun toutes(): Flow<List<RegleConfig>>

    /** La règle pour une catégorie si elle existe, sinon la règle globale (categorieId NULL). */
    @Query(
        "SELECT * FROM regles_config WHERE cle = :cle AND (categorieId = :categorieId OR categorieId IS NULL) " +
            "ORDER BY categorieId IS NULL ASC LIMIT 1",
    )
    suspend fun pour(cle: String, categorieId: Int?): RegleConfig?

    @Query("SELECT * FROM regles_config WHERE id = :id")
    suspend fun parId(id: Int): RegleConfig?

    @Query("SELECT COUNT(*) FROM regles_config")
    fun nombreEnDirect(): Flow<Int>

    @Insert
    suspend fun insererToutes(regles: List<RegleConfig>)

    @Update
    suspend fun modifier(regle: RegleConfig)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE typeEpreuveId = :typeEpreuveId ORDER BY ordre ASC, id ASC")
    fun parEpreuve(typeEpreuveId: Int): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE typeEpreuveId = :typeEpreuveId AND actif = 1 ORDER BY ordre ASC, id ASC")
    suspend fun activesPourEpreuve(typeEpreuveId: Int): List<Question>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun parId(id: Int): Question?

    @Insert
    suspend fun inserer(question: Question): Long

    @Update
    suspend fun modifier(question: Question)
}

@Dao
interface CriterePratiqueDao {
    @Query("SELECT * FROM criteres_pratiques WHERE typeEpreuveId = :typeEpreuveId ORDER BY id ASC")
    fun parEpreuve(typeEpreuveId: Int): Flow<List<CriterePratique>>

    @Query("SELECT * FROM criteres_pratiques WHERE typeEpreuveId = :typeEpreuveId AND actif = 1 ORDER BY id ASC")
    suspend fun actifsPourEpreuve(typeEpreuveId: Int): List<CriterePratique>

    @Query("SELECT * FROM criteres_pratiques WHERE id = :id")
    suspend fun parId(id: Int): CriterePratique?

    @Insert
    suspend fun inserer(critere: CriterePratique): Long

    @Update
    suspend fun modifier(critere: CriterePratique)
}

@Dao
interface CentreDao {
    @Query("SELECT * FROM centres ORDER BY nom ASC")
    fun tous(): Flow<List<Centre>>

    @Query("SELECT * FROM centres WHERE regionId = :regionId AND actif = 1 ORDER BY nom ASC")
    fun actifsParRegion(regionId: Int): Flow<List<Centre>>

    @Query("SELECT * FROM centres WHERE actif = 1 ORDER BY nom ASC")
    suspend fun listeActifs(): List<Centre>

    @Query("SELECT * FROM centres WHERE id = :id")
    suspend fun parId(id: Int): Centre?

    @Insert
    suspend fun inserer(centre: Centre): Long

    @Update
    suspend fun modifier(centre: Centre)
}
