package mg.itu.att.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tentatives, évaluations et résultats (cadrage §7 et §9).
 * Règle absolue : on n'écrase ni ne supprime jamais une tentative ou un résultat.
 */

// ---------- TENTATIVES ----------

enum class StatutTentative { EN_COURS, TERMINEE, VALIDEE, ANNULEE }

/** La n-ième tentative d'un candidat pour une épreuve. L'examinateur est renseigné à la saisie, pas avant. */
@Entity(
    tableName = "tentatives",
    foreignKeys = [
        ForeignKey(entity = Candidat::class, parentColumns = ["id"], childColumns = ["candidatId"]),
        ForeignKey(entity = Inscription::class, parentColumns = ["id"], childColumns = ["inscriptionId"]),
        ForeignKey(entity = TypeEpreuve::class, parentColumns = ["id"], childColumns = ["typeEpreuveId"]),
        ForeignKey(entity = Examinateur::class, parentColumns = ["id"], childColumns = ["examinateurId"]),
    ],
    indices = [
        Index(value = ["candidatId", "typeEpreuveId", "numero"], unique = true),
        Index("inscriptionId"), Index("typeEpreuveId"), Index("examinateurId"),
    ],
)
data class Tentative(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val candidatId: Int,
    val inscriptionId: Int,
    val typeEpreuveId: Int,
    val numero: Int,
    val examinateurId: Int? = null,
    val statut: StatutTentative = StatutTentative.EN_COURS,
    /** Date et heure ISO "AAAA-MM-JJTHH:MM". */
    val dateHeure: String,
)

// ---------- ÉVALUATIONS ----------

/** La saisie de l'examinateur pour une tentative, figée sur une version de barème. */
@Entity(
    tableName = "evaluations",
    foreignKeys = [
        ForeignKey(entity = Tentative::class, parentColumns = ["id"], childColumns = ["tentativeId"]),
        ForeignKey(entity = Bareme::class, parentColumns = ["id"], childColumns = ["baremeId"]),
    ],
    indices = [Index(value = ["tentativeId"], unique = true), Index("baremeId")],
)
data class Evaluation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tentativeId: Int,
    val baremeId: Int,
    val dateSaisie: String,
    val observations: String? = null,
)

/**
 * Une ligne de la feuille d'examen théorique (épreuve orale) : la question posée, ce que le candidat a répondu,
 * les points que l'examinateur lui attribue (entre 0 et les points de la question ; null = pas encore noté).
 */
@Entity(
    tableName = "reponses_candidat",
    foreignKeys = [
        ForeignKey(entity = Evaluation::class, parentColumns = ["id"], childColumns = ["evaluationId"]),
        ForeignKey(entity = Question::class, parentColumns = ["id"], childColumns = ["questionId"]),
    ],
    indices = [Index(value = ["evaluationId", "questionId"], unique = true), Index("questionId")],
)
data class ReponseCandidat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val evaluationId: Int,
    val questionId: Int,
    val reponseDonnee: String? = null,
    val pointsAttribues: Double? = null,
)

/** La note d'un critère de conduite (structure seulement, grille à confirmer, Q2). */
@Entity(
    tableName = "evaluations_critere",
    foreignKeys = [
        ForeignKey(entity = Evaluation::class, parentColumns = ["id"], childColumns = ["evaluationId"]),
        ForeignKey(entity = CriterePratique::class, parentColumns = ["id"], childColumns = ["critereId"]),
    ],
    indices = [Index(value = ["evaluationId", "critereId"], unique = true), Index("critereId")],
)
data class EvaluationCritere(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val evaluationId: Int,
    val critereId: Int,
    val note: Double? = null,
    val fauteEliminatoire: Boolean = false,
    val observation: String? = null,
)

// ---------- RÉSULTATS (jamais écrasés) ----------

enum class StatutResultat { CALCULE, VALIDE_ATT, CORRIGE, ANNULE }

/**
 * Le résultat calculé d'une tentative. Une correction crée une NOUVELLE ligne
 * qui pointe sur l'ancienne par `remplaceResultatId` ; l'ancienne reste lisible.
 */
@Entity(
    tableName = "resultats",
    foreignKeys = [
        ForeignKey(entity = Tentative::class, parentColumns = ["id"], childColumns = ["tentativeId"]),
        ForeignKey(entity = Bareme::class, parentColumns = ["id"], childColumns = ["baremeId"]),
        ForeignKey(entity = Utilisateur::class, parentColumns = ["id"], childColumns = ["valideParId"]),
        ForeignKey(entity = Resultat::class, parentColumns = ["id"], childColumns = ["remplaceResultatId"]),
    ],
    indices = [Index("tentativeId"), Index("baremeId"), Index("valideParId"), Index("remplaceResultatId")],
)
data class Resultat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tentativeId: Int,
    val baremeId: Int,
    val noteObtenue: Double,
    /** Copiés du barème au moment du calcul : le résultat reste lisible même si le barème change. */
    val noteMax: Double,
    val seuil: Double,
    val reussi: Boolean,
    val statut: StatutResultat = StatutResultat.CALCULE,
    val valideParId: Int? = null,
    val dateCalcul: String,
    val dateValidation: String? = null,
    val remplaceResultatId: Int? = null,
    val motifCorrection: String? = null,
    /**
     * Offline-first (cours S7, démo `demosync`) : false = validé localement, pas encore remonté au serveur central
     * de l'ATT. La base locale reste la source de vérité ; le réseau ne fait que la nourrir.
     */
    val synchronisee: Boolean = false,
)
