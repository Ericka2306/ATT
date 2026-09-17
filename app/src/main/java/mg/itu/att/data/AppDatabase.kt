package mg.itu.att.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * La base locale de l'application : la source de vérité (cours S7, offline-first).
 * Singleton obtenu par [obtenir], comme `AppDatabase.obtenir(context)` dans listedetailv3.
 *
 * Pendant le développement, `fallbackToDestructiveMigration` repart d'une base neuve
 * à chaque changement de schéma ; `version` est incrémentée à chaque modification d'entité
 * et le schéma sera figé avant la soutenance (docs/01 §7).
 */
@Database(
    entities = [
        // Référentiels
        Region::class, CategoriePermis::class, TypeEpreuve::class, Bareme::class, RegleConfig::class,
        Question::class, CriterePratique::class, Centre::class,
        // Acteurs
        Utilisateur::class, AutoEcole::class, Candidat::class, Examinateur::class,
        // Dossiers
        Dossier::class, PieceDossier::class,
        // Planification
        Session::class, Creneau::class, Inscription::class, Presence::class,
        // Examens
        Tentative::class, Evaluation::class, ReponseCandidat::class, EvaluationCritere::class, Resultat::class,
        // Traçabilité
        Historique::class,
    ],
    // v2 (16/09/2026, étape C7) : l'index unique (candidatId, sessionId) des inscriptions devient un index simple.
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    // Référentiels
    abstract fun regionDao(): RegionDao
    abstract fun categoriePermisDao(): CategoriePermisDao
    abstract fun typeEpreuveDao(): TypeEpreuveDao
    abstract fun baremeDao(): BaremeDao
    abstract fun regleConfigDao(): RegleConfigDao
    abstract fun questionDao(): QuestionDao
    abstract fun criterePratiqueDao(): CriterePratiqueDao
    abstract fun centreDao(): CentreDao

    // Acteurs
    abstract fun utilisateurDao(): UtilisateurDao
    abstract fun autoEcoleDao(): AutoEcoleDao
    abstract fun candidatDao(): CandidatDao
    abstract fun examinateurDao(): ExaminateurDao

    // Dossiers
    abstract fun dossierDao(): DossierDao
    abstract fun pieceDossierDao(): PieceDossierDao

    // Planification
    abstract fun sessionDao(): SessionDao
    abstract fun creneauDao(): CreneauDao
    abstract fun inscriptionDao(): InscriptionDao
    abstract fun presenceDao(): PresenceDao

    // Examens
    abstract fun tentativeDao(): TentativeDao
    abstract fun evaluationDao(): EvaluationDao
    abstract fun reponseCandidatDao(): ReponseCandidatDao
    abstract fun evaluationCritereDao(): EvaluationCritereDao
    abstract fun resultatDao(): ResultatDao

    // Traçabilité
    abstract fun historiqueDao(): HistoriqueDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun obtenir(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "att.db",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
