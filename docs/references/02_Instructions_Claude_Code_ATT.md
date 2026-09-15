> Version texte du fichier `02_Instructions_Claude_Code_ATT.docx` (même dossier). En cas de doute, le .docx fait foi.
INSTRUCTIONS DE DÉVELOPPEMENT — CLAUDE CODE
Module ATT — Projet universitaire
Utiliser ce document comme spécification avant toute implémentation.
1. Règles impératives
Le périmètre est ATT uniquement. Ne pas développer le CIM.
Ne pas inventer de règles administratives.
Ne pas coder en dur barèmes, seuils, catégories ou règles de repassage.
Smartphone non obligatoire.
Pas de notifications dans le MVP.
Pas d’affectation préalable candidat → examinateur sans confirmation.
Respecter les technologies et notions déjà apprises dans les cours.
Construire d’abord modèle métier/données, puis interfaces.
Toute règle inconnue = configuration ou « À confirmer ».
2. Ordre de développement
Analyser les exigences.
Identifier acteurs, permissions et entités.
Proposer/valider le modèle de données.
Produire cas d’utilisation et flux.
Authentification et rôles.
Auto-écoles, candidats, dossiers.
Catégories et règles configurables.
Centres, sessions, créneaux.
Inscriptions et présence.
Tentatives et épreuves.
Évaluation théorique.
Structure extensible pour conduite.
Calcul des résultats.
Consultation/impression.
Tests et cas particuliers.
3. Entités minimales
Utilisateur/Rôle
Auto-école
Candidat
Dossier
Catégorie de permis
Règle/Barème
Type d’épreuve
Question
Réponse
Critère pratique
Centre
Session
Créneau
Inscription
Présence
Tentative
Évaluation
Résultat
Examinateur
Historique
4. Planification
Ne jamais dépasser la capacité.
Éviter doubles inscriptions.
Prévoir convocation et passage estimé.
Gérer absent/retard/report.
Conserver l’historique.
Permettre impression.
5. Examinateur
En V1, ne pas supposer une affectation préalable.
Prévoir saisie de réponses, notes, critères et observations selon la procédure confirmée.
L’ATT conserve la validation administrative finale selon la règle retenue.
6. Échec
Créer une nouvelle tentative.
Conserver toutes les tentatives.
Identifier les épreuves à repasser.
Ne pas supposer les délais ou la conservation : rendre configurable.
7. Permissions
Auto-école : uniquement ses candidats.
Candidat : uniquement ses données.
Examinateur : données nécessaires à son évaluation.
Admin ATT : gestion opérationnelle.
Super Admin : règles et configuration.
Tracer les modifications sensibles.
8. Questions à poser plutôt qu’inventer
Quel seuil ? Quelle durée ? Quelle règle de repassage ? Quelle faute éliminatoire ? Quelles règles de présence/retard ? Quel rôle exact de l’examinateur ? Quelle grille de conduite ? Quels documents ?
9. Critères de réussite du MVP
Créer/valider un candidat.
Créer une session et ses créneaux.
Inscrire un candidat.
Enregistrer présence.
Créer une tentative et évaluer.
Calculer selon le barème configurable.
Conserver l’historique.
Respecter les rôles.
Imprimer les informations essentielles.
10. Méthode de collaboration
Ne pas coder tout le projet en une fois.
Après chaque étape, indiquer ce qui a été créé, les fichiers modifiés, les tests et les décisions restantes.
Ne pas ajouter de fonctionnalités hors périmètre sans accord.
Si une décision dépasse les connaissances déjà apprises, expliquer simplement l’option avant de l’utiliser.
