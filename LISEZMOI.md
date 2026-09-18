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
| [docs/06_GUIDE_DU_CODE.md](docs/06_GUIDE_DU_CODE.md) | Carte du code : à quoi sert chaque fichier, dans quel ordre le lire |
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

## Comptes de test (version de développement)

Insérés au premier lancement d'une version de développement seulement (`BuildConfig.DEBUG`), affichés sur l'écran de connexion avec un bouton par rôle :

| Rôle | Identifiant | Mot de passe |
|---|---|---|
| Super administrateur | `superadmin` | `ChangezMoi2026` |
| Administrateur ATT | `admin` | `admin2026` |
| Auto-école (Analamanga) | `autoecole` | `autoecole2026` |
| Examinateur (Analamanga) | `examinateur` | `examinateur2026` |
| Candidat (RAKOTO Hery, de l'auto-école de démonstration) | `candidat` | `candidat2026` |

Les deux premiers existent aussi en version livrée (à changer dès la première connexion) ; les trois autres n'existent qu'en développement. Pour repartir de zéro : `adb shell pm clear mg.itu.att` ou désinstaller l'application.

Comptes créés pendant la démonstration ci-dessous : `lalana` / `lalana2026` (auto-école) et `naina` / `naina2026` (examinateur).

## Déroulé de démonstration

Parcours complet, d'une base vide jusqu'au relevé imprimé. Compter une vingtaine de minutes. **L'ordre compte** : une épreuve théorique sans question configurée ne peut pas s'ouvrir.

**1. Configuration — `superadmin`**
1. Configuration → Centres d'examen → « + » : *Centre ATT Soarano*, région Analamanga, adresse.
2. Configuration → Catégories et épreuves → *B* → *Épreuve théorique* → « Ajouter une question » **trois fois** (par exemple 10, 10 et 10,5 points). Sans au moins une question active, l'examinateur ne pourra pas ouvrir l'épreuve.
3. Au passage : Configuration → Règles montre que délais, capacités et tolérances sont en base, marqués « à confirmer ».

**2. Acteurs — `admin`**
4. Auto-écoles → « + » : *Auto-ecole Lalana*, Analamanga → sur sa fiche, « Créer un compte » (`lalana` / `lalana2026`).
5. Examinateurs → « + » : *RANDRIA Naina* avec son compte (`naina` / `naina2026`).

**3. Candidat et dossier — `lalana`**
6. Mes candidats → « + » : *RAKOTO Jean*, né le 2004-05-17.
7. Sur sa fiche : catégorie *B* → « Ouvrir un dossier » → cocher les quatre pièces → « Soumettre à l'ATT ».

**4. Décision et session — `admin`**
8. Dossiers à traiter → le dossier → « Valider ». (Essayer « Refuser » sans motif : l'application le refuse.)
9. Sessions → « + » : catégorie B, épreuve théorique, **date du jour**, 08:00. L'aperçu des créneaux se recalcule à chaque frappe.
10. Sur la fiche de session : « Ouvrir aux inscriptions » → « Inscriptions » → « Inscrire » le candidat. Il reçoit un numéro d'appel (001) et un créneau.
11. « Convocation » imprime sa convocation.

**5. Jour de l'examen — `admin`**
12. Fiche de session → « Démarrer » → « Appel » → « Présent ». Au-delà de la tolérance, l'application propose « Accepter le retard » ou « Refuser ».
13. « Liste d'appel » imprime la feuille de secours à cocher.

**6. Épreuve — `naina`**
14. Sessions du jour → la session → « Ouvrir le passage n° 1 ». L'examinateur ne voit **que le numéro 001**, jamais le nom.
15. Saisir les points de chaque question, puis « Terminer l'épreuve ». Le résultat est calculé aussitôt.

**7. Résultat et documents — `admin`**
16. Résultats à valider → le résultat : le calcul est détaillé (« 18 / 20,5 × 30 = 26,34 »), puis « Valider le résultat ».
17. « Imprimer le relevé ». Sur la fiche de session, « Liste des admis ».
18. Pour montrer la traçabilité : « Corriger (recalculer) » avec un motif crée une **nouvelle** ligne ; l'ancienne reste lisible, marquée « Remplacé ».
19. Historique : les modifications du parcours, avec leur auteur, filtrables par période, objet et utilisateur.

**7 bis. Synchronisation — `admin`**
19 bis. Synchronisation : le résultat validé est « Envoyé » (réseau on). Couper le réseau avec l'interrupteur, valider un autre résultat : « En attente », l'application ne bloque pas ; rétablir le réseau, « Synchroniser maintenant » : la file se vide et le panneau serveur se remplit.

**8. Consultation — `lalana`**
20. Mes inscriptions et Résultats : l'auto-école ne voit que ses candidats, et le résultat seulement une fois validé.

Deux pièges pendant la démonstration :
- Le clavier peut recouvrir le bouton de validation d'un formulaire : le fermer avant d'appuyer.
- L'heure de l'appareil sert d'heure de convocation : un candidat pointé l'après-midi pour une session de 08:00 est « en retard », ce qui est normal.

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
- Police de caractères : « Plus Jakarta Sans » (Tokotype), licence SIL Open Font License 1.1, fichier `app/src/main/res/font/plus_jakarta_sans.ttf`, licence dans `docs/references/OFL_plus_jakarta_sans.txt`.
- Logo et icône de l'application : dessins vectoriels réalisés pour ce projet. Ce n'est pas le logo officiel de l'ATT.

## État d'avancement

Voir la colonne « Statut » de [docs/02_PLAN_DE_TRAVAIL.md](docs/02_PLAN_DE_TRAVAIL.md). Phases A (cadrage), B (socle technique) et C (fonctionnalités, jusqu'à l'impression) terminées ; phase D en cours : tests et cas particuliers faits, qualité et soutenance en préparation.

Le schéma de la base est figé (version 3) et exporté dans `app/schemas/`. 121 tests unitaires sur les fonctions de `metier/`.
