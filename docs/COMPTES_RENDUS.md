# Comptes rendus d'étape — Projet ATT

Format (instructions §10) : ce qui a été créé, les fichiers modifiés, les tests, les décisions restantes.

---

## Étape A1 — Analyse des exigences et règles — 15/09/2026

**Créé**
- `CLAUDE.md` — règles condensées pour Claude Code.
- `docs/01_REGLES_DE_DEVELOPPEMENT.md` — règles impératives, pile, architecture, conventions, permissions, protocole IA.
- `docs/02_PLAN_DE_TRAVAIL.md` — phases A à D, étapes, critères de fin.
- `docs/03_MODELE_DE_DONNEES.md` — proposition d'entités, statuts, invariants (à valider).
- `docs/04_QUESTIONS_A_VALIDER.md` — règles administratives inconnues et résultats de recherche.
- `docs/HORS_COURS.md` — registre des notions non vues en cours.
- `docs/references/` — cadrage et instructions en texte, synthèse des cours, synthèse des projets d'exemple.
- `JOURNAL-IA.md` — journal IA au format du Mini-TP 1.

**Modifié** : aucun fichier de code (le squelette Android Studio est inchangé).

**Tests** : sans objet (aucun code).

**Décisions prises le 15/09/2026 (dev 1 + Claude, dev 2 informé)**
1. Modèle de données `docs/03` validé tel que proposé.
2. Valeurs par défaut « à confirmer » de `docs/04` §A acceptées ; les questions Q1 à Q13 restent à poser à l'enseignant.
3. Choix techniques T1 à T6 acceptés : AGP 9.3.2 avec Kotlin embarqué, versions 2026 (Room 2.8.5, Navigation 2.10.1, Lifecycle 2.11.0, BOM 2026.08.00), minSdk 26 + PBKDF2, impression HTML → PrintManager, JUnit 4 sur `metier/`, session en mémoire.
4. Notions hors cours n° 1 à 10 acceptées, à expliquer dans le code au moment de l'usage.
5. Répartition : dev 1 sur le socle et C1–C3, dev 2 sur C4–C5 après B1.

**Dépôt** : https://github.com/Ericka2306/ATT (public, branche `main`), premier commit le 15/09/2026. Les documents de suivi (comptes rendus, questions, journal IA) sont versionnés volontairement : ils font partie du livrable.

**Décisions restantes**
1. Obtenir les réponses de l'enseignant aux questions Q1 à Q13.

---

## Étape B0 — Socle Gradle et projet — 15/09/2026

**Créé**
- `app/src/main/java/mg/itu/att/MainActivity.kt` — une seule Activity, `MaterialTheme` + `Surface` + `AppNavigation()` (squelette du cours).
- `app/src/main/java/mg/itu/att/Navigation.kt` — `NavHost` avec une route provisoire `accueil` et l'écran `EcranAccueilProvisoire` (supprimé à l'étape C1).
- `docs/captures/B0_ecran_provisoire.png` — capture de l'application lancée sur l'émulateur.

**Modifié**
- `gradle/libs.versions.toml` — plugins Compose 2.2.10 et KSP 2.3.12 ; Compose BOM 2026.08.00, activity-compose 1.13.0, navigation-compose 2.10.1, lifecycle-viewmodel-compose 2.11.0, Room 2.8.5 ; suppression d'appcompat et material (MDC Views).
- `build.gradle.kts` — déclaration des plugins Compose et KSP (`apply false`), pas de plugin `kotlin.android` (Kotlin intégré à AGP 9).
- `app/build.gradle.kts` — `namespace`/`applicationId` = `mg.itu.att`, `minSdk 26`, Java 17 + `kotlin { compilerOptions { jvmTarget } }`, `buildFeatures.compose`, dépendances du cours.
- `app/src/main/AndroidManifest.xml` — thème `@android:style/Theme.Material.Light.NoActionBar`, déclaration de `.MainActivity` (MAIN/LAUNCHER).
- Tests d'exemple déplacés de `com/example/att` vers `mg/itu/att`.
- Supprimés : `res/values/themes.xml`, `res/values-night/themes.xml`, `res/values/colors.xml` (thème MaterialComponents inutile avec Compose).

**Tests**
- `./gradlew :app:assembleDebug` : vert (premier essai échoué avec KSP 2.2.10-2.0.2 : « Using kotlin.sourceSets DSL … is not allowed with built-in Kotlin » ; corrigé en KSP 2.3.12).
- `./gradlew :app:testDebugUnitTest` : vert (test d'exemple).
- Installation et lancement sur l'émulateur `Small_Phone` : `MainActivity` au premier plan, écran provisoire affiché (capture).

**Décisions restantes**
1. Le contenu passe sous la barre d'état (comportement identique aux projets du cours) : à régler avec `Scaffold` à l'étape C1 (notion hors cours n° 7, déjà acceptée).
2. Étape suivante : A4 (cas d'utilisation et liste des écrans), puis B1 (base de données).

---

## Étape A4 — Cas d'utilisation, flux et écrans — 15/09/2026

**Créé**
- `docs/05_CAS_UTILISATION.md` — parcours global, 14 cas d'utilisation (acteur, préconditions, flux nominal, erreurs, étape du plan), tableau des contraintes du cadrage §11, 27 routes et ~22 écrans, menu par rôle, 12 ViewModels prévus.

**Modifié**
- `docs/README.md`, `CLAUDE.md` — référence au nouveau document.
- `docs/02_PLAN_DE_TRAVAIL.md` — A4 🟢, tableau « Où en est-on ? ».

**Tests** : sans objet (document).

**Décisions restantes**
1. Relire les 14 UC, en particulier UC07 (qui inscrit ?), UC08 (qui fait l'appel ?) et UC09 (saisie question par question ou points directs ?) : ce sont les trois points où les réponses de l'enseignant (Q4, Q6, Q8, Q1) changeraient les écrans.
2. Étape suivante : B1 (base de données : entités, DAO, `AppDatabase`, données initiales).

---

## Étape B1 — Base de données — 15/09/2026

**Créé** (`app/src/main/java/mg/itu/att/`)
- `data/EntitesReferentiels.kt` — `Region`, `CategoriePermis`, `TypeEpreuve`, `Bareme`, `RegleConfig` (+ `ClesRegles`, `TypeValeur`), `Question`, `Reponse`, `CriterePratique`, `Centre`.
- `data/EntitesActeurs.kt` — `Role`, `Utilisateur`, `AutoEcole`, `Candidat`, `Examinateur`.
- `data/EntitesDossiers.kt` — `StatutDossier`, `Dossier`, `PieceDossier`.
- `data/EntitesPlanification.kt` — `Session`, `Creneau`, `Inscription` (index unique candidat × session, `numeroAnonymat`), `Presence`, et leurs statuts.
- `data/EntitesExamens.kt` — `Tentative` (index unique candidat × épreuve × numéro), `Evaluation`, `ReponseCandidat`, `EvaluationCritere`, `Resultat` (+ `remplaceResultatId`), et leurs statuts.
- `data/EntitesHistorique.kt` — `Historique`.
- `data/ReferentielsDao.kt`, `ActeursDao.kt`, `DossiersDao.kt`, `PlanificationDao.kt`, `ExamensDao.kt`, `HistoriqueDao.kt` — 25 DAO : `Flow` pour les listes et compteurs, `suspend` pour les requêtes ponctuelles et les écritures. `ResultatDao` et `HistoriqueDao` n'ont ni `@Update` ni `@Delete` (règle R6).
- `data/AppDatabase.kt` — 25 entités, version 1, singleton `obtenir(context)`, fichier `att.db`.
- `data/DonneesInitiales.kt` — 24 régions, 6 catégories (A', A, B, C, D, E) avec 2 épreuves et 2 barèmes d'exemple chacune, 13 règles configurables, comptes `superadmin` et `admin` ; tout ce qui n'est pas une région ou un compte est marqué `aConfirmer = true`.
- `securite/MotDePasse.kt` — hachage PBKDF2WithHmacSHA256, sel aléatoire, 10 000 itérations, comparaison en temps constant.
- `ui/socle/SocleViewModel.kt` — ViewModel provisoire (init + `combine` + `stateIn`, pattern listedetailv3) qui insère les données au premier lancement et affiche des compteurs.
- `app/src/test/java/mg/itu/att/securite/MotDePasseTest.kt` — 5 tests JUnit.
- `docs/captures/B1_base_initialisee.png`.

**Modifié**
- `Navigation.kt` — l'écran provisoire affiche les compteurs de la base.
- `docs/03_MODELE_DE_DONNEES.md` — champ `aConfirmer` étendu aux catégories, épreuves, barèmes, questions et critères ; `categoriePrealableCode` sur la catégorie ; `pointsSaisisDirectement` sur l'évaluation ; règle `CAPACITE_CRENEAU_DEFAUT`.

**Tests**
- `./gradlew :app:assembleDebug` : vert. `./gradlew :app:testDebugUnitTest` : 6 tests verts (5 `MotDePasseTest` + 1 exemple).
- Émulateur : application réinstallée à neuf, écran « Base de données prête » avec 24 régions, 6 catégories, 13 règles, 2 utilisateurs (capture).
- Lecture directe de `att.db` : 25 tables ; regions 24, categories_permis 6, types_epreuve 12, baremes 12, regles_config 13, utilisateurs 2 ; empreintes au format `pbkdf2$10000$…`, aucun mot de passe en clair.

**Décisions restantes**
1. Les mots de passe initiaux (`superadmin` / `ChangezMoi2026`, `admin` / `admin2026`) sont dans `DonneesInitiales.kt` : à changer dès la première connexion (écran C1) ; ne pas les réutiliser en production.
2. Aucun centre, aucune question, aucun critère de conduite n'est préchargé : ils se créent dans les écrans de configuration (C4, C5, dev 2).
3. Étape suivante : B2 (fonction de traçage `Historique` + `@Transaction`), puis C1 (connexion).

---

## Étape C1 — Base du front-end : session, connexion, accueil par rôle — 15/09/2026

**Créé** (`app/src/main/java/mg/itu/att/`)
- `ui/communs/Cadre.kt` — `EcranAvecBarre` (Scaffold + TopAppBar : titre, bouton Retour, action à droite), `ContenuColonne`, `TexteErreur`, `EcranAVenir` (écran générique tant qu'une fonctionnalité n'est pas développée).
- `ui/communs/Formats.kt` — `dateDuJour`, `heureCourante`, `maintenantIso`, `formatDate`, `Role.libelle()` (formatage à la main, comme le cours).
- `ui/connexion/ConnexionViewModel.kt` — `SessionUtilisateur` (id, nom, rôle, région / auto-école / examinateur / candidat liés), `EtatConnexion`, `seConnecter(onSucces)` (vérification PBKDF2, même message pour identifiant inconnu et mot de passe faux, compte désactivé refusé), `seDeconnecter()`. Remplit la base au premier lancement (ancien `SocleViewModel` supprimé).
- `ui/connexion/EcranConnexion.kt` — deux `OutlinedTextField` (mot de passe masqué), bouton, message d'erreur ; l'état de saisie vit dans le ViewModel.
- `ui/accueil/MenuParRole.kt` — `Routes` (routes de docs/05 §4) et `menuPour(role)` : fonction pure, une liste d'entrées par rôle conforme à la matrice des permissions.
- `ui/accueil/EcranAccueil.kt` — nom, rôle, cartes du menu, bouton Déconnexion.
- `app/src/test/java/mg/itu/att/ui/accueil/MenuParRoleTest.kt` — 5 tests JUnit (menu non vide, configuration réservée au Super Admin, auto-école et candidat sans dossiers/historique/auto-écoles, candidat = parcours seul, routes uniques).
- **Identité visuelle (demande du dev 1)** : `ui/theme/Theme.kt` (`ThemeATT` : bleu institutionnel, vert Madagascar, ambre de signalisation, formes arrondies), `res/drawable/ic_logo_att.xml` (logo vectoriel maison : route vers l'horizon), icône adaptative de l'application (`ic_launcher_foreground/background.xml`), `res/drawable-nodpi/photo_route_rn44.jpg` (RN44, Wikimedia Commons, CC0, 1080 px, 264 Ko). Écran de connexion avec bandeau photo voilé de bleu, logo et carte de saisie ; accueil avec bandeau bleu (logo, nom, rôle, date) et cartes à pastille d'icône ; barre de titre bleue avec flèche retour et icône de déconnexion ; écran « En construction » illustré.
- `docs/captures/C1_connexion.png`, `C1_connexion_erreur.png`, `C1_accueil_admin.png`, `C1_a_venir.png`, `C1_apres_deconnexion.png`.

**Modifié**
- `Navigation.kt` — `ConnexionViewModel` partagé au-dessus du `NavHost` ; routes `connexion`, `accueil`, une route « à venir » par entrée de menu (titre = libellé du menu), `avenir/{libelle}` ; connexion et déconnexion vident la pile (`popUpTo`).
- `MainActivity.kt` — `ThemeATT` à la place de `MaterialTheme`.
- `gradle/libs.versions.toml`, `app/build.gradle.kts` — dépendance `material-icons-core` 1.7.8 (le jeu d'icônes n'est plus fourni avec material3).
- `LISEZMOI.md` — crédits de la photo et du logo.
- `docs/HORS_COURS.md` — n° 11 `popUpTo`, n° 12 `PasswordVisualTransformation`, n° 13 palette `MaterialTheme`, n° 14 `Icon`/`IconButton`, n° 15 `Image`/`painterResource`/dégradé, n° 16 `verticalScroll`/`statusBarsPadding`.
- `docs/02_PLAN_DE_TRAVAIL.md` — C1 🟢, B2 reportée avec C2, tableau « Où en est-on ? ».

**Tests**
- `assembleDebug` vert ; `testDebugUnitTest` : 11 tests verts (5 mot de passe, 5 menu, 1 exemple).
- Émulateur, piloté par `adb` : mauvais mot de passe → « Identifiant ou mot de passe incorrect. » ; `admin` / `admin2026` → accueil « Administrateur ATT » avec ses 6 entrées ; carte « Sessions » → écran « à venir » → Retour ; Déconnexion → écran de connexion ; bouton retour du téléphone → sortie de l'application, pas de retour à l'accueil.
- La barre de titre gère la barre d'état : le problème noté en B0 est réglé.

**Décisions restantes**
1. Aucun écran de changement de mot de passe : à ajouter quand les comptes seront créés depuis l'application (C2), avec les mots de passe initiaux à changer.
2. Étape suivante : C2 (auto-écoles et leurs comptes) avec le traçage `Historique` de B2 ; le dev 2 peut démarrer C4 dès la fusion de B1.
