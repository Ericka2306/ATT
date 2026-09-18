# Plan de travail — Projet ATT

> Ordre imposé par les instructions §2 : analyse → acteurs/entités → modèle de données → cas d'utilisation → auth/rôles → auto-écoles/candidats/dossiers → règles configurables → centres/sessions/créneaux → inscriptions/présence → tentatives/épreuves → évaluation théorique → structure conduite → calcul des résultats → consultation/impression → tests et cas particuliers.
> Règle : **une étape à la fois**, compte rendu à la fin de chaque étape (`docs/COMPTES_RENDUS.md`), build et tests verts avant de passer à la suivante.

## 📍 Où en est-on ? (mis à jour automatiquement par Claude à chaque fin d'étape)

| | |
|---|---|
| **Dernière étape terminée** | D2 (qualité : relecture croisée, 12 corrections, schéma figé, guide du code, démonstration rejouée) — 18/09/2026, en attente de relecture (branche `etape-D2-qualite`) ; D1 fusionnée (PR #19) |
| **Étape en cours** | D2a (design de l'interface) et rapport technique — dev 1 + Claude, branche `etape-D2a-design` (18/09/2026) ; D2c (synchronisation) faite sur la même branche ; D2b (pièces jointes du dossier) à prendre par le dev 2 |
| **Prochaine étape** | D2b pour le dev 2, puis D3 (soutenance : démonstration scriptée, répétée deux fois sans erreur) — le déroulé est écrit dans `LISEZMOI.md` |
| **Qui fait quoi** | le dev 1 a livré jusqu'à C10 ; le dev 2 a repris C11, C12, C13, D1 et D2, une pull request par étape du plan |
| **Dernier compte rendu** | `docs/COMPTES_RENDUS.md` → « Étape D2 » |
| **Dernier commit poussé** | `etape-D1-scenarios` — « Ajoute les tests des contraintes du cadrage et corrige la saisie des motifs (étape D1) », PR #19 fusionnée |

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
| C11 Calcul des résultats | `metier/CalculResultat.kt` (pur), création `Resultat`, validation ATT, correction traçable (nouvelle ligne), échec → nouvelle tentative | `ui/resultats/`, tests JUnit | **Calculer selon le barème configurable** ; **conserver l'historique** ; tests verts | 🟢 17/09/2026 (dev 2 ; calcul enchaîné à la clôture de l'épreuve, Q2 bis : note rapportée au barème) |
| C12 Consultation | parcours candidat (soi), vue auto-école (ses candidats), vues ATT (sessions, résultats, historique), filtres par région | `ui/candidats/EcranParcours`, `ui/resultats/EcranConsultation` | **Respecter les rôles** | 🟢 17/09/2026 (dev 2 ; la consultation des résultats validés est livrée avec C11) |
| C13 Impression | convocation individuelle, liste d'appel de session, relevé de résultat (HTML → `PrintManager` ou PDF) | `ui/impression/` | **Imprimer les informations essentielles** ; fonctionne hors réseau | 🟢 17/09/2026 (dev 2 ; 4 documents, liste d'appel en version ATT et examinateur) |

## Phase D — Consolidation

| Étape | Contenu | Critère de fin | Statut |
|---|---|---|---|
| D1 Tests et cas particuliers | JUnit sur `metier/` ; scénarios manuels des contraintes §11 (dossier refusé, non éligible, session complète, absence/retard/report/annulation, erreur de notation corrigée, échec + nouvelle tentative, examinateur indisponible, conflit de créneaux, candidat sans smartphone, procédure de secours = listes imprimées) | tableau de scénarios coché dans `docs/COMPTES_RENDUS.md` | 🟢 18/09/2026 (dev 2 ; 10 contraintes cochées, 119 tests verts, 3 défauts de saisie corrigés) |
| D2a Design de l'interface | refonte visuelle pour mobile : thème (formes, typographie), composants communs (barre claire, champs, sélecteurs, cartes, pastilles, états vides), accueil en grille, connexion ; puis retouches écran par écran | l'application « donne envie » : captures avant/après validées par le dev 1 | 🟡 depuis le 18/09/2026 (dev 1 + Claude, branche `etape-D2a-design`) |
| D2b Dossiers : pièces jointes numériques (dev 2) | l'auto-école soumet déjà le dossier dans l'application (C3) ; **à ajouter** : pour chaque pièce, l'auto-école peut **joindre un fichier** (photo ou PDF pris dans le téléphone : appareil photo, galerie ou fichiers) ; le fichier est copié dans le stockage privé de l'application (pas de serveur), son chemin dans `PieceDossier.fichier` (schéma v4) ; l'Admin ATT l'ouvre depuis l'écran du dossier pour vérifier sans attendre le papier. Joindre est **facultatif** : sans fichier, la vérification se fait sur le dossier papier apporté par l'auto-école, donc plus lentement. À faire aussi : le candidat ne voit pas un dossier encore en brouillon dans son parcours. Notions hors cours à documenter avant usage : sélecteur de fichier (`ActivityResultContracts`), copie dans `filesDir`, affichage d'une image depuis un fichier | scénario : `autoecole` joint une photo à deux pièces et soumet → `admin` ouvre les fichiers et valide ; dossier sans fichier toujours accepté ; test JUnit sur la règle « fichier facultatif » | ⚪ demandé par le dev 1 le 18/09/2026 |
| D2c Synchronisation hors ligne d'abord | appliquer la démo `demosync` du cours S7 : drapeau `synchronisee` sur les résultats validés (schéma v4), faux serveur en mémoire avec interrupteur, remontée automatique après validation, écran « Synchronisation » (file d'attente, bouton, panneau serveur), marque « envoyé / à envoyer » sur la liste des résultats | démonstration en quatre temps comme au cours : réseau on, réseau off, données acquises, resynchronisation | 🟢 18/09/2026 (dev 1 + Claude) |
| D2 Qualité | relecture croisée du binôme, suppression des logs, schéma Room figé (`version` finale), `LISEZMOI.md` (ouverture, déroulé de démo, comptes de test), captures | chaque membre explique chaque fichier sans IA | 🟢 18/09/2026 (dev 2 ; 12 défauts corrigés, schéma v3 figé et exporté, `docs/06_GUIDE_DU_CODE.md`, démonstration complète rejouée) |
| D3 Soutenance | démo scriptée : parcours complet auto-école → dossier → validation → session → inscription → appel → tentative → évaluation → résultat → impression | démo répétée deux fois sans erreur | ⚪ |

---

## Répartition dans le binôme

| Qui | Étapes | Remarque |
|---|---|---|
| Dev 1 (Ericka) + Claude | A4, B0 à B2, C1, C1b, C2, C3, **puis C4 et C5** (réassignés le 16/09/2026 : le dev 2 n'avait pas commencé), puis C6 à C11 | ne dépend de personne |
| Dev 2 (Mahery) | C12 (consultation par rôle, parcours candidat), C13 (impression), D1 (scénarios de test des cas particuliers), **puis C11** (reprise le 17/09/2026 : le dev 1 a terminé sa part à C10) | une branche et une pull request par sous-étape, relecture avant merge ; reprend à partir du tableau « Où en est-on ? » |
| Dev 2 (Mahery), suite | D2b (pièces jointes numériques du dossier), puis D2 côté qualité | demandé par le dev 1 le 18/09/2026 |
| Dev 1 (Ericka) + Claude | D2a (design de l'interface), puis D3 (démo) | depuis le 18/09/2026 |
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
