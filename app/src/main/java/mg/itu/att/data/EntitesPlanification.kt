package mg.itu.att.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Sessions d'examen, créneaux, inscriptions et présence (cadrage §6). */

// ---------- SESSIONS ----------

enum class StatutSession { PLANIFIEE, OUVERTE, COMPLETE, EN_COURS, TERMINEE, ANNULEE }

/** Une session = une catégorie, une épreuve, un centre, une date, une capacité. La région est celle du centre. */
@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(entity = CategoriePermis::class, parentColumns = ["id"], childColumns = ["categorieId"]),
        ForeignKey(entity = TypeEpreuve::class, parentColumns = ["id"], childColumns = ["typeEpreuveId"]),
        ForeignKey(entity = Centre::class, parentColumns = ["id"], childColumns = ["centreId"]),
        ForeignKey(entity = Utilisateur::class, parentColumns = ["id"], childColumns = ["creeParId"]),
    ],
    indices = [Index("categorieId"), Index("typeEpreuveId"), Index("centreId"), Index("creeParId"), Index("date")],
)
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categorieId: Int,
    val typeEpreuveId: Int,
    val centreId: Int,
    /** Date ISO "AAAA-MM-JJ". */
    val date: String,
    /** Heure "HH:MM". */
    val heureConvocation: String,
    val capacite: Int,
    val dureeCreneauMin: Int,
    val margeMin: Int,
    val statut: StatutSession = StatutSession.PLANIFIEE,
    val creeParId: Int,
)

// ---------- CRÉNEAUX ----------

/** Un créneau de passage dans une session. Capacité 1 = créneau individuel (à confirmer, Q5). */
@Entity(
    tableName = "creneaux",
    foreignKeys = [
        ForeignKey(entity = Session::class, parentColumns = ["id"], childColumns = ["sessionId"]),
    ],
    indices = [Index("sessionId")],
)
data class Creneau(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val ordre: Int,
    val heureDebut: String,
    val heureFinEstimee: String,
    val capacite: Int,
)

// ---------- INSCRIPTIONS ----------

/** DEMANDE = demandée par l'auto-école, à confirmer par l'ATT (si la règle l'autorise, Q8). */
enum class StatutInscription { DEMANDE, INSCRIT, CONFIRME, REPORTE, ANNULE }

/**
 * L'inscription d'un candidat (dossier validé) à une session.
 * Une seule inscription ACTIVE par candidat et par session : c'est `ReglesInscription.verifier` qui le garantit
 * (une inscription annulée ou reportée peut être suivie d'une nouvelle sur la même session), pas un index unique.
 */
@Entity(
    tableName = "inscriptions",
    foreignKeys = [
        ForeignKey(entity = Candidat::class, parentColumns = ["id"], childColumns = ["candidatId"]),
        ForeignKey(entity = Dossier::class, parentColumns = ["id"], childColumns = ["dossierId"]),
        ForeignKey(entity = Session::class, parentColumns = ["id"], childColumns = ["sessionId"]),
        ForeignKey(entity = Creneau::class, parentColumns = ["id"], childColumns = ["creneauId"]),
    ],
    indices = [Index("candidatId"), Index("dossierId"), Index("sessionId"), Index("creneauId")],
)
data class Inscription(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val candidatId: Int,
    val dossierId: Int,
    val sessionId: Int,
    val creneauId: Int? = null,
    val statut: StatutInscription = StatutInscription.INSCRIT,
    /** Date ISO. */
    val dateInscription: String,
    val heurePassageEstimee: String? = null,
    /** Numéro d'appel anonyme dans la session : l'examinateur ne voit que ce numéro (Q4). */
    val numeroAnonymat: String,
    val motif: String? = null,
)

// ---------- PRÉSENCE ----------

enum class StatutPresence { EN_ATTENTE, PRESENT, ABSENT, EN_RETARD, EN_COURS, TERMINE }

/** La présence liée à une inscription (une ligne par inscription). */
@Entity(
    tableName = "presences",
    foreignKeys = [
        ForeignKey(entity = Inscription::class, parentColumns = ["id"], childColumns = ["inscriptionId"]),
    ],
    indices = [Index(value = ["inscriptionId"], unique = true)],
)
data class Presence(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val inscriptionId: Int,
    val statut: StatutPresence = StatutPresence.EN_ATTENTE,
    val heureAppel: String? = null,
    val heureArrivee: String? = null,
    val remarque: String? = null,
)
