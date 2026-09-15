# Règles de développement — Projet ATT

> Ce document est la référence de l'équipe (binôme + Claude Code). Il découle du cahier de cadrage, des instructions Claude Code, des supports du module M1 (voir `docs/references/`) et de l'analyse du squelette Android Studio.
> Ordre de priorité en cas de conflit : (1) instructions et cahier de cadrage, (2) ce document, (3) habitudes personnelles.

---

## 1. Règles impératives (non négociables)

| # | Règle | Origine |
|---|---|---|
| R1 | Périmètre = ATT uniquement. Pas de CIM, pas de délivrance de permis, pas de notifications push/SMS, pas de paiement. | Instructions §1, cadrage §13 |
| R2 | Ne jamais inventer une règle administrative. Toute règle inconnue = ligne de configuration (`RegleConfig`) avec `aConfirmer = true`, ou mention « À confirmer » dans `docs/04_QUESTIONS_A_VALIDER.md`. | Instructions §1, §8 |
| R3 | Aucun barème, seuil, catégorie, délai, nombre de tentatives, durée ou capacité codé en dur dans le Kotlin. Ces valeurs sont lues en base. | Instructions §1, cadrage §8 |
| R4 | Le candidat existe sans smartphone : tout parcours doit être réalisable par l'ATT ou l'auto-école, avec impression. | Cadrage §10 |
| R5 | Pas d'affectation préalable candidat → examinateur en V1. | Instructions §1, §5 |
| R6 | Ne jamais écraser ni supprimer un résultat ou une tentative. Correction = nouvelle ligne + `Historique`. | Cadrage §9, instructions §6 |
| R7 | Respecter les technologies apprises en cours (liste §3). Toute notion hors cours est déclarée dans `docs/HORS_COURS.md` avec 2 à 3 phrases d'explication **avant** d'être utilisée. | Instructions §1, §10 |
| R8 | Construire d'abord le modèle métier/données, puis les écrans. Jamais l'inverse. | Instructions §1 |
| R9 | Ne pas coder tout le projet d'un coup : une étape du plan à la fois, avec compte rendu (créé / modifié / testé / décisions restantes). | Instructions §10 |
| R10 | Ne pas ajouter de fonctionnalité hors périmètre sans accord explicite du binôme. | Instructions §10 |
| R11 | Application spécifique à Madagascar : régions malgaches, ariary, formats locaux ; pas de mécanisme multi-pays. | Précision équipe |
| R12 | Rôles et permissions respectés dans chaque écran et chaque ViewModel (matrice §6). | Instructions §7 |

---

## 2. Pile technique

### 2.1 Build (alignement du squelette sur le cours, adapté à AGP 9)

- **AGP 9.3.2** (déjà présent) : Kotlin est intégré au plugin Android (version embarquée 2.2.10). On ne déclare **pas** `org.jetbrains.kotlin.android`, on n'utilise **pas** `kapt` (incompatible), on n'utilise plus `kotlinOptions`.
- Plugins : `com.android.application`, `org.jetbrains.kotlin.plugin.compose` (version = Kotlin embarquée), `com.google.devtools.ksp` (version alignée sur Kotlin embarquée).
- Catalogue de versions `gradle/libs.versions.toml` conservé (généré par Android Studio ; le cours écrit les versions en dur, la différence est de forme).
- `compileSdk 37`, `targetSdk 37`, `minSdk 26` (Android 8.0, requis pour PBKDF2WithHmacSHA256, décision T3 de `docs/04`), Java 17, `buildFeatures { compose = true }`.
- Dépendances = celles du cours : Compose BOM + `material3` + `ui` + `activity-compose`, `navigation-compose`, `lifecycle-viewmodel-compose`, `room-runtime` + `room-ktx` + `ksp(room-compiler)`, `core-ktx`. Supprimer `appcompat` et `material` (MDC Views), inutiles avec Compose.
- Versions proposées (décision T1/T2 de `docs/04`, à confirmer par un premier build à l'étape B0) :

| Composant | Cours (2024) | Stable au 15/09/2026 | Proposé pour ATT | Pourquoi |
|---|---|---|---|---|
| AGP | 8.5.2 / 8.13.2 | 9.3.x | **9.3.2** (squelette) | migration officielle documentée, Kotlin intégré |
| Kotlin | 2.0.20 (plugin) | 2.4.20 | **2.2.10 embarqué par AGP** | zéro configuration, version par défaut d'AGP 9 |
| Plugin Compose | 2.0.20 | = Kotlin | **2.2.10** | doit égaler la version Kotlin |
| KSP | 2.0.20-1.0.25 | 2.3.12 | **2.3.12** | KSP 2.2.x utilise l'ancienne DSL `kotlin.sourceSets`, refusée par le Kotlin intégré d'AGP 9 (constaté au premier build) ; depuis 2.3.0 KSP est versionné indépendamment de Kotlin |
| Compose BOM | 2024.09.03 | 2026.08.00 | **2026.08.00** (repli 2025.x) | exige AGP ≥ 9.1.1 et compileSdk 37 : OK |
| navigation-compose | 2.8.0 | 2.10.1 | **2.10.1** | Navigation 2 = ce que le cours enseigne ; Navigation 3 exclue |
| lifecycle-viewmodel-compose | 2.8.4 | 2.11.0 | **2.11.0** | |
| Room | 2.6.1 | 2.8.5 (Room 3 exclu) | **2.8.5** | 2.6.1 est antérieur à KSP2 ; même API `@Entity/@Dao/@Database` |
| activity-compose | 1.9.2 | 1.13.0 | **1.13.0** | |
| Gradle wrapper | 9.3.0 | 9.7.1 | **9.5.0** (squelette) | minimum requis par AGP 9.3 |

  **Versions confirmées par le premier build vert du 15/09/2026 (étape B0)** : ce tableau est désormais la référence ; `gradle/libs.versions.toml` fait foi en cas d'écart.
- Aucune autre bibliothèque tierce (pas de Hilt, Koin, Retrofit, Coil, Timber…).

### 2.2 Identité du projet
- Package, `namespace` et `applicationId` : **`mg.itu.att`** (convention de tous les projets du cours). Sources dans `app/src/main/java/mg/itu/att/`.
- Nom affiché : `ATT` ; base de données : `att.db`.
- Une seule `MainActivity : ComponentActivity`, thème `@android:style/Theme.Material.Light.NoActionBar`, `MaterialTheme` par défaut.

---

## 3. Ce que l'on utilise, ce que l'on n'utilise pas

### 3.1 Autorisé (vu en cours S1 à S8)
Kotlin idiomatique (`val`, `data class`, `copy`, `when`, null safety, collections `map/filter/groupBy/sumOf/sortedBy/find`), coroutines (`suspend`, `viewModelScope.launch`, `Flow`, `combine`, `stateIn`), Activity + cycle de vie + Logcat, Compose (`Column`, `Row`, `LazyColumn`, `Card`, `Text`, `Button`, `OutlinedButton`, `FilterChip`, `Switch`, `Modifier`, `remember`/`mutableStateOf`), Navigation Compose (routes chaînes, argument = identifiant, `popBackStack`), MVVM (`ViewModel`/`AndroidViewModel`, `StateFlow`, duo `_uiState`/`uiState`, `update { copy() }`, `collectAsState`), Room (`@Entity`, `@Dao`, `@Database`, `Flow` vs `suspend`, singleton `obtenir()`), offline-first (base = source de vérité), `AlertDialog`/`Snackbar` (« à reconnaître »).

### 3.2 Hors cours mais nécessaire au projet → déclaré dans `docs/HORS_COURS.md` avant usage
| Notion | Pourquoi nécessaire | Explication à donner |
|---|---|---|
| `OutlinedTextField`, `KeyboardOptions` | saisie candidat, session, notes | « un `Text` dont la valeur est un état et qui signale `onValueChange`, comme le compteur du TP4 » |
| `Scaffold`, `TopAppBar`, `FloatingActionButton` | ~15 écrans, besoin d'une structure | « conteneur Material 3 = `Column` avec des emplacements nommés » |
| `@ForeignKey`, `@Index`, `@Transaction` | ~20 entités liées, historique atomique | « le SQL qu'on connaît (FOREIGN KEY, INDEX, BEGIN/COMMIT) posé dans des annotations » |
| `enum class` stocké en base | statuts | « Room stocke l'enum en texte, comme `ModeAffichage` du TP7 mais persisté » |
| Dates en `String` ISO / `Long` | sessions, convocations | « pas de `@TypeConverter` : on formate à la main comme `heureCourante()` de la démo » |
| Fonctions de calcul pures (`object CalculResultat`) | barème configurable, testable sans Android | « une fonction Kotlin sans Android, comme les fonctions de `Collectes.kt` » |
| Tests unitaires JUnit sur les calculs | critères MVP « calculer selon le barème » | « le M2 fait les tests ; ici uniquement sur les fonctions pures, pour prouver le barème » |
| Hachage de mot de passe (PBKDF2 via `SecretKeyFactory` + sel `SecureRandom`) | authentification locale | « jamais de mot de passe en clair ; fonction à sens unique de la bibliothèque Java standard, pas de bibliothèque tierce » |
| Impression (`PrintManager` + HTML dans `WebView`, ou `PdfDocument`) | convocations, listes | « l'API d'impression Android ; approche la plus simple retenue à l'étape 12 » |
| `rememberSaveable` | champs de formulaire survivant à la rotation | à éviter : porter l'état de saisie dans le ViewModel (règle S6) |

### 3.3 Interdit (explicitement hors module ou M2)
Hilt/injection de dépendances, Retrofit/Ktor/réseau réel, WorkManager, DataStore (sauf justification), routes typées Navigation, Navigation 3, multi-module, Paging, `LiveData`, `collectAsStateWithLifecycle`, `withContext(Dispatchers.IO)` explicite (Room gère), `runBlocking` dans Android, `Thread.sleep`, opérateur `!!`, boucles `for/while` là où une opération de collection suffit, `MutableStateFlow` exposé publiquement, `navController` passé à un écran, objet passé en argument de route.

---

## 4. Architecture

```
Ecran* (Compose)  →  XxxViewModel (StateFlow<EtatXxx>)  →  XxxDao (Room)  →  AppDatabase
      ↑ état descend (lecture seule)        ↓ événements remontent (fonctions)
```

- **Une responsabilité par couche** : l'écran affiche et signale ; le ViewModel porte l'état et décide ; le DAO stocke. Pas de Repository (jamais codé en cours) sauf besoin démontré et déclaré.
- **Un ViewModel par fonctionnalité** (`CandidatsViewModel`, `SessionsViewModel`, `EvaluationViewModel`, …), `AndroidViewModel(application)` pour obtenir `AppDatabase.obtenir(application)` sans Factory. Créé par `viewModel()` dans le `composable(...)` du `NavHost`.
- **État = une `data class EtatXxx` immuable** exposée en `StateFlow` (`uiState`), construite par `combine(...).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatXxx())` pour les données Room, ou `MutableStateFlow` + `update { copy() }` pour l'état de saisie.
- **Toute écriture** passe par `viewModelScope.launch { dao.xxx() }` ; toute liste affichée vient d'un `Flow` du DAO.
- **Logique métier** (calcul de résultat, contrôle de capacité, éligibilité, tentatives) dans des fonctions pures Kotlin, `object` sans dépendance Android (`metier/`), appelées par les ViewModels et testées par JUnit.
- **Navigation** : routes chaînes en français (`"candidats"`, `"candidat/{candidatId}"`, `"session/{sessionId}/appel"`), argument = identifiant lu par `toIntOrNull()`, null géré sans plantage ; les écrans reçoivent des lambdas (`onRetour`, `onCandidatClick`).
- **Offline-first** : Room est la source de vérité ; l'application fonctionne intégralement sans réseau. Pas de synchronisation réseau dans le MVP (pas de serveur) ; si une démonstration de synchronisation est demandée, reproduire le pattern `demosync` (champ `synchronisee`, `FauxServeur`).
- **Permissions** : chaque ViewModel reçoit l'utilisateur courant (`SessionUtilisateur`) et filtre ses requêtes (auto-école → ses candidats ; candidat → lui-même ; Admin ATT régional → sa région). Les écrans n'affichent que les actions autorisées.

### 4.1 Arborescence
```
app/src/main/java/mg/itu/att/
├── MainActivity.kt
├── Navigation.kt                 // AppNavigation() : NavHost et routes
├── data/
│   ├── Entites*.kt               // @Entity par domaine (Referentiels, Acteurs, Dossiers, Planification, Examens, Historique)
│   ├── *Dao.kt                   // un @Dao par entité (ou par domaine si peu de requêtes)
│   ├── AppDatabase.kt            // @Database + obtenir(context) + "att.db"
│   └── DonneesInitiales.kt       // régions, superadmin, exemples marqués « à confirmer »
├── metier/
│   ├── CalculResultat.kt         // fonctions pures : score, réussite, fautes éliminatoires
│   ├── ReglesPlanification.kt    // capacité, doublons, éligibilité, retard/absence
│   └── ReglesTentatives.kt       // numéro de tentative, épreuves à repasser
├── ui/
│   ├── connexion/                // EcranConnexion + ConnexionViewModel
│   ├── configuration/            // référentiels, barèmes, règles (Super Admin)
│   ├── autoecoles/
│   ├── candidats/                // liste, détail, formulaire, dossier
│   ├── sessions/                 // liste, création, créneaux, inscriptions, appel
│   ├── evaluation/               // tentative, saisie théorie, saisie conduite (structure)
│   ├── resultats/                // consultation, validation, correction
│   ├── impression/               // convocation, liste d'appel, relevé
│   └── communs/                  // Formats.kt (dates, ariary), composants partagés
└── securite/
    └── MotDePasse.kt             // hachage + vérification
```

---

## 5. Conventions de code

- **Langue** : identifiants en **français sans accents** (`Candidat`, `EtatSessions`, `enregistrerPresence`, `parCandidat`), mots-clés du framework en anglais tels quels (`uiState`, `onClick`, `viewModel`). Textes d'interface en français en dur (comme le cours) ; `strings.xml` seulement pour `app_name`.
- **Nommage** : entités au singulier, tables au pluriel (`@Entity(tableName = "candidats")`) ; DAO `CandidatDao` avec méthodes descriptives (`tous()`, `parId(id)`, `parAutoEcole(autoEcoleId)`, `inserer`, `modifier`) ; écrans préfixés `Ecran` ; états `EtatXxx` ; ViewModels `XxxViewModel` ; routes en minuscules ; tags Logcat en majuscules.
- **Style** : `val` par défaut, `data class` immuables, `copy()`, `when` expression, opérations de collections, `?.let { } ?: "…"`, virgule finale sur les listes multi-lignes, imports explicites, littéraux avec `_` (`250_000`).
- **Commentaires** : KDoc court en français sur chaque classe et fonction publique, expliquant le *pourquoi* ; séparateurs `// ---------- TITRE ----------` pour les sections d'un fichier ; en-tête de fichier décrivant son rôle.
- **Erreurs** : modélisées par des valeurs (`null`, `Boolean`, champ `erreur: String?` dans l'état) ; pas de `try/catch` généralisé ; pas d'exception pour un cas métier.
- **Taille** : un écran = un fichier ; un ViewModel ≤ ~200 lignes ; une fonction ≤ ~40 lignes ; sinon découper.
- **Logs** : `Log.i("ATT_XXX", …)` uniquement pour les preuves demandées par le cours ; retirés ou réduits avant rendu.

---

## 6. Matrice des permissions (rappel)

| Ressource | Super Admin | Admin ATT | Auto-école | Examinateur | Candidat |
|---|---|---|---|---|---|
| Référentiels, barèmes, règles, centres | CRUD | Lecture | — | Lecture | — |
| Auto-écoles, comptes | CRUD | CRUD | Lecture (la sienne) | — | — |
| Candidats, dossiers | CRUD | Valider/refuser | CRUD (les siens) | Lecture (à évaluer) | Lecture (soi) |
| Sessions, créneaux, inscriptions | CRUD | CRUD | Lecture (+ demande, à confirmer) | Lecture | Lecture (soi) |
| Présence | CRUD | CRUD | — | — | Lecture |
| Tentatives, évaluations | CRUD | Valider | — | Créer/saisir | Lecture |
| Résultats | CRUD | Valider/corriger | Lecture (ses candidats) | Lecture (ses éval.) | Lecture (soi) |
| Historique | Lecture | Lecture | — | — | — |

---

## 7. Données et base

- Clés étrangères explicites `xxxId: Int` + `@ForeignKey` + `@Index` ; pas de `@Relation`/`@Embedded` sauf besoin déclaré (préférer des requêtes SQL explicites avec `JOIN` et des `data class` de projection).
- Statuts en `enum class` ; dates/heures en `String` ISO 8601 ; montants en ariary entiers (`Int`/`Long`).
- `fallbackToDestructiveMigration()` accepté pendant le développement ; **version de schéma incrémentée** à chaque modification d'entité ; avant soutenance, figer le schéma.
- Jeu de données initial inséré au premier lancement (`if (dao.nombre() == 0)`), contenant uniquement des référentiels et des exemples marqués « à confirmer ».
- Toute modification sensible écrit `Historique` dans la même fonction `@Transaction` du DAO.
- Aucun document/scan stocké tant que la question des données autorisées n'est pas tranchée.

---

## 8. Sécurité minimale

- Mots de passe hachés avec PBKDF2 (`SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")`, sel de 16 octets `SecureRandom` par utilisateur, ~10 000 itérations, comparaison `MessageDigest.isEqual`), jamais en clair, jamais loggés. `EncryptedSharedPreferences` (déprécié) et toute bibliothèque tierce sont exclus.
- Session utilisateur en mémoire dans un `StateFlow` du `ConnexionViewModel` (partagé au-dessus du `NavHost`) ; déconnexion = retour à l'écran de connexion et `popBackStack` complet.
- Aucun accès réseau, aucune permission Android supplémentaire dans le manifeste.

---

## 9. Tests et vérification

- Tests JUnit (`app/src/test`) obligatoires sur `metier/` : calcul de résultat (seuil atteint / non atteint / faute éliminatoire / barème versionné), contrôle de capacité, double inscription, numéro de tentative, épreuves à repasser.
- Pour chaque étape du plan : build `./gradlew :app:assembleDebug` vert, tests `./gradlew :app:testDebugUnitTest` verts, vérification manuelle sur émulateur du scénario de l'étape, capture d'écran dans `docs/captures/` si utile.
- Pas de tests instrumentés ni de tests Compose (M2), sauf demande.

---

## 10. Méthode de travail et Git

- Dépôt Git **public** (exigence des mini-TP), branche `main` stable, une branche par étape (`etape-05-auth`), commits en français à l'impératif (« Ajoute l'entité Candidat et son DAO »), petits et fréquents.
- `.gitignore` : `local.properties`, `build/`, `.gradle/`, `.idea/` (sauf `.idea/codeStyles` si partagé), `*.db`.
- **Suivi de l'avancement (automatique)** : le tableau « Où en est-on ? » en tête de `docs/02_PLAN_DE_TRAVAIL.md` est la source unique de vérité pour les deux devs. Claude le met à jour sans qu'on le lui demande : au début d'une étape (ligne 🟡, « Étape en cours ») et à la fin (ligne 🟢 datée, dernière terminée / prochaine / qui), puis commit et push. Pas de suivi parallèle dans un autre outil : un dev qui arrive lit ce tableau, puis le dernier compte rendu.
- Fin de chaque étape : compte rendu dans `docs/COMPTES_RENDUS.md` (créé / modifié / tests / décisions restantes) + entrée dans `JOURNAL-IA.md` si l'IA a été utilisée + mise à jour du plan + commit + push.
- Chaque membre du binôme doit pouvoir expliquer chaque fichier sans IA (restitution individuelle S8) : relecture croisée avant merge.
- Les fichiers `.docx` de cadrage et leurs versions texte sont dans `docs/references/` ; le `.docx` fait foi.

---

## 11. Protocole IA (Claude Code)

- Une étape du plan à la fois ; Claude annonce ce qu'il va faire, fait, puis rend compte (créé / modifié / tests / décisions restantes).
- Avant d'utiliser une notion hors cours : l'ajouter dans `docs/HORS_COURS.md` avec l'explication simple, puis l'utiliser.
- Toute règle administrative rencontrée sans source : ajouter la question dans `docs/04_QUESTIONS_A_VALIDER.md`, créer une `RegleConfig` par défaut `aConfirmer = true`, continuer.
- L'IA ne réécrit pas un fichier entier sans raison ; elle liste ses remarques et laisse le binôme trancher (`JOURNAL-IA.md`).
- Voie fermée pour les parties évaluées individuellement : le binôme écrit lui-même les fonctions de `metier/` et les DAO principaux ; l'IA relit et critique.
