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

---

## Étapes B2 + C2 — Traçabilité et auto-écoles — 15/09/2026

**Créé** (`app/src/main/java/mg/itu/att/`)
- `data/Tracage.kt` — `ActionsHistorique`, `EntitesHistorique`, `AppDatabase.tracer(...)` (une ligne d'historique, appelée dans `db.withTransaction { }` avec la modification), `AutoEcole.resume()`.
- `metier/Dates.kt` — `dateDuJour`, `heureCourante`, `maintenantIso`, `formatDate` (déplacées depuis `ui/communs/Formats.kt` : fonctions pures, sans Android).
- `metier/ValidationAutoEcole.kt` — `validerFiche` (nom, région, adresse, doublon dans la région) et `validerCompte` (identifiant ≥ 4 caractères sans espace, unique ; mot de passe ≥ 8).
- `ui/communs/Selecteurs.kt` — `SelecteurRegion` (`DropdownMenu`, verrouillable pour un administrateur régional).
- `ui/autoecoles/AutoEcolesViewModel.kt` — un ViewModel partagé par les quatre écrans (liste filtrée par région avec jointure région, formulaire création/modification, fiche avec comptes et historique via `flatMapLatest`, création de compte). Toute écriture = `withTransaction` + `tracer`. Aucune suppression : désactivation/réactivation.
- `ui/autoecoles/EcranListeAutoEcoles.kt`, `EcranFormulaireAutoEcole.kt`, `EcranDetailAutoEcole.kt`, `EcranFormulaireCompte.kt`.
- Tests : `metier/ValidationAutoEcoleTest.kt` (7), `metier/DatesTest.kt` (4).
- Captures `docs/captures/C2_*.png` (liste vide, formulaire, erreur de validation, formulaire rempli, fiche, fiche avec compte, liste, accueil auto-école).

**Modifié**
- `data/ActeursDao.kt` — `UtilisateurDao.parAutoEcole`, `tousLesIdentifiants` ; `AutoEcoleDao.parIdEnDirect`, `nomsDansRegion`. `data/ReferentielsDao.kt` — `RegionDao.listeActives`.
- `Navigation.kt` — sous-graphe `graphAutoEcoles` (routes `autoecoles`, `autoecole/nouvelle`, `autoecole/{id}`, `autoecole/{id}/modifier`, `autoecole/{id}/compte`), ViewModel partagé obtenu sur l'entrée de la liste ; l'entrée « Auto-écoles » quitte la liste des écrans « à venir ».
- `ui/communs/Formats.kt` — ne garde que `Role.libelle()` et `String?.ouTiret()`.
- `docs/HORS_COURS.md` — n° 4 précisé (`withTransaction`), n° 17 `DropdownMenu`, n° 18 `flatMapLatest`.

**Tests**
- `assembleDebug` vert ; `testDebugUnitTest` : 22 tests verts.
- Émulateur (adb) : connexion `admin` → Auto-écoles (liste vide) → « + » → création sans région refusée (« Choisissez la région. ») → région Analamanga choisie dans la liste déroulante → création → fiche avec ligne d'historique CREATION → « Créer un compte » `soarano` / `Soarano2026` → fiche avec compte et ligne CREATION_COMPTE → liste à 1 → déconnexion → connexion `soarano` → accueil « Auto-école » avec ses 3 entrées.
- Lecture directe de `att.db` : 3 lignes d'historique (CREATION, CREATION_COMPTE ×2) horodatées et signées par l'utilisateur 2 (`admin`) ; compte `soarano` de rôle AUTO_ECOLE lié à l'auto-école 1 et à la région 1.

**Ajout (demande du dev 1)** : bloc « Comptes de test » sur l'écran de connexion, visible uniquement si `BuildConfig.DEBUG` (`buildFeatures.buildConfig = true`), avec deux boutons qui préremplissent les champs (`ConnexionViewModel.preremplir`). Capture `docs/captures/C2_connexion_comptes_test.png`.

**Décisions restantes**
1. Les administrateurs régionaux (`Utilisateur.regionId`) sont gérés (filtre verrouillé) mais aucun écran ne permet encore d'en créer : Super Admin, étape C4 (dev 2) ou plus tard.
2. Le doublon de nom est contrôlé par région uniquement (Q11) ; à ajuster si l'ATT a un agrément national unique.
3. Étape suivante : C3 (candidats et dossiers), qui réutilise `tracer`, `SelecteurRegion` et le pattern du ViewModel partagé.

---

## Étape C3 — Candidats et dossiers + factorisation — 15/09/2026

**Factorisation (demande du dev 1)** — `ui/communs/` : `Champs.kt` (`ChampTexte` = un champ en une ligne, `BoutonPrincipal`, `CaseACocher` ligne cliquable), `Cadre.kt` (`EcranStandard` remplace le couple `EcranAvecBarre` + `ContenuColonne`, `LigneInfo`, `CarteFiche`, `TitreSection`, `PastilleStatut`, `CarteIcone` avec complément à droite), `Historique.kt` (`LigneHistorique`), `Selecteurs.kt` (`SelecteurChoix` générique, `SelecteurRegion` n'en est qu'un cas), `NavigationCommune.kt` (`viewModelDuSousParcours<VM>()` générique + interface `ViewModelAvecSession`, `idArgument`). Dans les ViewModels, une fonction `modifierFormulaire { it.copy(...) }` remplace les six `changerX`. Les quatre écrans auto-écoles ont été réécrits avec ces briques : le formulaire passe de 95 à 42 lignes, la fiche de 130 à 75.

**Créé** (`app/src/main/java/mg/itu/att/`)
- `metier/ValidationCandidat.kt` (nom, prénom, date ISO plausible, auto-école) et `metier/ReglesDossier.kt` (`ageA`, `verifierEligibilite` selon l'âge minimum configuré, `peutOuvrirDossier` = un seul dossier en cours ou validé par catégorie, `dossierComplet`, `piecesDepuisRegle`) — 8 tests JUnit.
- `ui/candidats/CandidatsViewModel.kt` — liste filtrée par rôle (auto-école : les siens ; admin régional : sa région ; ATT : tous) + recherche + filtre auto-école ; formulaire ; fiche (dossiers par catégorie, ouverture d'un dossier avec ses pièces attendues) ; dossier (pièces à cocher, soumission avec contrôle d'éligibilité, décision ATT valider / incomplet / refuser avec motif obligatoire) ; liste « à traiter ». Toute écriture = `withTransaction` + `tracer`.
- `ui/candidats/Statuts.kt`, `EcranListeCandidats.kt`, `EcranFormulaireCandidat.kt`, `EcranDetailCandidat.kt`, `EcranDossier.kt`, `EcranDossiersATraiter.kt`.
- Captures `docs/captures/C3_*.png` (liste vide, formulaire, fiche, dossier brouillon, dossier soumis, fiche après soumission, liste, dossiers à traiter, décision, dossier validé).

**Modifié**
- `data/DonneesInitiales.kt`, `data/EntitesReferentiels.kt` — règle `PIECES_DOSSIER` (liste Torolalana pour A/A'/B, surcharge C/D/E avec certificat médical et copie du permis B), `SEPARATEUR_PIECES`.
- `data/Tracage.kt` — `Candidat.resume()`, `Dossier.resume()`. DAO : `CandidatDao.parRegion`, `AutoEcoleDao.listeActives`, `DossierDao.tous` / `listePourCandidat`, `CategoriePermisDao.listeActives`.
- `Navigation.kt` — `graphCandidats` (routes `candidats`, `candidat/nouveau`, `candidat/{id}`, `candidat/{id}/modifier`, `dossier/{id}`, `dossiers`) ; `graphAutoEcoles` réécrit avec l'utilitaire générique ; « Candidats » et « Dossiers à traiter » quittent les écrans « à venir ».
- `docs/HORS_COURS.md` — n° 20 `Checkbox`, n° 21 fonction générique `inline reified`, n° 22 `modifierFormulaire`.

**Tests**
- `assembleDebug` vert ; `testDebugUnitTest` : 30 tests verts. Aucun interdit des règles.
- Émulateur, piloté par le texte des éléments (script `uiautomator`) depuis des données vides : `admin` crée l'auto-école Soarano et le compte `soarano` → `soarano` crée le candidat RAKOTO Hery (né en 2004), ouvre un dossier B (4 pièces attendues chargées depuis la règle), coche les 4 pièces (« complet »), soumet → `admin` voit le dossier dans « Dossiers à traiter », l'ouvre, le valide. Base : dossier `VALIDE` daté et signé, 7 lignes d'historique (création/compte par `admin` ; candidat, dossier, soumission par `soarano` ; validation par `admin`).
- Non exercés sur l'émulateur (logique couverte par les tests) : décision « incomplet » puis resoumission, refus, refus de soumission pour âge insuffisant, doublon de candidat.

**Décisions restantes**
1. L'homonymie (même nom, prénom, date de naissance) bloque la création ; le cadrage parle d'« avertissement » : à assouplir si l'ATT le souhaite.
2. La modification d'un dossier après validation est impossible ; une correction passe par un nouveau dossier (à confirmer).
3. Étape suivante : C6 (inscriptions) quand C5 (dev 2) sera fusionnée ; en attendant, C12 (parcours candidat, consultation) ou C13 (impression de la fiche candidat).

---

## Étape C1b — Comptes : examinateurs, administrateurs ATT, mot de passe — 16/09/2026

Étape ajoutée au plan (sous « Authentification et rôles ») : sans elle, aucun examinateur ne peut se connecter pour C8/C9, et les mots de passe initiaux ne peuvent pas être changés.

**Créé** (`app/src/main/java/mg/itu/att/`)
- `metier/ValidationCompte.kt` — règles communes à tous les comptes (`validerCreation`, `validerMotDePasse`, `validerChangement`) ; `ValidationAutoEcole.validerCompte` y délègue. 3 tests.
- `ui/comptes/ComptesViewModel.kt` — examinateurs (liste filtrée par région, création **avec son compte dans une seule transaction**, modification, désactivation qui suit sur le compte), comptes (liste de tous les comptes, création d'un Administrateur ATT national ou régional par le Super Admin, désactivation sauf de son propre compte), changement de son mot de passe (ancien vérifié, nouveau haché). Tout tracé.
- `ui/comptes/EcranListeExaminateurs.kt`, `EcranFormulaireExaminateur.kt`, `EcranComptes.kt` (+ `EcranFormulaireAdmin`), `EcranMotDePasse.kt`.
- `outils/pilote_emulateur.sh` — pilotage de l'émulateur par le texte des éléments (`tap`, `saisir`, `visible`, `capture`, `relancer`, `connexion`) : sert aux vérifications de fin d'étape et pourra rejouer la démo de soutenance.
- Captures `docs/captures/C1b_*.png`.

**Modifié**
- `ui/accueil/MenuParRole.kt` — routes `examinateurs`, `comptes`, `mot-de-passe` ; « Examinateurs » pour l'ATT, « Comptes » pour le Super Admin, « Mon mot de passe » pour tous. Test mis à jour (5 tests).
- `Navigation.kt` — `graphComptes`. `data/Tracage.kt` — entité `Examinateur`, résumés `Examinateur`/`Utilisateur`. `data/ActeursDao.kt` — `UtilisateurDao.parExaminateur`.

**Tests**
- `assembleDebug` vert ; `testDebugUnitTest` : 33 tests verts.
- Émulateur (pilote par texte) : `admin` crée l'examinateur RABE Jean + compte `rabe` → change son mot de passe → se déconnecte → se reconnecte avec le nouveau → remet l'initial → `rabe` se connecte et voit « Sessions du jour » → `superadmin` ouvre « Comptes » (5 comptes) et crée `admin.toamasina` régional (Atsinanana). Base : compte `rabe` lié à l'examinateur 1 ; historique CREATION, CREATION_COMPTE ×2, CHANGEMENT_MOT_DE_PASSE ×2.

**Décisions restantes**
1. Les comptes de test restent affichés sur l'écran de connexion tant que `BuildConfig.DEBUG` ; à la soutenance, changer les mots de passe initiaux depuis « Mon mot de passe ».
2. Aucune branche du dev 2 (C4, C5) au 16/09 : C6 à C11 sont bloquées. Si le retard persiste, le dev 1 reprend C4/C5.

---

## Étape C4 — Configuration des référentiels et des règles — 16/09/2026

Réassignée au dev 1 le 16/09 (le dev 2 n'avait pas commencé) ; le dev 2 reprendra C12, C13 et D1 à son arrivée.

**Créé** (`app/src/main/java/mg/itu/att/`)
- `metier/ValidationConfiguration.kt` — validation de forme : catégorie (code unique, âge 10–99), épreuve (code unique par catégorie, ordre ≥ 1), barème (0 ≤ seuil ≤ note max), règle (valeur conforme au type ENTIER / DECIMAL / BOOLEEN / TEXTE), question (énoncé, points > 0, ≥ 2 réponses, une seule bonne), critère, centre. 5 tests.
- `ui/configuration/ConfigurationViewModel.kt` — un ViewModel partagé pour tout le sous-parcours : catégories (création/modification), épreuves d'une catégorie, épreuve (barèmes, questions, critères), règles, centres. **Un barème utilisé n'est jamais modifié** : « Nouvelle version » ferme la version courante à la date du jour et crée la suivante. Questions et critères se désactivent, ne se suppriment pas. Tout tracé.
- `ui/configuration/EcranConfiguration.kt` (menu + badge `PastilleAConfirmer`), `EcranCategories.kt` (+ formulaire), `EcranDetailCategorie.kt` (+ formulaire d'épreuve), `EcranEpreuve.kt` (+ formulaires barème, question à 4 réponses, critère), `EcranRegles.kt` (+ formulaire), `EcranCentres.kt` (+ formulaire).
- Captures `docs/captures/C4_*.png`.

**Modifié**
- `data/Tracage.kt` — entités et résumés pour catégorie, épreuve, barème, règle, question, critère, centre. `data/ReferentielsDao.kt` — `CategoriePermisDao.parIdEnDirect`, `TypeEpreuveDao.parIdEnDirect` / `toutes`, `ReponseDao.toutes` / `insererToutes`.
- `Navigation.kt` — `graphConfiguration` (16 routes sous `config/…`) ; « Configuration » quitte les écrans « à venir ».
- `outils/pilote_emulateur.sh` — `remplacer` (vide un champ prérempli avant saisie), `haut`, défilement vers le haut si l'élément n'est pas trouvé vers le bas.
- `docs/02_PLAN_DE_TRAVAIL.md` — répartition réassignée, C4 🟢.

**Tests**
- `assembleDebug` vert ; `testDebugUnitTest` : 38 tests verts.
- Émulateur (`superadmin`) : Configuration → Catégories → B → Épreuve théorique → nouvelle version du barème (30 / 21 : v1 fermée, v2 courante) → question « Que signifie un feu orange fixe ? » avec 3 réponses, bonne = B → Épreuve de conduite → critère « Démarrage en côte » (2 pt, éliminatoire) → Règles → `TENTATIVES_MAX` = 3 → `DELAI_REPASSAGE_JOURS` = « abc » refusé (« doit être un entier ») → Centres → « Centre ATT Soarano » (Analamanga, capacité 40). Base : toutes les lignes présentes, historique CREATION/MODIFICATION pour chacune.
- Note : une saisie contenant une apostrophe a fait dériver le premier passage du pilote (limite d'`adb shell input text`) ; un critère parasite « 12 pt » s'est retrouvé sur l'épreuve théorique de l'émulateur. Données de test uniquement, code non concerné ; le pilote documente désormais la limite.

**Décisions restantes**
1. La modification d'une question existante n'est pas prévue (désactivation puis nouvelle question), pour ne jamais altérer une question déjà utilisée dans une évaluation ; à confirmer.
2. Les réponses sont limitées à quatre par question (format QCM courant) ; Q1 dira si le format officiel diffère.
3. Étape suivante : C5 (sessions et créneaux) — dev 1.

---

## Étape C5 — Sessions et créneaux — 16/09/2026

**Créé** (`app/src/main/java/mg/itu/att/`)
- `metier/ReglesPlanification.kt` — `genererCreneaux` (découpe la capacité en créneaux successifs à partir de l'heure de convocation, dernier créneau avec le reste, capacité 1 = créneau individuel), `ajouterMinutes`, `heureValide`, `validerSession` (date non passée, formats, capacités ≥ 1), `avertissementConflit`. 6 tests.
- `ui/sessions/SessionsViewModel.kt` — liste avec libellés (centre, région, catégorie, épreuve, inscrits) filtrée par région pour un admin régional ; formulaire prérempli depuis les règles `CAPACITE_SESSION_DEFAUT`, `CAPACITE_CRENEAU_DEFAUT`, `DUREE_CRENEAU_MIN`, `MARGE_CRENEAU_MIN` (la capacité du centre s'applique si connue), épreuves rechargées selon la catégorie, aperçu des créneaux recalculé à chaque saisie, avertissement si une autre session est prévue au même centre le même jour ; création session + créneaux en une transaction ; détail (créneaux et remplissage, historique) ; changement de statut PLANIFIEE → OUVERTE → EN_COURS → TERMINEE et annulation avec motif obligatoire, qui annule aussi les inscriptions actives (§11).
- `ui/sessions/Statuts.kt`, `EcranListeSessions.kt` (`FilterChip` à venir / du jour / passées / toutes, comme le TP7), `EcranFormulaireSession.kt`, `EcranDetailSession.kt`.
- Captures `docs/captures/C5_*.png`.

**Modifié**
- `data/PlanificationDao.kt` — `InscriptionDao.toutes`, `listePourSession` ; `data/ReferentielsDao.kt` — `CentreDao.listeActifs`, `TypeEpreuveDao.listeActivesPourCategorie` ; `data/Tracage.kt` — `Session.resume()`.
- `Navigation.kt` — `graphSessions` (`sessions`, `session/nouvelle`, `session/{id}`, et `…/inscrire`, `…/appel` en « à venir » pour C6/C7) ; « Sessions » quitte les écrans « à venir ».

**Tests**
- `assembleDebug` vert ; `testDebugUnitTest` : 44 tests verts.
- Émulateur (`admin`) : formulaire prérempli (30 / 5 / 15 / 5 depuis les règles), catégorie B → épreuve théorique proposée, centre Soarano → capacité 40 du centre appliquée, saisie 12 candidats / 5 par créneau → aperçu « 3 créneaux de 08:00 à 08:55 » → création → fiche avec 3 créneaux (5, 5, 2 places) → « Ouvrir aux inscriptions » → statut Ouverte. Date passée refusée (« La date est déjà passée. »). Seconde session le même jour : avertissement « 1 autre session déjà prévue dans ce centre » ; annulation avec motif « Examinateur indisponible » → statut Annulée, ligne ANNULATION avec motif. Base : 2 sessions, 3 + 8 créneaux aux heures attendues.
- Non exercés : démarrage/terminaison (boutons présents, même code que l'ouverture), annulation avec inscriptions (C6).

**Décisions restantes**
1. Une session = une épreuve. Si l'ATT organise théorie et conduite le même jour, on crée deux sessions (Q9).
2. Le conflit de centre est un avertissement, pas un blocage : plusieurs sessions le même jour au même centre sont possibles (à confirmer, Q5).
3. Étape suivante : C6 (inscriptions), qui branchera `session/{id}/inscrire`.

---

## Étape C6 — Inscriptions — 16/09/2026

**Créé** (`app/src/main/java/mg/itu/att/`)
- `data/Regles.kt` — lecture typée des règles (`regleEntier`, `regleBooleen`, `regleTexte`), catégorie prioritaire sur global.
- `metier/ReglesInscription.kt` — `ContexteInscription` (tous les faits nécessaires) et `verifier` : session ouverte, dossier validé, pas de double inscription, pas d'autre session le même jour, capacité, région (`EXAMEN_DANS_REGION_AUTO_ECOLE`), tentatives max, délai de repassage, théorie réussie avant conduite (`CONDUITE_APRES_THEORIE_REUSSIE`) ; `choisirCreneau` (premier avec une place), `numeroAnonymat` (rang à trois chiffres jamais réutilisé), `joursEntre`. 7 tests.
- `ui/inscriptions/InscriptionsViewModel.kt` — rassemble les faits en base, applique `verifier`, enregistre inscription + présence `EN_ATTENTE` + historique en une transaction ; la session passe `COMPLETE` à la dernière place et redevient `OUVERTE` à une annulation ou un report ; l'ATT inscrit, confirme (demande d'auto-école si la règle `AUTO_ECOLE_PEUT_INSCRIRE` l'autorise), reporte et annule avec motif ; l'auto-école ne voit que ses candidats.
- `ui/inscriptions/EcranInscriptions.kt` — inscrits (n° d'appel, candidat, créneau, statut, actions), places restantes, recherche d'un candidat éligible, choix du créneau ou premier disponible.
- Captures `docs/captures/C6_*.png`.

**Modifié**
- `data/ExamensDao.kt` — `TentativeDao.listePourCandidatEtEpreuve`, `ResultatDao.dernierReussi` (jointure tentatives) ; `data/PlanificationDao.kt` — `InscriptionDao.listePourCandidat` ; `data/Tracage.kt` — `Inscription.resume()`.
- `ui/sessions/EcranDetailSession.kt` — bouton « Inscriptions » dès que la session n'est plus planifiée, visible par tous les rôles autorisés.
- `Navigation.kt` — route `session/{id}/inscrire` branchée.

**Tests**
- `assembleDebug` vert ; `testDebugUnitTest` : 51 tests verts.
- Émulateur (`admin`) : session du 05/10 (12 places) → Inscriptions → RAKOTO Hery proposé (seul candidat à dossier B validé) → inscrit n° 001, créneau 08:00, présence créée → compteur « 1 inscrit(s) / 12 » sur la fiche. Session à 1 place le 12/10 → inscription → « 0 place restante », fiche « Complète » → report avec motif → « Reporté », place libérée, session « Ouverte ». Base : 2 inscriptions, 2 présences, historiques CREATION / REPORT / MODIFICATION de session.
- Couverts par les tests seulement : double inscription, conflit de jour, région, délai, tentatives max, théorie avant conduite (aucune tentative ni résultat n'existe encore).

**Décisions restantes**
1. Le numéro d'appel anonyme est un rang (001, 002…) : simple et imprimable ; si l'ATT veut un numéro non prédictible, un tirage aléatoire remplacera `numeroAnonymat` sans toucher aux écrans.
2. « Reporter » libère la place et laisse l'inscription en `REPORTE` ; la réinscription à une autre session se fait ensuite depuis cette session-là (pas de report « automatique » vers une session choisie, Q4).
3. Étape suivante : C7 (présence et appel).

---

## Étape C7 — Présence et appel — 16/09/2026

**Créé** (`app/src/main/java/mg/itu/att/`)
- `metier/ReglesPresence.kt` — `minutesDeRetard`, `statutArrivee` (PRESENT dans la tolérance `TOLERANCE_RETARD_MIN`, EN_RETARD au-delà), `absentReporteAutomatiquement` (règle `REGLE_ABSENCE` : REPORT_AUTO ou NOUVELLE_INSCRIPTION), `transitionsPossibles`. 4 tests.
- `ui/appel/AppelViewModel.kt` — liste d'appel par créneau puis numéro (inscriptions actives, hors demandes), compteurs présents / retards / absents / en attente, règles lues en base pour la catégorie de la session ; « Présent » enregistre l'heure d'arrivée et décide PRESENT ou EN_RETARD ; retard accepté (remarque conservée) ou refusé (absent) ; absent → report automatique de l'inscription ou message « à réinscrire » selon la règle ; correction possible tant que la session n'est pas terminée. L'examinateur ne voit que les numéros d'appel. Tout tracé.
- `ui/appel/EcranAppel.kt` — cartes par candidat avec pastille de statut et actions selon l'état.
- Captures `docs/captures/C7_appel_attente.png`, `C7_retard_accepte.png`, `C7_absent.png` (trois états distincts, vérifiées une par une).

**Modifié**
- `data/EntitesPlanification.kt`, `data/AppDatabase.kt` — **schéma v2** : l'index unique (candidatId, sessionId) des inscriptions devient un index simple. Motif : un candidat dont l'inscription a été reportée ou annulée ne pouvait pas être réinscrit à la même session (plantage `SQLiteConstraintException` constaté sur l'émulateur) ; l'unicité d'une inscription *active* est garantie par `ReglesInscription.verifier`. `docs/03` mis à jour.
- `ui/sessions/EcranDetailSession.kt` — bouton « Appel » dès que la session est ouverte ; « Démarrer » / « Terminer » séparés.
- `Navigation.kt` — route `session/{id}/appel` branchée. `data/Tracage.kt` — `Presence.resume()`.

**Tests**
- `assembleDebug` vert ; `testDebugUnitTest` : 55 tests verts.
- Émulateur, base vide (v2) : toute la chaîne rejouée par le pilote (centre → auto-école + compte → session B 12/10 ouverte → candidat + dossier soumis → validation → inscription n° 001) puis appel : « Présent » à 19:26 pour une convocation à 08:00 → « En retard » (686 min) → accepté → Présent avec remarque → corrigé → Absent avec message « à réinscrire » (règle NOUVELLE_INSCRIPTION). Base : quatre lignes d'historique de présence dans l'ordre.
- Non exercé sur émulateur : `REGLE_ABSENCE = REPORT_AUTO` (couvert par le test ; le report réutilise le code de C6).

**Décisions restantes**
1. Qui fait l'appel (Q6) : l'ATT dans cette version ; l'examinateur voit la liste sans les noms et sans boutons.
2. Une présence marquée avant l'heure de convocation est « Présent » (avance) ; la tolérance ne s'applique qu'au retard.
3. Étape suivante : C8 (tentatives).

---

## Étape C8 — Tentatives — 16/09/2026

**Créé** (`app/src/main/java/mg/itu/att/`)
- `metier/ReglesTentatives.kt` — `numeroSuivant` (jamais réutilisé, même après annulation), `peutOuvrir` (candidat présent ou en cours, reprise d'une tentative en cours, refus si une tentative existe déjà pour cette inscription, `TENTATIVES_MAX`). 4 tests.
- `ui/evaluation/TentativesViewModel.kt` — sessions à évaluer (du jour et à venir, non planifiées ni annulées, région de l'examinateur), candidats d'une session avec présence, tentative existante, prochain numéro et motif de refus ; `ouvrirTentative` crée la n-ième tentative avec l'examinateur connecté (null si ouverte par l'ATT), passe la présence EN_COURS, trace, ou reprend la tentative en cours.
- `ui/evaluation/EcranSessionsExaminateur.kt` (menu « Sessions du jour » de l'examinateur), `EcranTentatives.kt` (numéros d'appel seuls pour l'examinateur ; « Ouvrir la tentative n° N » / « Continuer l'évaluation » / motif de refus).
- Captures `C8_passages.png` (ATT : passage n° 1 en cours, « Continuer l'évaluation »), `C8_examinateur.png` (numéros seuls).

**Modifié**
- `data/ExamensDao.kt` — `TentativeDao.toutes`, `parSession` (jointure inscriptions), `parIdEnDirect` ; `data/PlanificationDao.kt` — `PresenceDao.toutes` ; `data/Tracage.kt` — `Tentative.resume()`, entité `Tentative`.
- `Navigation.kt` — routes `evaluation`, `session/{id}/tentatives`, `tentative/{id}/theorie` et `/conduite` (« à venir » pour C9/C10) ; `ui/sessions/EcranDetailSession.kt` — bouton « Tentatives » pour l'ATT.

**Tests**
- `assembleDebug` vert ; `testDebugUnitTest` : 59 tests verts.
- Émulateur : `admin` remet RAKOTO présent → Tentatives → « Ouvrir la tentative n° 1 » → écran C9 « à venir » → retour : « tentative n° 1 en cours » et « Continuer l'évaluation » (reprise sans doublon) ; création de l'examinateur `rabe` → connexion → « Sessions du jour » → session B → liste avec numéros d'appel sans les noms. Base : tentative n° 1 EN_COURS (examinateur null car ouverte par l'ATT), présence EN_COURS, historique CREATION.
- Captures vérifiées une par une après le renommage « tentative » → « passage » à l'écran (décision du 17/09) : passage n° 1 en cours avec « Continuer l'évaluation », vue examinateur avec « n° 001 » sans nom.

**Décisions restantes**
1. Une tentative ouverte par l'ATT n'a pas d'examinateur ; il sera renseigné à la saisie (C9) par l'examinateur connecté (Q6).
2. La règle `TENTATIVES_MAX` s'applique ici aussi (déjà à l'inscription) : double sécurité.
3. Étape suivante : C9 (évaluation théorique, modes TIRAGE / DIRECT).

---

## Étape C9 — Évaluation théorique (orale) — 17/09/2026

> Première version faite en QCM (réponses A/B/C/D, case « sur papier »), d'après la presse et le cadrage. Corrigée le jour même sur le témoignage du dev 1 : l'épreuve est **orale**, l'examinateur a une feuille avec les questions posées, les réponses données et les points, puis un total. Tout ce qui suit décrit la version corrigée ; la table `reponses` (QCM) a été retirée, schéma v3.

**Créé** (`app/src/main/java/mg/itu/att/`)
- `metier/ReglesTheorie.kt` — `ModesTheorie` (TIRAGE / DIRECT), `tirerSujet` (mélange puis prend chaque question qui tient dans la note max du barème : jamais au-dessus, jamais deux fois la même, points variables 2/3/5/10…), `proposables` / `proposer` / `pointsDisponibles` (mode direct : questions non posées qui tiennent dans les points restants, filtre par points), `verifierOuverture` (barème courant, questions actives, mode connu → sinon renvoi à la configuration), `pointsValides` (0 à max de la question, virgule acceptée), `totalAttribue`, `sansNote`, `verifierFin` (au moins une question, toutes notées). Le hasard est injecté (`Random`) pour être testable. 8 tests.
- `ui/evaluation/EvaluationTheorieViewModel.kt` — à l'ouverture, crée l'`Evaluation` figée sur le barème courant et, en mode TIRAGE, les lignes `ReponseCandidat` du sujet ; en mode DIRECT, propose une question au hasard (graine locale, « Une autre », filtre par points) et « Poser cette question » ajoute une ligne. `saisir` (réponse donnée, points) garde la frappe en local et met la base à jour derrière ; `terminer` : toutes les lignes réécrites, observations figées, tentative TERMINEE, présence TERMINE, examinateur connecté enregistré si la tentative avait été ouverte par l'ATT (décision C8 n° 1), trois traces dans une seule transaction. `SessionUtilisateur.peutEvaluer()` partagé avec les tentatives.
- `ui/evaluation/EcranEvaluationTheorie.kt` — la feuille d'examen à l'écran : en-tête (passage, candidat, mode, points posés / note max, total attribué, durée et heure d'ouverture), bloc « Prochaine question » en mode direct, une carte par question posée (énoncé, points, réponse attendue en italique, champ « Réponse du candidat », champ « Points attribués (0 à N) »), observations, « Terminer l'épreuve » avec rappel des questions non notées ; lecture seule une fois le passage clos.
- Captures `C9_theorie_tirage.png`, `C9_theorie_direct.png`, `C9_passages.png`.

**Modifié**
- `data/EntitesReferentiels.kt` — entité `Reponse` supprimée, `Question.reponseAttendue` ; `data/EntitesExamens.kt` — `ReponseCandidat` = `reponseDonnee` + `pointsAttribues` (plus de `reponseId`), `Evaluation` sans `pointsSaisisDirectement` ; `data/AppDatabase.kt` — **schéma v3** (base de l'émulateur recréée) ; `data/ReferentielsDao.kt` — `ReponseDao` supprimé ; `data/ExamensDao.kt` — `EvaluationDao.parTentativeEnDirect`, `ReponseCandidatDao.parEvaluationDansLOrdre`, `pourQuestion`, `insererToutes` ; `data/Tracage.kt` — entité `Evaluation`, `Evaluation.resume()`, `Question.resume()` avec la réponse attendue.
- `metier/ValidationConfiguration.kt` — `validerQuestion(enonce, points)` ; `ui/configuration/ConfigurationViewModel.kt`, `EcranEpreuve.kt` — formulaire de question : énoncé, points, réponse attendue facultative ; liste des questions avec « attendu : … ».
- `ui/evaluation/EcranTentatives.kt` — un passage clos affiche « Consulter l'épreuve » au lieu du motif de refus en rouge ; `TentativesViewModel.kt` — utilise `peutEvaluer()` partagé.
- `Navigation.kt` — route `tentative/{tentativeId}/theorie` branchée sur `EcranEvaluationTheorie` (la conduite reste « à venir », C10).
- `docs/03` (Question, ReponseCandidat, schéma v3), `docs/04` (Q1 réécrite sur le témoignage), `docs/05` (UC09), `docs/HORS_COURS.md` (n° 23 `Random` injecté), `LISEZMOI.md`.

**Tests**
- `assembleDebug` vert ; `testDebugUnitTest` : 67 tests verts.
- Émulateur (pilote par texte, **base vide, schéma v3**) : `superadmin` crée le centre ATT Soarano, une version 2 du barème théorique B (20 / 12, depuis l'écran), 4 questions orales (10, 5, 3, 2 pt) avec réponse attendue, l'auto-école ; `admin` crée la session du jour, RAKOTO puis RASOA (dossier B validé, inscription, appel) ; passage de RASOA en TIRAGE : sujet de 4 questions = 20/20 posés, réponse « Arret obligatoire au feu », points 10 / 5 / 0 / 1 → total 16 / 20, observation, « Terminer » → « Consulter l'épreuve » ; `superadmin` passe `MODE_THEORIE` à `DIRECT` depuis Configuration → Règles ; RABE (3ᵉ candidat) : « Prochaine question (7 pt restants) » après deux questions posées (13/20), « Une autre », « Poser cette question », réponse saisie. Base : 3 évaluations (2 closes, 1 en cours), lignes `ReponseCandidat` avec réponse et points, historique CREATION / VALIDATION d'évaluation, tentatives et présences tracées.
- Défaut trouvé et corrigé pendant la vérification : la première version affichait dans le champ « Réponse du candidat » la valeur relue à travers le `combine` de l'état ; la frappe rapide du pilote a donné « Arret oigatoireb ». La saisie est maintenant un `StateFlow` exposé directement (même duo `_uiState`/`uiState` que le cours), la base est mise à jour derrière.
- Captures vérifiées une par une (Read) : `C9_theorie_tirage.png` (RASOA, 20/20 posés, total 16/20, réponse et points saisis, réponse attendue en italique), `C9_theorie_direct.png` (RABE, 13/20 posés, proposition « Distance de securite ? », filtre par points), `C9_passages.png` (deux passages terminés avec « Consulter l'épreuve », un en cours).

**Décisions restantes**
1. Une note partielle sur une question (par ex. 2 sur 3) est admise ; si l'ATT note en tout ou rien, rien à changer, l'examinateur saisit 0 ou le maximum (Q1).
2. En mode DIRECT, rien n'oblige l'examinateur à atteindre la note max avant de terminer ; le calcul (C11) rapportera le total à la note max du barème. À confirmer (Q1 bis).
3. Le sujet et la feuille sont en base ; leur impression relève de C13.
4. Étape suivante : C10 (structure de l'épreuve de conduite).

**À relire**
- `app/src/main/java/mg/itu/att/metier/ReglesTheorie.kt` — nouveau : tirage, propositions, points valides, contrôles d'ouverture et de fin.
- `app/src/test/java/mg/itu/att/metier/ReglesTheorieTest.kt` — nouveau : 8 tests ; `ValidationConfigurationTest.kt` — test de la question orale.
- `app/src/main/java/mg/itu/att/ui/evaluation/EvaluationTheorieViewModel.kt` — nouveau : création de l'évaluation et du sujet, saisie (réponse, points) exposée directement, clôture tracée.
- `app/src/main/java/mg/itu/att/ui/evaluation/EcranEvaluationTheorie.kt` — nouveau : la feuille d'examen à l'écran.
- `app/src/main/java/mg/itu/att/ui/evaluation/EcranTentatives.kt` — « Consulter l'épreuve » pour un passage clos ; `TentativesViewModel.kt` — `peutEvaluer()` factorisé.
- `app/src/main/java/mg/itu/att/data/EntitesReferentiels.kt`, `EntitesExamens.kt`, `AppDatabase.kt` (v3), `ReferentielsDao.kt`, `ExamensDao.kt`, `Tracage.kt` — QCM retiré, feuille d'examen en base.
- `app/src/main/java/mg/itu/att/metier/ValidationConfiguration.kt`, `ui/configuration/ConfigurationViewModel.kt`, `EcranEpreuve.kt` — question orale : énoncé, points, réponse attendue.
- `app/src/main/java/mg/itu/att/Navigation.kt` — route théorie branchée.
- `docs/02_PLAN_DE_TRAVAIL.md`, `docs/03_MODELE_DE_DONNEES.md`, `docs/04_QUESTIONS_A_VALIDER.md` (Q1 réécrite), `docs/05_CAS_UTILISATION.md`, `docs/HORS_COURS.md`, `JOURNAL-IA.md`, `LISEZMOI.md`, `docs/captures/C9_*.png`.

---

## Étape C10 — Structure de l'épreuve de conduite — 17/09/2026

**Créé** (`app/src/main/java/mg/itu/att/`)
- `metier/ReglesConduite.kt` — `NoteCritere` (points max, points saisis, critère éliminatoire, faute cochée ; une faute cochée vaut 0), `pointsGrille`, `verifierOuverture` (barème courant, au moins un critère actif → sinon renvoi à la configuration, Q2), `totalAttribue`, `fauteEliminatoire`, `sansNote`, `verifierFin` (tous les critères notés). 3 tests. `metier/Points.kt` — `pointsValides` partagé avec la théorie.
- `ui/evaluation/EvaluationConduiteViewModel.kt` — à l'ouverture, crée l'`Evaluation` sur le barème courant et une ligne `EvaluationCritere` par critère actif (grille figée) ; `saisir` (points, faute, observation) exposé directement comme pour la théorie, base mise à jour derrière ; `terminer` : lignes réécrites, observations figées, tentative TERMINEE, présence TERMINE, examinateur enregistré, trois traces dans une transaction.
- `ui/evaluation/EcranEvaluationConduite.kt` — la grille à l'écran : en-tête (passage, candidat, grille N critères / points, total / note max, bandeau rouge « faute éliminatoire »), une carte par critère (points attribués, case « Faute éliminatoire commise » si le critère l'admet, observation), observations générales, « Terminer l'épreuve ».
- Captures `C10_conduite.png` (faute cochée sur « Respect des priorités », total 7/20, bandeau rouge), `C10_passages.png` (passage terminé, « Consulter l'épreuve »).

**Modifié**
- `data/ExamensDao.kt` — `EvaluationCritereDao.pourCritere`, `insererToutes` ; `data/ReferentielsDao.kt` — `CriterePratiqueDao.parId`.
- `metier/ReglesTheorie.kt`, `ui/evaluation/EvaluationTheorieViewModel.kt`, test — utilisent `pointsValides` partagé.
- `Navigation.kt` — route `tentative/{tentativeId}/conduite` branchée sur `EcranEvaluationConduite` (plus d'écran « à venir » dans l'évaluation).

**Tests**
- `assembleDebug` vert ; `testDebugUnitTest` : 70 tests verts (67 + 3).
- Émulateur (pilote par texte) : `superadmin` passe `CONDUITE_APRES_THEORIE_REUSSIE` à `false` (aucun résultat n'existe avant C11) et crée 3 critères sur l'épreuve de conduite B (5 pt, 3 pt, 10 pt éliminatoire) ; `admin` crée une session de conduite : l'inscription le jour même est **refusée** (« Le candidat a déjà une session le 17/09/2026, conflit de créneaux »), donc session le lendemain, inscription n° 001, appel, passage n° 1 : points 4 et 3, faute cochée sur le 3ᵉ critère → total 7/20 et bandeau rouge ; faute décochée, 8 pt → 15/20, observation, « Terminer » → « Consulter l'épreuve ». Base : évaluation close, 3 lignes `EvaluationCritere`, historique.
- Captures vérifiées une par une (Read).

**Décisions restantes**
1. La somme des points de la grille (18) ne vaut pas forcément la note max du barème (20) : C11 doit décider si le total est rapporté à la note max ou comparé tel quel au seuil. À poser (Q2).
2. Une faute éliminatoire fait perdre l'épreuve quel que soit le total (invariant docs/03 §6) ; l'écran l'annonce, C11 l'applique.
3. Grille de démonstration seulement : les critères créés dans le test ne sont pas ceux de l'ATT, tous « à confirmer ».
4. Décision du dev 1 (17/09/2026) : **pour le moment on garde le partage actuel des rôles** : l'examinateur fait passer l'examen ; l'Admin ATT organise (sessions, inscriptions, appel) et peut aussi ouvrir un passage et évaluer « en secours » (Q6). À revoir plus tard : réserver l'évaluation aux examinateurs, et qui fait l'appel.
5. Étape suivante : C11 (calcul et validation des résultats).

**À relire**
- `app/src/main/java/mg/itu/att/metier/ReglesConduite.kt`, `Points.kt` — nouveaux ; `ReglesTheorie.kt` — `pointsValides` déplacé.
- `app/src/test/java/mg/itu/att/metier/ReglesConduiteTest.kt` — nouveau ; `ReglesTheorieTest.kt` — adapté.
- `app/src/main/java/mg/itu/att/ui/evaluation/EvaluationConduiteViewModel.kt`, `EcranEvaluationConduite.kt` — nouveaux.
- `app/src/main/java/mg/itu/att/ui/evaluation/EvaluationTheorieViewModel.kt` — import de `pointsValides`.
- `app/src/main/java/mg/itu/att/data/ExamensDao.kt`, `ReferentielsDao.kt` — requêtes ajoutées.
- `app/src/main/java/mg/itu/att/Navigation.kt` — route conduite branchée.
- `docs/02_PLAN_DE_TRAVAIL.md`, `docs/04_QUESTIONS_A_VALIDER.md`, `JOURNAL-IA.md`, `docs/captures/C10_*.png`.

---

## Étape C12a — Historique des modifications (UC14) — 17/09/2026

> Première sous-étape de C12 (consultation par rôle), reprise par le dev 2. Cette partie (historique) est livrée seule ; le reste de C12 suit dans une pull request unique, comme les étapes précédentes.

**Créé** (`app/src/main/java/mg/itu/att/`)
- `metier/FiltresHistorique.kt` — `peutConsulter(role)` (Super Admin et Admin ATT seulement, docs/01 §6), `joursAvant`, `dateDebut(periode, aujourdHui)`, `filtrer(lignes, entite, utilisateurId, periode, aujourdHui)` (filtres combinables, ordre du DAO conservé), `entitesPresentes`, `libelleEntite` (« Passage » pour `Tentative`, comme à l'écran). Fonctions pures, la date du jour est un paramètre. 6 tests.
- `ui/historique/HistoriqueViewModel.kt` — `EtatHistorique` (lignes avec libellé d'objet et nom d'utilisateur, filtres choisis, objets et utilisateurs présents, `autorise`, total) construit par `combine(historiqueDao.tout(), utilisateurDao.tous(), filtres, session).stateIn`. Aucune écriture. Un rôle non autorisé reçoit un état vide.
- `ui/historique/EcranHistorique.kt` — périodes en `FilterChip` (Tout / Aujourd'hui / 7 jours / 30 jours), sélecteurs « Objet » et « Utilisateur » (`SelecteurChoix` avec « Tous »), compteur « n modification(s) sur total », une `CarteIcone` par ligne (date, action, objet n°, auteur, résumé, motif). Un clic ouvre la fiche de l'objet quand elle existe.
- Captures `C12a_historique.png`, `C12a_historique_filtre_candidat.png`, `C12a_ouverture_fiche.png`, `C12a_menu_auto_ecole_sans_historique.png` (prises sur l'émulateur).

**Modifié**
- `Navigation.kt` — `graphHistorique` : route `historique` branchée sur `EcranHistorique` (retirée de la boucle « à venir ») ; ouverture d'une fiche candidat, dossier, session ou auto-école depuis une ligne (la liste racine du sous-parcours est empilée avant la fiche, pour que `viewModelDuSousParcours` trouve son entrée de pile).
- `docs/02_PLAN_DE_TRAVAIL.md` — tableau « Où en est-on ? » (C10 fusionnée, C12a terminée, prochaine : reste de C12), répartition : le dev 2 reprend C11 à C13 et D1.

**Tests**
- `assembleDebug` vert ; `testDebugUnitTest` : 73 tests verts (67 + 6).
- Émulateur (pilote par texte, base vide) : `admin` crée l'auto-école « Auto-ecole Lalana » (Analamanga), le candidat RAKOTO Jean et le compte `lalana` → Historique : « 2 modification(s) sur 2 » puis 3, ordre du plus récent au plus ancien, objet et auteur affichés ; filtre Objet = Candidat → 1 ligne ; Aujourd'hui → toutes ; Utilisateur = Administrateur ATT → toutes ; clic sur la ligne auto-école → fiche « Auto-ecole Lalana » ; retour → Historique. Connexion `lalana` (auto-école) : le menu n'a pas d'entrée Historique.

**Décisions restantes**
1. La route `historique/{entite}/{entiteId}` prévue dans docs/05 §4.1 n'est pas ajoutée : chaque fiche (candidat, dossier, session, auto-école) affiche déjà son propre historique (`LigneHistorique`), et l'écran global ouvre la fiche. À retirer de docs/05 si le binôme confirme.
2. Le filtre « Objet » ne propose que les objets présents dans l'historique (pas la liste complète des entités), pour éviter les choix vides.
3. Étape suivante : reste de C12 (sections inscriptions et passages sur la fiche candidat, compte et parcours candidat, inscriptions de l'auto-école).

**À relire**
- `app/src/main/java/mg/itu/att/metier/FiltresHistorique.kt` — nouveau : permission, périodes, filtrage, libellés.
- `app/src/test/java/mg/itu/att/metier/FiltresHistoriqueTest.kt` — nouveau : 6 tests (rôles, jours avant, début de période, filtres combinés, libellés).
- `app/src/main/java/mg/itu/att/ui/historique/HistoriqueViewModel.kt` — nouveau : état de l'écran, lecture seule.
- `app/src/main/java/mg/itu/att/ui/historique/EcranHistorique.kt` — nouveau : l'écran et ses filtres.
- `app/src/main/java/mg/itu/att/Navigation.kt` — `graphHistorique` ajouté, `HISTORIQUE` retiré des routes « à venir ».
- `docs/02_PLAN_DE_TRAVAIL.md` — avancement et répartition.
- `docs/captures/C12a_*.png` — 4 captures de l'émulateur.

---

## Étape C12 — Consultation par rôle (UC12) — 17/09/2026

> Suite de C12a (historique, PR #15). Cette étape complète la consultation : le parcours d'un candidat (inscriptions, passages), son compte de connexion facultatif, « Mon parcours » côté candidat et « Mes inscriptions » côté auto-école. La consultation des résultats validés arrive avec C11, qui les crée.

**Créé** (`app/src/main/java/mg/itu/att/`)
- `metier/ReglesConsultation.kt` — `peutVoirCandidat` (Super Admin tout ; Admin ATT sa région ; auto-école ses candidats ; candidat lui-même ; examinateur aucune fiche, il travaille sur des numéros d'appel), `peutGererCandidat` / `peutCreerCompteCandidat` (ATT et auto-école), `estAVenir`, `libelleSession`. Fonctions pures, date du jour en paramètre. 6 tests.
- `ui/candidats/SectionsParcours.kt` — sections « Inscriptions et convocations » et « Passages d'épreuve » (`LazyListScope`), partagées par la fiche candidat et « Mon parcours » ; `PastilleTentative` et le libellé des statuts de passage.
- `ui/candidats/EcranParcours.kt` — « Mon parcours » du candidat connecté : identité, dossiers, inscriptions, passages, en lecture seule ; message explicite si le compte n'est rattaché à aucun candidat.
- `ui/candidats/EcranFormulaireCompteCandidat.kt` — création du compte de connexion d'un candidat (compte facultatif, cadrage §10), même formulaire que celui d'une auto-école, mot de passe haché.
- `ui/inscriptions/MesInscriptionsViewModel.kt` + `EcranMesInscriptions.kt` — « Mes inscriptions » de l'auto-école : convocations de ses candidats (à venir / passées / toutes), centre, créneau, passage estimé, numéro d'appel, présence ; lecture seule, les actions restent sur l'écran d'inscriptions d'une session.
- Captures `C12_fiche_candidat.png`, `C12_fiche_candidat_parcours.png`, `C12_compte_candidat_cree.png`, `C12_mes_inscriptions_auto_ecole.png`, `C12_accueil_candidat.png`, `C12_mon_parcours.png`, `C12_dossier_lecture_seule.png` (prises sur l'émulateur).

**Modifié**
- `ui/candidats/CandidatsViewModel.kt` — `EtatDetailCandidat` porte désormais les inscriptions (avec session, centre, créneau, présence), les passages, les comptes et les droits (`peutGerer`, `peutCreerCompte`) ; un candidat hors périmètre n'est pas affiché (« introuvable », comme un identifiant invalide) ; `creerCompte` (rôle CANDIDAT, tracé deux fois : compte et candidat) ; le candidat ne peut plus soumettre un dossier (`peutSoumettre` restreint aux gestionnaires).
- `ui/candidats/EcranDetailCandidat.kt` — sections du parcours, bloc « Compte de connexion », actions de gestion masquées pour un lecteur.
- `data/ActeursDao.kt` — `UtilisateurDao.parCandidat` ; `data/PlanificationDao.kt` — `CreneauDao.tous`.
- `ui/accueil/MenuParRole.kt` — l'auto-école a « Mes inscriptions » (route `mes-inscriptions`) au lieu de la liste des sessions de l'ATT ; `Navigation.kt` — routes `candidat/{id}/compte`, `parcours`, `mes-inscriptions` (plus d'écran « à venir » pour le parcours).
- `docs/05_CAS_UTILISATION.md` (nouvelles routes, menu auto-école), `docs/02_PLAN_DE_TRAVAIL.md` (C12 🟢, prochaine étape C11).

**Tests**
- `assembleDebug` vert ; `testDebugUnitTest` : 83 tests verts (76 + 6 de `ReglesConsultation` + 1 sur le menu de l'auto-école).
- Émulateur (pilote par texte) : `superadmin` crée le centre ATT Soarano ; `admin` ouvre le dossier B de RAKOTO Jean, le soumet et le valide, crée la session du jour, inscrit le candidat (n° 001, créneau 08:00), fait l'appel, puis ouvre sa fiche : sections « Inscriptions et convocations » (centre, n° d'appel, créneau, passage estimé, présence) et « Passages d'épreuve » visibles, compte `rakoto` créé et tracé ; `lalana` (auto-école) : « Mes inscriptions » affiche 1 inscription à venir de son candidat et ouvre sa fiche ; `rakoto` (candidat) : accueil réduit à « Mon parcours », parcours en lecture seule, dossier consultable **sans** bouton « Soumettre à l'ATT ».
- Captures vérifiées une par une (Read).

**Décisions restantes**
1. Le compte candidat est créé par l'ATT ou par l'auto-école depuis la fiche (le candidat ne s'inscrit pas lui-même) : cohérent avec le cadrage §4 et l'absence de serveur. À confirmer avec le binôme.
2. L'examinateur ne consulte aucune fiche candidat (il travaille par numéro d'appel, Q4) : sa vue « Mes évaluations » viendra avec C11.
3. Un administrateur régional filtre par la région de l'auto-école du candidat (le candidat n'a pas de région propre). À confirmer (Q11).
4. Étape suivante : C11 (calcul et validation des résultats, avec leur consultation par rôle).

**À relire**
- `app/src/main/java/mg/itu/att/metier/ReglesConsultation.kt` — nouveau : permissions de consultation, période, libellé de session.
- `app/src/test/java/mg/itu/att/metier/ReglesConsultationTest.kt` — nouveau : 6 tests ; `ui/accueil/MenuParRoleTest.kt` — test du menu auto-école.
- `app/src/main/java/mg/itu/att/ui/candidats/SectionsParcours.kt`, `EcranParcours.kt`, `EcranFormulaireCompteCandidat.kt` — nouveaux.
- `app/src/main/java/mg/itu/att/ui/candidats/CandidatsViewModel.kt` — parcours, comptes, droits, création du compte candidat.
- `app/src/main/java/mg/itu/att/ui/candidats/EcranDetailCandidat.kt` — sections du parcours et compte, actions selon le rôle.
- `app/src/main/java/mg/itu/att/ui/inscriptions/MesInscriptionsViewModel.kt`, `EcranMesInscriptions.kt` — nouveaux.
- `app/src/main/java/mg/itu/att/data/ActeursDao.kt`, `PlanificationDao.kt` — deux requêtes ajoutées.
- `app/src/main/java/mg/itu/att/ui/accueil/MenuParRole.kt`, `Navigation.kt` — menu et routes.
- `docs/02_PLAN_DE_TRAVAIL.md`, `docs/05_CAS_UTILISATION.md`, `docs/captures/C12_*.png` — 7 captures de l'émulateur.

---

## Étape C11 — Calcul, validation et correction des résultats — 17/09/2026

> Étape reprise par le dev 2 (le dev 1 a livré jusqu'à C10). Le calcul s'enchaîne à la clôture d'une épreuve (UC10) ; l'ATT valide, corrige si besoin, et le résultat validé devient visible par l'auto-école et le candidat (UC12). Rien n'est jamais réécrit : une correction crée une nouvelle ligne (R6).

**Créé** (`app/src/main/java/mg/itu/att/`)
- `metier/CalculResultat.kt` — `totalTheorie` / `totalPoseTheorie`, `totalConduite` / `totalGrilleConduite` (faute cochée = 0 point), `fauteEliminatoire`, `rapporterAuBareme` (règle de trois quand le sujet ou la grille ne totalise pas la note maximale — décision Q2 bis), `calculer` (réussi = seuil atteint **et** aucune faute éliminatoire), `calculerTheorie`, `calculerConduite`, `verifierCalcul`, `verifierCorrection`, `arrondir`. Fonctions pures. 9 tests.
- `metier/ReglesRepassage.kt` — `reussiteEncoreValable` (conservation configurable, 0 = sans limite), `inscriptibleLe` (délai de repassage), `epreuvesARepasser` (jamais réussies, échouées, ou réussite expirée) avec la raison et la date de réinscription. 4 tests.
- `data/CalculEnBase.kt` — `calculerPourTentative` (rassemble la feuille de théorie ou la grille de conduite et le barème figé, la décision revient à la fonction pure) et `enregistrerResultat` (insère la ligne `Resultat` et la trace ; en correction, référence le résultat remplacé).
- `ui/resultats/ResultatsViewModel.kt` — liste filtrée par rôle (ATT : tout, file « à valider » ; auto-école et candidat : résultats **validés** de leurs candidats ; examinateur : les passages qu'il a saisis), détail (calcul relu, versions, historique, épreuves à repasser), `valider`, `corriger`.
- `ui/resultats/EcranResultats.kt`, `EcranDetailResultat.kt`, `Statuts.kt` — un seul écran de liste, titré selon le rôle ; le détail explique la note (points attribués, rapport au barème, seuil, faute éliminatoire), propose la validation et la correction avec motif, et liste les versions successives.
- Captures `C11_resultats_a_valider.png`, `C11_detail_resultat.png`, `C11_resultat_valide.png`, `C11_correction.png`, `C11_resultats_auto_ecole.png` (prises sur l'émulateur).

**Modifié**
- `ui/evaluation/EvaluationTheorieViewModel.kt` et `EvaluationConduiteViewModel.kt` — le calcul est enchaîné dans la transaction de clôture (`enregistrerResultat`), conformément à UC10.
- `data/ExamensDao.kt` — `ResultatDao.tous`, `parIdEnDirect`, `listePourTentative`, `modifier` (validation et mise hors circuit d'un résultat remplacé ; la note n'est jamais retouchée) ; `data/ReferentielsDao.kt` — `QuestionDao.listePourEpreuve`, `CriterePratiqueDao.listePourEpreuve` (une question désactivée reste lisible pour un sujet déjà posé) ; `data/Tracage.kt` — `Resultat.resume()`.
- `Navigation.kt` — `graphResultats` : routes `resultats` et `resultat/{resultatId}` (plus aucun écran « à venir » au menu) ; une correction ouvre le nouveau résultat.
- `docs/02_PLAN_DE_TRAVAIL.md` (C11 🟢, prochaine étape C13), `docs/04_QUESTIONS_A_VALIDER.md` (Q2 bis : rapport au barème), `docs/05_CAS_UTILISATION.md` (routes des résultats).

**Tests**
- `assembleDebug` vert ; `testDebugUnitTest` : 96 tests verts (83 + 13).
- Émulateur : base vide, `superadmin` crée le centre et 3 questions de 10 points sur l'épreuve théorique B (barème 30 / seuil 20) ; `admin` crée l'auto-école et son compte, le candidat, valide le dossier, crée la session du jour, inscrit, fait l'appel (retard accepté) ; passage n° 1, sujet tiré au sort (3 questions, 30/30 posés), notes 10 + 10 + 0 = 20/30 → résultat **calculé automatiquement à la clôture**, statut « À valider », réussi (seuil 20 atteint) ; validation par l'ATT ; correction avec motif → nouvelle ligne, l'ancienne passe en « Remplacé » et reste lisible dans « Versions de ce passage » ; validation de la version corrigée ; `lalana` (auto-école) voit « 1 résultat validé » pour son candidat.
- **Deux défauts trouvés pendant la vérification, corrigés** :
  1. La saisie du motif de correction perdait des caractères : l'état du détail relit la base à chaque émission, donc chaque frappe repartait d'une valeur périmée. Le motif est désormais un `StateFlow` à part (`SaisieCorrection`), comme la saisie de la feuille d'examen en C9.
  2. Un résultat corrigé restait au statut `CORRIGE` et ne pouvait plus être validé : il n'était donc **jamais** visible par l'auto-école ni par le candidat. Une correction repasse maintenant par la validation de l'ATT (la file « à valider » contient les résultats `CALCULE` **et** `CORRIGE`).

**Décisions restantes**
1. Q2 bis (nouvelle) : la note est **rapportée au barème** quand le sujet ou la grille ne totalise pas la note maximale (règle de trois). À confirmer avec l'ATT ; l'autre option serait de comparer le total brut au seuil.
2. La correction recalcule depuis la saisie de l'examinateur : pour corriger une note, l'examinateur doit d'abord modifier sa feuille. Une correction « à la main » (saisir directement une note) n'est pas prévue : elle contournerait le barème.
3. Un résultat validé ne peut plus être re-validé ; il ne peut qu'être corrigé (nouvelle ligne), et la correction repasse par la validation avant d'être publiée.
4. Étape suivante : C13 (impression : convocation, liste d'appel, liste des admis, relevé de résultat).

**À relire**
- `app/src/main/java/mg/itu/att/metier/CalculResultat.kt`, `ReglesRepassage.kt` — nouveaux : tout le calcul et les règles de repassage, sans Android.
- `app/src/test/java/mg/itu/att/metier/CalculResultatTest.kt`, `ReglesRepassageTest.kt` — nouveaux : 13 tests (seuil, faute éliminatoire, rapport au barème, conservation, délais).
- `app/src/main/java/mg/itu/att/data/CalculEnBase.kt` — nouveau : le calcul côté base, à appeler dans une transaction.
- `app/src/main/java/mg/itu/att/ui/resultats/` — nouveaux : ViewModel et deux écrans, filtrés par rôle.
- `app/src/main/java/mg/itu/att/ui/evaluation/EvaluationTheorieViewModel.kt`, `EvaluationConduiteViewModel.kt` — une ligne ajoutée : le calcul enchaîné à la clôture.
- `app/src/main/java/mg/itu/att/data/ExamensDao.kt`, `ReferentielsDao.kt`, `Tracage.kt` — requêtes et résumé ajoutés.
- `app/src/main/java/mg/itu/att/Navigation.kt` — `graphResultats`.
- `docs/02_PLAN_DE_TRAVAIL.md`, `docs/04_QUESTIONS_A_VALIDER.md`, `docs/05_CAS_UTILISATION.md`, `docs/captures/C11_*.png` — 6 captures de l'émulateur.

---

## Étape C13 — Impression des documents — 17/09/2026

> Quatre documents imprimables (UC13), tous construits en HTML par des fonctions pures puis confiés au service d'impression d'Android (imprimante ou enregistrement en PDF). Fonctionne sans réseau : c'est la procédure de secours du cadrage §10.

**Créé** (`app/src/main/java/mg/itu/att/`)
- `metier/DocumentsImpression.kt` — `page` (squelette commun : en-tête ATT, styles d'impression, pied de page avec la mention « document interne, sans valeur officielle » et la date d'édition), `echapper` (un nom avec `&` ou `<` ne casse pas la page), puis `convocation`, `listeAppel` (version ATT avec les noms / version examinateur avec les numéros seuls, colonnes Présent / Absent / Retard à cocher), `listeAdmis` (uniquement les résultats validés par l'ATT) et `releve`. Fonctions pures. 9 tests.
- `ui/impression/ImpressionViewModel.kt` — rassemble les données de chaque document et **vérifie les droits** : une auto-école n'imprime que les convocations et relevés de ses candidats, un candidat que les siens, la liste des admis est réservée à l'ATT, et un relevé non validé n'est imprimable que par l'ATT.
- `ui/impression/EcranImpression.kt` — un seul écran d'aperçu : la page est affichée dans une `WebView` (`AndroidView`) et le bouton « Imprimer » la remet à `PrintManager`.
- Captures `C13_convocation.png`, `C13_liste_appel.png`, `C13_liste_admis.png`, `C13_releve.png`, `C13_convocation_auto_ecole.png` (prises sur l'émulateur).

**Modifié**
- `Navigation.kt` — `graphImpression` : les quatre routes de docs/05 §4.6 (`impression/convocation/{inscriptionId}`, `…/appel/{sessionId}`, `…/admis/{sessionId}`, `…/releve/{resultatId}`).
- Boutons d'accès : `EcranDetailSession` (« Liste d'appel », « Liste des admis »), `EcranInscriptions` (« Convocation » par inscrit), `EcranMesInscriptions` (« Imprimer la convocation » pour une session à venir), `EcranDetailResultat` (« Imprimer le relevé »).
- `docs/HORS_COURS.md` — notion n° 24 (`AndroidView` + `PrintManager`) déclarée avant usage (R7) ; `docs/02_PLAN_DE_TRAVAIL.md` (C13 🟢, prochaine étape D1).

**Tests**
- `assembleDebug` vert ; `testDebugUnitTest` : 105 tests verts (96 + 9).
- Émulateur : `admin` imprime la liste d'appel (1 candidat, n° 001, cases à cocher), la liste des admis (« 1 sur 1 résultat(s) validé(s) », RAKOTO Jean admis 20/30), la convocation (centre, date, heure de passage estimée, 4 pièces à apporter) et le relevé (note, réussite, date de validation) ; `lalana` (auto-école) imprime la convocation de son propre candidat depuis « Mes inscriptions ». Chaque document porte la mention et la date d'édition.
- **Défaut trouvé et corrigé** (hors périmètre C13, mais bloquant) : dans `EcranDetailSession`, tous les boutons d'action étaient dans une seule `Row`, qui déborde de l'écran — « Démarrer » et « Terminer » étaient invisibles et inaccessibles. Les actions sont désormais réparties sur trois lignes.

**Décisions restantes**
1. La liste d'appel « version examinateur » est produite quand un examinateur imprime la liste ; l'ATT obtient la version nommée. C'est le rôle connecté qui décide, il n'y a pas de bouton séparé.
2. La liste des admis n'affiche que les résultats **validés** : un résultat calculé mais non validé n'y figure pas (UC10).
3. L'impression passe par le service d'Android : sur un émulateur sans imprimante, l'option « Enregistrer au format PDF » est disponible.
4. Étape suivante : D1 (tests et cas particuliers, les 10 contraintes du cadrage §11).

**À relire**
- `app/src/main/java/mg/itu/att/metier/DocumentsImpression.kt` — nouveau : les quatre documents, en HTML, sans Android.
- `app/src/test/java/mg/itu/att/metier/DocumentsImpressionTest.kt` — nouveau : 9 tests (échappement, mention, anonymat de la version examinateur, comptage des admis).
- `app/src/main/java/mg/itu/att/ui/impression/ImpressionViewModel.kt` — nouveau : assemblage des données et contrôle des droits.
- `app/src/main/java/mg/itu/att/ui/impression/EcranImpression.kt` — nouveau : aperçu `WebView` + `PrintManager`.
- `app/src/main/java/mg/itu/att/Navigation.kt` — `graphImpression` et les boutons passés aux écrans.
- `app/src/main/java/mg/itu/att/ui/sessions/EcranDetailSession.kt` — boutons répartis sur plusieurs lignes (défaut d'affichage corrigé) ; `ui/inscriptions/EcranInscriptions.kt`, `EcranMesInscriptions.kt`, `ui/resultats/EcranDetailResultat.kt` — un bouton d'impression chacun.
- `docs/HORS_COURS.md` (n° 24), `docs/02_PLAN_DE_TRAVAIL.md`, `docs/captures/C13_*.png` — 5 captures de l'émulateur.
