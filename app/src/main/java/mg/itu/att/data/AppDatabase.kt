package mg.itu.att.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * La base locale de l'application : la source de vérité (cours S7, offline-first).
 * Singleton obtenu par [obtenir], comme `AppDatabase.obtenir(context)` dans listedetailv3.
 *
 * **Schéma figé le 18/09/2026 (étape D2) : version 3, définitive.** Aucune entité ne change plus.
 * `exportSchema = true` écrit le schéma complet dans `app/schemas/` : c'est le schéma de référence,
 * versionné avec le code et lisible sans ouvrir la base.
 *
 * `fallbackToDestructiveMigration` reste en place : si le schéma changeait encore, l'application
 * repartirait d'une base neuve plutôt que de planter. Il n'y a pas de migration à écrire puisqu'aucune
 * donnée réelle n'est encore en service (docs/01 §7).
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
    // v3 (17/09/2026, étape C11) : résultats corrigés (`remplaceResultatId`, motif de correction).
    version = 3,
    exportSchema = true,
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
