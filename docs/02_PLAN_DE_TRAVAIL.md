# Plan de travail — Projet ATT

> Ordre imposé par les instructions §2 : analyse → acteurs/entités → modèle de données → cas d'utilisation → auth/rôles → auto-écoles/candidats/dossiers → règles configurables → centres/sessions/créneaux → inscriptions/présence → tentatives/épreuves → évaluation théorique → structure conduite → calcul des résultats → consultation/impression → tests et cas particuliers.
> Règle : **une étape à la fois**, compte rendu à la fin de chaque étape (`docs/COMPTES_RENDUS.md`), build et tests verts avant de passer à la suivante.

## 📍 Où en est-on ? (mis à jour automatiquement par Claude à chaque fin d'étape)

| | |
|---|---|
| **Dernière étape terminée** | C12a (historique des modifications) — 17/09/2026, en attente de relecture (branche `etape-C12a-historique`, PR #15) ; C10 fusionnée (PR #14) |
| **Étape en cours** | C12 (consultation par rôle) : C12a 🟢, le reste de C12 en une seule pull request suivante |
| **Prochaine étape** | C12 (sections inscriptions et passages de la fiche candidat, compte et parcours candidat, inscriptions de l'auto-école) — dev 2 |
| **Qui fait quoi** | depuis le 17/09/2026, le dev 1 a livré jusqu'à C10 ; le dev 2 reprend C11, C12, C13 et D1, une pull request par étape du plan (comme les précédentes) |
| **Dernier compte rendu** | `docs/COMPTES_RENDUS.md` → « Étape C12a » |
| **Dernier commit poussé** | `etape-C10-conduite` — « Ajoute l'évaluation de conduite (étape C10) », PR #14 fusionnée |

Légende : 🟢 fait · 🟡 en cours · ⚪ à faire · 🔒 bloqué par une question à valider

---

## Phase A — Cadrage (sans code)

| Étape | Contenu | Livrables | Critère de fin | Statut |
|---|---|---|---|---|
| A1 Analyse des exigences | Lecture du cadrage, des instructions, des cours ; règles ; plan | `CLAUDE.md`, `docs/01`, `docs/02`, `docs/references/`, `JOURNAL-IA.md` | documents relus par le binôme | 🟢 |
| A2 Acteurs, permissions, entités | matrice des permissions, liste des entités | `docs/01` §6, `docs/03` | validés par le binôme | 🟢 (validé 15/09/2026, dev 1 + Claude ; dev 2 informé) |
| A3 Modèle de données | attributs, statuts, relations, invariants, questions | `docs/03_MODELE_DE_DONNEES.md`, `docs/04_QUESTIONS_A_VALIDER.md` | **validation explicite du binôme** ; réponses obtenues ou valeurs par défaut « à confirmer » fixées | 🟢 (validé 15/09/2026 avec les valeurs par défaut ; Q1–Q13 restent à poser à l'enseignant) |
| A4 Cas d'utilisation et flux | UC01–UC14, parcours écran par écran, liste des écrans et routes | `docs/05_CAS_UTILISATION.md` (schéma des écrans + routes) | chaque UC a un acteur, des préconditions, un flux nominal, les cas d'erreur du cadrage §11 | 🟢 15/09/2026 |

## Phase B — Socle technique

| Étape | Contenu | Livrables | Critère de fin | Statut |
|---|---|---|---|---|
| B0 Socle Gradle et projet | `git init` + dépôt public ; package `mg.itu.att` ; Compose/Navigation/ViewModel/Room-KSP ; Java 17 ; suppression appcompat/material ; `MainActivity` + `AppNavigation` + écran « Bonjour ATT » ; thème | `build.gradle.kts`, `libs.versions.toml`, `AndroidManifest.xml`, `MainActivity.kt`, `Navigation.kt`, `.gitignore` | `./gradlew :app:assembleDebug` vert, appli lancée sur émulateur, versions définitives notées dans `docs/01` §2.1 et `HORS_COURS.md` | 🟢 15/09/2026 |
| B1 Base de données | toutes les entités de `docs/03`, DAO minimaux (`tous`, `parId`, `inserer`, `modifier`), `AppDatabase.obtenir()`, données initiales (régions, superadmin, exemples « à confirmer ») | `data/Entites*.kt`, `data/*Dao.kt`, `data/AppDatabase.kt`, `data/DonneesInitiales.kt` | build vert, base créée au premier lancement, inspection avec App Inspection d'Android Studio | 🟢 15/09/2026 |
| B2 Historique et transactions | DAO `Historique` (fait en B1), fonction utilitaire `tracer(...)`, `@Transaction` sur les écritures sensibles | `data/HistoriqueDao.kt`, `metier/Trace.kt` | une écriture de test produit sa ligne d'historique | 🟢 15/09/2026 (avec C2) |

## Phase C — Fonctionnalités MVP (ordre des instructions §2)

| Étape | Contenu | Écrans / ViewModels | Critère de fin (= critère MVP §9) | Statut |
|---|---|---|---|---|
| C1 Authentification et rôles | connexion, session utilisateur, hachage, déconnexion, accueil par rôle | `ui/connexion/EcranConnexion`, `ConnexionViewModel`, `ui/accueil/EcranAccueil` | chaque rôle voit uniquement son menu ; mot de passe jamais en clair | 🟢 15/09/2026 |
| C2 Auto-écoles | CRUD auto-écoles + compte utilisateur associé, filtre par région | `ui/autoecoles/` | Admin ATT crée une auto-école dans une région ; l'auto-école se connecte | 🟢 15/09/2026 |
| C1b Comptes | examinateurs et leurs comptes (Admin ATT), comptes Admin ATT nationaux ou régionaux (Super Admin), changement de son mot de passe (tous), désactivation | `ui/comptes/` | un examinateur créé se connecte et voit son menu ; un mot de passe initial peut être changé ; tout est tracé | 🟢 16/09/2026 |
| C3 Candidats et dossiers | CRUD candidat (auto-école : les siens), constitution et soumission du dossier, pièces, validation/refus/incomplet par ATT avec motif + historique | `ui/candidats/` (liste, détail, formulaire, dossier) | **Créer/valider un candidat** ; dossier incomplet/refusé traité ; candidat sans compte géré | 🟢 15/09/2026 |
| C4 Catégories et règles configurables | écrans Super Admin : catégories, types d'épreuve, barèmes versionnés, `RegleConfig`, questions/réponses, critères pratiques (structure) | `ui/configuration/` | aucune valeur en dur dans le code ; changement d'un seuil visible au calcul | 🟢 16/09/2026 (dev 1, réassignée) |
| C5 Centres, sessions, créneaux | CRUD centres (région), création session (catégorie, épreuve, centre, date, capacité, convocation, durée/marge), génération automatique des créneaux, statuts, annulation | `ui/sessions/` | **Créer une session et ses créneaux** ; capacité respectée | 🟢 16/09/2026 (dev 1 ; centres faits en C4) |
| C6 Inscriptions | inscription d'un candidat éligible (dossier VALIDE, catégorie, capacité, doublon, région selon règle), affectation créneau, heure de passage estimée, report/annulation, historique | `ui/sessions/EcranInscriptions` | **Inscrire un candidat** ; session complète et double inscription refusées | 🟢 16/09/2026 |
| C7 Présence | liste d'appel, statuts présent/absent/retard/en cours/terminé, tolérance de retard configurable, réorganisation selon règle, historique | `ui/sessions/EcranAppel` | **Enregistrer présence** | 🟢 16/09/2026 (valeurs par défaut Q4 : tolérance 15 min, absent = nouvelle inscription) |
| C8 Tentatives | ouverture d'une tentative pour un candidat présent, numérotation, examinateur saisi à ce moment, épreuves à repasser | `ui/evaluation/EcranTentatives`, `metier/ReglesTentatives.kt` | **Créer une tentative** ; tentatives conservées | 🟢 16/09/2026 |
| C9 Évaluation théorique | saisie des réponses du candidat question par question (ou saisie directe du nombre de points si procédure papier), observations | `ui/evaluation/EcranEvaluationTheorie` | **Évaluer** ; réponses stockées | 🟢 17/09/2026 (Q1 : valeurs d'exemple configurables) |
| C10 Structure conduite | écran de saisie des critères/notes/observations/fautes, sans grille imposée (données de configuration vides) | `ui/evaluation/EcranEvaluationConduite` | écran fonctionnel avec critères de démonstration marqués « à confirmer » | 🟢 17/09/2026 (Q2 : grille configurable) |
| C11 Calcul des résultats | `metier/CalculResultat.kt` (pur), création `Resultat`, validation ATT, correction traçable (nouvelle ligne), échec → nouvelle tentative | `ui/resultats/`, tests JUnit | **Calculer selon le barème configurable** ; **conserver l'historique** ; tests verts | ⚪ |
| C12 Consultation | parcours candidat (soi), vue auto-école (ses candidats), vues ATT (sessions, résultats, historique), filtres par région | `ui/candidats/EcranParcours`, `ui/resultats/EcranConsultation` | **Respecter les rôles** | 🟡 C12a historique 🟢 17/09/2026 ; reste de C12 en cours (dev 2) ; résultats validés livrés avec C11 |
| C13 Impression | convocation individuelle, liste d'appel de session, relevé de résultat (HTML → `PrintManager` ou PDF) | `ui/impression/` | **Imprimer les informations essentielles** ; fonctionne hors réseau | ⚪ |

## Phase D — Consolidation

| Étape | Contenu | Critère de fin | Statut |
|---|---|---|---|
| D1 Tests et cas particuliers | JUnit sur `metier/` ; scénarios manuels des contraintes §11 (dossier refusé, non éligible, session complète, absence/retard/report/annulation, erreur de notation corrigée, échec + nouvelle tentative, examinateur indisponible, conflit de créneaux, candidat sans smartphone, procédure de secours = listes imprimées) | tableau de scénarios coché dans `docs/COMPTES_RENDUS.md` | ⚪ |
| D2 Qualité | relecture croisée du binôme, suppression des logs, schéma Room figé (`version` finale), `LISEZMOI.md` (ouverture, déroulé de démo, comptes de test), captures | chaque membre explique chaque fichier sans IA | ⚪ |
| D3 Soutenance | démo scriptée : parcours complet auto-école → dossier → validation → session → inscription → appel → tentative → évaluation → résultat → impression | démo répétée deux fois sans erreur | ⚪ |

---

## Répartition dans le binôme

| Qui | Étapes | Remarque |
|---|---|---|
| Dev 1 (Ericka) + Claude | A4, B0 à B2, C1, C1b, C2, C3, **puis C4 et C5** (réassignés le 16/09/2026 : le dev 2 n'avait pas commencé), puis C6 à C11 | ne dépend de personne |
| Dev 2 (Mahery) | C12 (consultation par rôle, parcours candidat), C13 (impression), D1 (scénarios de test des cas particuliers), **puis C11** (reprise le 17/09/2026 : le dev 1 a terminé sa part à C10) | une branche et une pull request par sous-étape, relecture avant merge ; reprend à partir du tableau « Où en est-on ? » |
| Ensemble | D2, D3 | relecture croisée, démo |

Règle de passage de relais : le dev 2 reprend toujours à partir de `docs/COMPTES_RENDUS.md` (dernière étape terminée) et de `CLAUDE.md`, sans reprendre ce qui est marqué 🟢.

## Ordre de développement à l'intérieur d'une étape

1. Entités/DAO nécessaires (si absents) et migration de version.
2. Fonctions métier pures + tests JUnit.
3. ViewModel + `EtatXxx`.
4. Écran(s) Compose + route(s) dans `Navigation.kt`.
5. Historique et permissions.
6. Vérification sur émulateur, compte rendu, journal IA, commit.

## Points de vigilance

- Les étapes 🔒 avancent avec des **valeurs par défaut marquées « à confirmer »** ; elles ne sont pas bloquantes pour le code, seulement pour la validation finale des règles.
- Pas de synchronisation réseau ni de serveur dans le MVP : l'application est mono-appareil, base locale. Si le binôme veut démontrer l'offline-first avec plusieurs appareils, ce sera une perspective (pattern `demosync`), hors MVP.
- Chaque nouvelle notion hors cours passe par `docs/HORS_COURS.md` avant usage.
- Nombre d'écrans estimé : ~18. Tenir la sobriété du cours (`Column`, `Card`, `Button`, `LazyColumn`).

## Estimation indicative (binôme, hors cours)

| Phase | Charge |
|---|---|
| A (cadrage) | 1 semaine, dont validation des questions |
| B (socle) | 1 semaine |
| C1–C6 | 3 semaines |
| C7–C11 | 3 semaines |
| C12–C13 | 1 semaine |
| D | 1 semaine |

Total ≈ 10 semaines. À ajuster selon les dates de rendu communiquées par l'enseignant (non précisées dans les documents disponibles).
