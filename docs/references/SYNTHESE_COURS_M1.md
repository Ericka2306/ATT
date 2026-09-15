# Synthèse des supports du module M1 « Initiation au développement mobile natif Kotlin, assistée par IA » (ITUniversity, Antananarivo)

Sources lues intégralement : `01_Etat_de_l_art_M1.pdf`, `02_Syllabus_M1.pdf`, `Coroutines.pdf` (séance 2), `S3_Anatomie_Android.pdf`, `S4_Compose.pdf`, `S5_Navigation.pdf`, `S6_MVVM.pdf`, `S7_Room.pdf`, `S8_Synthese.pdf`, `MiniTP1_Enonce_etudiants.docx`, `MiniTP2…6_Enonce_etudiants.pdf`, plus les projets fournis dans `/Users/mac/M1/KOTLIN/` (`cycledevie`, `carteproduit`, `listedetail`, `listedetailv2`, `listedetailv3`, `demosync`, `minitp1`, `minitp2`) pour les versions de bibliothèques et le style de code réellement pratiqué.

Remarque préalable : il n'existe **aucun énoncé de Mini-TP 7** dans le dossier (le Mini-TP 7 n'est décrit que sur la dernière diapositive de `S7_Room.pdf` et dans le code de `listedetailv3`). Il n'existe **aucun document décrivant le projet final ATT** (cahier des charges, grille, dates) : tout ce que les supports disent de l'évaluation est repris ci-dessous, et les manques sont signalés explicitement.

---

## 1. Syllabus et évaluation

### 1.1 Identité du module
- Intitulé : « Initiation au développement mobile natif Kotlin, assistée par IA » — Module M1, kit pédagogique daté de juillet 2026.
- Volume : **8 séances de 4 h = 32 h**. « 1 concept clé par séance », « 8 mini-TP sur code fourni ».
- Prérequis : POO, bases de données relationnelles et SQL, « aucune expérience mobile requise ».
- Devise du module (syllabus p. 8) : « Comprendre chaque ligne avant d'en écrire des milliers : le M1 construit les concepts, le M2 construira l'application. »

### 1.2 Compétences visées (C1–C6, syllabus p. 2)
- C1 · Lire et compléter du Kotlin idiomatique
- C2 · Décrire l'anatomie Android : composants, cycle de vie, Intents
- C3 · Expliquer Compose, l'état et la navigation
- C4 · Décrire MVVM et compléter un ViewModel simple
- C5 · Écrire des requêtes Room, expliquer l'offline-first
- C6 · Juger les propositions de l'IA sur des tâches ciblées

### 1.3 Déroulé des séances (syllabus p. 5–6)

| S | Séance | Concept clé | Mini-TP (sur code fourni) |
|---|--------|-------------|---------------------------|
| 1 | Kotlin essentiel | « La sécurité par le typage : les erreurs passent de l'exécution à la compilation » | « Lire et transformer » : 5 fonctions à trous sur `Collectes.kt` |
| 2 | Coroutines | « La concurrence sans douleur : suspend, async/await » | « Prédire puis vérifier » : 3 programmes à prédire, écarts expliqués |
| 3 | Anatomie d'une app Android | « Des composants orchestrés par le système ; un cycle de vie qu'on subit si on ne le comprend pas » | « Observer le cycle de vie » : callbacks prédits puis vérifiés au Logcat ; Intent de partage |
| 4 | Compose : UI et état | « L'UI comme fonction de l'état : recomposition, remember » | « Faire vivre un écran » : carte produit rendue réactive, recomposition au log |
| 5 | Navigation | « Une application est un graphe d'écrans : naviguer, passer des arguments, revenir » | « Relier deux écrans » : NavHost complété, argument passé de la liste au détail |
| 6 | Architecture MVVM | « Une responsabilité par couche ; l'état descend, les événements remontent » | « Compléter la couche manquante » : ViewModel à trous, puis casser le flux pour voir |
| 7 | Room et offline-first | « La base locale comme source de vérité ; le réseau synchronise quand il peut » | « Trois requêtes » : tri, filtre, agrégat ; synchronisation montrée en démo |
| 8 | Synthèse et ouverture | « Ce que l'IA fait bien et mal ; le chemin parcouru, et l'ouverture KMP » | Retour sur les journaux IA · démo KMP · **restitution individuelle sans IA** |

Les mini-TP « s'emboîtent : la même mini-application liste/détail s'enrichit de séance en séance » (coopérative agricole malgache : produits Vanille, Café, Girofle, Litchi, Poivre ; collectes ; prix en ariary).

### 1.4 Pédagogie officielle (syllabus p. 3)
Quatre gestes : **Lire** (« comprendre du code écrit par d'autres — la compétence professionnelle la plus sous-enseignée »), **Prédire** (« écrire ce que le programme va afficher AVANT de l'exécuter ; l'écart révèle l'incompréhension »), **Compléter** (« des trous ciblés, un par concept. Jamais de projet à créer de zéro »), **Casser pour comprendre** (« introduire volontairement le défaut et constater le problème par soi-même »).

### 1.5 Modalités d'évaluation : ce que disent (et ne disent pas) les documents
- **Aucun document du dossier ne décrit le projet final** (ATT), ni ses critères, ni ses livrables, ni la soutenance, ni les dates. Chaque énoncé de mini-TP se termine par : « Ces dépôts servent au suivi de votre progression. Les modalités d'évaluation du module vous seront précisées ultérieurement. »
- Ce qui est acquis par les mini-TP (à considérer comme les attentes de forme implicites du projet) :
  - dépôt via un **formulaire unique « Sn · Dépôt des livrables »** (Google Forms) en fin de séance ;
  - le projet est rendu en **lien Git public** (« le projet avec le partage fonctionnel, en lien GIT public », TP3 ; « en URL Git », TP4–6) ; en S4 également un ZIP ;
  - une **feuille de prédictions/observations** remplie (photo ou PDF) ;
  - des **captures Logcat filtrées** (tag `CYCLE`, tag `RECOMP`) comme preuves ;
  - un **JOURNAL-IA** : fichier `JOURNAL-IA.md` à la racine (TP1) puis champ « JOURNAL-IA » du formulaire (TP2–6), format « quelques lignes par séance : la tâche, le verdict porté sur la proposition de l'IA, et pourquoi » (syllabus p. 4).
- Séance 8 : « **restitution individuelle sans IA** » (syllabus p. 6) — à anticiper : chaque membre du binôme doit pouvoir expliquer le code sans assistance.
- **Binôme** : le mot n'apparaît qu'à propos du niveau supérieur (syllabus p. 7, « Pratique complète » / M2) : « Un projet développé de bout en bout, en binôme · Offline-first implémenté, tests, injection de dépendances · Fonctionnalité IA embarquée, distribution · Protocole IA complet, journal détaillé ». Tous les mini-TP M1 sont « travail individuel ». Le projet ATT en binôme emprunte donc le format « pratique complète », mais seule la partie « offline-first implémenté » et « journal » relève du périmètre M1 ; tests, DI, IA embarquée et distribution sont explicitement du M2.

### 1.6 Ce que le syllabus et l'état de l'art disent de l'IA / Claude Code
- État de l'art p. 10, « L'IA dans le développement mobile : trois niveaux » :
  1. *Assistants dans l'IDE* : « Gemini in Android Studio, JetBrains AI, Copilot, Claude : complétion, explication d'erreurs, génération de tests, revue de code. »
  2. *Agents de développement* : « Tâches complètes sous supervision humaine : implémenter une fonctionnalité, corriger un bug, refactoriser (**Claude Code**, modes agentiques). »
  3. *IA embarquée dans l'app* : « Modèles sur l'appareil (ML Kit, Gemini Nano) ou via API. Enjeu malgache : l'inférence locale contourne les limites de connectivité. »
  - « Le défi pédagogique : Un étudiant peut désormais produire une application fonctionnelle sans comprendre son code. L'IA amplifie ceux qui maîtrisent les fondamentaux — et court-circuite l'apprentissage de ceux qui les découvrent. »
- État de l'art p. 11, choix n° 3 « IA encadrée : Protocole deux voies (sans / avec IA), appliqué sur des mini-TP ciblés dès le M1 ». Conclusion : « Enseigner Kotlin/Compose maintenant = enseigner le standard Android, la porte du multiplateforme, et le rapport critique au code généré. »
- Syllabus p. 4, « Le protocole IA, version M1 : deux voies, format allégé » :
  - **Voie fermée** (l'essentiel du temps) : lire le code fourni, prédire sur papier, compléter les trous, « Complétion IA de l'IDE désactivée », objectif « construire les schémas mentaux ».
  - **Voie ouverte** (une tâche unique par séance) : « Juger une revue IA de son code · Reformuler une explication d'erreur · Trier des remarques (pertinente / non pertinente) · Vérifier un test généré, juger un diagnostic », objectif « installer le réflexe du jugement ».
  - **Journal IA allégé** : « Quelques lignes par séance : la tâche, le verdict porté sur la proposition de l'IA, et pourquoi ».
- S8 p. 3, bilan des journaux : ce que l'IA fait bien (« Expliquer un comportement déjà observé · Proposer des variantes de mise en page · Repérer des maladresses de style »), ce qu'elle fait mal (« Diagnostics plausibles mais faux · Remarques hors périmètre · Tests qui ne testent rien »), ce que l'étudiant doit savoir faire (« Vérifier avant d'accepter · Distinguer "correct" de "pertinent ici" · Reformuler plutôt que copier · Dire non avec un argument »). « Elle est utile quand vous pouvez VÉRIFIER sa réponse. » « Elle échoue quand elle ne voit pas le contexte — et elle échoue avec assurance. »
- Question posée en S8 : « sur quelle tâche de ce semestre auriez-vous appris MOINS si l'IA avait été autorisée ? »

---

## 2. Notions et technologies enseignées, séance par séance

### Séance 1 — Kotlin essentiel (Mini-TP 1, `Collectes.kt`, projet Kotlin/JVM console)
- `data class` (equals/hashCode/toString/copy générés), `val` immuable, `copy()` pour « corriger » sans muter.
- **Null safety** : types nullables `Double?`, `?.`, `?:`, `let` ; « l'opérateur `!!` est interdit ».
- `when` en expression (sans `if`).
- Collections : `groupBy`, `mapValues`, `sumOf`, `filter`, `sortedByDescending`, `maxByOrNull`, « sans boucle » `for`/`while`.
- Comparaison Java/Kotlin (état de l'art p. 4) : « 5× moins de code — et surtout : les erreurs de nullité passent de l'exécution à la compilation ».
- Contexte métier : coopérative agricole (Produit, Producteur, Collecte), montants en Ar formatés « 1 250 000 Ar ».

### Séance 2 — Coroutines (`Coroutines.pdf`, Mini-TP 2, 3 programmes console)
- Pourquoi : « Un seul thread pour l'interface » ; ANR ; « La règle d'or du mobile : rien ne doit jamais bloquer le thread de l'interface. »
- Thread vs coroutine (métaphore guichet/client) ; coût ≈ 1 Mo/thread vs coroutine « quasi gratuite ».
- `suspend` (« une CAPACITÉ »), `delay` (vs `Thread.sleep` qui bloque), `launch` (→ `Job`, « lancer et oublier »), `async`/`await` (→ `Deferred<T>`, « lancer et récupérer »), `runBlocking` (« réservé au pont console/tests. Jamais dans Android »).
- Quatre façons d'appeler une fonction suspend : appel direct, depuis `launch`, depuis `async`, depuis une autre `suspend`.
- Séquentiel vs concurrent : « c'est la position des await qui fait la concurrence », `measureTimeMillis`.
- **En survol seulement** (p. 9) : `Dispatchers.Main` (UI), `Dispatchers.IO` (« réseau, fichiers, base de données »), « le travail lourd ailleurs, le résultat sur Main » ; `Flow` (« un flux de valeurs dans le temps », « se collecte », « Alimentera l'interface réactive (StateFlow, séance 6) »). « Aujourd'hui : savoir que cela existe. »
- Annonce : dans Android « le ViewModel s'en chargera (séance 6) » (= `viewModelScope`).

### Séance 3 — Anatomie d'une application Android (`S3`, Mini-TP 3, projet `cycledevie` en **XML/Views + AppCompat**)
- Les quatre composants : **Activity** (seule étudiée), Service, Broadcast Receiver, Content Provider — « Tous déclarés dans le manifest — la carte d'identité de l'application ».
- Cycle de vie : `onCreate → onStart → onResume → onPause → onStop → onDestroy`, `onRestart`. « Vous n'appelez JAMAIS ces fonctions — le système les appelle, vous les redéfinissez. Et toute ressource prise dans onResume se libère dans onPause. »
- Rotation = destruction + recréation de l'Activity ; « mon écran se vide quand je tourne le téléphone » ; annonce du ViewModel (S6).
- **Intents** : explicite (`Intent(this, SecondActivity::class.java)`, `startActivity`) ; implicite (`Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, …)`, `Intent.createChooser`).
- Backstack : « Chaque écran ouvert s'empile ; le bouton retour dépile. »
- Outillage : **Logcat** (filtrer `tag:CYCLE`), **points d'arrêt**/débogueur, **lecture d'une stack trace** (« Chercher la PREMIÈRE ligne qui mentionne notre paquet »).
- Projet fourni : `mg.itu.cycledevie`, `MainActivity` + `SecondActivity`, layout `activity_main.xml`, `findViewById`, `Log.i("CYCLE", …)`. Dépendances : `core-ktx 1.13.1`, `appcompat 1.7.0` ; `compileSdk 35`, `minSdk 24`, Java 17.

### Séance 4 — Jetpack Compose : l'UI comme fonction de l'état (`S4`, Mini-TP 4, projet `carteproduit`)
- Paradigme : « L'interface est une photographie de l'état. L'état change ? On reprend la photo. » ; recomposition ; plus de `findViewById`/`setText`.
- `@Composable`, conteneurs `Column`, `Row`, `Card`, `Text`, `Button` ; `MaterialTheme.typography.titleMedium/titleLarge` ; `Modifier` : `padding`, `fillMaxWidth`, `clickable`, `weight(1f)` ; « L'ORDRE COMPTE : padding AVANT clickable → la marge n'est pas cliquable ».
- État local : `var quantite by remember { mutableStateOf(0) }` ; « `remember` survit aux recompositions — pas à la rotation ».
- Log de recomposition `Log.i("RECOMP", …)` comme instrument de mesure.
- Listes : `LazyColumn { items(produits) { p -> ProduitCard(p) } }` ; « seuls les éléments visibles sont composés ».
- Lire un layout XML (table de correspondance : LinearLayout→Column/Row, TextView→Text, RecyclerView→LazyColumn, match_parent→fillMaxWidth/fillMaxSize) : « On ne l'écrit plus — on sait le lire. »
- TP4 : compteur (TODO A), carte sélectionnable (état booléen + `Modifier.clickable` + couleur qui change, TODO B).

### Séance 5 — Navigation (`S5`, Mini-TP 5, projet `listedetail`)
- Bibliothèque : **Navigation Compose** (`androidx.navigation:navigation-compose:2.8.0`), routes **chaînes** : `"liste"`, `"detail/{produitId}"`.
- `rememberNavController()`, `NavHost(navController, startDestination = "liste")`, `composable("route") { … }`, `navController.navigate("detail/$produitId")`, `popBackStack()`, retour système « gratuit ».
- Argument : `backStackEntry.arguments?.getString("produitId")?.toIntOrNull()` puis `produits.find { it.id == id }` et `if (produit != null)`. « Pourquoi l'identifiant, pas l'objet ? La route est du texte, comme une URL ; l'écran retrouve la donnée par lui-même (find aujourd'hui, requête Room en séance 7) ; un identifiant invalide n'affiche rien — il ne plante pas ».
- Principe : « Les écrans ne connaissent pas le navController : ils reçoivent des lambdas — l'écran signale, la navigation décide. »
- Mentionnés « à reconnaître » : `AlertDialog` (confirmation), `Snackbar` (information passagère).
- Projet : une seule `MainActivity : ComponentActivity`, `setContent { MaterialTheme { Surface { AppNavigation() } } }`, écrans `EcranListe(produits, onProduitClick)` et `EcranDetail(produit, onRetour)`.

### Séance 6 — Architecture MVVM (`S6`, Mini-TP 6, projet `listedetailv2`)
- Trois couches : **UI Compose** (« AFFICHE l'état · SIGNALE les gestes. Ne calcule rien, ne décide rien, ne va chercher aucune donnée »), **ViewModel** (« PORTE l'état · TRAITE les événements … ne connaît rien d'Android : testable sans téléphone »), **Repository (source)** (« FOURNIT les données. Seul à savoir d'où elles viennent : mémoire aujourd'hui, base Room en séance 7 ») — *le Repository est nommé sur la diapositive mais jamais codé dans les projets fournis*.
- Flux unidirectionnel : « L'état DESCEND … en lecture seule ; les événements REMONTENT … par appels de fonctions ; aucune autre direction n'est autorisée. »
- Bibliothèque : `androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4` ; `class ProduitsViewModel : ViewModel()` ; `val viewModel: ProduitsViewModel = viewModel()` (« UN ViewModel partagé par les deux écrans »).
- État : `data class EtatUi(val produits: List<Produit> = emptyList(), val poidsPanierKg: Int = 0)` ; duo `private val _uiState = MutableStateFlow(EtatUi(…))` / `val uiState: StateFlow<EtatUi> = _uiState` ; « le flux unidirectionnel est garanti par les TYPES, pas par la discipline ».
- Événement : `_uiState.update { etat -> etat.copy(poidsPanierKg = etat.poidsPanierKg + poidsKg) }`.
- UI : `val etat by viewModel.uiState.collectAsState()`.
- Le ViewModel survit à la rotation (« Ne mettez pas l'état dans l'écran : l'écran est jetable, l'état ne doit pas l'être »).
- Casser le flux : variable `poidsTriche` hors circuit → pas de recomposition.

### Séance 7 — Room et offline-first (`S7`, Mini-TP 7 « Trois requêtes » sur `listedetailv3`, démo `demosync`)
- Offline-first : « L'écran lit TOUJOURS la base locale ; le réseau alimente la base quand il est là » ; « La base locale n'est pas un cache : c'est la source de vérité. Le réseau ne fait que la nourrir. » ; « La coupure n'est plus une panne : c'est un retard de synchronisation. »
- **Room** (`androidx.room:room-runtime:2.6.1`, `room-ktx:2.6.1`, `ksp("androidx.room:room-compiler:2.6.1")`, plugin `com.google.devtools.ksp 2.0.20-1.0.25`) en trois éléments :
  - `@Entity(tableName = "produits") data class Produit(@PrimaryKey val id: Int, …, val prixKg: Double?, …)` ; `@PrimaryKey(autoGenerate = true) val id: Int = 0` (démo) ; « Un Double? devient une colonne NULL autorisée ».
  - `@Dao interface` : `@Query` (« Le SQL est VÉRIFIÉ À LA COMPILATION »), `@Insert`, `@Update`, `@Delete`, `@Query("DELETE …")` ; paramètres `:nomDuParametre`.
  - `@Database(entities = [...], version = 1, exportSchema = false) abstract class AppDatabase : RoomDatabase()` avec singleton `companion object { @Volatile instance … synchronized … Room.databaseBuilder(context.applicationContext, …, "cooperative.db").fallbackToDestructiveMigration().build() }`.
- **Flow ou suspend** : « Je veux être tenu au courant → Flow » ; « question ponctuelle → suspend » ; « Room refuse de compiler une requête bloquante sur le thread principal ». Requêtes du TP : tri `ORDER BY prixKg IS NULL, prixKg DESC`, filtre `WHERE stockKg > :seuilKg`, agrégat `SELECT SUM(stockKg)` → `Flow<Double?>`.
- ViewModel branché sur Room (projet v3) : `class ProduitsViewModel(application: Application) : AndroidViewModel(application)`, `AppDatabase.obtenir(application).produitDao()`, `viewModelScope.launch { if (dao.parId(1) == null) dao.insererTous(produitsInitiaux) }`, `combine(flow1, flow2, …, mode) { … EtatUi(...) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatUi())`, `enum class ModeAffichage`, `FilterChip` pour les modes.
- **Synchronisation** (démo `demosync`) : entité `Collecte` avec drapeau `synchronisee: Boolean = false` ; `enregistrerCollecte` = `dao.inserer(c)` **puis** `synchroniser()` ; boucle `for (c in dao.enAttente()) { if (FauxServeur.envoyer(c)) dao.modifier(c.copy(synchronisee = true)) else break }` ; `FauxServeur` = `object` en mémoire avec `delay(600)` et interrupteur ; `Switch`, `OutlinedButton`, `CardDefaults.cardColors`, `Color(0xFF…)`. « Inverser ces deux lignes … donnerait le modèle naïf. »
- **Perspective seulement** : « Le faux serveur devient une interface Retrofit · le bouton "Synchroniser" devient WorkManager ».
- **DataStore** (p. 7, présenté mais pas pratiqué) : `stringPreferencesKey`, `dataStore.data.map { prefs -> prefs[MODE] ?: "nom" }` (Flow), `dataStore.edit { … }` (suspend) ; critère « on veut interroger → Room · on veut retenir un réglage → DataStore » ; « DataStore remplace SharedPreferences, qui écrivait sur le thread de l'interface ».

### Séance 8 — Synthèse
- « Une application, sept couches » : 1 Langage · 2 Asynchrone · 3 Plateforme · 4 Interface · 5 Navigation · 6 Architecture · 7 Données. « Vous n'avez pas appris sept sujets — vous avez construit une application, une couche par séance. »
- Ouverture KMP « non exigible » : partager modèles/logique/accès données, garder l'UI native.

### Socle technique commun des projets fournis (à reproduire tel quel)
- Gradle Kotlin DSL, sans catalogue de versions : `com.android.application 8.5.2`, `org.jetbrains.kotlin.android 2.0.20`, `org.jetbrains.kotlin.plugin.compose 2.0.20`, `com.google.devtools.ksp 2.0.20-1.0.25`.
- `compileSdk 35`, `targetSdk 35`, `minSdk 24`, Java 17, `buildFeatures { compose = true }`.
- `androidx.compose:compose-bom:2024.09.03`, `activity-compose:1.9.2`, `material3`, `ui`, `navigation-compose:2.8.0`, `lifecycle-viewmodel-compose:2.8.4`, `core-ktx:1.13.1`, `room-runtime/room-ktx/room-compiler 2.6.1`.
- Manifest minimal : une seule `MainActivity` exportée avec `MAIN/LAUNCHER`, thème `@android:style/Theme.Material.Light.NoActionBar`, `@string/app_name`.
- Package `mg.itu.<nomapp>` ; fichiers `MainActivity.kt` (écrans + navigation), `ProduitsViewModel.kt` (état + ViewModel), `Donnees.kt` (Entity + DAO + Database + jeu initial), `FauxServeur.kt`.

---

## 3. Bonnes pratiques et règles explicitement énoncées par l'enseignant

### Style de code et nommage
- **Tout est nommé en français** dans le code fourni : `Produit`, `Collecte`, `EtatUi`, `EcranListe`, `EcranDetail`, `AppNavigation`, `ProduitsViewModel`, `ajouterAuPanier`, `changerMode`, `enregistrerCollecte`, `synchroniser`, `tousLesProduits`, `parId`, `insererTous`, `parPrixDecroissant`, `stockSuperieurA`, `stockTotal`, `formatAriary`, `AppDatabase.obtenir(context)`, `produitsInitiaux`, `FauxServeur`, `reseauDisponible`, tables `produits`/`collectes`, fichier `cooperative.db`. Les commentaires sont en français et abondants (KDoc `/** … */` sur chaque fonction de DAO). Les mots-clés/API restent en anglais (`uiState`, `viewModel`, `onClick`).
- Fichiers peu nombreux et thématiques (un fichier données, un fichier ViewModel, un fichier écrans), paquet unique `mg.itu.xxx`.
- Kotlin idiomatique : `data class` + `val` immuables, `copy()`, `when` expression, opérations de collections plutôt que boucles, `?.let { } ?: "…"` pour l'affichage d'un nullable (« un prix null ne peut pas s'afficher par accident »).
- Interdits explicites : `!!` (TP1) ; `for`/`while` là où les collections suffisent (TP1 partie C) ; `runBlocking` dans Android (S2 p. 6) ; `Thread.sleep` ; tout blocage du thread UI.

### Architecture recommandée (règles textuelles)
- « Compose → ViewModel (StateFlow) → Repository → Room / Retrofit, orchestré par les coroutines. C'est le socle du cours. » (état de l'art p. 3)
- « Une responsabilité par couche : L'UI dessine, le ViewModel expose l'état, le Repository arbitre, les sources stockent → chaque couche se teste isolément. » (état de l'art p. 6)
- « Décision structurante : offline-first. Room est la source de vérité ; le réseau synchronise quand il est disponible. Adapté à la connectivité malgache. » (état de l'art p. 6)
- « Ne mettez pas l'état dans l'écran : l'écran est jetable, l'état ne doit pas l'être. » (S6 p. 2)
- « L'état DESCEND : du ViewModel vers l'UI, en lecture seule · Les événements REMONTENT : de l'UI vers le ViewModel, par appels de fonctions · Aucune autre direction n'est autorisée. » (S6 p. 4)
- « `_uiState` privé et mutable · `uiState` public et en lecture seule : le flux unidirectionnel est garanti par les TYPES, pas par la discipline. » (S6 p. 5)
- « `ajouterAuPanier` ne MODIFIE rien : elle fabrique un nouvel `EtatUi` et le met à la place de l'ancien » ; « L'ancien n'est jamais touché (les champs sont des val). » (S6 p. 6)
- « Les écrans ne connaissent pas le navController : ils reçoivent des lambdas — l'écran signale, la navigation décide. » (S5 p. 3)
- Passer un **identifiant** dans la route, pas l'objet ; relire avec `toIntOrNull` et gérer le null (S5 p. 4).
- « Le critère de choix : "Je veux être tenu au courant" → Flow ; "Je pose une question ponctuelle" → suspend. » (S7 p. 4)
- « Le geste central : la base d'abord » — `dao.inserer(c)` puis `synchroniser()` ; drapeau `synchronisee` ; « on réessaiera » (S7 p. 6).
- « On veut interroger → Room · on veut retenir un réglage → DataStore. » (S7 p. 7)
- Un ViewModel partagé entre les écrans d'un même graphe (`viewModel()` appelé dans `AppNavigation`).
- Compose : `remember` pour l'état purement local d'un composable ; `LazyColumn` pour les listes ; réutiliser les composables (« la composition, c'est aussi la réutilisation ») ; attention à l'ordre des `Modifier`.
- Cycle de vie : « toute ressource prise dans onResume se libère dans onPause ».
- Déboguer : Logcat filtré par tag, points d'arrêt, lire la première ligne de la stack trace qui mentionne « notre paquet ».
- `fallbackToDestructiveMigration()` accepté « Pour ce TP : si le schéma change, on repart d'une base neuve ».

### Conseils / règles sur l'IA
- Voie fermée : « aucune assistance IA — désactivez la complétion IA de votre IDE » pendant les étapes principales.
- Voie ouverte : une seule tâche IA par séance, l'IA « dans un seul rôle : être jugée ». Prompts imposés : « Fais une revue de cette fonction Kotlin : est-elle idiomatique ? lisible ? Liste les points, ne réécris pas tout. » / « Fais une revue de ce code de navigation Compose : liste tes remarques, ne réécris pas tout. » / « Fais une critique de ce ViewModel Kotlin/Compose : liste tes remarques, ne réécris pas tout. » / « Diagnostique ce crash : quelle ligne, quelle cause, quelle correction ? » / « Propose UNE variante de mise en page … sans ajouter de fonctionnalité. »
- « Un "je rejette" bien argumenté vaut mieux qu'un "j'accepte" passif. » (TP1)
- « La reformulation est le livrable ; la réponse brute de l'IA ne vaut rien ici. » (TP2)
- « L'IA suggérera probablement des choses hors périmètre (routes typées, ViewModel, animations…) : les classer "non pertinentes ici" est exactement l'exercice. » (TP5)
- « L'IA parlera probablement d'injection de dépendances, de Repository, de tests unitaires : ce sont des sujets réels, mais hors du périmètre de ce module — le dire en le justifiant est exactement l'exercice. » (TP6)
- « Garder SA version avec de bonnes raisons est un résultat parfaitement valable. » (TP4)
- Format du journal (gabarit TP1) : `# Journal IA — Mini-TP n — <Nom Prénom>` puis `- Fonction soumise`, `- Remarque principale de l'IA`, `- Mon verdict (accepte / rejette / nuance) et pourquoi`.

---

## 4. Ce qui N'EST PAS enseigné

### 4.1 Mentionné seulement comme « perspective », « ouverture » ou « hors périmètre » (utilisable uniquement si on l'explique et le justifie)
- **Repository** : nommé dans l'architecture cible (état de l'art p. 3 et 6, S6 p. 3 « Repository (source) ») mais **jamais codé** ; le ViewModel des projets fournis appelle directement le DAO. En TP6 la remarque IA « utiliser un Repository » est classée « non pertinente ici ».
- **Injection de dépendances / Hilt** : citée en TP6 comme « hors du périmètre de ce module » et réservée au M2 (syllabus p. 7).
- **Tests unitaires / tests générés** : « juger un test généré » (voie ouverte TP7 : « que teste-t-il RÉELLEMENT ? ») ; « tests » = M2 ; « ViewModel testable sans téléphone » est une promesse, pas une pratique.
- **Retrofit / Ktor** (API distante) : état de l'art p. 6, S7 p. 6, `FauxServeur.kt` (« serait remplacée par une interface Retrofit — le reste du code ne changerait pas »).
- **WorkManager** : S7 p. 6 et `LISEZMOI.md` (« en production, ce rôle revient à WorkManager »).
- **DataStore (Preferences)** : une diapositive S7 p. 7 avec extrait de code et critère de choix ; aucune dépendance ni TP. Statut : « présenté, non pratiqué ».
- **Dispatchers.Main / Dispatchers.IO** : « en survol » (S2 p. 9). Aucun `withContext` dans le code fourni ; Room + `suspend` gèrent le thread eux-mêmes.
- **Flow (opérateurs)** : `combine`, `stateIn`, `SharingStarted.WhileSubscribed`, `map` (DataStore) apparaissent dans le code fourni v3/démo, sans être développés en cours. `collectAsState()` utilisé (pas `collectAsStateWithLifecycle`).
- **Service, BroadcastReceiver, ContentProvider** : nommés en S3 p. 2 (« Aujourd'hui : l'Activity »).
- **AlertDialog, Snackbar** : « À reconnaître aujourd'hui ; vous les utiliserez naturellement le moment venu » (S5 p. 5).
- **Routes typées, animations de navigation** : citées en TP5 comme « hors périmètre ».
- **IA embarquée (ML Kit, Gemini Nano)**, **distribution** : M2 / état de l'art.
- **KMP / Compose Multiplatform, SwiftUI, Flutter, React Native** : panorama et ouverture S8, « non exigible ».
- **XML/Views, AppCompat, findViewById, RecyclerView** : « à savoir LIRE », plus à écrire (S4 p. 8) ; utilisés seulement dans `cycledevie` (S3).

### 4.2 Jamais mentionné dans aucun support (à éviter, ou à introduire avec une explication simple et une justification)
- Multi-module Gradle, catalogue de versions `libs.versions.toml`, Kotlin DSL avancé (les projets fournis déclarent les versions en dur dans `build.gradle.kts`).
- `Scaffold`, `TopAppBar`, `NavigationBar`/barre d'onglets, `FloatingActionButton`, `ModalBottomSheet`, thèmes personnalisés Material 3 (`ui-tooling`, `@Preview`, couleur dynamique), icônes `material-icons`.
- Saisie utilisateur : `TextField`/`OutlinedTextField`, validation de formulaire, `KeyboardOptions`, `DatePicker`, gestion du clavier/focus.
- `SavedStateHandle`, `rememberSaveable`, `derivedStateOf`, `LaunchedEffect`/`SideEffect`/`DisposableEffect`, `CompositionLocal`, `Modifier.animate*`.
- Navigation : graphes imbriqués, deep links, arguments typés `navArgument`, `navigate` avec `popUpTo`/`launchSingleTop`, ViewModel scopé à une destination, `hiltViewModel`.
- ViewModel : `ViewModelProvider.Factory` / `viewModelFactory` (le cours utilise `AndroidViewModel(application)` pour éviter la factory), `SharedFlow`/événements one-shot, `sealed class`/`sealed interface` d'état ou d'événements, `Result`, gestion d'erreur/chargement structurée.
- Coroutines : `withContext`, `CoroutineExceptionHandler`, `supervisorScope`, `try/catch` autour des suspend, annulation explicite, `flowOn`, `debounce`, `Channel`.
- Room : `@ForeignKey`, `@Relation`/`@Embedded`/`@Transaction` (jointures multi-tables), `@TypeConverter` (dates, enums), `@Index`, migrations réelles, `RoomDatabase.Callback` de pré-remplissage, `@Upsert`, pagination (Paging 3), chiffrement.
- Réseau réel : `HttpURLConnection`, OkHttp, Ktor client, kotlinx.serialization / Moshi / Gson, permission `INTERNET`, détection de connectivité (`ConnectivityManager`).
- Stockage : SharedPreferences (cité seulement comme remplacé), fichiers, `EncryptedSharedPreferences`, export CSV/PDF, `FileProvider`.
- Dates/heures : `java.time` (la démo utilise `java.util.Calendar` et formate à la main), `SimpleDateFormat`, fuseaux.
- Permissions runtime, appareil photo, capteurs, localisation/cartes, notifications, `Service` foreground, `AlarmManager`.
- Authentification, rôles, sécurité, chiffrement, biométrie.
- Ressources : `strings.xml` (seule `app_name` existe ; les textes sont en dur dans les composables), localisation, `dimens`, `drawable`, icônes d'application personnalisées.
- Tests instrumentés (Espresso, Compose UI test), Robolectric, `kotlinx-coroutines-test`, Turbine, JUnit dans les projets fournis (aucune dépendance de test).
- Qualité : ktlint/detekt, CI/CD, signature APK, ProGuard/R8, `release` build.
- Accessibilité, thème sombre, internationalisation, tablettes/adaptatif.
- Bibliothèques tierces quelconques (Coil, Accompanist, Timber, Koin, etc.).

---

## 5. Mini-TP 1 à 7

### Mini-TP 1 — « Lire et transformer » (S1, docx, individuel, projet console `minitp1`)
- **Objectif** : lire un fichier Kotlin idiomatique fourni (`Collectes.kt`, coopérative agricole malgache), prédire 4 sorties (P1–P4), compléter 5 fonctions à trous, porter un premier jugement sur une revue IA.
- **Notions** : data class + `copy()` (`corrigerPoids`), null safety `?.` (`prixEstime`), `when` (`categorieDePoids` : petite < 10 kg, moyenne 10–25, grosse au-delà), `groupBy`+`mapValues`+`sumOf` (`totalParProduit`), `filter`+`sortedByDescending` (`collectesValorisables`), bonus `groupBy`+`maxByOrNull` (`producteurLePlusActif`).
- **Règles** : aucune IA pendant A/B/C, complétion IDE désactivée ; `!!` interdit ; aucune boucle en partie C ; annotations `// A-1, // A-2, // A-3` (construction connue de Java, nouvelle comprise, non comprise).
- **Voie ouverte** : revue IA d'UNE fonction (prompt imposé), verdict en 3 lignes.
- **Livrables** : `Collectes.kt` complété + annotations ; feuille de prédictions avec écarts expliqués ; `JOURNAL-IA.md` (3 lignes, gabarit fourni). Dépôt « sur la plateforme du cours avant la fin de la séance ».

### Mini-TP 2 — « Prédire puis vérifier » (S2, individuel, 3 programmes console `minitp2`)
- **Objectif** : prédire ordre d'affichage et durée de 3 programmes (`launch` ; `async` concurrent ; « le piège » de l'`await` mal placé), expliquer les écarts, transformer P2 en séquentiel et P3 en concurrent « par simple déplacement des await », reformuler une explication IA.
- **Notions** : `runBlocking`, `launch`, `async`/`await`, `delay`, `measureTimeMillis`.
- **Règles** : prédire sur papier avant d'exécuter ; aucune IA étapes 1–3 ; « Si vous ajoutez ou retirez autre chose, c'est que vous cherchez trop loin ».
- **Voie ouverte** : faire expliquer UN écart par l'IA, reformuler en 3 lignes « sans recopier une seule phrase de l'IA ».
- **Livrables** (formulaire « S2 · Dépôt des livrables ») : `Programme2.kt` et `Programme3.kt` transformés avec durées mesurées en commentaire et dans le formulaire ; feuille (photo/PDF) ; reformulation dans le champ JOURNAL-IA.

### Mini-TP 3 — « Observer le cycle de vie » (S3, individuel, app `CycleDeVie` XML)
- **Objectif** : prédire puis vérifier au Logcat (`tag:CYCLE`) les callbacks pour rotation (onPause→onStop→onDestroy→onCreate→onStart→onResume) et accueil/retour (onPause→onStop→onRestart→onStart→onResume) ; constater le changement de numéro d'instance ; compléter `partagerCollecte()` avec un Intent implicite `ACTION_SEND` (`text/plain`, « Collecte du jour : 4,5 kg de vanille », `Intent.createChooser`) ; bonus : entrelacement `CYCLE`/`CYCLE-2` avec `SecondActivity`.
- **Notions** : cycle de vie, Intent explicite/implicite, Logcat, stack trace, `findViewById` (lecture).
- **Voie ouverte** : juger un diagnostic IA d'une stack trace (`NullPointerException: findViewById(R.id.btnPartage) must not be null`, ligne 29) — « bonne ligne ? bonne cause ? bonne correction ? », indice dans `activity_main.xml`.
- **Livrables** (formulaire S3) : feuille remplie ; captures Logcat filtrées annotées (une par scénario) ; **projet en lien Git public** ; verdict 3 lignes dans JOURNAL-IA.

### Mini-TP 4 — « Faire vivre un écran » (S4, individuel, app `CarteProduit` Compose)
- **Objectif** : prédire le nombre de lignes `RECOMP` (démarrage, puis 3 clics) ; TODO A compteur `remember { mutableStateOf(0) }` + `Button` ; TODO B carte sélectionnable (état booléen, `Modifier.clickable`, couleur changeante) ; observation bonus : à la rotation « tout se réinitialise ».
- **Notions** : `@Composable`, `remember`/`mutableStateOf`/`by`, recomposition, `Modifier`, `Card`/`Column`/`Row`/`Text`/`Button`, `Log.i("RECOMP")`.
- **Règles** : ne pas déplacer le log RECOMP ; aucune IA étapes 1–3.
- **Voie ouverte** : demander UNE variante de mise en page à l'IA « sans ajouter de fonctionnalité », juger en 3 lignes (lisibilité, cohérence visuelle, simplicité).
- **Livrables** (formulaire S4) : feuille ; capture Logcat `RECOMP` ; projet (URL Git / ZIP) ; jugement dans JOURNAL-IA.

### Mini-TP 5 — « Relier deux écrans » (S5, individuel, app `ListeDetail`)
- **Objectif** : « SEULE la fonction `AppNavigation()` est à modifier — trois TODO » : TODO 1 route `composable("detail/{produitId}")` (recopier le modèle « pour le COMPRENDRE »), TODO 2 `navigate` au clic, TODO 3 `popBackStack`. Vérifier que Girofle ouvre bien Girofle ; observer le retour système depuis le détail puis depuis la liste (« la pile est vide donc ça quitte l'application ») ; cas limite du Litchi (prix null → `?:`).
- **Notions** : NavHost, routes, arguments, backstack, `toIntOrNull`, null safety jusque dans l'UI.
- **Voie ouverte** : revue IA de `AppNavigation()`, trier chaque remarque pertinente / non pertinente ICI (routes typées, ViewModel, animations = non pertinentes).
- **Livrables** (formulaire S5) : feuille ; projet en URL Git ; synthèse du tri dans JOURNAL-IA.

### Mini-TP 6 — « Compléter la couche manquante » (S6, individuel, app `ListeDetail v2`)
- **Objectif** : « seul `ProduitsViewModel.kt` est à modifier (deux TODO) » : TODO 1 duo `_uiState`/`uiState`, TODO 2 `ajouterAuPanier` avec `update` + `copy`. Vérifier panier partagé entre écrans et survie à la rotation. Étape 3 « casser le flux » : `poidsTriche` hors circuit (pas de recomposition ; à la rotation la valeur « rattrape » puis se perd). Question de contrôle : pourquoi `StateFlow` et non `MutableStateFlow` ? (« Pour que l'interface puisse seulement observer l'état et pas le modifier »).
- **Notions** : ViewModel, StateFlow, `update`/`copy`, `collectAsState`, flux unidirectionnel, durée de vie.
- **Voie ouverte** : critique IA du ViewModel, tri pertinente / non pertinente (DI/Hilt, Repository, tests unitaires = « hors du périmètre de ce module »).
- **Livrables** (formulaire S6) : feuille ; projet en URL Git ; synthèse du tri dans JOURNAL-IA.

### Mini-TP 7 — « Trois requêtes » (S7, d'après la diapositive S7 p. 8 et `listedetailv3` ; **pas d'énoncé PDF dans le dossier**)
- **Objectif** : lire `Donnees.kt`, prédire (écran avant TODO ; persistance après fermeture/réouverture) ; écrire 3 requêtes DAO : TODO 1 tri `ORDER BY prixKg IS NULL, prixKg DESC` → `Flow<List<Produit>>`, TODO 2 filtre `WHERE stockKg > :seuilKg`, TODO 3 `SELECT SUM(stockKg)` → `Flow<Double?>` ; étape 3 : brancher au moins un mode + le stock total dans le `combine` du ViewModel ; observer la place du Litchi.
- **Notions** : Entity/DAO/Database, Flow vs suspend, SQL vérifié à la compilation, `combine`/`stateIn`, `AndroidViewModel`, `FilterChip`, offline-first (démo `demosync`).
- **Voie ouverte** : faire générer un test d'une requête par l'IA ; « que teste-t-il RÉELLEMENT, et que faudrait-il vérifier en plus ? » (3 lignes).
- **Livrables** : non précisés dans le dossier ; par analogie, feuille + URL Git + JOURNAL-IA.

### Constantes de forme sur tous les mini-TP
- Toujours : lire → prédire sur papier → exécuter → expliquer les écarts (« un écart n'est pas une faute »).
- Toujours une seule tâche IA, toujours un verdict argumenté en 3 lignes recopié dans le champ JOURNAL-IA.
- Toujours le code déposé en **dépôt Git public**, projet ouvert dans Android Studio ; émulateur ou appareil physique.
- Pas de README exigé dans les mini-TP ; le seul fichier de documentation vu est `LISEZMOI.md` (démo, en français) et `JOURNAL-IA.md`.

---

## 6. Implications pour le projet ATT (gestion des examens du permis de conduire — Agence des Transports Terrestres)

### 6.1 Pile technique recommandée (strictement « déjà apprise »)
Reproduire à l'identique le socle des projets fournis (`listedetailv3` + `demosync`) :
- **Langage / build** : Kotlin 2.0.20 (K2), AGP 8.5.2, KSP 2.0.20-1.0.25, Java 17, `compileSdk/targetSdk 35`, `minSdk 24`, Gradle Kotlin DSL avec versions en dur (pas de catalogue).
- **UI** : Jetpack Compose via BOM `2024.09.03`, Material 3, `activity-compose 1.9.2` ; **une seule Activity** (`MainActivity : ComponentActivity`, `setContent { MaterialTheme { Surface { AppNavigation() } } }`).
- **Navigation** : `navigation-compose 2.8.0`, routes chaînes (`"examens"`, `"examen/{examenId}"`, `"candidat/{candidatId}"`), argument = identifiant, relu avec `toIntOrNull()` et null géré, écrans qui reçoivent des lambdas (`onExamenClick`, `onRetour`).
- **État / architecture** : `lifecycle-viewmodel-compose 2.8.4` ; un ViewModel par écran ou partagé par graphe via `viewModel()` ; `data class EtatUi(...)` immuable ; duo `_uiState: MutableStateFlow` / `uiState: StateFlow` ; `update { it.copy(...) }` ; `collectAsState()` côté UI ; flux unidirectionnel strict.
- **Données** : Room 2.6.1 (`@Entity`, `@Dao`, `@Database`, singleton `obtenir(context)`, `fallbackToDestructiveMigration()`), lectures en `Flow` pour les listes/compteurs, `suspend` pour les requêtes ponctuelles et toutes les écritures (`@Insert`, `@Update`, `@Delete`, `@Query("DELETE …")`), SQL explicite avec `ORDER BY`, `WHERE … :param`, `SUM/COUNT`.
- **Asynchrone** : `viewModelScope.launch { }` pour toute écriture ; jamais `runBlocking`, jamais de blocage du thread UI ; `combine(...).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatUi())` pour composer plusieurs Flow de la base (modèle exact du cours).
- **Offline-first** : Room = source de vérité ; si une « synchronisation » est requise, reproduire la démo : drapeau `synchronisee`, `dao.inserer()` puis `synchroniser()`, boucle sur `enAttente()` avec `break` au premier échec, serveur simulé par un `object FauxServeur` en mémoire avec `delay()` et interrupteur (`Switch`).
- **Composables autorisés (vus en cours)** : `Column`, `Row`, `Spacer`, `Card`, `Text`, `Button`, `OutlinedButton`, `Switch`, `FilterChip`, `LazyColumn`/`items`, `MaterialTheme.typography/colorScheme`, `Modifier.padding/fillMaxWidth/fillMaxSize/height/weight/clickable/background`, `CardDefaults.cardColors`, `Color(0xFF…)`, `RoundedCornerShape`, `FontFamily.Monospace` ; `AlertDialog` et `Snackbar` sont « reconnus » et peuvent être utilisés avec une phrase d'explication.
- **Organisation des fichiers** (comme le cours) : paquet unique `mg.itu.att` ; `MainActivity.kt` (Activity + `AppNavigation` + écrans, ou un fichier par écran `EcranXxx.kt`), `XxxViewModel.kt`, `Donnees.kt` (entités + DAO + `AppDatabase` + jeu de données initial), éventuellement `FauxServeur.kt`.
- **Nommage** : français pour les classes, fonctions, propriétés, tables et commentaires (`Candidat`, `Examen`, `Inscription`, `EcranListeExamens`, `EtatUi`, `enregistrerResultat`, `parId`, `tousLesExamens`), KDoc en français sur chaque fonction de DAO ; identifiants API en anglais tels quels.
- **Outillage de preuve** : `Log.i("TAG", …)` + Logcat filtré, captures d'écran ; dépôt Git public ; `JOURNAL-IA.md` à la racine (gabarit TP1 : tâche, remarque principale de l'IA, verdict accepte/rejette/nuance et pourquoi, une entrée par usage de l'IA) ; `LISEZMOI.md` en français plutôt que README anglais.

### 6.2 Points qui dépassent le cours et devront être « expliqués simplement avant utilisation »
Classés du moins au plus risqué vis-à-vis de la contrainte :
1. **Saisie de formulaires** (`OutlinedTextField`, `KeyboardOptions`) : indispensable pour créer un candidat/un examen, jamais vu en cours. Expliquer : « un `TextField` est un `Text` dont la valeur est un état `remember`/ViewModel et qui signale `onValueChange` — même principe que le compteur du TP4 ».
2. **`rememberSaveable`** ou état de saisie porté par le ViewModel : préférer le ViewModel (règle S6) ; ne pas introduire `SavedStateHandle`.
3. **`Scaffold`/`TopAppBar`/`FloatingActionButton`/`NavigationBar`** : confort d'UI non enseigné ; utilisables si présentés comme « conteneurs Material 3 équivalents à `Column` avec des emplacements nommés ». Rester sobre ; `Column` + `Button` restent le défaut du cours.
4. **Plusieurs entités liées** (Candidat ↔ Examen ↔ Résultat) : rester sur des **clés étrangères manuelles** (`val candidatId: Int`) et des requêtes SQL explicites avec `JOIN` ou requêtes séparées combinées par `combine` ; éviter `@Relation`/`@Embedded`/`@Transaction` ou les introduire avec une explication SQL (le cours dit « Le SQL que vous connaissez déjà — simplement posé dans des annotations »).
5. **Dates** : le cours n'enseigne rien ; stocker en `String` ISO (`"2026-09-15"`) ou `Long` (epoch) et formater à la main comme `heureCourante()`/`formatAriary()` ; pas de `@TypeConverter` sauf explication.
6. **Enums en base** (statut d'examen : programmé/réussi/échoué) : Room accepte les `enum class` nativement (stockés en texte) ; c'est cohérent avec `enum class ModeAffichage` vu en v3, mais le stockage d'un enum dans une Entity n'a pas été montré → une phrase d'explication.
7. **Repository** : nommé sur les diapositives, jamais codé. Deux options : (a) rester fidèle au code fourni (ViewModel → DAO direct), (b) ajouter une classe `XxxRepository` en la justifiant par la diapositive S6 p. 3 (« Seul à savoir d'où elles viennent ») — acceptable, mais sans DI/Hilt : instanciation manuelle dans le ViewModel.
8. **`AndroidViewModel(application)` vs Factory** : conserver `AndroidViewModel` + `AppDatabase.obtenir(application)` comme en v3 pour éviter toute `ViewModelProvider.Factory` (non enseignée).
9. **Gestion d'erreurs/chargement** (`try/catch` autour des suspend, champ `chargement`/`erreur` dans `EtatUi`) : non enseigné ; si nécessaire, rester sur des booléens dans `EtatUi` comme `syncEnCours` de la démo.
10. **DataStore** (préférences : agent connecté, dernière synchro) : présenté en S7 p. 7 avec code mais jamais pratiqué ; utilisable en citant la diapositive et en ajoutant la dépendance `androidx.datastore:datastore-preferences` avec une explication ; sinon, stocker le réglage dans une petite table Room ou dans l'état du ViewModel.
11. **`collectAsStateWithLifecycle`, `withContext(Dispatchers.IO)`** : ne pas les introduire ; `collectAsState()` et Room/suspend suffisent (« Room refuse de compiler une requête bloquante sur le thread principal »).
12. **Réseau réel (Retrofit/Ktor), WorkManager, Hilt, tests unitaires/instrumentés, multi-module, catalogue de versions, IA embarquée, distribution** : explicitement M2 ou « perspective » → **à exclure** du projet, ou au plus les citer en « perspectives » dans la soutenance/LISEZMOI, exactement comme le cours le fait.
13. **Strings en ressources / localisation / thème sombre / icônes** : non enseignés ; les textes en dur en français (comme dans tous les projets fournis) sont conformes au cours.

### 6.3 Règles de développement à dériver (proposition)
- Chaque fichier/notion utilisé doit pouvoir être rattaché à une séance (S1–S7) ; sinon le noter dans une section « Hors cours — expliqué » du `LISEZMOI.md` avec 2–3 phrases.
- Respecter les interdits textuels : pas de `!!`, pas de `runBlocking`, pas de boucle là où une opération de collection suffit, pas d'état mutable partagé hors ViewModel, pas de `MutableStateFlow` exposé, pas de `navController` passé aux écrans, pas d'objet passé en argument de route.
- Toute écriture en base passe par `viewModelScope.launch` et une fonction `suspend` du DAO ; toute liste affichée provient d'un `Flow` du DAO.
- Rester dans le protocole IA du module : voie fermée pour le code cœur, voie ouverte documentée dans `JOURNAL-IA.md` (tâche, remarque, verdict argumenté) ; préparer la « restitution individuelle sans IA » de S8 (chaque membre du binôme sait expliquer chaque ligne).
- Livrables à prévoir même si non précisés : dépôt Git public, `LISEZMOI.md` en français (ouverture, déroulé de démo à la manière de `demosync/LISEZMOI.md`), captures Logcat/écran, `JOURNAL-IA.md`, feuille de prédictions si demandée.
