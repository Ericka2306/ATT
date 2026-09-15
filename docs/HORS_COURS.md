# Notions hors cours utilisées dans le projet

Règle R7 : toute notion non enseignée dans le module M1 (voir `docs/references/SYNTHESE_COURS_M1.md` §4) est ajoutée ici **avant** d'être utilisée, avec une explication simple compréhensible par le binôme et rattachée à ce qui a été vu en cours.

| # | Notion | Étape | Pourquoi on en a besoin | Explication simple | Décision |
|---|---|---|---|---|---|
| 1 | Catalogue de versions `libs.versions.toml` | 0 | généré par Android Studio, évite de répéter les versions | même chose que les chaînes `"2.8.0"` du cours, rangées dans un fichier unique | accepté |
| 2 | Kotlin intégré à AGP 9, plugin Compose 2.2.x, KSP 2.x | 0 | le squelette est en AGP 9.3.2 (le cours en 8.x) | AGP 9 embarque le compilateur Kotlin ; on aligne seulement Compose et KSP dessus | accepté (repli : AGP 8.13.2 + Kotlin 2.0.20 comme `carteproduit`) |
| 3 | `@ForeignKey`, `@Index` sur les entités Room | 2 | ~20 tables liées, intégrité des identifiants | le `FOREIGN KEY` / `INDEX` de SQL, posé en annotation comme `@Query` | accepté 15/09/2026 |
| 4 | `@Transaction` dans un DAO | 2 | écrire une modification et son `Historique` ensemble | `BEGIN … COMMIT` : soit tout, soit rien | accepté 15/09/2026 |
| 5 | `enum class` persisté par Room | 2 | statuts (dossier, session, présence…) | Room enregistre le nom de l'enum en texte ; on relit `Statut.valueOf` automatiquement | accepté 15/09/2026 |
| 6 | `OutlinedTextField` + `KeyboardOptions` | 5 | formulaires (candidat, session, notes) | un `Text` dont la valeur est un état et qui appelle `onValueChange`, comme le compteur du TP4 | accepté 15/09/2026 |
| 7 | `Scaffold` / `TopAppBar` / `FloatingActionButton` | 5 | ~15 écrans, barre de titre et bouton d'action | un `Column` Material avec des emplacements nommés (barre, contenu, bouton) | accepté 15/09/2026 (rester sobre) |
| 8 | Hachage PBKDF2 + sel (`SecretKeyFactory`, `SecureRandom`) | C1 | authentification locale sans mot de passe en clair | fonction à sens unique, volontairement lente, de la bibliothèque Java standard ; on compare des empreintes, pas des mots de passe | accepté 15/09/2026 (minSdk 26) |
| 9 | Fonctions pures dans `metier/` + tests JUnit | 6 à 13 | prouver le calcul selon barème et les contrôles de planification | des fonctions Kotlin sans Android, comme `Collectes.kt` du TP1, exécutées par JUnit | accepté 15/09/2026 |
| 10 | Impression Android (`PrintManager`, `WebView` HTML ou `PdfDocument`) | 14 | convocations et listes imprimables (cadrage §6, §10) | on génère une page HTML et on la confie au service d'impression du téléphone | accepté 15/09/2026 |

Ajouter une ligne à chaque nouvelle notion. Une notion refusée par le binôme est remplacée par l'équivalent vu en cours.
