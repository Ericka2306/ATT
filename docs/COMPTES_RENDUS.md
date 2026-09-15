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

**Décisions restantes**
1. Créer le dépôt Git public et fournir son URL (étape B0).
2. Obtenir les réponses de l'enseignant aux questions Q1 à Q13.
