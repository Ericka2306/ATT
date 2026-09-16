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

## Entrée 4 — 15/09/2026 — Étapes B2 et C2, traçabilité et auto-écoles

- Code soumis : `data/Tracage.kt`, `metier/ValidationAutoEcole.kt`, `ui/autoecoles/*`, `ui/communs/Selecteurs.kt`, `Navigation.kt` (écrits par l'IA).
- Remarque principale de l'IA : écrire la modification et sa ligne d'historique dans un même `db.withTransaction { }` pour qu'elles soient enregistrées ensemble ou pas du tout ; et ne jamais supprimer une auto-école, seulement la désactiver.

⚠️ Un seul ViewModel partagé par les quatre écrans du sous-parcours, obtenu avec `viewModel(navController.getBackStackEntry("autoecoles"))` : c'est le « ViewModel au-dessus de la navigation » du cours S6, mais limité à un sous-parcours au lieu de toute l'application. À vérifier : est-ce que le binôme sait l'expliquer ?

⚠️ Les règles de validation (`validerFiche`, `validerCompte`) sont des fonctions pures qui reçoivent les listes de noms/identifiants existants au lieu d'interroger la base : c'est ce qui les rend testables sans Android (11 tests). Le ViewModel fait la requête, la fonction fait le jugement.

⚠️ `flatMapLatest` (fiche qui suit un identifiant qui change) et `DropdownMenu` sont hors cours : ajoutés dans `docs/HORS_COURS.md` n° 17 et 18, à valider.

- Mon verdict (accepte / rejette / nuance) et pourquoi : <à compléter par le dev 1>.

---

## Entrée 5 — 15/09/2026 — Étape C3, candidats et dossiers, factorisation

- Code soumis : `ui/communs/*` (factorisation), `ui/candidats/*`, `metier/ReglesDossier.kt`, `metier/ValidationCandidat.kt`.
- Remarque principale de l'IA : le dev 1 trouvait « beaucoup de code pour peu de fonctionnalités » ; la mesure a montré que 45 % des lignes sont le schéma (entités/DAO), les commentaires et les imports. La factorisation a porté sur les écrans : un champ = une ligne (`ChampTexte`), un cadre unique (`EcranStandard`), un utilitaire générique pour le ViewModel de sous-parcours, une fonction `modifierFormulaire` au lieu de six `changerX`.

⚠️ `modifierFormulaire { it.copy(nom = v) }` expose la forme de l'état à l'écran ; le cours préfère un événement nommé par geste (`ajouterAuPanier`). Compromis assumé pour la taille du code, noté HORS_COURS n° 22 : à trancher par le binôme.

⚠️ La règle « un seul dossier en cours par catégorie » et « âge minimum » sont dans `ReglesDossier`, fonctions pures qui reçoivent la catégorie et la date : l'âge minimum vient de la base (`aConfirmer = true`), jamais du code (règle R3).

⚠️ Le pilotage de l'émulateur par coordonnées a dérivé plusieurs fois ; l'IA est passée à un pilotage par le texte des éléments (`uiautomator dump`), plus fiable. Sans lien avec le code livré, mais utile pour la démo de soutenance.

- Mon verdict (accepte / rejette / nuance) et pourquoi : <à compléter par le dev 1>.

---

## Entrée 6 — 16/09/2026 — Étape C1b, comptes

- Code soumis : `ui/comptes/*`, `metier/ValidationCompte.kt`, `outils/pilote_emulateur.sh`.
- Remarque principale de l'IA : créer l'examinateur et son compte dans une seule transaction (pas d'examinateur sans compte, pas de compte orphelin), et faire suivre la désactivation de la fiche au compte.

⚠️ L'IA a ajouté l'étape C1b au plan de sa propre initiative (justifiée : C8/C9 ont besoin d'examinateurs connectés, et les mots de passe initiaux devaient pouvoir être changés). Le dev 1 a interrompu puis validé la reprise : à consigner comme décision d'équipe.

⚠️ Le compte courant ne peut pas se désactiver lui-même ; un Super Admin peut en revanche désactiver l'autre Super Admin s'il en existe un : à discuter (garder au moins un Super Admin actif ?).

✅ Le script de pilotage par texte (`uiautomator dump`) rend les vérifications reproductibles : il pourra servir de « démo scriptée » à la soutenance (étape D3).

- Mon verdict (accepte / rejette / nuance) et pourquoi : <à compléter par le dev 1>.

---

## Entrée 7 — <date> — <tâche>

- Fonction/Code soumis :
- Remarque principale de l'IA :
- Mon verdict (accepte / rejette / nuance) et pourquoi :
