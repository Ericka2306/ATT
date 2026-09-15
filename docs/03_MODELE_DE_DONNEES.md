# Modèle de données ATT — proposition à valider

> Statut : **VALIDÉ le 15/09/2026 et implémenté à l'étape B1** (`app/src/main/java/mg/itu/att/data/Entites*.kt`). En cas d'écart, le code fait foi et ce document est mis à jour.
> Ajouts faits à l'implémentation : `aConfirmer` sur `CategoriePermis`, `TypeEpreuve`, `Bareme`, `Question`, `CriterePratique` (pas seulement `RegleConfig`) ; `categoriePrealableCode` sur `CategoriePermis` (C/D/E exigent B) ; `pointsSaisisDirectement` sur `Evaluation` (procédure papier, Q1) ; règle `CAPACITE_CRENEAU_DEFAUT`.
> Tout ce qui est marqué « À confirmer » vient d'une règle administrative inconnue : ce n'est jamais codé en dur, c'est une configuration.

Sources : cahier de cadrage §3 à §11, instructions §3 (entités minimales), précisions de l'équipe (application Madagascar uniquement, centres et auto-écoles rattachés à une région).

---

## 1. Vue d'ensemble

```
Region ──< Centre ──< Session ──< Creneau
   │                     │            │
   └──< AutoEcole ──< Candidat ──< Dossier ──< PieceDossier
                          │            │
                          │       CategoriePermis ──< TypeEpreuve ──< Bareme
                          │                                │    ├──< Question ──< Reponse
                          │                                │    └──< CriterePratique
                          │                                │
                          └──< Inscription (Session, Creneau?, Dossier) ── Presence
                                     │
                                     └──< Tentative (TypeEpreuve, Examinateur?)
                                              ├── Evaluation ──< ReponseCandidat / EvaluationCritere
                                              └──< Resultat (jamais écrasé)

Utilisateur (role) ── AutoEcole? / Candidat? / Examinateur? / Region?
RegleConfig (clé/valeur, par catégorie ou globale)
Historique (journal de toute modification sensible)
```

Conventions Room retenues (cf. règles de développement) :
- clés primaires `Int` auto-générées (`@PrimaryKey(autoGenerate = true) val id: Int = 0`) ;
- clés étrangères = champs `xxxId: Int` explicites (comme dans le cours), avec `@ForeignKey` + `@Index` déclarés sur l'entité (écart mineur au cours, expliqué dans les règles) ;
- dates en `String` ISO (`"2026-09-15"`, `"2026-09-15T08:30"`) ou `Long` epoch, formatées à la main ;
- statuts en `enum class` (Room les stocke en texte) ;
- entités et tables nommées en français sans accents, tables au pluriel.

---

## 2. Référentiels (configurés par le Super Admin)

### Region
Référentiel des régions de Madagascar. Le découpage exact utilisé par l'ATT (24 régions administratives depuis 2023, ou directions régionales de l'ATT) est **À confirmer** ; le jeu de données initial contiendra les 24 régions (liste dans `docs/references/RECHERCHE_ATT_ET_ANDROID.md` §A.8) avec possibilité de désactiver.

| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| code | String | ex. `ANA` (Analamanga) — unique |
| nom | String | |
| actif | Boolean | |

### CategoriePermis
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| code | String | `A`, `A'`, `B`, `C`, `D`, `E`, `F`… (liste exacte À confirmer) — unique |
| libelle | String | |
| ageMinimum | Int? | À confirmer, null = non renseigné |
| actif | Boolean | |

### TypeEpreuve
Une épreuve d'une catégorie (théorie, conduite ; extensible : manœuvres, circulation…).

| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| categorieId | Int | FK → CategoriePermis |
| code | String | `THEORIE`, `CONDUITE`, … |
| libelle | String | |
| ordre | Int | ordre de passage (1 = théorie) |
| obligatoire | Boolean | |
| dureeMinutes | Int? | durée par défaut d'un passage, À confirmer |
| actif | Boolean | |

### Bareme (versionné)
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| typeEpreuveId | Int | FK → TypeEpreuve |
| version | Int | incrémenté à chaque modification ; un résultat pointe toujours sur la version utilisée |
| noteMax | Double | À confirmer |
| seuilReussite | Double | À confirmer |
| dateDebutValidite | String | |
| dateFinValidite | String? | null = barème courant |

Règle : on ne modifie jamais un barème déjà utilisé par un résultat ; on crée une nouvelle version.

### Question / Reponse (épreuve théorique)
| Question | | |
|---|---|---|
| id | Int | PK |
| typeEpreuveId | Int | FK → TypeEpreuve |
| enonce | String | |
| points | Double | |
| actif | Boolean | une question désactivée reste en base (référencée par d'anciennes évaluations) |

| Reponse | | |
|---|---|---|
| id | Int | PK |
| questionId | Int | FK → Question |
| texte | String | |
| estCorrecte | Boolean | |

### CriterePratique (épreuve de conduite — structure seulement)
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| typeEpreuveId | Int | FK → TypeEpreuve |
| libelle | String | |
| points | Double | |
| eliminatoire | Boolean | faute éliminatoire éventuelle, À confirmer |
| actif | Boolean | |

Aucune donnée initiale : « ne rien inventer avant validation de l'ATT ». Le jeu de données de démonstration sera explicitement marqué « exemple ».

### RegleConfig
Règles génériques configurables, lues par le code au moment du calcul (jamais codées en dur).

| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| cle | String | ex. `TENTATIVES_MAX`, `DELAI_REPASSAGE_JOURS`, `CONSERVATION_EPREUVE_REUSSIE_JOURS`, `TOLERANCE_RETARD_MIN`, `REGLE_ABSENCE` (`REPORT_AUTO` / `NOUVELLE_INSCRIPTION`), `MARGE_CRENEAU_MIN`, `DUREE_CRENEAU_MIN`, `CAPACITE_SESSION_DEFAUT`, `EXAMEN_DANS_REGION_AUTO_ECOLE`, `CONDUITE_APRES_THEORIE_REUSSIE` |
| valeur | String | |
| typeValeur | Enum | `ENTIER`, `DECIMAL`, `BOOLEEN`, `TEXTE` |
| categorieId | Int? | null = règle globale ; sinon surcharge pour une catégorie |
| description | String | |
| aConfirmer | Boolean | true tant que la valeur n'a pas été validée par l'ATT |

### Centre
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| regionId | Int | FK → Region |
| nom | String | |
| adresse | String | |
| capaciteParDefaut | Int? | |
| actif | Boolean | |

---

## 3. Acteurs

### Utilisateur
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| identifiant | String | login, unique |
| motDePasseHash | String | jamais en clair (cf. règles : hachage) |
| nom | String | |
| role | Enum | `SUPER_ADMIN`, `ADMIN_ATT`, `AUTO_ECOLE`, `EXAMINATEUR`, `CANDIDAT` |
| regionId | Int? | Admin ATT régional (À confirmer) |
| autoEcoleId | Int? | si role = AUTO_ECOLE |
| examinateurId | Int? | si role = EXAMINATEUR |
| candidatId | Int? | si role = CANDIDAT (compte facultatif) |
| actif | Boolean | |

### AutoEcole
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| regionId | Int | FK → Region |
| nom | String | |
| numeroAgrement | String? | À confirmer |
| adresse | String | |
| telephone | String? | |
| actif | Boolean | |

### Candidat
Existe **sans** compte utilisateur (smartphone non obligatoire).

| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| autoEcoleId | Int | FK → AutoEcole |
| nom | String | |
| prenom | String | |
| dateNaissance | String | ISO |
| cin | String? | À confirmer (donnée stockable ?) |
| telephone | String? | |
| adresse | String? | |
| actif | Boolean | |

### Examinateur
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| nom | String | |
| matricule | String? | |
| regionId | Int? | |
| actif | Boolean | |

Pas d'affectation préalable examinateur → candidat en V1 : l'examinateur est renseigné sur la Tentative au moment de la saisie.

---

## 4. Dossier

### Dossier
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| candidatId | Int | FK → Candidat |
| categorieId | Int | FK → CategoriePermis |
| statut | Enum | `BROUILLON`, `SOUMIS`, `INCOMPLET`, `VALIDE`, `REFUSE` |
| motif | String? | motif d'incomplétude/refus |
| dateSoumission | String? | |
| dateDecision | String? | |
| decideParId | Int? | FK → Utilisateur (Admin ATT) |

### PieceDossier
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| dossierId | Int | FK → Dossier |
| typePiece | String | liste des pièces À confirmer (configurable via TypePiece ou RegleConfig) |
| fournie | Boolean | |
| remarque | String? | |

Aucun document n'est stocké (pas de scan/photo) tant que « données/documents que l'ATT peut stocker » n'est pas confirmé.

---

## 5. Planification

### Session
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| categorieId | Int | FK → CategoriePermis |
| typeEpreuveId | Int | FK → TypeEpreuve (une session = une épreuve ; À confirmer si une session couvre plusieurs épreuves) |
| centreId | Int | FK → Centre (la région est celle du centre) |
| date | String | ISO |
| heureConvocation | String | `"07:30"` |
| capacite | Int | |
| dureeCreneauMin | Int | depuis RegleConfig par défaut |
| margeMin | Int | idem |
| statut | Enum | `PLANIFIEE`, `OUVERTE`, `COMPLETE`, `EN_COURS`, `TERMINEE`, `ANNULEE` |
| creeParId | Int | FK → Utilisateur |

### Creneau
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| sessionId | Int | FK → Session |
| ordre | Int | |
| heureDebut | String | |
| heureFinEstimee | String | |
| capacite | Int | 1 = créneau individuel (possibilité réglementaire À confirmer) |

### Inscription
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| candidatId | Int | FK → Candidat |
| dossierId | Int | FK → Dossier (doit être VALIDE) |
| sessionId | Int | FK → Session |
| creneauId | Int? | FK → Creneau |
| statut | Enum | `DEMANDE` (par l'auto-école, si autorisé — Q8), `INSCRIT`, `CONFIRME`, `REPORTE`, `ANNULE` |
| dateInscription | String | |
| heurePassageEstimee | String? | |
| numeroAnonymat | String | numéro d'appel anonyme par session (pratique ATT anti-corruption, Q4) ; l'examinateur ne voit que ce numéro |

Contraintes : `UNIQUE(candidatId, sessionId)` ; nombre d'inscriptions actives ≤ capacité de la session et du créneau.

### Presence
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| inscriptionId | Int | FK → Inscription, unique |
| statut | Enum | `EN_ATTENTE`, `PRESENT`, `ABSENT`, `EN_RETARD`, `EN_COURS`, `TERMINE` |
| heureAppel | String? | |
| heureArrivee | String? | |
| remarque | String? | |

---

## 6. Examens

### Tentative
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| candidatId | Int | FK → Candidat |
| inscriptionId | Int | FK → Inscription |
| typeEpreuveId | Int | FK → TypeEpreuve |
| numero | Int | n-ième tentative de ce candidat pour cette épreuve (jamais réutilisé) |
| examinateurId | Int? | FK → Examinateur, renseigné à la saisie |
| statut | Enum | `EN_COURS`, `TERMINEE`, `VALIDEE`, `ANNULEE` |
| dateHeure | String | |

### Evaluation
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| tentativeId | Int | FK → Tentative, unique |
| baremeId | Int | FK → Bareme (version figée) |
| dateSaisie | String | |
| observations | String? | |

### ReponseCandidat (théorie)
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| evaluationId | Int | FK → Evaluation |
| questionId | Int | FK → Question |
| reponseId | Int? | FK → Reponse, null = sans réponse |

### EvaluationCritere (conduite — structure)
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| evaluationId | Int | FK → Evaluation |
| critereId | Int | FK → CriterePratique |
| note | Double? | |
| fauteEliminatoire | Boolean | |
| observation | String? | |

### Resultat (jamais écrasé)
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| tentativeId | Int | FK → Tentative |
| baremeId | Int | FK → Bareme |
| noteObtenue | Double | |
| noteMax | Double | copiée du barème |
| seuil | Double | copié du barème |
| reussi | Boolean | |
| statut | Enum | `CALCULE`, `VALIDE_ATT`, `CORRIGE`, `ANNULE` |
| valideParId | Int? | FK → Utilisateur |
| dateCalcul | String | |
| remplaceResultatId | Int? | FK → Resultat : une correction crée une nouvelle ligne qui pointe vers l'ancienne |
| motifCorrection | String? | |

Le résultat courant d'une tentative = le plus récent non annulé. Les anciens restent lisibles.

---

## 7. Traçabilité

### Historique
| Champ | Type | Note |
|---|---|---|
| id | Int | PK |
| entite | String | `Dossier`, `Resultat`, `Presence`, `RegleConfig`, … |
| entiteId | Int | |
| action | String | `CREATION`, `MODIFICATION`, `VALIDATION`, `REFUS`, `CORRECTION`, `ANNULATION` |
| ancienneValeur | String? | résumé texte |
| nouvelleValeur | String? | |
| utilisateurId | Int | FK → Utilisateur |
| dateHeure | String | |
| motif | String? | |

Écrit systématiquement pour : décision sur dossier, inscription/annulation/report, présence, création/validation/correction de résultat, modification d'un référentiel ou d'une règle.

---

## 8. Invariants à implémenter (contrôlés dans le ViewModel ou une fonction de calcul pure, testables sans Android)

1. Inscription impossible si la session est `COMPLETE`/`TERMINEE`/`ANNULEE` ou si la capacité (session et créneau) est atteinte.
2. Un candidat n'a qu'une inscription active par session ; pas deux inscriptions le même jour sur des sessions qui se chevauchent (À confirmer).
3. Inscription possible seulement si le dossier est `VALIDE` et de la même catégorie que la session.
4. La conduite n'est ouverte qu'après réussite de la théorie si `CONDUITE_APRES_THEORIE_REUSSIE = true` (À confirmer).
5. Un résultat n'est jamais modifié ni supprimé ; correction = nouvelle ligne + Historique.
6. Le calcul lit noteMax/seuil/points dans Bareme, Question, CriterePratique ; une faute éliminatoire entraîne l'échec quel que soit le score.
7. Numéro de tentative = max(numero) + 1 par (candidat, type d'épreuve) ; blocage si `TENTATIVES_MAX` atteint (si la règle est définie).
8. Après échec : les épreuves réussies sont conservées pendant `CONSERVATION_EPREUVE_REUSSIE_JOURS` ; sinon à repasser.
9. Présence `ABSENT` → application de `REGLE_ABSENCE` ; `EN_RETARD` si arrivée > convocation + `TOLERANCE_RETARD_MIN`.
10. Centres et auto-écoles ont toujours une région ; les listes ATT sont filtrables par région ; règle `EXAMEN_DANS_REGION_AUTO_ECOLE` (À confirmer) contrôlée à l'inscription.
11. Toute modification sensible écrit une ligne d'Historique dans la même transaction.

---

## 9. Jeu de données initial (premier lancement)

- Régions : les 24 régions de Madagascar (à ajuster selon le découpage ATT).
- Utilisateur `superadmin` (mot de passe initial à changer), un `admin` ATT de démonstration.
- Catégories, types d'épreuves, barèmes, règles : **valeurs d'exemple marquées `aConfirmer = true`**, modifiables dans l'écran de configuration. Aucune valeur n'est présentée comme officielle.
- Aucun candidat, session ni résultat réel.
