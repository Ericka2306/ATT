package mg.itu.att.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Référentiels configurés par le Super Admin (cours S7 : une data class = une table).
 *
 * Tout ce qui porte `aConfirmer = true` est une valeur d'exemple, jamais une règle officielle
 * de l'ATT : elle s'affiche avec un badge « À confirmer » et se modifie dans l'écran de configuration.
 * Les clés étrangères sont des champs `xxxId` explicites, déclarés à Room par @ForeignKey
 * (le FOREIGN KEY de SQL posé en annotation — voir docs/HORS_COURS.md n° 3).
 */

// ---------- RÉGIONS ----------

/** Une région de Madagascar. Centres et auto-écoles y sont rattachés. */
@Entity(tableName = "regions", indices = [Index(value = ["code"], unique = true)])
data class Region(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,
    val nom: String,
    val actif: Boolean = true,
)

// ---------- CATÉGORIES DE PERMIS ----------

/** Une catégorie de permis (A', A, B, C, D, E…). */
@Entity(tableName = "categories_permis", indices = [Index(value = ["code"], unique = true)])
data class CategoriePermis(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,
    val libelle: String,
    /** null = non renseigné. */
    val ageMinimum: Int? = null,
    /** Code de la catégorie qu'il faut déjà détenir (ex. "B" pour C, D, E), null sinon. */
    val categoriePrealableCode: String? = null,
    val actif: Boolean = true,
    val aConfirmer: Boolean = true,
)

// ---------- TYPES D'ÉPREUVE ----------

/** Une épreuve d'une catégorie : théorie, conduite… ordonnée. */
@Entity(
    tableName = "types_epreuve",
    foreignKeys = [
        ForeignKey(entity = CategoriePermis::class, parentColumns = ["id"], childColumns = ["categorieId"]),
    ],
    indices = [Index("categorieId"), Index(value = ["categorieId", "code"], unique = true)],
)
data class TypeEpreuve(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categorieId: Int,
    /** THEORIE, CONDUITE… (texte libre pour rester extensible). */
    val code: String,
    val libelle: String,
    val ordre: Int,
    val obligatoire: Boolean = true,
    /** Durée par défaut d'un passage, en minutes ; null = non renseignée. */
    val dureeMinutes: Int? = null,
    val actif: Boolean = true,
    val aConfirmer: Boolean = true,
)

// ---------- BARÈMES (versionnés) ----------

/**
 * Le barème d'une épreuve. On ne modifie jamais un barème déjà utilisé par un résultat :
 * on crée une nouvelle version. Un résultat pointe toujours sur la version qui a servi.
 */
@Entity(
    tableName = "baremes",
    foreignKeys = [
        ForeignKey(entity = TypeEpreuve::class, parentColumns = ["id"], childColumns = ["typeEpreuveId"]),
    ],
    indices = [Index("typeEpreuveId"), Index(value = ["typeEpreuveId", "version"], unique = true)],
)
data class Bareme(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val typeEpreuveId: Int,
    val version: Int,
    val noteMax: Double,
    val seuilReussite: Double,
    /** Date ISO "AAAA-MM-JJ". */
    val dateDebutValidite: String,
    /** null = barème courant. */
    val dateFinValidite: String? = null,
    val aConfirmer: Boolean = true,
)

// ---------- RÈGLES CONFIGURABLES ----------

/** Type de la valeur d'une règle, pour la valider à la saisie. */
enum class TypeValeur { ENTIER, DECIMAL, BOOLEEN, TEXTE }

/**
 * Une règle métier générique lue par le code au moment du calcul (jamais codée en dur).
 * Les clés connues sont listées dans [ClesRegles].
 */
@Entity(
    tableName = "regles_config",
    foreignKeys = [
        ForeignKey(entity = CategoriePermis::class, parentColumns = ["id"], childColumns = ["categorieId"]),
    ],
    indices = [Index(value = ["cle", "categorieId"], unique = true), Index("categorieId")],
)
data class RegleConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cle: String,
    val valeur: String,
    val typeValeur: TypeValeur,
    /** null = règle globale ; sinon surcharge pour une catégorie. */
    val categorieId: Int? = null,
    val description: String,
    val aConfirmer: Boolean = true,
)

/** Clés des règles configurables. Le code ne connaît que les clés, jamais les valeurs. */
object ClesRegles {
    const val TENTATIVES_MAX = "TENTATIVES_MAX"
    const val DELAI_REPASSAGE_JOURS = "DELAI_REPASSAGE_JOURS"
    const val CONSERVATION_EPREUVE_REUSSIE_JOURS = "CONSERVATION_EPREUVE_REUSSIE_JOURS"
    const val TOLERANCE_RETARD_MIN = "TOLERANCE_RETARD_MIN"
    const val REGLE_ABSENCE = "REGLE_ABSENCE"
    const val MARGE_CRENEAU_MIN = "MARGE_CRENEAU_MIN"
    const val DUREE_CRENEAU_MIN = "DUREE_CRENEAU_MIN"
    const val CAPACITE_SESSION_DEFAUT = "CAPACITE_SESSION_DEFAUT"
    const val CAPACITE_CRENEAU_DEFAUT = "CAPACITE_CRENEAU_DEFAUT"
    const val EXAMEN_DANS_REGION_AUTO_ECOLE = "EXAMEN_DANS_REGION_AUTO_ECOLE"
    const val CONDUITE_APRES_THEORIE_REUSSIE = "CONDUITE_APRES_THEORIE_REUSSIE"
    const val AUTO_ECOLE_PEUT_INSCRIRE = "AUTO_ECOLE_PEUT_INSCRIRE"
    const val DUREE_THEORIE_MIN = "DUREE_THEORIE_MIN"
    /** Mode de choix des questions de théorie : TIRAGE (sujet tiré au sort) ou DIRECT (choix de l'examinateur) — Q1 bis, étape C9. */
    const val MODE_THEORIE = "MODE_THEORIE"
    /** Pièces attendues dans un dossier, séparées par « ; » ; surcharge possible par catégorie (Q7). */
    const val PIECES_DOSSIER = "PIECES_DOSSIER"
    const val SEPARATEUR_PIECES = ";"
}

// ---------- QUESTIONS (épreuve théorique, orale) ----------

/**
 * Une question de l'épreuve théorique, posée à l'oral : l'examinateur note la réponse du candidat et lui attribue
 * des points (pas de choix multiples, témoignage du dev 1, 17/09/2026). Désactivée plutôt que supprimée :
 * d'anciennes évaluations la référencent.
 */
@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(entity = TypeEpreuve::class, parentColumns = ["id"], childColumns = ["typeEpreuveId"]),
    ],
    indices = [Index("typeEpreuveId")],
)
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val typeEpreuveId: Int,
    val enonce: String,
    val points: Double,
    /** Aide-mémoire pour l'examinateur, jamais montré à l'auto-école ni au candidat. */
    val reponseAttendue: String? = null,
    val ordre: Int = 0,
    val actif: Boolean = true,
    val aConfirmer: Boolean = true,
)

// ---------- CRITÈRES PRATIQUES (épreuve de conduite, structure seulement) ----------

/** Un critère d'évaluation de conduite. Aucune donnée initiale : grille à confirmer par l'ATT. */
@Entity(
    tableName = "criteres_pratiques",
    foreignKeys = [
        ForeignKey(entity = TypeEpreuve::class, parentColumns = ["id"], childColumns = ["typeEpreuveId"]),
    ],
    indices = [Index("typeEpreuveId")],
)
data class CriterePratique(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val typeEpreuveId: Int,
    val libelle: String,
    val points: Double,
    val eliminatoire: Boolean = false,
    val actif: Boolean = true,
    val aConfirmer: Boolean = true,
)

// ---------- CENTRES D'EXAMEN ----------

/** Un centre d'examen, toujours rattaché à une région. */
@Entity(
    tableName = "centres",
    foreignKeys = [
        ForeignKey(entity = Region::class, parentColumns = ["id"], childColumns = ["regionId"]),
    ],
    indices = [Index("regionId")],
)
data class Centre(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val regionId: Int,
    val nom: String,
    val adresse: String,
    val capaciteParDefaut: Int? = null,
    val actif: Boolean = true,
)
