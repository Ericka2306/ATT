# CLAUDE.md — Projet ATT

Application Android (Kotlin, Jetpack Compose, Room) de gestion des examens du permis de conduire pour l'ATT (Agence des Transports Terrestres, Madagascar). Projet universitaire M1 en binôme. **Répondre et commenter en français.**

## Documents de référence (lire avant toute implémentation)
- `docs/01_REGLES_DE_DEVELOPPEMENT.md` — règles impératives, pile technique, architecture, conventions, permissions, protocole IA.
- `docs/02_PLAN_DE_TRAVAIL.md` — étapes ordonnées, livrables et critères de fin d'étape. **Ne faire qu'une étape à la fois.**
- `docs/03_MODELE_DE_DONNEES.md` — entités, statuts, invariants (proposition à valider).
- `docs/04_QUESTIONS_A_VALIDER.md` — règles administratives inconnues : ne jamais les inventer.
- `docs/HORS_COURS.md` — toute notion non vue en cours doit y être expliquée avant usage.
- `docs/references/` — cahier de cadrage, instructions, synthèses des cours et des projets d'exemple.

## Règles absolues
1. Périmètre ATT uniquement : pas de CIM, pas de notifications, pas de paiement, pas de réseau réel.
2. Aucun barème, seuil, catégorie, délai, capacité ou règle de repassage codé en dur : tout est en base (`RegleConfig`, `Bareme`), avec `aConfirmer = true` si la valeur n'est pas validée par l'ATT.
3. Ne jamais inventer une règle administrative : ajouter la question dans `docs/04_QUESTIONS_A_VALIDER.md` et continuer avec une configuration par défaut.
4. Ne jamais écraser ni supprimer une tentative ou un résultat : nouvelle ligne + `Historique`.
5. Le candidat peut ne pas avoir de smartphone : tout doit être faisable par l'ATT ou l'auto-école, avec impression.
6. Pas d'affectation préalable candidat → examinateur.
7. Application pour Madagascar uniquement ; centres et auto-écoles rattachés à une région.
8. Rester dans les technologies du cours (Compose, Navigation Compose, ViewModel/StateFlow, Room/KSP, coroutines). Interdits : Hilt, Retrofit, WorkManager, LiveData, `!!`, `runBlocking`, `kapt`, `MutableStateFlow` exposé, `navController` passé aux écrans.
9. Modèle de données d'abord, écrans ensuite. Une étape du plan à la fois, puis compte rendu : créé / modifié / tests / décisions restantes.
10. Pas de fonctionnalité hors périmètre sans accord du binôme.

## Pile et conventions (résumé)
- AGP 9.3.2 (Kotlin intégré, pas de plugin `kotlin.android`, pas de `kotlinOptions`), plugin Compose + KSP alignés sur la version Kotlin embarquée, Java 17, minSdk 24, compileSdk 37.
- Package `mg.itu.att`. Une seule `MainActivity`, `MaterialTheme` par défaut, Navigation Compose avec routes chaînes et identifiants en argument.
- Architecture : `Ecran*` → `XxxViewModel` (`AndroidViewModel`, `StateFlow<EtatXxx>`, `combine`/`stateIn`, `viewModelScope.launch`) → `XxxDao` (`Flow` pour lire, `suspend` pour écrire) → `AppDatabase.obtenir()`. Logique métier dans `metier/` (fonctions pures testées JUnit).
- Identifiants en français sans accents, KDoc court en français, textes d'UI en français, tables au pluriel, statuts en `enum class`, dates en `String` ISO.
- Arborescence : `data/`, `metier/`, `ui/<fonctionnalite>/`, `securite/`, `Navigation.kt`.

## Commandes
```bash
./gradlew :app:assembleDebug          # build
./gradlew :app:testDebugUnitTest      # tests unitaires (metier/)
./gradlew :app:installDebug           # installer sur l'émulateur
```

## Début et fin de chaque étape (automatique, sans qu'on ait à le demander)
**Au début** : créer la branche `etape-XX-nom` depuis `main` à jour, passer la ligne de l'étape à 🟡 dans `docs/02_PLAN_DE_TRAVAIL.md` et remplir « Étape en cours » du tableau « Où en est-on ? ».

**À la fin**, dans cet ordre :
1. Build et tests verts, vérification manuelle sur émulateur.
2. Compte rendu dans `docs/COMPTES_RENDUS.md` (créé / modifié / tests / décisions restantes).
3. Entrée dans `JOURNAL-IA.md` si l'IA a produit ou critiqué du code.
4. `docs/02_PLAN_DE_TRAVAIL.md` : ligne de l'étape à 🟢 avec la date, tableau « Où en est-on ? » mis à jour (dernière terminée, en cours, prochaine, qui).
5. **Ne pas commiter, ne pas pousser.** Laisser toutes les modifications non commitées dans l'arbre de travail : le dev les relit dans l'onglet Git d'Android Studio (ou `git diff`) avant toute publication.
6. Terminer le compte rendu par une section « À relire » : la liste des fichiers modifiés avec, pour chacun, une ligne sur ce qui a changé. Puis attendre.
7. Quand le dev dit « ok » (ou « commit », « pousse ») : commit en français à l'impératif sur la **branche de l'étape** (`etape-XX-nom`, créée au début de l'étape), puis `git push origin <branche>` et ouverture d'une pull request vers `main` avec `gh pr create`. **Jamais de commit ni de push directement sur `main`** : la fusion se fait dans la pull request, par le dev, après relecture.

Le tableau « Où en est-on ? » est la seule source de vérité de l'avancement pour les deux devs. Ne jamais terminer une étape sans l'avoir mis à jour.
