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

## Entrée 7 — 16/09/2026 — Étape C4, configuration

- Code soumis : `ui/configuration/*`, `metier/ValidationConfiguration.kt`.
- Remarque principale de l'IA : un barème déjà utilisé par un résultat ne doit jamais changer ; « modifier le barème » est donc remplacé par « nouvelle version » (l'ancienne est fermée à la date du jour), et chaque résultat gardera la version qui a servi (règle R6 appliquée aux règles elles-mêmes).

⚠️ Le code ne connaît que la FORME des règles (un seuil ≤ note max, un entier pour un délai), jamais leur valeur : c'est la traduction concrète de « ne pas coder en dur » (R3). À vérifier par le binôme : aucune constante métier dans `ValidationConfiguration` hormis les bornes de saisie (âge 10–99).

⚠️ Les questions et critères se désactivent et ne se modifient pas, pour ne pas altérer d'anciennes évaluations. C'est plus contraignant qu'une édition libre ; le binôme peut décider d'autoriser la modification tant qu'aucune évaluation n'y fait référence.

⚠️ Pilotage : l'apostrophe dans un texte saisi casse `adb shell input text` ; contournement documenté dans le script. Sans lien avec le code livré.

- Mon verdict (accepte / rejette / nuance) et pourquoi : <à compléter par le dev 1>.

---

## Entrée 8 — 16/09/2026 — Étape C5, sessions et créneaux

- Code soumis : `metier/ReglesPlanification.kt`, `ui/sessions/*`.
- Remarque principale de l'IA : la génération des créneaux est une fonction pure (`genererCreneaux`) réutilisée deux fois avec la même entrée : pour l'aperçu à la frappe dans le formulaire et pour l'insertion en base. Ce que l'utilisateur voit est exactement ce qui sera enregistré.

⚠️ Les valeurs par défaut du formulaire (capacité, durée, marge) sont lues dans `RegleConfig` à l'ouverture, jamais écrites dans le code (R3). À vérifier par le binôme : `SessionsViewModel.preparerFormulaire`.

⚠️ Deux textes identiques à l'écran (titre de section et bouton « Annuler la session ») ont trompé le pilote par texte, et auraient pu troubler un utilisateur : bouton renommé « Confirmer l'annulation ». Leçon : un bouton ne doit pas porter le même libellé qu'un titre.

⚠️ Le conflit de centre (§11) est un avertissement non bloquant, faute de règle connue (Q5) ; le binôme peut le rendre bloquant via une règle si l'ATT le demande.

- Mon verdict (accepte / rejette / nuance) et pourquoi : <à compléter par le dev 1>.

---

## Entrée 9 — 16/09/2026 — Étape C6, inscriptions

- Code soumis : `metier/ReglesInscription.kt`, `ui/inscriptions/*`, `data/Regles.kt`.
- Remarque principale de l'IA : séparer « rassembler les faits » (ViewModel, lectures en base) de « juger » (`ReglesInscription.verifier`, fonction pure sur un `ContexteInscription`) : les neuf contrôles du cadrage (§6, §11) sont testés sans Android, et l'ordre des messages va du plus bloquant au plus fin.

⚠️ Toutes les règles (région, délai, tentatives max, théorie avant conduite) sont lues par `regleEntier` / `regleBooleen` au moment de l'inscription, avec la règle de la catégorie prioritaire sur la règle globale. À vérifier par le binôme : aucune valeur n'est écrite dans le code.

⚠️ `joursEntre` calcule un écart de dates avec la formule des jours juliens plutôt qu'avec `java.time` (hors cours) : à la fois plus « fait main » comme le cours et moins lisible. Le binôme peut préférer `java.time.LocalDate` en l'ajoutant à HORS_COURS.

⚠️ Le passage automatique de la session en COMPLETE / retour en OUVERTE est décidé dans le ViewModel, dans la même transaction que l'inscription : cohérent, mais c'est une règle métier implicite ; elle est documentée dans le compte rendu.

- Mon verdict (accepte / rejette / nuance) et pourquoi : <à compléter par le dev 1>.

---

## Entrée 10 — 16/09/2026 — Étape C7, présence et appel

- Code soumis : `metier/ReglesPresence.kt`, `ui/appel/*`, schéma v2 des inscriptions.
- Remarque principale de l'IA : la vérification sur émulateur a révélé un vrai défaut de conception, pas un bug de frappe : l'index unique (candidat, session) en base contredisait la règle métier « une inscription reportée ou annulée peut être suivie d'une nouvelle ». Le test unitaire de la règle passait ; seule la base réelle a montré la contradiction. Leçon pour le binôme : les invariants qui dépendent d'un statut ne se mettent pas dans un index SQL, mais dans une fonction pure.

⚠️ La correction change le schéma (v2) avec `fallbackToDestructiveMigration` : les données de test de l'émulateur sont perdues, ce qui est accepté en développement (docs/01 §7) ; le pilote a rejoué toute la chaîne depuis une base vide en une passe.

⚠️ L'horloge de l'émulateur (19 h) fait classer tout « Présent » en retard pour une convocation à 08:00 : ce n'est pas une anomalie, c'est la règle. Le scénario a donc exercé le chemin « retard accepté » plutôt que « présent direct ».

⚠️ Deux perturbations sans lien avec le code : des projets du cours lancés sur le même émulateur passaient devant ATT ; puis l'émulateur a été fermé et relancé par l'IA. Le pilote vérifie désormais qu'ATT est au premier plan avant chaque action.

- Mon verdict (accepte / rejette / nuance) et pourquoi : <à compléter par le dev 1>.

---

## Entrée 11 — 16/09/2026 — Étape C8, tentatives

- Code soumis : `metier/ReglesTentatives.kt`, `ui/evaluation/*`.
- Remarque principale de l'IA : « ouvrir » et « reprendre » une tentative passent par la même fonction : s'il existe déjà une tentative en cours pour l'inscription, on la rouvre au lieu d'en créer une seconde ; s'il en existe une terminée, on refuse. Une tentative n'est donc jamais dupliquée ni écrasée (R6).

⚠️ L'examinateur n'est pas affecté d'avance (R5) : la tentative enregistre l'examinateur connecté au moment de l'ouverture, ou rien si c'est l'ATT qui ouvre. Le binôme doit décider si l'ATT peut ouvrir une tentative (utile au guichet) ou si c'est réservé aux examinateurs (Q6).

⚠️ Le pilote a raté une carte du menu pendant un enchaînement de retours ; sans conséquence sur le code, mais la démo de soutenance gagnera à repartir de l'accueil (`relancer` + connexion) à chaque scénario.

- Mon verdict (accepte / rejette / nuance) et pourquoi : <à compléter par le dev 1>.

---

## Entrée 12 — 17/09/2026 — Étape C9, évaluation théorique

- Code soumis : `metier/ReglesTheorie.kt`, `ui/evaluation/EvaluationTheorieViewModel.kt`, `EcranEvaluationTheorie.kt`, schéma v3.
- Remarque principale de l'IA : la première version était un QCM (réponses A/B/C/D) parce que la presse et le cadrage le disaient ; le dev 1, qui vient de passer l'épreuve, a décrit une épreuve orale avec une feuille « question posée / réponse donnée / points / total ». L'IA avait respecté la règle « ne pas inventer » en suivant les sources écrites, mais une source de terrain vaut plus qu'un article : la question Q1 est réécrite, la table QCM supprimée, l'écran devient la feuille d'examen.

⚠️ Le tirage au sort reste une fonction pure qui reçoit son générateur de hasard (`Random(graine)`) : avec une graine fixe le sujet est reproductible, le test vérifie qu'il atteint la note max sans la dépasser ni répéter, avec des questions à points variables.

⚠️ Le sujet est créé en base dès l'ouverture de l'écran (gardé par « une seule fois par identifiant ») pour être figé avant la première réponse. Un écran qui écrit à l'ouverture est inhabituel dans le cours : à discuter en binôme.

⚠️ Ce que tape l'examinateur est gardé dans le ViewModel et recopié en base à chaque frappe (et en bloc à la clôture) : si l'écran affichait la valeur relue depuis Room, le curseur sauterait. À relire.

- Mon verdict (accepte / rejette / nuance) et pourquoi : <à compléter par le dev 1>.

---

## Entrée 13 — 17/09/2026 — Étape C10, structure de l'épreuve de conduite

- Code soumis : `metier/ReglesConduite.kt`, `metier/Points.kt`, `ui/evaluation/EvaluationConduiteViewModel.kt`, `EcranEvaluationConduite.kt`.
- Remarque principale de l'IA : l'écran de conduite est bâti sur le même patron que la théorie (feuille = lignes créées à l'ouverture, saisie exposée directement, clôture tracée), sans copier la logique : ce qui était commun (`pointsValides`) a été sorti dans `metier/Points.kt`. Une faute éliminatoire vaut 0 point sur son critère et perd l'épreuve, mais l'application ne fixe aucune grille : tout vient de la configuration.

⚠️ Après la correction de C9 (QCM → oral), l'IA n'a pas de témoignage sur le déroulement réel de l'épreuve de conduite : la structure « critères, points, faute éliminatoire, observation » est une hypothèse de travail. Le dev 1, qui vient de passer l'examen, est la meilleure source : à décrire avant C11.

⚠️ Pendant la vérification, l'inscription à une seconde session le même jour a été refusée par la règle de conflit de créneaux (C6) : comportement voulu, mais il oblige à planifier théorie et conduite à des dates ou heures différentes.

- Mon verdict (accepte / rejette / nuance) et pourquoi : <à compléter par le dev 1>.

---

## Entrée 14 — 17/09/2026 — Étape C12a, historique des modifications (dev 2)

- Code soumis : `metier/FiltresHistorique.kt`, `ui/historique/HistoriqueViewModel.kt`, `EcranHistorique.kt`, `graphHistorique` dans `Navigation.kt`.
- Remarque principale de l'IA : docs/05 prévoit une route `historique/{entite}/{entiteId}`, mais chaque fiche affiche déjà son historique avec `LigneHistorique` ; l'IA propose de ne pas doubler cette vue et de faire l'inverse, ouvrir la fiche depuis l'écran global. Pour ouvrir une fiche gérée par un sous-parcours (`viewModelDuSousParcours`), il faut d'abord empiler la liste racine, sinon `getBackStackEntry` échoue.

⚠️ Le filtre « Objet » du `SelecteurChoix` attend un identifiant entier : l'IA utilise la position de l'entité dans la liste des objets présents. Ça marche, mais c'est un détour ; une variante du sélecteur acceptant une chaîne serait plus directe. À discuter.

⚠️ La date du jour est passée en paramètre aux fonctions de `FiltresHistorique` pour que les tests soient reproductibles, comme le `Random` injecté en C9.

- Mon verdict (accepte / rejette / nuance) et pourquoi : <à compléter par le dev 2>.

---

## Entrée 15 — <date> — <tâche>

- Fonction/Code soumis :
- Remarque principale de l'IA :
- Mon verdict (accepte / rejette / nuance) et pourquoi :
