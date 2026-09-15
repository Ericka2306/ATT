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
