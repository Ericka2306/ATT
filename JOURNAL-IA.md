# Journal IA — Projet ATT — MAMINIAINA Ericka et <Binôme>

Format hérité du Mini-TP 1 : une entrée par usage de l'IA (revue, diagnostic, génération, décision d'architecture).
Règle du module : « un "je rejette" bien argumenté vaut mieux qu'un "j'accepte" passif ». La reformulation est le livrable, pas la réponse brute de l'IA.

---

## Entrée 1 — 15/09/2026 — Cadrage et règles du projet

- Tâche soumise : analyse du cahier de cadrage, des instructions et des supports de cours ; rédaction des règles de développement et du plan de travail.
- Remarque principale de l'IA : le squelette Android Studio (AGP 9.3.2, `com.example.att`, sans Compose ni Room) n'est pas aligné sur la pile du cours ; il faut ajouter Compose, Navigation Compose, ViewModel et Room via KSP, et passer le package en `mg.itu.att`.

⚠️ L'IA propose d'ajouter `@ForeignKey`/`@Index` sur les entités Room et un `Repository` : non enseignés en cours, à justifier avant usage.

⚠️ L'IA rappelle que les règles administratives (seuils, délais, catégories) ne sont pas connues : elles doivent rester de la configuration marquée « À confirmer ».

- Mon verdict (accepte / rejette / nuance) et pourquoi : <à compléter par l'équipe après lecture des documents dans docs/>.

---

## Entrée 2 — 15/09/2026 — Étape B0, configuration Gradle

- Code soumis : `gradle/libs.versions.toml`, `build.gradle.kts`, `app/build.gradle.kts` (écrits par l'IA).
- Remarque principale de l'IA : avec AGP 9, ne pas déclarer le plugin `org.jetbrains.kotlin.android` ni `kotlinOptions` ; aligner le plugin Compose sur la version Kotlin embarquée (2.2.10) et utiliser KSP plutôt que kapt.

⚠️ L'IA avait d'abord proposé KSP 2.2.10-2.0.2 (version citée par la documentation AGP 9.0). Le build a échoué : « Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin ». Correction : KSP 2.3.12, versionné indépendamment de Kotlin.

✅ Les versions des bibliothèques ont été vérifiées sur les dépôts Maven avant d'être écrites (aucune version inventée).

- Mon verdict (accepte / rejette / nuance) et pourquoi : <à compléter par le dev 1 : vérifier soi-même l'erreur en remettant KSP 2.2.10-2.0.2 et en relançant le build, pour pouvoir l'expliquer sans IA>.

---

## Entrée 3 — 15/09/2026 — Étapes B1 et C1, base de données et base du front-end

- Code soumis : `data/Entites*.kt`, `data/*Dao.kt`, `DonneesInitiales.kt`, `securite/MotDePasse.kt`, `ui/connexion/*`, `ui/accueil/*`, `ui/communs/*`, `Navigation.kt` (écrits par l'IA).
- Remarque principale de l'IA : garantir « jamais écrasé » par la structure plutôt que par la discipline : `ResultatDao` et `HistoriqueDao` n'ont ni `@Update` ni `@Delete`, comme le duo `_uiState`/`uiState` du cours garantit le flux unidirectionnel « par les types ».

⚠️ L'IA a introduit `popUpTo` (vider la pile à la connexion/déconnexion) et `PasswordVisualTransformation`, non vus en cours : ajoutés dans `docs/HORS_COURS.md` n° 11 et 12 avec une explication.

⚠️ Pour prévenir le ViewModel du succès de connexion sans `LaunchedEffect` (hors cours), l'IA a choisi un callback `seConnecter(onSucces)` : l'écran signale, la navigation décide (règle S5). À vérifier : est-ce lisible pour le binôme ?

✅ Les menus par rôle sont une fonction pure `menuPour(role)` testée par JUnit, indépendante d'Android.

⚠️ Design (demande du dev 1) : l'IA a choisi une photo CC0 de Wikimedia Commons (RN44, route non asphaltée) plutôt qu'une image quelconque trouvée sur le web, et un logo dessiné pour le projet plutôt que le logo officiel de l'ATT (droits non vérifiés). Le dev 1 peut remplacer la photo par une route asphaltée s'il préfère une image plus « officielle » ; il suffit de changer le fichier `photo_route_rn44.jpg` et le crédit dans `LISEZMOI.md`.

⚠️ L'IA a d'abord oublié que `material-icons-core` n'est plus fourni avec material3 (erreur `Unresolved reference 'icons'`) : dépendance ajoutée explicitement en 1.7.8.

- Mon verdict (accepte / rejette / nuance) et pourquoi : <à compléter par le dev 1>.

---

## Entrée 4 — <date> — <tâche>

- Fonction/Code soumis :
- Remarque principale de l'IA :
- Mon verdict (accepte / rejette / nuance) et pourquoi :
