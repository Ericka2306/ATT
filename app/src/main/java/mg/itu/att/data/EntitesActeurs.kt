package mg.itu.att.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Les acteurs : utilisateurs (comptes), auto-écoles, candidats, examinateurs.
 * Un candidat existe sans compte (smartphone non obligatoire, cadrage §10).
 */

// ---------- RÔLES ET UTILISATEURS ----------

/** Les cinq rôles du cahier de cadrage (§4). */
enum class Role { SUPER_ADMIN, ADMIN_ATT, AUTO_ECOLE, EXAMINATEUR, CANDIDAT }

/**
 * Un compte de connexion. Le mot de passe n'est jamais stocké en clair :
 * `motDePasseHash` est l'empreinte PBKDF2 produite par [mg.itu.att.securite.MotDePasse].
 */
@Entity(
    tableName = "utilisateurs",
    foreignKeys = [
        ForeignKey(entity = Region::class, parentColumns = ["id"], childColumns = ["regionId"]),
        ForeignKey(entity = AutoEcole::class, parentColumns = ["id"], childColumns = ["autoEcoleId"]),
        ForeignKey(entity = Examinateur::class, parentColumns = ["id"], childColumns = ["examinateurId"]),
        ForeignKey(entity = Candidat::class, parentColumns = ["id"], childColumns = ["candidatId"]),
    ],
    indices = [
        Index(value = ["identifiant"], unique = true),
        Index("regionId"), Index("autoEcoleId"), Index("examinateurId"), Index("candidatId"),
    ],
)
data class Utilisateur(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val identifiant: String,
    val motDePasseHash: String,
    val nom: String,
    val role: Role,
    /** Admin ATT régional (à confirmer, Q11) ; null = national. */
    val regionId: Int? = null,
    /** Renseigné si role = AUTO_ECOLE. */
    val autoEcoleId: Int? = null,
    /** Renseigné si role = EXAMINATEUR. */
    val examinateurId: Int? = null,
    /** Renseigné si role = CANDIDAT (compte facultatif). */
    val candidatId: Int? = null,
    val actif: Boolean = true,
)

// ---------- AUTO-ÉCOLES ----------

/** Une auto-école agréée, rattachée à une région. */
@Entity(
    tableName = "auto_ecoles",
    foreignKeys = [
        ForeignKey(entity = Region::class, parentColumns = ["id"], childColumns = ["regionId"]),
    ],
    indices = [Index("regionId")],
)
data class AutoEcole(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val regionId: Int,
    val nom: String,
    /** Numéro d'agrément, à confirmer (Q7). */
    val numeroAgrement: String? = null,
    val adresse: String,
    val telephone: String? = null,
    val actif: Boolean = true,
)

// ---------- CANDIDATS ----------

/** Un candidat au permis, toujours rattaché à une auto-école (parcours officiel Torolalana). */
@Entity(
    tableName = "candidats",
    foreignKeys = [
        ForeignKey(entity = AutoEcole::class, parentColumns = ["id"], childColumns = ["autoEcoleId"]),
    ],
    indices = [Index("autoEcoleId")],
)
data class Candidat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val autoEcoleId: Int,
    val nom: String,
    val prenom: String,
    /** Date ISO "AAAA-MM-JJ". */
    val dateNaissance: String,
    /** Numéro de CIN, facultatif tant que Q7 n'est pas tranchée. */
    val cin: String? = null,
    val telephone: String? = null,
    val adresse: String? = null,
    val actif: Boolean = true,
)

// ---------- EXAMINATEURS ----------

/** Un examinateur assermenté de l'ATT. Aucune affectation préalable à un candidat (V1). */
@Entity(
    tableName = "examinateurs",
    foreignKeys = [
        ForeignKey(entity = Region::class, parentColumns = ["id"], childColumns = ["regionId"]),
    ],
    indices = [Index("regionId")],
)
data class Examinateur(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nom: String,
    val matricule: String? = null,
    val regionId: Int? = null,
    val actif: Boolean = true,
)
