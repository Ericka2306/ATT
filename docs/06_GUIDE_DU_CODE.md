# Guide du code — à lire avant la soutenance

But de ce document : pouvoir ouvrir n'importe quel fichier du projet et dire en une phrase ce qu'il fait et pourquoi il est là (critère de fin de l'étape D2). Il ne remplace pas le code : il en donne la carte et l'ordre de lecture.

---

## 1. Le chemin d'une action, du bouton à la base

Toujours le même, dans les quatre couches vues en cours (S6 et S7) :

```
Ecran*  (Compose)  →  *ViewModel  (StateFlow)  →  *Dao  (Room)  →  base att.db
   ↑                      │
   └── uiState.collectAsState()
```

**Exemple à savoir raconter : « l'ATT valide un dossier ».**

1. [ui/candidats/EcranDossier.kt](../app/src/main/java/mg/itu/att/ui/candidats/EcranDossier.kt) affiche le dossier et le bouton « Valider ». L'écran ne décide rien : il appelle `vm.decider(...)`.
2. [ui/candidats/CandidatsViewModel.kt](../app/src/main/java/mg/itu/att/ui/candidats/CandidatsViewModel.kt) vérifie le motif, puis écrit.
3. L'écriture passe par [data/Tracage.kt](../app/src/main/java/mg/itu/att/data/Tracage.kt) : `db.withTransaction { … tracer(...) }`. La modification et sa trace sont enregistrées ensemble, ou pas du tout.
4. [data/DossiersDao.kt](../app/src/main/java/mg/itu/att/data/DossiersDao.kt) fait le `UPDATE`, [data/HistoriqueDao.kt](../app/src/main/java/mg/itu/att/data/HistoriqueDao.kt) ajoute la ligne d'historique.
5. Le `Flow` du DAO émet la nouvelle valeur, le `StateFlow` du ViewModel la recompose, l'écran se met à jour tout seul. Personne ne « rafraîchit » l'écran à la main.

**Trois règles de lecture qui expliquent presque tout le code :**

- *Une décision métier n'est jamais dans un écran ni dans un ViewModel.* Elle est dans `metier/`, en fonction pure, sans Android, donc testable par JUnit. Un ViewModel rassemble les faits, la fonction juge. Voir `ReglesInscription.verifier(ContexteInscription)`.
- *Aucun barème, seuil, délai ou âge n'est écrit dans le code* (règle R3). Tout est lu en base par [data/Regles.kt](../app/src/main/java/mg/itu/att/data/Regles.kt) et affiché avec un badge « À confirmer » tant que l'ATT ne l'a pas validé.
- *On n'écrase jamais une tentative ni un résultat* (règle R6). Une correction ajoute une ligne et marque l'ancienne « Remplacé ». C'est pour cela qu'il n'y a **aucun** `@Delete` dans [data/ExamensDao.kt](../app/src/main/java/mg/itu/att/data/ExamensDao.kt).

---

## 2. Carte des fichiers

### `data/` — la base et ce qu'on y met (14 fichiers)

| Fichier | Ce qu'il contient |
|---|---|
| `AppDatabase.kt` | la base Room : la liste des entités, la version, et le singleton `obtenir(context)` |
| `EntitesActeurs.kt` | utilisateurs (comptes), auto-écoles, candidats, examinateurs, régions |
| `EntitesReferentiels.kt` | catégories, épreuves, barèmes, règles configurables, questions, critères |
| `EntitesDossiers.kt` | dossiers d'un candidat et pièces fournies |
| `EntitesPlanification.kt` | centres, sessions, créneaux, inscriptions, présences |
| `EntitesExamens.kt` | tentatives, évaluations (théorie et conduite), résultats |
| `EntitesHistorique.kt` | la table d'historique : qui a fait quoi, quand, sur quel objet |
| `ActeursDao.kt`, `ReferentielsDao.kt`, `DossiersDao.kt`, `PlanificationDao.kt`, `ExamensDao.kt`, `HistoriqueDao.kt` | les requêtes. Règle du cours : `Flow` pour suivre une donnée, `suspend` pour une question ponctuelle ou une écriture |
| `Regles.kt` | lecture typée d'une règle configurable ; la règle d'une catégorie prime sur la règle globale |
| `Tracage.kt` | `tracer(...)` : la ligne d'historique, écrite dans la même transaction que la modification |
| `CalculEnBase.kt` | rassemble la saisie et le barème, appelle `CalculResultat`, enregistre le résultat sans rien écraser |
| `DonneesInitiales.kt` | ce qui est inséré au premier lancement : 24 régions, 2 comptes, valeurs d'exemple « à confirmer » |

### `metier/` — les décisions, en fonctions pures (18 fichiers)

Aucun import Android. Chaque fichier a ses tests dans `app/src/test/`.

| Fichier | La question à laquelle il répond |
|---|---|
| `ReglesDossier.kt` | ce candidat a-t-il l'âge ? a-t-il déjà un dossier en cours ? |
| `ReglesPlanification.kt` | quels créneaux pour cette session ? y a-t-il un conflit le même jour ? |
| `ReglesInscription.kt` | ce candidat peut-il être inscrit ici ? (dossier, capacité, délai, conflit, prérequis) |
| `ReglesPresence.kt` | présent, en retard ou absent ? que fait-on d'un absent ? |
| `ReglesTentatives.kt` | peut-on ouvrir un passage ? quel numéro porte-t-il ? |
| `ReglesTheorie.kt`, `ReglesConduite.kt` | la forme d'une saisie d'épreuve (mode de saisie, ligne de grille) |
| `Points.kt` | ce que l'examinateur a tapé est-il un nombre valable ? |
| `CalculResultat.kt` | la note, rapportée au barème, et réussi ou non |
| `ReglesRepassage.kt` | après un échec : quelles épreuves, à partir de quelle date |
| `ReglesConsultation.kt` | qui a le droit de voir ou de gérer ce candidat |
| `FiltresHistorique.kt` | qui lit l'historique, et comment on le filtre |
| `DocumentsImpression.kt` | le HTML des quatre documents imprimables |
| `ValidationCandidat.kt`, `ValidationAutoEcole.kt`, `ValidationCompte.kt`, `ValidationConfiguration.kt` | la saisie des formulaires est-elle complète et bien formée |
| `Dates.kt` | dates et heures en texte ISO, sans bibliothèque externe |

### `ui/` — un dossier par fonctionnalité

Dans chaque dossier : un `*ViewModel.kt` et un ou plusieurs `Ecran*.kt`. Un fichier `Statuts.kt` porte les libellés et les pastilles de couleur du domaine.

| Dossier | Écrans | Cas d'utilisation |
|---|---|---|
| `connexion/` | `EcranConnexion` | UC01 — se connecter ; `SessionUtilisateur` est l'objet passé à tous les autres ViewModels |
| `accueil/` | `EcranAccueil`, `MenuParRole` | le menu dépend du rôle connecté (fonction pure, testée) |
| `configuration/` | catégories, centres, épreuve, règles | UC02 — le Super Admin configure, rien n'est codé en dur |
| `autoecoles/` | liste, fiche, formulaire, compte | UC03 |
| `candidats/` | liste, fiche, dossier, dossiers à traiter, parcours | UC04, UC05, UC12 |
| `sessions/` | liste, fiche, formulaire | UC06 |
| `inscriptions/` | `EcranInscriptions`, `EcranMesInscriptions` | UC07, UC12 |
| `appel/` | `EcranAppel` | UC08 — présent, en retard, absent |
| `evaluation/` | sessions de l'examinateur, tentatives, théorie, conduite | UC09 |
| `resultats/` | liste, détail | UC10, UC11 — calcul, validation ATT, correction traçable |
| `impression/` | `EcranImpression` | UC13 — aperçu `WebView` puis `PrintManager` |
| `historique/` | `EcranHistorique` | UC14 |
| `comptes/` | comptes, examinateurs, mot de passe | comptes de connexion, UC03 et UC09 |
| `communs/` | `Cadre`, `Champs`, `Selecteurs`, `Historique`, `Formats`, `NavigationCommune` | les briques reprises partout : `EcranStandard`, `CarteFiche`, `ChampTexte`, `viewModelDuSousParcours` |
| `theme/` | `Theme.kt` | la palette et la typographie |

### Le reste

| Fichier | Rôle |
|---|---|
| `MainActivity.kt` | une seule Activity, comme dans les projets du cours |
| `Navigation.kt` | toutes les routes ; un écran ne reçoit jamais le `navController`, seulement des lambdas |
| `securite/MotDePasse.kt` | empreinte PBKDF2 avec sel : un mot de passe n'est jamais stocké en clair |

---

## 3. Les questions probables en soutenance

**« Où est la logique métier ? »** Dans `metier/`, en fonctions pures. Montrer `ReglesInscription.verifier` et son test `ContraintesCadrageTest`.

**« Comment garantissez-vous qu'un résultat n'est jamais écrasé ? »** Aucun `@Delete` ni `@Update` sur `Resultat` dans `ExamensDao`. Une correction crée une ligne qui pointe sur la précédente ; l'ancienne passe en `ANNULE` et reste affichée. Montrer `CalculEnBase.enregistrerResultat`.

**« Et si l'ATT change le barème ? »** Rien à recompiler : le barème est une ligne en base, versionnée par date de validité. Un résultat garde le barème avec lequel il a été calculé. Écran : Configuration → catégorie → épreuve → barèmes.

**« Que se passe-t-il si le candidat n'a pas de téléphone ? »** Rien ne l'exige : l'auto-école et l'ATT font tout le parcours, et la convocation comme le relevé s'impriment. Le compte candidat est facultatif (`EcranFormulaireCompteCandidat`).

**« Pourquoi une transaction pour l'historique ? »** Pour qu'une modification sensible et sa trace soient enregistrées ensemble ou pas du tout : sinon on pourrait avoir une décision sans auteur. Montrer `Tracage.kt`.

**« Qu'est-ce que vous n'avez pas fait ? »** Le hors-périmètre (CIM, paiement, notifications, réseau) et les règles administratives non confirmées : elles sont listées dans `docs/04_QUESTIONS_A_VALIDER.md` et s'affichent « à confirmer » dans l'application au lieu d'être inventées.

---

## 4. Ce qu'il faut savoir expliquer dans le code lui-même

- **`combine` + `stateIn`** (S7) : plusieurs sources Room réunies en un seul état d'écran.
- **Un champ de texte ne passe jamais par un flux qui relit la base.** Sinon la frappe perd des caractères — défaut rencontré trois fois (C9, C11, D1). Les motifs et les saisies d'examen ont donc leur propre `MutableStateFlow` privé.
- **`viewModelDuSousParcours`** (`ui/communs/NavigationCommune.kt`) : liste, fiche et formulaire d'un même parcours partagent un ViewModel, obtenu sur l'entrée de pile de la route racine.
- **`flatMapLatest`** : quand l'identifiant affiché change, on abandonne l'ancien flux Room et on écoute le nouveau.
- Toutes les notions non vues en cours sont expliquées une par une dans [HORS_COURS.md](HORS_COURS.md), avec la raison de leur emploi.
