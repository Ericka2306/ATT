package mg.itu.att.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Le dossier d'un candidat pour une catégorie, et ses pièces. */

/** Cycle de vie d'un dossier (cadrage §11 : incomplet / refusé). */
enum class StatutDossier { BROUILLON, SOUMIS, INCOMPLET, VALIDE, REFUSE }

@Entity(
    tableName = "dossiers",
    foreignKeys = [
        ForeignKey(entity = Candidat::class, parentColumns = ["id"], childColumns = ["candidatId"]),
        ForeignKey(entity = CategoriePermis::class, parentColumns = ["id"], childColumns = ["categorieId"]),
        ForeignKey(entity = Utilisateur::class, parentColumns = ["id"], childColumns = ["decideParId"]),
    ],
    indices = [Index("candidatId"), Index("categorieId"), Index("decideParId")],
)
data class Dossier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val candidatId: Int,
    val categorieId: Int,
    val statut: StatutDossier = StatutDossier.BROUILLON,
    /** Motif d'incomplétude ou de refus, obligatoire pour ces deux statuts. */
    val motif: String? = null,
    /** Dates ISO. */
    val dateSoumission: String? = null,
    val dateDecision: String? = null,
    /** L'Admin ATT qui a décidé. */
    val decideParId: Int? = null,
)

/** Une pièce attendue dans un dossier : on note seulement si elle est fournie (aucun scan stocké, Q7). */
@Entity(
    tableName = "pieces_dossier",
    foreignKeys = [
        ForeignKey(entity = Dossier::class, parentColumns = ["id"], childColumns = ["dossierId"]),
    ],
    indices = [Index("dossierId")],
)
data class PieceDossier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dossierId: Int,
    /** Libellé de la pièce (liste issue du portail Torolalana, modifiable). */
    val typePiece: String,
    val fournie: Boolean = false,
    val remarque: String? = null,
)
