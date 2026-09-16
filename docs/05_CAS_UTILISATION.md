# Cas d'utilisation, flux et écrans — Projet ATT

> Étape A4 du plan. Ce document relie le cahier de cadrage (§5 parcours, §11 contraintes, §12 MVP) au modèle de données (`docs/03`) et aux écrans à construire. Chaque cas d'utilisation (UC) indique l'acteur, les préconditions, le flux nominal, les cas d'erreur et l'étape du plan qui l'implémente.
> Les règles marquées « (RegleConfig) » sont lues en base, jamais codées en dur. Les valeurs par défaut sont dans `docs/04`.

---

## 1. Parcours global

```
Auto-école                 Admin ATT                    Examinateur          Système / ATT
────────────────────────── ──────────────────────────── ──────────────────── ─────────────────────
UC04 crée le candidat
UC04 constitue le dossier
UC04 soumet le dossier ──▶ UC05 valide / refuse / incomplet
                           UC06 crée la session + créneaux
                           UC07 inscrit le candidat (ou UC07b : demande auto-école)
                           UC13 imprime convocations + liste d'appel
                           UC08 fait l'appel (présent / absent / retard)
                                                        UC09 ouvre la tentative
                                                        UC09 saisit l'évaluation ──▶ UC10 calcule le résultat
                           UC10 valide le résultat
                           UC11 échec → nouvelle tentative (règles de repassage)
UC12 consulte ses candidats  UC12 consulte tout            UC12 ses évaluations   Candidat : UC12 son parcours (facultatif)
                           UC13 imprime le relevé
                           UC14 consulte l'historique
```

Le projet s'arrête au résultat validé et imprimable. Rien n'est prévu pour le CIM.

---

## 2. Cas d'utilisation

### UC01 — Se connecter
| | |
|---|---|
| Acteur | tous les rôles |
| Précondition | un `Utilisateur` actif existe (le superadmin est créé au premier lancement) |
| Flux nominal | 1. Saisir identifiant et mot de passe. 2. Le système compare l'empreinte PBKDF2. 3. La session utilisateur (`SessionUtilisateur` : id, rôle, région/auto-école/candidat/examinateur liés) est portée par `ConnexionViewModel`. 4. Navigation vers l'accueil du rôle. |
| Erreurs | identifiant inconnu ou mot de passe faux → message générique « Identifiant ou mot de passe incorrect » ; compte inactif → « Compte désactivé ». |
| Déconnexion | retour à l'écran de connexion, pile de navigation vidée. |
| Étape | C1 |

### UC02 — Configurer les référentiels et les règles (Super Admin)
| | |
|---|---|
| Acteur | Super Admin |
| Objets | `Region`, `CategoriePermis`, `TypeEpreuve`, `Bareme` (versionné), `RegleConfig`, `Question`/`Reponse`, `CriterePratique`, `Centre` |
| Flux nominal | 1. Choisir le référentiel. 2. Lister, créer, modifier, désactiver. 3. Chaque modification écrit `Historique`. 4. Pour un barème déjà utilisé par un résultat : modification interdite, création d'une nouvelle version. |
| Erreurs | code de catégorie ou de région en doublon ; désactivation d'un élément référencé par une session ouverte → refus avec message ; valeur de règle du mauvais type. |
| Règle | tout élément créé par défaut au premier lancement porte `aConfirmer = true` et l'écran l'affiche avec un badge « À confirmer ». |
| Étape | C4 (dev 2), centres en C5 |

### UC03 — Gérer les auto-écoles et leurs comptes (Admin ATT)
| | |
|---|---|
| Acteur | Admin ATT (Super Admin aussi) |
| Flux nominal | 1. Lister les auto-écoles (filtre par région). 2. Créer : nom, région, agrément, adresse, téléphone. 3. Créer le compte `Utilisateur` de rôle AUTO_ECOLE lié. 4. Désactiver / réactiver. |
| Erreurs | nom en doublon dans la même région ; identifiant de compte déjà pris ; Admin ATT régional (RegleConfig) ne voit que sa région. |
| Étape | C2 |

### UC04 — Créer un candidat et constituer son dossier (Auto-école)
| | |
|---|---|
| Acteur | Auto-école (Admin ATT peut le faire pour elle : candidat sans smartphone, auto-école sans accès) |
| Précondition | auto-école active |
| Flux nominal | 1. Créer le candidat (nom, prénom, date de naissance, CIN facultatif, téléphone, adresse) rattaché à l'auto-école. 2. Ouvrir un dossier pour une catégorie. 3. Cocher les pièces fournies (liste de pièces du référentiel, Q7). 4. Soumettre : statut `SOUMIS`, date, `Historique`. |
| Erreurs | candidat déjà existant (même nom + prénom + date de naissance) → avertissement ; âge < âge minimum de la catégorie (référentiel) → refus de soumission avec message ; dossier déjà `VALIDE` pour cette catégorie → pas de nouveau dossier ; catégorie exigeant un permis préalable (C/D/E → B) → pièce « copie permis B » obligatoire. |
| Étape | C3 |

### UC05 — Valider, refuser ou déclarer incomplet un dossier (Admin ATT)
| | |
|---|---|
| Acteur | Admin ATT |
| Précondition | dossier `SOUMIS` |
| Flux nominal | 1. Lister les dossiers soumis (filtre région, auto-école, catégorie). 2. Ouvrir le dossier, voir les pièces. 3. Décider : `VALIDE`, `INCOMPLET` (motif, retour à l'auto-école qui complète et resoumet), `REFUSE` (motif). 4. `Historique` avec décideur, date, motif. |
| Erreurs | décision sans motif pour incomplet/refus → bloqué ; dossier modifié par l'auto-école pendant la décision → relecture (dernier état en base). |
| Contrainte §11 | « Dossier incomplet/refusé », « Candidat non éligible ». |
| Étape | C3 |

### UC06 — Créer une session et ses créneaux (Admin ATT)
| | |
|---|---|
| Acteur | Admin ATT |
| Précondition | centre actif dans la région ; catégorie et type d'épreuve actifs |
| Flux nominal | 1. Saisir catégorie, épreuve, centre, date, heure de convocation, capacité, durée de créneau et marge (préremplies depuis RegleConfig). 2. Le système génère les créneaux : `capacite / capaciteCreneau` créneaux successifs à partir de l'heure de convocation, avec `heureFinEstimee`. 3. Statut `PLANIFIEE` puis `OUVERTE`. 4. `Historique`. |
| Erreurs | date passée ; capacité ≤ 0 ; même centre, même date, même heure → avertissement de conflit (§11 « Conflit de créneaux ») ; annulation d'une session avec inscrits → toutes les inscriptions passent `ANNULE` avec `Historique`, et la liste des candidats à réinscrire est proposée. |
| Étape | C5 (dev 2) |

### UC07 — Inscrire un candidat à une session (Admin ATT)
| | |
|---|---|
| Acteur | Admin ATT ; variante UC07b : l'auto-école **demande** l'inscription si `AUTO_ECOLE_PEUT_INSCRIRE = true` (RegleConfig, Q8), statut `DEMANDE` que l'ATT confirme |
| Précondition | dossier `VALIDE` de la même catégorie ; session `OUVERTE` |
| Flux nominal | 1. Depuis la session, chercher un candidat éligible (ou depuis le candidat, choisir une session). 2. Contrôles (fonctions pures `metier/ReglesPlanification`) : capacité de la session et du créneau, pas de double inscription, pas de session le même jour qui se chevauche, région (RegleConfig `EXAMEN_DANS_REGION_AUTO_ECOLE`), délai de repassage écoulé (RegleConfig), théorie réussie si épreuve conduite et `CONDUITE_APRES_THEORIE_REUSSIE`. 3. Affecter un créneau (premier disponible ou choisi). 4. Générer `numeroAnonymat` et `heurePassageEstimee`. 5. Statut `INSCRIT`, `Historique`. |
| Erreurs | session complète → refus + proposition de la prochaine session du centre ; double inscription → refus ; candidat non éligible → message précisant la règle qui bloque. |
| Report / annulation | `REPORTE` (vers une autre session, nouvelle inscription liée) ou `ANNULE`, toujours avec motif et `Historique`. |
| Contraintes §11 | « Session complète », « Conflit de créneaux », « Report », « Annulation ». |
| Étape | C6 |

### UC08 — Faire l'appel et gérer la présence (Admin ATT)
| | |
|---|---|
| Acteur | Admin ATT (examinateur : à confirmer, Q6) |
| Précondition | session le jour J, statut `OUVERTE` → passe `EN_COURS` à l'ouverture de l'appel |
| Flux nominal | 1. Liste d'appel triée par créneau puis numéro d'anonymat. 2. Pour chaque inscrit : `PRESENT` (heure d'arrivée), `ABSENT`, `EN_RETARD` (arrivée > convocation + `TOLERANCE_RETARD_MIN`). 3. Réorganisation : selon `REGLE_ABSENCE`, l'absent est reporté automatiquement ou doit être réinscrit ; les créneaux libérés sont proposés aux retardataires. 4. Passage : `EN_COURS` → `TERMINE`. 5. `Historique` à chaque changement. |
| Erreurs | présence modifiée après `TERMINE` → uniquement par correction tracée avec motif ; appel sur une session d'un autre jour → avertissement. |
| Panne réseau/électricité (§10) | la liste d'appel imprimée (UC13) sert de secours ; la saisie se fait ensuite dans l'application, qui fonctionne sans réseau. |
| Étape | C7 |

### UC09 — Ouvrir une tentative et saisir l'évaluation (Examinateur)
| | |
|---|---|
| Acteur | Examinateur (Admin ATT en secours) |
| Précondition | inscription avec présence `PRESENT` ou `EN_RETARD` accepté ; pas d'affectation préalable examinateur → candidat |
| Flux nominal | 1. L'examinateur choisit la session puis un candidat présent (identifié par son numéro d'anonymat). 2. Le système crée la `Tentative` n° = max + 1 pour (candidat, épreuve), `examinateurId` = utilisateur courant, présence → `EN_COURS`. 3. **Théorie** : pour chaque `Question` active de l'épreuve, choisir la `Reponse` du candidat (ou « sans réponse ») ; ou saisie directe du nombre de points si la procédure est papier (Q1). 4. **Conduite** : pour chaque `CriterePratique` actif, note, faute éliminatoire, observation. 5. Observations générales, puis « Terminer » : `Evaluation` figée avec `baremeId` courant, tentative `TERMINEE`, présence `TERMINE`. 6. Le calcul (UC10) s'enchaîne. |
| Erreurs | tentative déjà ouverte pour ce candidat et cette épreuve aujourd'hui → reprise, pas de doublon ; `TENTATIVES_MAX` atteint → refus ; aucune question/critère actif → refus avec renvoi à la configuration ; examinateur indisponible (§11) → tout examinateur connecté peut saisir, aucune affectation n'est requise. |
| Étape | C8 (tentative), C9 (théorie), C10 (conduite) |

### UC10 — Calculer et valider le résultat
| | |
|---|---|
| Acteur | Système, puis Admin ATT |
| Flux nominal | 1. `metier/CalculResultat` : somme des points des réponses correctes (théorie) ou des notes par critère (conduite) ; `reussi = note ≥ seuil` du barème référencé **et** aucune faute éliminatoire. 2. Création d'un `Resultat` `CALCULE` (note, noteMax, seuil copiés). 3. L'Admin ATT relit et passe `VALIDE_ATT` ; `Historique`. 4. Le résultat validé devient visible pour l'auto-école et le candidat. |
| Correction (§11 « Erreur de notation ») | l'Admin ATT crée un nouveau `Resultat` `CORRIGE` qui référence l'ancien (`remplaceResultatId`) avec motif ; l'ancien reste en base et lisible ; `Historique`. |
| Erreurs | barème sans seuil → refus de calcul ; tentative non terminée → pas de calcul. |
| Étape | C11 |

### UC11 — Échec et nouvelle tentative
| | |
|---|---|
| Acteur | Admin ATT / Auto-école (constat), Système (règles) |
| Flux nominal | 1. Résultat `reussi = false` validé. 2. `metier/ReglesTentatives` calcule les épreuves à repasser : celles non réussies, plus celles réussies dont la conservation (`CONSERVATION_EPREUVE_REUSSIE_JOURS`) est expirée. 3. Le candidat redevient inscriptible à une session de cette épreuve à partir de `date + DELAI_REPASSAGE_JOURS`. 4. Le parcours du candidat affiche toutes les tentatives, jamais écrasées. |
| Erreurs | `TENTATIVES_MAX` atteint → statut « à confirmer avec l'ATT » affiché, pas de blocage définitif codé en dur. |
| Étape | C11 |

### UC12 — Consulter
| Acteur | Ce qu'il voit | Étape |
|---|---|---|
| Candidat (compte facultatif) | son parcours : dossier, inscriptions, convocations, tentatives, résultats validés | C12 |
| Auto-école | ses candidats, leurs dossiers, inscriptions, résultats validés ; filtres par statut | C12 |
| Examinateur | les sessions du jour, les candidats présents (numéro d'anonymat), ses évaluations | C12 |
| Admin ATT | tout, filtré par région si régional ; tableaux : dossiers à traiter, sessions à venir, résultats à valider | C12 |
| Super Admin | tout + configuration + historique | C12 |

### UC13 — Imprimer
| Document | Contenu | Qui | Étape |
|---|---|---|---|
| Convocation individuelle | candidat, catégorie, épreuve, centre, adresse, date, heure de convocation, heure de passage estimée, numéro d'anonymat, pièces à apporter | Admin ATT, Auto-école | C13 |
| Liste d'appel de session | créneaux, numéros d'anonymat, noms (version ATT) ou numéros seuls (version examinateur), colonnes présence à cocher | Admin ATT | C13 |
| Liste des admis d'une session | numéros/noms, épreuve, réussite | Admin ATT | C13 |
| Relevé de résultat | candidat, tentative n°, épreuve, note/noteMax/seuil, réussi, date de validation, mention « document interne, sans valeur officielle tant que la procédure ATT n'est pas confirmée » | Admin ATT, Auto-école | C13 |

Mécanisme : page HTML générée → `WebView` → `PrintManager` (impression ou enregistrement PDF). Fonctionne sans réseau.

### UC14 — Consulter l'historique
| | |
|---|---|
| Acteur | Super Admin, Admin ATT |
| Flux nominal | liste des entrées `Historique` filtrable par entité, utilisateur, période ; depuis un dossier, un résultat ou une session, bouton « Historique » filtré sur cet objet. |
| Étape | B2 (écriture), C12 (écran) |

---

## 3. Contraintes du cadrage §11 → cas d'utilisation

| Contrainte | UC | Traitement |
|---|---|---|
| Dossier incomplet/refusé | UC05 | statuts + motif + retour auto-école |
| Candidat non éligible | UC04, UC07 | âge, dossier validé, catégorie, délai, théorie préalable (règles configurables) |
| Session complète | UC07 | refus + proposition de la prochaine session |
| Absence, retard, report, annulation | UC07, UC08 | statuts + règles configurables + historique |
| Erreur de notation, correction traçable | UC10 | nouveau résultat `CORRIGE`, ancien conservé |
| Échec et nouvelle tentative | UC11 | tentative n+1, épreuves à repasser |
| Examinateur indisponible | UC09 | aucune affectation préalable ; tout examinateur peut saisir |
| Conflit de créneaux | UC06, UC07 | avertissement centre/date/heure ; pas deux inscriptions qui se chevauchent |
| Candidat sans smartphone | UC04, UC12, UC13 | tout est saisi par l'auto-école/ATT ; convocation imprimée |
| Panne réseau/électricité | UC08, UC13 | base locale ; listes imprimées avant la session |

---

## 4. Écrans et routes

Une seule `MainActivity`, un `NavHost`, routes en chaînes ; l'argument est toujours un identifiant `Int`. Les écrans reçoivent des lambdas, jamais le `navController`. Nommage : fichier `EcranXxx.kt`, ViewModel `XxxViewModel`, état `EtatXxx`.

### 4.1 Communs
| Route | Écran | Rôles | UC |
|---|---|---|---|
| `connexion` | `EcranConnexion` | tous | UC01 |
| `accueil` | `EcranAccueil` (menu selon le rôle, compteurs : dossiers à traiter, sessions du jour, résultats à valider) | tous | UC12 |
| `historique` / `historique/{entite}/{entiteId}` | `EcranHistorique` | Super Admin, Admin ATT | UC14 |

### 4.1 bis Comptes (étape C1b)
| Route | Écran | Rôles | UC |
|---|---|---|---|
| `examinateurs`, `examinateur/nouveau`, `examinateur/{examinateurId}` | `EcranListeExaminateurs`, `EcranFormulaireExaminateur` (création avec compte, modification, désactivation) | Super Admin, Admin ATT | UC01 |
| `comptes`, `compte/nouvel-admin` | `EcranComptes`, `EcranFormulaireAdmin` (administrateur national ou régional) | Super Admin | UC01 |
| `mot-de-passe` | `EcranMotDePasse` | tous | UC01 |

### 4.2 Configuration (dev 2)
| Route | Écran | UC |
|---|---|---|
| `config` | `EcranConfiguration` (liste des référentiels) | UC02 |
| `config/regions`, `config/categories`, `config/epreuves`, `config/regles`, `config/centres` | `EcranListeReferentiel` générique + `EcranFormulaireReferentiel` | UC02 |
| `config/baremes/{typeEpreuveId}` | `EcranBaremes` (versions, création d'une version) | UC02 |
| `config/questions/{typeEpreuveId}` | `EcranQuestions` + `question/{questionId}` (réponses) | UC02 |
| `config/criteres/{typeEpreuveId}` | `EcranCriteres` | UC02 |

### 4.3 Auto-écoles, candidats, dossiers
| Route | Écran | UC |
|---|---|---|
| `autoecoles` | `EcranListeAutoEcoles` (filtre région) | UC03 |
| `autoecole/nouvelle`, `autoecole/{autoEcoleId}` | `EcranFormulaireAutoEcole` (+ compte) | UC03 |
| `candidats` | `EcranListeCandidats` (auto-école : les siens ; ATT : filtres) | UC04, UC12 |
| `candidat/nouveau`, `candidat/{candidatId}/modifier` | `EcranFormulaireCandidat` | UC04 |
| `candidat/{candidatId}` | `EcranDetailCandidat` (identité, dossiers, inscriptions, tentatives, résultats = parcours) | UC12 |
| `dossier/nouveau/{candidatId}`, `dossier/{dossierId}` | `EcranDossier` (pièces, soumission ; côté ATT : décision + motif) | UC04, UC05 |
| `dossiers` | `EcranDossiersATraiter` (ATT : soumis, filtres) | UC05 |

### 4.4 Sessions, inscriptions, présence
| Route | Écran | UC |
|---|---|---|
| `sessions` | `EcranListeSessions` (à venir / du jour / passées, filtre région et centre) | UC06, UC12 |
| `session/nouvelle` | `EcranFormulaireSession` (génération des créneaux) | UC06 |
| `session/{sessionId}` | `EcranDetailSession` (créneaux, inscrits, statut, actions : inscrire, appel, imprimer, annuler) | UC06 |
| `session/{sessionId}/inscrire` | `EcranInscription` (recherche candidat éligible, contrôles, créneau) | UC07 |
| `session/{sessionId}/appel` | `EcranAppel` (présence par créneau) | UC08 |

### 4.5 Évaluation et résultats
| Route | Écran | UC |
|---|---|---|
| `evaluation` | `EcranSessionsExaminateur` (sessions du jour, candidats présents par numéro) | UC09 |
| `tentative/{tentativeId}/theorie` | `EcranEvaluationTheorie` (questions/réponses ou points) | UC09 |
| `tentative/{tentativeId}/conduite` | `EcranEvaluationConduite` (critères, fautes, observations) | UC09 |
| `resultats` | `EcranResultatsAValider` (ATT) | UC10 |
| `resultat/{resultatId}` | `EcranDetailResultat` (détail du calcul, valider, corriger avec motif, historique) | UC10, UC11 |

### 4.6 Impression
| Route | Écran | UC |
|---|---|---|
| `impression/convocation/{inscriptionId}` | `EcranImpression` (aperçu HTML + bouton Imprimer) | UC13 |
| `impression/appel/{sessionId}` | idem, liste d'appel | UC13 |
| `impression/admis/{sessionId}` | idem, liste des admis | UC13 |
| `impression/releve/{resultatId}` | idem, relevé de résultat | UC13 |

Total : 27 routes, environ 22 fichiers d'écran (les écrans génériques de référentiel et d'impression sont réutilisés).

---

## 5. Navigation par rôle (menu de l'accueil)

| Rôle | Entrées du menu |
|---|---|
| Super Admin | Configuration · Auto-écoles · Candidats · Dossiers à traiter · Sessions · Résultats à valider · Historique |
| Admin ATT | Auto-écoles · Candidats · Dossiers à traiter · Sessions (créer, inscrire, appel, imprimer) · Résultats à valider · Historique |
| Auto-école | Mes candidats (créer, dossiers) · Mes inscriptions et convocations · Résultats de mes candidats |
| Examinateur | Sessions du jour · Mes évaluations |
| Candidat | Mon parcours (dossier, convocation, résultats) |

Un écran ouvert par une route n'affiche que les actions autorisées par le rôle de `SessionUtilisateur` ; une route appelée sans droit renvoie à l'accueil.

---

## 6. ViewModels prévus (un par fonctionnalité)

`ConnexionViewModel` (session utilisateur, partagé au-dessus du `NavHost`), `ConfigurationViewModel`, `AutoEcolesViewModel`, `CandidatsViewModel`, `DossiersViewModel`, `SessionsViewModel`, `InscriptionViewModel`, `AppelViewModel`, `EvaluationViewModel`, `ResultatsViewModel`, `HistoriqueViewModel`, `ImpressionViewModel`. Chacun : `AndroidViewModel`, `StateFlow<EtatXxx>`, `combine`/`stateIn` sur les `Flow` Room, écritures dans `viewModelScope.launch`, contrôles métier délégués à `metier/`.
