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
