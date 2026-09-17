# ATT — Digitalisation des examens du permis de conduire (Madagascar)

Application Android (Kotlin, Jetpack Compose, Room) pour la gestion des examens du permis de conduire à l'Agence des Transports Terrestres : dossiers des candidats, sessions et créneaux, présence, tentatives, évaluation et résultats.

Projet universitaire M1 — module « Initiation au développement mobile natif Kotlin, assistée par IA » (ITUniversity, Antananarivo), réalisé en binôme.

## Périmètre

Uniquement l'ATT : de la constitution du dossier par l'auto-école jusqu'au résultat validé. La délivrance du permis (CIM), les notifications et le paiement sont hors périmètre. Aucune règle administrative n'est codée en dur : barèmes, seuils, délais et catégories sont configurables et marqués « à confirmer » tant qu'ils ne sont pas validés par l'ATT.

## Où lire quoi

| Document | Contenu |
|---|---|
| [docs/01_REGLES_DE_DEVELOPPEMENT.md](docs/01_REGLES_DE_DEVELOPPEMENT.md) | Règles, pile technique, architecture, conventions |
| [docs/02_PLAN_DE_TRAVAIL.md](docs/02_PLAN_DE_TRAVAIL.md) | Étapes, livrables, avancement |
| [docs/03_MODELE_DE_DONNEES.md](docs/03_MODELE_DE_DONNEES.md) | Entités et invariants |
| [docs/04_QUESTIONS_A_VALIDER.md](docs/04_QUESTIONS_A_VALIDER.md) | Règles administratives inconnues, sources, valeurs par défaut |
| [docs/HORS_COURS.md](docs/HORS_COURS.md) | Notions non vues en cours, expliquées avant usage |
| [docs/COMPTES_RENDUS.md](docs/COMPTES_RENDUS.md) | Compte rendu de chaque étape |
| [JOURNAL-IA.md](JOURNAL-IA.md) | Journal de l'usage de l'IA (protocole du module) |
| [docs/references/](docs/references/) | Cahier de cadrage, instructions, synthèses des cours, recherche documentaire |

## Ouvrir et lancer

Android Studio → File → Open → dossier `ATT` → attendre la synchronisation Gradle → lancer sur un émulateur ou un appareil (Android 8.0 minimum).

```bash
./gradlew :app:assembleDebug        # construire
./gradlew :app:testDebugUnitTest    # tests unitaires
```

## Glossaire (mots de l'écran ↔ mots du cadrage et du code)

| À l'écran | Dans le code et les documents | Sens |
|---|---|---|
| Passage | `Tentative` (cadrage §9, instructions §3) | un candidat passe une épreuve une fois ; numéroté, jamais effacé ; le résultat et les corrections s'y rattachent |
| Inscription | `Inscription` | le candidat est convoqué à une session, avec un numéro d'appel et un créneau |
| Appel / Présence | `Presence` | le candidat est venu (présent, en retard, absent) |
| Session | `Session` | une épreuve d'une catégorie, dans un centre, à une date, avec des créneaux |
| Dossier | `Dossier` | les pièces du candidat pour une catégorie, validées par l'ATT |

## Rejouer un scénario sur l'émulateur

`outils/pilote_emulateur.sh` pilote l'application par le texte des boutons et des champs (`tap "Se connecter"`, `saisir "Identifiant" "admin"`). Les scénarios de vérification de chaque étape sont décrits dans `docs/COMPTES_RENDUS.md`.

```bash
source outils/pilote_emulateur.sh
relancer            # redémarre l'application (relancer effacer : repart de zéro)
connexion admin admin2026
tap "Auto-écoles"
```

## Crédits

- Photo de l'écran de connexion : « Madagascar RN44 » par Diorit, Wikimedia Commons, licence CC0 (domaine public), réduite à 1080 px.
- Logo et icône de l'application : dessins vectoriels réalisés pour ce projet. Ce n'est pas le logo officiel de l'ATT.

## État d'avancement

Voir la colonne « Statut » de [docs/02_PLAN_DE_TRAVAIL.md](docs/02_PLAN_DE_TRAVAIL.md). Phase A (cadrage) terminée ; phase B (socle technique) en cours.
