# Synthèse du code du module M1 « Kotlin / Android » (ITUniversity) — base pour le projet ATT

Sources analysées (hors `build/` et `.gradle/`) : `minitp1`, `minitp2`, `cycledevie`, `carteproduit`, `listedetail`, `listedetailv2`, `listedetailv3`, `demosync` dans `/Users/mac/M1/KOTLIN`, et le squelette `/Users/mac/ATT`. Les versions « intégrées à AGP 9.3.2 » ont été vérifiées dans le cache Gradle local (`~/.gradle/caches/modules-2/files-2.1/com.android.tools.build/gradle/9.3.2/.../gradle-9.3.2.pom`) et dans les jars d'AGP.

---

## 1. Tableau comparatif des configurations Gradle

### 1.1 Projets Kotlin JVM purs (séances 1 et 2)

| Projet | Plugin | Kotlin | Gradle wrapper | JVM | Dépendances |
|---|---|---|---|---|---|
| `minitp1` (`minitp1-kotlin-essentiel`) | `kotlin("jvm")` | **2.3.20** | 9.3.0 | `jvmToolchain(25)` | aucune |
| `minitp2` (`minitp2-coroutines`) | `kotlin("jvm")` | **2.3.20** | 9.3.0 | `jvmToolchain(25)` | `org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0` |

Sources dans `src/main/kotlin/` sans package (fichiers à la racine : `Collectes.kt`, `Demo.kt`, `Programme1..3.kt`), chaque fichier a son propre `main()`.

### 1.2 Projets Android du cours et squelette ATT

| Élément | cycledevie (TP3) | carteproduit (TP4) | listedetail (TP5) | listedetailv2 (TP6) | listedetailv3 (TP7) | demosync (démo S7) | **ATT (squelette)** |
|---|---|---|---|---|---|---|---|
| `rootProject.name` | CycleDeVie | CarteProduit | ListeDetail | ListeDetailV2 | ListeDetailV3 | DemoSyncOffline | ATT |
| namespace / applicationId | `mg.itu.cycledevie` | `mg.itu.carteproduit` | `mg.itu.listedetail` | `mg.itu.listedetail` | `mg.itu.listedetail` | `mg.itu.demosync` | `com.example.att` |
| AGP (`com.android.application`) | **8.13.2** | **8.13.2** | 8.5.2 | 8.5.2 | 8.5.2 | 8.5.2 | **9.3.2** |
| Plugin Kotlin | `org.jetbrains.kotlin.android` **2.0.20** (déclaré explicitement) | idem 2.0.20 | idem 2.0.20 | idem 2.0.20 | idem 2.0.20 | idem 2.0.20 | **aucun plugin déclaré** : Kotlin **intégré à AGP 9** (AGP 9.3.2 dépend de `kotlin-gradle-plugin` **2.2.10** et `kotlin-stdlib` 2.2.10) |
| Plugin Compose (`org.jetbrains.kotlin.plugin.compose`) | — | 2.0.20 | 2.0.20 | 2.0.20 | 2.0.20 | 2.0.20 | — |
| KSP (`com.google.devtools.ksp`) | — | — | — | — | **2.0.20-1.0.25** | 2.0.20-1.0.25 | — |
| Déclaration des versions | chaînes littérales dans `build.gradle.kts` racine (`apply false`) puis `id(...)` dans `app/` | idem | idem | idem | idem | idem | **catalogue `gradle/libs.versions.toml`** + `alias(libs.plugins...)` |
| Gradle wrapper | 9.3.0 | 9.3.0 | 9.3.0 | 9.3.0 | 9.3.0 | 9.3.0 | **9.5.0** |
| `foojay-resolver-convention` (settings) | 1.0.0 | 1.0.0 | 1.0.0 | 1.0.0 | absent | absent | 1.0.0 |
| `gradle-daemon-jvm.properties` | toolchain 25 (JetBrains) | idem | idem | idem | fichier vide | fichier vide | toolchain 25 |
| compileSdk / minSdk / targetSdk | 35 / 24 / 35 | 35 / 24 / 35 | 35 / 24 / 35 | 35 / 24 / 35 | 35 / 24 / 35 | 35 / 24 / 35 | **`release(37)`** / 24 / 37 |
| Java (`compileOptions`) | 17 | 17 | 17 | 17 | 17 | 17 | **11** |
| `kotlinOptions { jvmTarget }` | "17" | "17" | "17" | "17" | "17" | "17" | absent (bloc `kotlinOptions` n'existe plus dans AGP 9) |
| `buildFeatures { compose = true }` | non | **oui** | oui | oui | oui | oui | non |
| Compose BOM | — | `androidx.compose:compose-bom:2024.09.03` | 2024.09.03 | 2024.09.03 | 2024.09.03 | 2024.09.03 | — |
| material3 | — | `androidx.compose.material3:material3` (via BOM → 1.3.0) | idem | idem | idem | idem | `com.google.android.material:material:1.14.0` (MDC Views, pas Compose) |
| `androidx.compose.ui:ui` | — | via BOM | via BOM | via BOM | via BOM | via BOM | — |
| activity-compose | — | 1.9.2 | 1.9.2 | 1.9.2 | 1.9.2 | 1.9.2 | — |
| navigation-compose | — | — | **2.8.0** | 2.8.0 | 2.8.0 | — (un seul écran) | — |
| lifecycle-viewmodel-compose | — | — | — | **2.8.4** | 2.8.4 | 2.8.4 | — |
| Room | — | — | — | — | **room-runtime 2.6.1, room-ktx 2.6.1, `ksp("androidx.room:room-compiler:2.6.1")`** | idem 2.6.1 | — |
| Coroutines | transitives (via lifecycle/room-ktx) ; jamais déclarées explicitement en Android | | | | | | — |
| core-ktx | 1.13.1 | 1.13.1 | 1.13.1 | 1.13.1 | 1.13.1 | 1.13.1 | 1.19.0 |
| appcompat | **1.7.0** (`AppCompatActivity` + layouts XML) | — | — | — | — | — | 1.8.0 |
| Tests | aucun | aucun | aucun | aucun | aucun | aucun | junit 4.13.2, androidx junit 1.3.0, espresso 3.7.0 |
| `gradle.properties` | `-Xmx2048m`, `android.useAndroidX=true`, `kotlin.code.style=official` | idem | idem | idem | idem | idem | idem + `-Dfile.encoding=UTF-8` + **`org.gradle.configuration-cache=true`** |
| Thème (manifest) | `Theme.CycleDeVie` (parent `Theme.AppCompat.Light.DarkActionBar`, `res/values/themes.xml`) | `@android:style/Theme.Material.Light.NoActionBar` (aucun `themes.xml`) | idem | idem | idem | idem | `Theme.ATT` (parent `Theme.MaterialComponents.DayNight.DarkActionBar`) |
| Activity de base | `AppCompatActivity` (2 activities) | `ComponentActivity` + `setContent` | idem | idem | idem | idem | aucune activity déclarée |

Observations :
- Les trois projets Compose de listedetail/demosync déclarent AGP 8.5.2, les deux plus récents (cycledevie, carteproduit) AGP 8.13.2 ; tous sur Gradle 9.3.0 et Kotlin 2.0.20. La pile de référence du cours est donc : **AGP 8.x + Kotlin 2.0.20 + Compose BOM 2024.09.03 + navigation-compose 2.8.0 + lifecycle-viewmodel-compose 2.8.4 + Room 2.6.1 via KSP**.
- Aucun projet du cours n'utilise de catalogue de versions, ni de tests, ni Hilt, ni Retrofit, ni WorkManager (cités seulement en commentaire dans `demosync` comme « ce qu'on ferait en production »).
- `material3` est obtenu via la BOM (version résolue 1.3.0 dans le cache). Les coroutines ne sont jamais ajoutées à la main côté Android (elles viennent avec `lifecycle`/`room-ktx`).
- Dans le cache Gradle de la machine sont présents (donc utilisables hors-ligne) : AGP 8.5.2 / 8.13.2 / 9.3.2, Kotlin 2.0.20 / 2.2.10 (KGP) / 2.3.20, plugin Compose **2.0.20 uniquement**, KSP **2.0.20-1.0.25 uniquement**, Room 2.6.1, BOM 2024.09.03, navigation 2.8.0, lifecycle-viewmodel-compose 2.8.4, activity-compose 1.9.2, SDK platforms android-35 et android-37.0.

---

## 2. Conventions de code observées

### 2.1 Nommage et langue
- **Package racine : `mg.itu.<nomduprojet>`** (`mg.itu.cycledevie`, `mg.itu.carteproduit`, `mg.itu.listedetail`, `mg.itu.demosync`), identique pour `namespace` et `applicationId`. Fichiers sous `app/src/main/java/mg/itu/<projet>/` (dossier `java`, même pour du Kotlin). Aucun sous-package dans les projets du cours.
- **Identifiants en français, sans accents** : classes `Produit`, `Collecte`, `Producteur`, `EtatUi`, `EtatDemo`, `ProduitsViewModel`, `DemoViewModel`, `ProduitDao`, `CollecteDao`, `FauxServeur`, `ModeAffichage` ; fonctions `formatAriary`, `ajouterAuPanier`, `changerMode`, `enregistrerCollecte`, `synchroniser`, `basculerReseau`, `heureCourante`, `obtenir` ; propriétés `poidsKg`, `prixKg`, `synchronisee`, `enAttente`, `reseau`, `syncEnCours`, `journalServeur`. Les accents n'apparaissent que dans les chaînes affichées et les commentaires (`"prix non fixé"`, `"Réseau coupé"`).
- Quelques noms restent « techniques » en anglais quand c'est le vocabulaire du framework : `MainActivity`, `AppNavigation`, `AppDatabase`, `uiState`, `viewModel`, `navController`, `onCreate`.
- **Fichiers** : `MainActivity.kt` (Activity + navigation + écrans + utilitaires), `ProduitsViewModel.kt` (le ViewModel et son `EtatUi`), `Donnees.kt` (Entity + DAO + Database + jeu de données initial), `FauxServeur.kt` (objet simulant le réseau).
- **Composables** : préfixe `Ecran` pour un écran complet (`EcranListe`, `EcranDetail`, `EcranDemo`), nom de composant + type pour un morceau (`ProduitCard`), `AppNavigation` pour le `NavHost`. Callbacks nommés `on...` en anglais/français mixte : `onProduitClick: (Int) -> Unit`, `onRetour: () -> Unit`.
- **État** : la classe d'état s'appelle `EtatUi` (ou `EtatDemo`), exposée sous le nom `uiState` ; la variable locale observée s'appelle `etat` (`val etat by viewModel.uiState.collectAsState()`). Le duo privé/public s'écrit `_uiState` / `uiState`.
- **DAO** : interface `<Entite>Dao`, méthodes en français décrivant la requête : `tousLesProduits()`, `parId(id)`, `insererTous(...)`, `parPrixDecroissant()`, `stockSuperieurA(seuilKg)`, `stockTotal()`, `toutes()`, `enAttente()`, `nombreEnAttente()`, `inserer(...)`, `modifier(...)`.
- **Entités** : `data class` en singulier (`Produit`, `Collecte`), table au pluriel (`@Entity(tableName = "produits")`, `"collectes"`), fichier de base nommé `cooperative.db` / `demo_sync.db`. `AppDatabase.obtenir(context)` (et non `getInstance`).
- **Routes de navigation** : chaînes simples en français : `"liste"`, `"detail/{produitId}"`, `navController.navigate("detail/$produitId")`.
- **Constantes** : `private val PRODUITS = listOf(...)` (majuscules pour une constante de fichier), littéraux numériques avec séparateur `_` (`250_000.0`, `5_000`).
- **Tags de log** en majuscules : `"CYCLE"`, `"CYCLE-2"`, `"RECOMP"` (`Log.i(tag, "...")`, `private val tag = "CYCLE"`).

### 2.2 Style Kotlin
- `val` partout ; `var` uniquement pour l'état Compose local (`var quantite by remember { mutableStateOf(0) }`) et volontairement pour le contre-exemple `var poidsTriche` (« hors circuit »).
- `data class` immuables avec valeurs par défaut (`val produits: List<Produit> = emptyList()`), évolution par `copy(...)`.
- **Nullabilité** : champs `Double?` avec sémantique documentée en commentaire (`// null = prix non fixé`), traitement par `?.let { } ?: "prix non fixé"`, `toIntOrNull()`, `if (produit != null)`. **L'opérateur `!!` est interdit** (règle écrite dans `Collectes.kt`).
- `when` en expression (`val liste = when (modeCourant) { ... }`), `enum class ModeAffichage { NOM, PRIX_DECROISSANT, STOCK_SUFFISANT }` parcouru avec `ModeAffichage.entries`.
- Fonctions d'extension (`fun Collecte.resume(): String`), fonctions à expression unique (`fun produitsCollectes(liste) = liste.map { ... }.distinct().sorted()`), opérations de collections préférées aux boucles (`map`, `filter`, `groupBy`, `mapValues`, `sumOf`, `sortedByDescending`, `find`, `takeLast`).
- `object` pour un singleton (`object FauxServeur`), `companion object` + `@Volatile` + `synchronized` pour la base.
- Virgule finale dans les listes de paramètres et d'arguments multi-lignes ; imports explicites (jamais `*`) dans les projets Android ; `import kotlinx.coroutines.*` toléré dans les TP JVM.
- Chaînes formatées par templates (`"Stock : ${p.stockKg} kg"`) ; `"%02d:%02d:%02d".format(...)`.

### 2.3 Commentaires
- **KDoc en français** (`/** ... */`) sur presque chaque classe, fonction publique et méthode de DAO, expliquant le *pourquoi* (« Un Flow : ré-émet à chaque changement »).
- En-tête de fichier en KDoc décrivant le TP (numéro, titre entre guillemets « », déroulé, TODO).
- Séparateurs de sections : `// ----------` avec titre en MAJUSCULES (`// LA NAVIGATION`, `// L'ENTITY — une table « produits »`).
- Les commentaires sont pédagogiques et abondants ; le code produit pour ATT devra être commenté dans le même esprit (court, en français, explique l'intention).

### 2.4 Gestion de l'état UI
- Séance 4 : état local `remember { mutableStateOf(...) }` avec délégation `by` (imports `getValue`/`setValue`).
- Séances 6 et 7 : **`StateFlow` dans le ViewModel, observé par `collectAsState()`** dans le composable. Jamais de `LiveData`, jamais de `mutableStateOf` dans le ViewModel, pas de `collectAsStateWithLifecycle`.
- État = **une seule `data class` immuable** (`EtatUi`) regroupant tout ce que l'écran affiche ; l'écran ne fait qu'observer ; les événements remontent par des fonctions du ViewModel (« l'état DESCEND, les événements REMONTENT »).
- Un seul ViewModel partagé par les écrans, créé au-dessus du `NavHost` (`val viewModel: ProduitsViewModel = viewModel()`), ou fourni comme paramètre par défaut du composable (`fun EcranDemo(viewModel: DemoViewModel = viewModel())`).

### 2.5 Gestion des erreurs
- Minimaliste : pas de `try/catch`, pas de `Result`, pas d'états `Loading/Error` dans le cours. Les cas d'erreur sont modélisés par des valeurs : `null` (prix non fixé, id introuvable), `Boolean` de retour (`FauxServeur.envoyer` → `false` si réseau coupé) et `break` dans la boucle de synchro. Room configuré avec `fallbackToDestructiveMigration()`.
- Les entrées utilisateur sont validées par des conversions sûres (`toIntOrNull()`).

### 2.6 UI Compose
- Squelette d'activité toujours identique :
  ```kotlin
  class MainActivity : ComponentActivity() {
      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          setContent {
              MaterialTheme {
                  Surface(Modifier.fillMaxSize()) { AppNavigation() }
              }
          }
      }
  }
  ```
- `MaterialTheme` par défaut (aucun `Theme.kt`, aucune palette personnalisée), couleurs prises dans `MaterialTheme.colorScheme.*`, typographie `MaterialTheme.typography.*` (`headlineSmall`, `titleMedium`, `bodyMedium`, `labelMedium`). Quelques `Color(0xFF...)` codées en dur dans la démo.
- Composants utilisés : `Column`, `Row`, `Spacer`, `LazyColumn` + `items`, `Card` + `CardDefaults.cardColors`, `Button`, `OutlinedButton`, `FilterChip`, `Switch`, `Text`, `Modifier.padding/fillMaxWidth/clickable/weight`, unités en `dp` (16, 12, 8, 24).
- Les textes d'interface sont écrits **en dur en français dans le code** (seul `app_name` est dans `strings.xml`, sauf `cycledevie` qui utilise `strings.xml` pour ses layouts XML).

---

## 3. Patterns d'architecture enseignés (extraits représentatifs)

### 3.1 Le ViewModel « simple » (séance 6, `listedetailv2/ProduitsViewModel.kt`)
```kotlin
/** L'état complet de l'interface, en une seule donnée immuable. */
data class EtatUi(
    val produits: List<Produit> = emptyList(),
    val poidsPanierKg: Int = 0,
)

class ProduitsViewModel : ViewModel() {
    // Le _uiState privé est MUTABLE : seul le ViewModel a le droit d'écrire.
    // Le uiState public est en LECTURE SEULE : l'UI ne fait qu'observer.
    private val _uiState = MutableStateFlow(EtatUi(produits = produits))
    val uiState: StateFlow<EtatUi> = _uiState

    /** Appelée par l'écran de détail quand l'utilisateur ajoute au panier. */
    fun ajouterAuPanier(poidsKg: Int) {
        _uiState.update { etat ->
            etat.copy(poidsPanierKg = etat.poidsPanierKg + poidsKg)
        }
    }
}
```

### 3.2 Le ViewModel branché sur Room (séance 7, `listedetailv3/ProduitsViewModel.kt`)
```kotlin
enum class ModeAffichage { NOM, PRIX_DECROISSANT, STOCK_SUFFISANT }

data class EtatUi(
    val produits: List<Produit> = emptyList(),
    val mode: ModeAffichage = ModeAffichage.NOM,
    val stockTotal: Double? = null,
)

class ProduitsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.obtenir(application).produitDao()
    private val mode = MutableStateFlow(ModeAffichage.NOM)

    init {
        // Premier lancement : on remplit la base si elle est vide.
        viewModelScope.launch {
            if (dao.parId(1) == null) dao.insererTous(produitsInitiaux)
        }
    }

    val uiState: StateFlow<EtatUi> =
        combine(
            dao.tousLesProduits(),
            dao.parPrixDecroissant(),
            dao.stockSuperieurA(10.0),
            dao.stockTotal(),
            mode,
        ) { parNom, parPrix, stockOk, total, modeCourant ->
            val liste = when (modeCourant) {
                ModeAffichage.NOM -> parNom
                ModeAffichage.PRIX_DECROISSANT -> parPrix
                ModeAffichage.STOCK_SUFFISANT -> stockOk
            }
            EtatUi(produits = liste, mode = modeCourant, stockTotal = total)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EtatUi(),
        )

    fun changerMode(nouveau: ModeAffichage) {
        mode.value = nouveau
    }
}
```
Points clés : `AndroidViewModel(application)` pour obtenir le `Context` de la base (pas de Factory, pas d'injection), **pas de couche Repository** (le ViewModel parle directement au DAO), la base est « la source de vérité ; l'écran n'en est qu'un reflet », les filtres/tri de l'UI sont des `MutableStateFlow` privés combinés avec les `Flow` Room.

### 3.3 Exposition de l'état et écrans (séance 7, `listedetailv3/MainActivity.kt`)
```kotlin
@Composable
fun EcranListe(
    viewModel: ProduitsViewModel,
    onProduitClick: (Int) -> Unit,
) {
    val etat by viewModel.uiState.collectAsState()

    Column(Modifier.padding(16.dp)) {
        Text("Produits de la coopérative", style = MaterialTheme.typography.headlineSmall)
        Text(
            etat.stockTotal?.let { "Stock total : $it kg" } ?: "Stock total : (non branché)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeAffichage.entries.forEach { m ->
                FilterChip(
                    selected = etat.mode == m,
                    onClick = { viewModel.changerMode(m) },
                    label = { Text(when (m) {
                        ModeAffichage.NOM -> "Nom"
                        ModeAffichage.PRIX_DECROISSANT -> "Prix ↓"
                        ModeAffichage.STOCK_SUFFISANT -> "Stock > 10"
                    }) },
                )
            }
        }
        LazyColumn {
            items(etat.produits) { p ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onProduitClick(p.id) },
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(p.nom, style = MaterialTheme.typography.titleMedium)
                        Text(
                            p.prixKg?.let { "${formatAriary(it)} / kg" } ?: "prix non fixé",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EcranDetail(produit: Produit, onRetour: () -> Unit) {
    Column(Modifier.padding(24.dp)) {
        Text(produit.nom, style = MaterialTheme.typography.headlineMedium)
        ...
        Button(onClick = onRetour) { Text("Retour à la liste") }
    }
}
```
L'écran de détail reçoit l'objet déjà résolu (`produit: Produit`) et un callback `onRetour` ; il ne connaît pas le `navController`.

### 3.4 Navigation : **Navigation Compose, une seule Activity** (séances 5 à 7)
```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // UN SEUL ViewModel, partagé par les deux écrans : l'état vit ici,
    // au-dessus de la navigation — il survit aux allers-retours ET à la rotation.
    val viewModel: ProduitsViewModel = viewModel()

    NavHost(navController = navController, startDestination = "liste") {

        composable("liste") {
            EcranListe(
                viewModel = viewModel,
                onProduitClick = { produitId -> navController.navigate("detail/$produitId") },
            )
        }

        composable("detail/{produitId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("produitId")?.toIntOrNull()
            val etat by viewModel.uiState.collectAsState()
            val produit = etat.produits.find { it.id == id }
            if (produit != null) {
                EcranDetail(produit = produit, onRetour = { navController.popBackStack() })
            }
        }
    }
}
```
Routes en chaînes de caractères (pas de routes typées, pas de `NavType`, pas de `navArgument`) ; l'argument est lu via `backStackEntry.arguments?.getString(...)?.toIntOrNull()` ; retour par `popBackStack()`. Le seul projet à plusieurs `Activity` est `cycledevie` (séance 3, `Intent` explicite/implicite avec `AppCompatActivity` et layouts XML) — c'est un TP d'observation du cycle de vie, pas le modèle d'architecture cible.

### 3.5 Room : Entity, DAO, Database, Flow (`listedetailv3/Donnees.kt`, `demosync/Donnees.kt`)
```kotlin
@Entity(tableName = "produits")
data class Produit(
    @PrimaryKey val id: Int,
    val nom: String,
    val origine: String,
    val prixKg: Double?,      // null = prix non fixé : la nullabilité va jusqu'en base
    val stockKg: Double,
)

@Entity(tableName = "collectes")
data class Collecte(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val produit: String,
    val poidsKg: Double,
    val heure: String,
    /** false = enregistrée localement, pas encore remontée au serveur. */
    val synchronisee: Boolean = false,
)

@Dao
interface ProduitDao {
    /** Un Flow : ré-émet à chaque changement. */
    @Query("SELECT * FROM produits ORDER BY nom ASC")
    fun tousLesProduits(): Flow<List<Produit>>

    @Query("SELECT * FROM produits WHERE id = :id")
    suspend fun parId(id: Int): Produit?

    @Insert
    suspend fun insererTous(produits: List<Produit>)

    @Query("SELECT * FROM produits ORDER BY prixKg IS NULL, prixKg DESC")
    fun parPrixDecroissant(): Flow<List<Produit>>

    @Query("SELECT * FROM produits WHERE stockKg > :seuilKg")
    fun stockSuperieurA(seuilKg: Double): Flow<List<Produit>>

    @Query("SELECT SUM(stockKg) FROM produits")
    fun stockTotal(): Flow<Double?>
}

@Database(entities = [Produit::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun produitDao(): ProduitDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun obtenir(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cooperative.db",
                )
                    // Pour ce TP : si le schéma change, on repart d'une base neuve.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}

/** Jeu de données initial, inséré au premier lancement. */
val produitsInitiaux = listOf(
    Produit(1, "Vanille Bourbon", "Sambava", 250_000.0, 18.5),
    ...
)
```
Règles : lectures « vivantes » en `Flow<List<T>>` / `Flow<Int>` / `Flow<Double?>` ; opérations ponctuelles en `suspend fun` (`parId`, `enAttente`, `inserer`, `modifier` avec `@Update`) ; pas de relations, pas de `TypeConverter`, pas de migrations, pas de `@Transaction` dans le cours ; singleton `obtenir()` ; jeu de données initial inséré dans `init { viewModelScope.launch { ... } }` du ViewModel si la table est vide.

### 3.6 Coroutines
- JVM (séance 2) : `runBlocking`, `launch`, `async`/`await`, `delay`, `measureTimeMillis`, différence séquentiel vs concurrent (déplacement des `await`).
- Android : **uniquement `viewModelScope.launch { ... }`** et fonctions `suspend` de Room / du faux serveur. Aucun `Dispatchers.IO`, aucun `withContext` : le cours s'appuie sur le fait que Room et `delay` sont déjà « suspend, ça ne bloque rien ». `Flow` combinés avec `combine` + `stateIn`.

### 3.7 Pattern offline-first de `demosync` (« la base d'abord, le réseau ensuite »)
```kotlin
data class EtatDemo(
    val collectes: List<Collecte> = emptyList(),
    val enAttente: Int = 0,
    val reseau: Boolean = true,
    val syncEnCours: Boolean = false,
    val journalServeur: List<String> = emptyList(),
)

class DemoViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DemoDatabase.obtenir(application).collecteDao()
    private val reseau = MutableStateFlow(true)
    private val syncEnCours = MutableStateFlow(false)
    private val journal = MutableStateFlow<List<String>>(emptyList())

    val uiState: StateFlow<EtatDemo> =
        combine(dao.toutes(), dao.nombreEnAttente(), reseau, syncEnCours, journal) {
            collectes, attente, res, sync, jrn -> EtatDemo(collectes, attente, res, sync, jrn)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatDemo())

    /**
     * LE GESTE CENTRAL : on écrit EN BASE d'abord — l'utilisateur voit sa collecte
     * tout de suite, réseau ou pas. La tentative d'envoi vient APRÈS, et son échec est sans gravité.
     */
    fun enregistrerCollecte(produit: String, poidsKg: Double) {
        viewModelScope.launch {
            val collecte = Collecte(produit = produit, poidsKg = poidsKg,
                                    heure = heureCourante(), synchronisee = false)
            dao.inserer(collecte)          // 1. la base, toujours — jamais bloquant
            synchroniser()                 // 2. le réseau, si possible
        }
    }

    /** Remonte au serveur tout ce qui est en attente. Sans réseau : ne casse rien. */
    fun synchroniser() {
        viewModelScope.launch {
            syncEnCours.value = true
            for (collecte in dao.enAttente()) {           // la file d'attente = WHERE synchronisee = 0
                val recue = FauxServeur.envoyer(collecte)
                if (recue) dao.modifier(collecte.copy(synchronisee = true))
                else break                 // réseau coupé : on s'arrête, on réessaiera
            }
            journal.value = FauxServeur.contenu()
            syncEnCours.value = false
        }
    }
}
```
```kotlin
/** FAUX SERVEUR — tout est en mémoire, aucune infrastructure réseau. */
object FauxServeur {
    @Volatile var reseauDisponible: Boolean = true
    private val recues = mutableListOf<String>()

    /** @return true si le serveur l'a reçue, false si le réseau est coupé. */
    suspend fun envoyer(collecte: Collecte): Boolean {
        delay(600)   // le temps d'un aller-retour réseau : suspend, ça ne bloque rien
        if (!reseauDisponible) return false
        recues.add("${collecte.produit} — ${collecte.poidsKg} kg (${collecte.heure})")
        return true
    }
    fun contenu(): List<String> = recues.toList()
    fun vider() = recues.clear()
}
```
Éléments du pattern à reprendre dans ATT : (1) champ booléen `synchronisee` sur l'entité ; (2) la **file d'attente est une requête** (`WHERE synchronisee = 0 ORDER BY id ASC`) et un compteur `Flow<Int>` pour l'affichage « n en attente » ; (3) insertion locale immédiate puis tentative de synchro ; (4) synchro séquentielle, arrêt à la première erreur, relance manuelle par un bouton « Synchroniser maintenant » (`enabled = !etat.syncEnCours && etat.enAttente > 0`) ; (5) statut visible sur chaque carte (`"synchronisée"` / `"en attente"`, couleur différente) ; (6) serveur simulé par un `object` avec interrupteur `reseauDisponible` (un `Switch` dans l'UI) — « dans une vraie application, cette classe serait une interface Retrofit, le reste du code ne changerait pas » ; WorkManager cité seulement comme évolution possible.

### 3.8 Thème Material
`MaterialTheme { Surface(Modifier.fillMaxSize()) { ... } }` sans personnalisation, manifeste sur `@android:style/Theme.Material.Light.NoActionBar`, aucun `themes.xml`/`colors.xml` nécessaire (le squelette Compose du cours n'a que `strings.xml` avec `app_name`).

---

## 4. Évolution pédagogique listedetail v1 → v2 → v3

| | v1 — Mini-TP 5 « Relier deux écrans » | v2 — Mini-TP 6 « Compléter la couche manquante » | v3 — Mini-TP 7 « Trois requêtes » |
|---|---|---|---|
| Fichiers | `MainActivity.kt` seul | + `ProduitsViewModel.kt` | + `Donnees.kt` (Room) |
| Données | `val produits = listOf(...)` global en mémoire, `data class Produit(id, nom, origine, prixKg?)` | idem (liste globale) | `@Entity Produit` (+ `stockKg`) en base SQLite, `produitsInitiaux` insérés au premier lancement |
| État | aucun état partagé ; les écrans reçoivent la liste en paramètre | `EtatUi(produits, poidsPanierKg)` dans `MutableStateFlow` ; `_uiState`/`uiState` ; `update { copy() }` | `EtatUi(produits, mode, stockTotal)` calculé par `combine(...)` de `Flow` Room `+ stateIn(viewModelScope, WhileSubscribed(5_000))` |
| ViewModel | aucun | `ViewModel()` simple ; démonstration « CASSER LE FLUX » (`var poidsTriche` hors circuit qui ne rafraîchit pas l'écran) | `AndroidViewModel(application)` pour accéder à `AppDatabase.obtenir(application)` ; `init` pour peupler la base |
| Écrans | `EcranListe(produits, onProduitClick)`, `EcranDetail(produit, onRetour)` | `EcranListe(viewModel, onProduitClick)` observe `uiState` ; `EcranDetail(produit, viewModel, onRetour)` a un bouton « Ajouter 1 kg au panier » | `EcranListe(viewModel, onProduitClick)` + `FilterChip` de tri/filtre + stock total ; `EcranDetail(produit, onRetour)` redevient purement affichage |
| Navigation | `NavHost` avec `"liste"` et `"detail/{produitId}"` (3 TODO : route, `navigate`, `popBackStack`) | identique, le ViewModel est créé au-dessus du `NavHost` | identique |
| Dépendances ajoutées | navigation-compose 2.8.0 | lifecycle-viewmodel-compose 2.8.4 | KSP + room-runtime/room-ktx/room-compiler 2.6.1 |
| Notion enseignée | Navigation Compose à une Activity, passage d'argument par la route | Flux unidirectionnel : état immuable porté par un ViewModel qui survit à la rotation, observé par `collectAsState()` | Persistance locale : la base est la source de vérité, requêtes SQL (tri avec NULL en dernier, filtre paramétré, agrégat), `Flow` réactifs combinés |

Architecture cible attendue par l'enseignant (v3 + demosync) : **`Ecran*` (Compose) → `XxxViewModel` (StateFlow<EtatUi>, viewModelScope) → `XxxDao` (Room, Flow/suspend) → `AppDatabase`**, plus un `FauxServeur` pour la synchro. Pas de Repository, pas d'injection de dépendances, pas de couche « domaine ».

---

## 5. Le `JOURNAL-IA.md` de minitp1 — format attendu

Contenu exact du fichier (`/Users/mac/M1/KOTLIN/minitp1/JOURNAL-IA.md`) :

```markdown
# Journal IA — Mini-TP 1 — <MAMINIAINA Ericka>

- Fonction soumise : categorieDePoids
- Remarque principale de l'IA : val categorie est inutile : tu stockes le résultat du when pour immédiatement le retourner.
  Tu pourrais directement retourner le when. C'est probablement le principal point à améliorer.

⚠️ Le nom categorie n'apporte pas beaucoup ici puisque le when exprime déjà clairement le résultat.

✅ Les espaces/indentations pourraient être un peu uniformisés. [...]

⚠️ Attention aux valeurs négatives : avec ton code, -5.0 sera classé "petite". [...]
- Mon verdict (accepte / rejette / nuance) et pourquoi : Pour le nom de catégorie il y a une nuance : ce n'est qu'une ligne. Pour les espaces et l'indentation, j'accepte. Pour les valeurs négatives j'accepte aussi parce qu'il a déjà expliqué qu'il faut le gérer.
```

Structure à reproduire pour ATT (une entrée par fonctionnalité/fonction soumise à l'IA) :
1. Titre `# Journal IA — <Projet / TP> — <NOM Prénom>`.
2. `- Fonction soumise :` (ou « Code soumis ») — le nom précis de l'élément.
3. `- Remarque principale de l'IA :` — la remarque jugée la plus importante.
4. Liste des autres remarques, préfixées `⚠️` (point d'attention) ou `✅` (style/mineur).
5. `- Mon verdict (accepte / rejette / nuance) et pourquoi :` — décision argumentée, remarque par remarque.

Règles de travail liées, écrites dans l'en-tête de `Collectes.kt` : phase principale **sans IA** (complétion IA de l'IDE désactivée), `!!` interdit, préférer les opérations de collections aux boucles. Le journal sert donc à tracer un usage *a posteriori* de l'IA (relecture/critique), avec un jugement personnel sur chaque suggestion.

---

## 6. Écarts et recommandations pour `/Users/mac/ATT`

### 6.1 Écarts constatés
| Point | ATT actuel | Cours | Action |
|---|---|---|---|
| Package | `com.example.att` | `mg.itu.<projet>` | passer à **`mg.itu.att`** |
| Kotlin | intégré à AGP 9.3.2 (2.2.10), aucun fichier Kotlin dans `main` | plugin explicite 2.0.20 | ne PAS ajouter `org.jetbrains.kotlin.android` (AGP 9 : « no longer required », message présent dans le jar AGP 9.3.2) |
| Compose | absent | plugin Compose + BOM + activity-compose + material3 + ui | ajouter |
| Navigation / ViewModel | absents | navigation-compose 2.8.0, lifecycle-viewmodel-compose 2.8.4 | ajouter |
| Room | absent | Room 2.6.1 avec **KSP** | ajouter avec KSP (kapt est explicitement **incompatible** avec le Kotlin intégré d'AGP 9) |
| Java | 11 | 17 | passer à 17 |
| `kotlinOptions { jvmTarget }` | n/a | `"17"` | bloc supprimé dans AGP 9 → utiliser `kotlin { compilerOptions { jvmTarget } }` |
| compileSdk/targetSdk | 37 (`release(37)`) | 35 | conserver 37 (platform `android-37.0` installée, requis par core-ktx 1.19.0) ; minSdk 24 identique |
| appcompat + material (MDC) | présents | absents des projets Compose | supprimer (inutiles avec `ComponentActivity`) |
| Thème manifeste | `Theme.ATT` (MaterialComponents) | `@android:style/Theme.Material.Light.NoActionBar` | aligner sur le cours (et supprimer `themes.xml`/`colors.xml` ou les laisser inutilisés) |
| Activity dans le manifeste | aucune | `.MainActivity` exported + intent-filter MAIN/LAUNCHER | ajouter |
| Catalogue de versions | oui | non (littéraux) | conserver le catalogue (c'est le squelette généré par Android Studio ; les technologies restent celles du cours) |
| `configuration-cache=true` | oui | non | conserver (fonctionne avec AGP 9 / KSP2) ; à désactiver seulement en cas de problème |
| Tests JUnit/Espresso | oui | non | conserver, sans obligation d'écrire des tests |

### 6.2 Versions à utiliser (cohérentes avec AGP 9.3.2)
Règle centrale : **la version du plugin Compose et celle de KSP doivent correspondre à la version de Kotlin réellement utilisée par le compilateur**, qui est celle embarquée par AGP 9.3.2 : **Kotlin 2.2.10** (vérifié dans le POM d'AGP 9.3.2 : `kotlin-gradle-plugin 2.2.10`). D'où :
- `org.jetbrains.kotlin.plugin.compose` → **2.2.10**
- `com.google.devtools.ksp` → **2.2.10-2.0.2** (KSP2 ; KSP1 n'est plus nécessaire)
- Le reste : versions du cours (BOM 2024.09.03, activity-compose 1.9.2, navigation-compose 2.8.0, lifecycle-viewmodel-compose 2.8.4, Room 2.6.1). Ces artefacts sont déjà dans le cache Gradle ; seuls le plugin Compose 2.2.10, KSP 2.2.10-2.0.2 et `kotlin-compiler-embeddable` 2.2.10 devront être téléchargés à la première synchronisation.
- Réserve : Room 2.6.1 a été écrit avant KSP2 ; il fonctionne pour des DAO simples comme ceux du cours, mais si le traitement d'annotations échoue avec KSP 2.2.10-2.0.2, passer à **Room 2.7.2** (même API `@Entity/@Dao/@Database`, support officiel de KSP2). De même, si le compilateur Compose 2.2.10 signale une incompatibilité avec le runtime 1.7.x de la BOM 2024.09.03, monter la BOM (par exemple `2025.06.01`, Compose 1.8.x) sans rien changer au code.
- Plan B si l'on veut une pile strictement identique au cours (et 100 % disponible hors-ligne) : revenir à **AGP 8.13.2 + Kotlin 2.0.20 + plugin Compose 2.0.20 + KSP 2.0.20-1.0.25** comme `carteproduit`, avec la DSL AGP 8 (`compileSdk = 35`, `kotlinOptions`, `isMinifyEnabled`). Cela demande de réécrire le `build.gradle.kts` généré par Android Studio (DSL `compileSdk { version = release(37) }` et `optimization { enable = false }` sont propres à AGP 9).

### 6.3 `gradle/libs.versions.toml` proposé
```toml
[versions]
agp = "9.3.2"
# Kotlin est intégré à AGP 9.3.2 (version embarquée : 2.2.10).
# Le plugin Compose et KSP doivent suivre cette version.
kotlin = "2.2.10"
ksp = "2.2.10-2.0.2"
coreKtx = "1.19.0"
activityCompose = "1.9.2"        # cours
composeBom = "2024.09.03"        # cours
navigationCompose = "2.8.0"      # cours
lifecycleViewmodelCompose = "2.8.4"  # cours
room = "2.6.1"                   # cours (2.7.2 si KSP2 pose problème)
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### 6.4 `build.gradle.kts` racine proposé
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
```
(Pas de `org.jetbrains.kotlin.android` : Kotlin est fourni par AGP 9.)

### 6.5 `app/build.gradle.kts` proposé
```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)   // inclut Kotlin (AGP 9)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "mg.itu.att"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "mg.itu.att"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization { enable = false }
        }
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Remplace l'ancien bloc kotlinOptions { jvmTarget = "17" } du cours :
// avec le Kotlin intégré d'AGP 9, l'extension `kotlin` est créée par AGP.
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // Compose — comme carteproduit / listedetail
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.core.ktx)

    // Navigation et ViewModel — comme listedetail v1/v2
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Room — la base de données locale, comme listedetail v3 / demosync
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```
Notes : `gradle.properties`, `settings.gradle.kts` (foojay 1.0.0, dépôts `google()`/`mavenCentral()`) et le wrapper 9.5.0 d'ATT peuvent rester tels quels (le cours est sur Gradle 9.3.0, compatible). Supprimer `appcompat` et `material` du catalogue (ou les laisser non référencés). Si `kotlin { compilerOptions }` n'était pas reconnu dans le script (accesseur absent), la solution de repli est `compileOptions` en 17 seul : AGP 9 aligne par défaut la cible JVM Kotlin sur `targetCompatibility`.

### 6.6 Manifeste et ressources
```xml
<application
    android:label="@string/app_name"
    android:theme="@android:style/Theme.Material.Light.NoActionBar">
    <activity android:name=".MainActivity" android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>
```
(garder `allowBackup`, icônes et `dataExtractionRules` déjà générés si on le souhaite ; `themes.xml`, `values-night/themes.xml` et `colors.xml` deviennent inutiles).

### 6.7 Package : `mg.itu.att`
Justification : les six projets Android du cours utilisent sans exception `mg.itu.<nom>` (préfixe TLD Madagascar + établissement, convention inverse de domaine), `namespace` = `applicationId`. `com.example` est le placeholder d'Android Studio (refusé sur le Play Store et signalé par lint). Concrètement : `namespace`/`applicationId` = `mg.itu.att`, sources dans `app/src/main/java/mg/itu/att/`, `package mg.itu.att` (déplacer `ExampleUnitTest.kt` et `ExampleInstrumentedTest.kt` vers `.../java/mg/itu/att/` avec le nouveau package).

### 6.8 Arborescence de packages recommandée (plusieurs écrans, plusieurs entités, sans sur-architecture)
Le cours reste à plat (3 fichiers). Pour ATT (candidats, examens, inscriptions, résultats, synchronisation…), on garde la même chaîne « Écran → ViewModel → DAO → Database » et on ajoute juste deux niveaux de rangement :

```
app/src/main/java/mg/itu/att/
├── MainActivity.kt              // ComponentActivity + setContent { MaterialTheme { Surface { AppNavigation() } } }
├── Navigation.kt                // AppNavigation() : NavHost, routes "candidats", "candidat/{candidatId}", "examens", ...
├── data/
│   ├── Entites.kt               // @Entity Candidat, Examen, Inscription, Resultat (data class, champ synchronisee)
│   ├── CandidatDao.kt           // un fichier @Dao par entité (ou Daos.kt si peu de requêtes)
│   ├── ExamenDao.kt
│   ├── AppDatabase.kt           // @Database + companion obtenir(context), "att.db", fallbackToDestructiveMigration()
│   ├── DonneesInitiales.kt      // jeux de données insérés au premier lancement (val candidatsInitiaux = listOf(...))
│   └── FauxServeur.kt           // object FauxServeur { reseauDisponible ; suspend fun envoyer(...) : Boolean }
└── ui/
    ├── candidats/
    │   ├── CandidatsViewModel.kt   // EtatCandidats + AndroidViewModel + StateFlow (combine/stateIn)
    │   ├── EcranListeCandidats.kt
    │   └── EcranDetailCandidat.kt
    ├── examens/
    │   ├── ExamensViewModel.kt
    │   ├── EcranListeExamens.kt
    │   └── EcranDetailExamen.kt
    ├── synchro/                    // si l'écran de synchronisation est séparé (compteur en attente, bouton Synchroniser)
    │   ├── SynchroViewModel.kt
    │   └── EcranSynchro.kt
    └── communs/
        └── Formats.kt              // formatAriary, formatDate... (fonctions utilitaires top-level)
```
Règles associées :
- Un ViewModel par fonctionnalité (`XxxViewModel : AndroidViewModel(application)`), obtenu par `viewModel()` dans le `composable(...)` du `NavHost` ou en paramètre par défaut de l'écran ; état unique `data class EtatXxx` exposé en `StateFlow` (`uiState`), observé par `collectAsState()`.
- Les écrans de détail reçoivent l'objet et des callbacks (`onRetour`, `onEnregistrer`), jamais le `navController`.
- Pas de Repository, pas de Hilt, pas de use cases, pas de `Dispatchers` explicites : `viewModelScope.launch` + DAO `suspend`/`Flow`, exactement comme v3 et demosync.
- Textes d'interface en français, identifiants en français sans accents, KDoc courte en français sur chaque classe/fonction publique, séparateurs `// ----` pour les sections.
- Tenir un `JOURNAL-IA.md` à la racine du projet au format de la section 5.
