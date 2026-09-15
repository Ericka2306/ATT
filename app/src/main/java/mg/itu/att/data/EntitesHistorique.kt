package mg.itu.att.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Journal de toute modification sensible (instructions §7 : « tracer les modifications sensibles »).
 * Écrit dans la même transaction que la modification (étape B2).
 */
@Entity(
    tableName = "historique",
    foreignKeys = [
        ForeignKey(entity = Utilisateur::class, parentColumns = ["id"], childColumns = ["utilisateurId"]),
    ],
    indices = [Index(value = ["entite", "entiteId"]), Index("utilisateurId"), Index("dateHeure")],
)
data class Historique(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** Nom de l'entité concernée : "Dossier", "Resultat", "Presence", "RegleConfig"… */
    val entite: String,
    val entiteId: Int,
    /** CREATION, MODIFICATION, VALIDATION, REFUS, CORRECTION, ANNULATION… */
    val action: String,
    /** Résumés texte, jamais des objets complets. */
    val ancienneValeur: String? = null,
    val nouvelleValeur: String? = null,
    val utilisateurId: Int,
    /** ISO "AAAA-MM-JJTHH:MM:SS". */
    val dateHeure: String,
    val motif: String? = null,
)
