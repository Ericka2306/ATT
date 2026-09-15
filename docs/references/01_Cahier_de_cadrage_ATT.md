> Version texte du fichier `01_Cahier_de_cadrage_ATT.docx` (même dossier). En cas de doute, le .docx fait foi.
CAHIER DE CADRAGE — PROJET ATT
Digitalisation et optimisation des examens du permis de conduire
Référence fonctionnelle — projet universitaire en binôme
1. Vision
Plateforme dédiée à la gestion des examens du permis au niveau de l’ATT. Elle doit améliorer l’expérience du citoyen et l’efficacité administrative en centralisant candidats, auto-écoles, sessions, planification, évaluations et résultats. Le CIM et la délivrance du permis sont hors périmètre.
2. Problématique
Tous les candidats peuvent devoir se présenter tôt pour l’appel, puis attendre plusieurs heures avant leur passage. Le système doit réduire cette attente lorsque l’organisation et les règles de l’ATT le permettent.
3. Objectifs
Centraliser les dossiers et examens.
Simplifier le suivi pour ATT et auto-écoles.
Optimiser sessions et créneaux.
Réduire l’attente et les déplacements inutiles.
Automatiser notes et barèmes.
Gérer absences, retards, reports, échecs et nouvelles tentatives.
Ne pas exclure les candidats sans smartphone.
Rendre les règles métier configurables.
4. Acteurs
Super Admin : configure catégories, épreuves, barèmes, seuils, critères, centres et règles de planification.
Admin ATT : gestion opérationnelle des candidats, auto-écoles, dossiers, sessions, créneaux, présences et résultats.
Auto-école : gère uniquement ses candidats et prépare/soumet les dossiers.
Candidat : consulte son parcours, examen et résultat ; accès numérique facultatif.
Examinateur : évalue le candidat et saisit les données de l’examen.
5. Parcours
Auto-école → dossier candidat → validation ATT → session → créneau → présence → épreuves → évaluation → calcul du résultat → validation ATT.
Après réussite, le projet ATT s’arrête ; la suite CIM est hors périmètre.
6. Planification
Session = catégorie/type, centre, date, capacité, statut, créneaux.
Empêcher dépassement de capacité et double inscription.
Prévoir heure de convocation et heure/plage estimée de passage si autorisé.
Durée et marge configurables.
Statuts : présent, absent, en retard, en attente, en cours, terminé.
Réorganisation en cas d’absence selon une règle configurable.
Historique des changements.
Convocations et listes imprimables.
7. Examens
Théorie : questions, réponses, points, note et seuil configurables.
Conduite : prévoir critères, notes, observations et fautes éventuelles, mais ne rien inventer avant validation de l’ATT.
Le système calcule automatiquement le résultat selon le barème.
8. Règles configurables
Catégories et types d’épreuves.
Notes maximales et seuils.
Points par question/critère.
Questions et critères.
Fautes éliminatoires éventuelles.
Durées et capacités.
Absence, retard, report.
Nombre de tentatives, délais et conservation d’épreuves réussies.
9. Tentatives
Conserver chaque tentative.
Ne jamais écraser un ancien résultat.
Savoir quelles épreuves sont réussies/à repasser.
Les règles exactes de repassage restent à confirmer.
10. Inclusion numérique
Smartphone non obligatoire.
Le candidat peut exister sans utiliser l’application.
Auto-école/ATT peuvent communiquer les informations.
Convocations et listes imprimables.
Prévoir une procédure de secours en cas de panne réseau/électricité.
11. Contraintes à couvrir
Dossier incomplet/refusé.
Candidat non éligible.
Session complète.
Absence, retard, report, annulation.
Erreur de notation et correction traçable.
Échec et nouvelle tentative.
Examinateur indisponible.
Conflit de créneaux.
Candidat sans smartphone.
12. MVP
Authentification et rôles.
Auto-écoles, candidats et dossiers.
Règles configurables.
Centres, sessions, créneaux et inscriptions.
Présence.
Tentatives.
Évaluation théorique.
Structure extensible pour conduite.
Calcul des résultats.
Consultation et impression.
13. Hors périmètre
Délivrance/renouvellement du permis au CIM.
Gestion complète du CIM.
Notifications push/SMS.
Paiement en ligne et fonctions avancées non nécessaires au MVP.
14. À rechercher/valider
Barèmes et seuils officiels.
Déroulement exact théorie/conduite.
Règles de repassage et conservation des épreuves.
Appel, présence, retard et absence.
Durée/capacité des sessions.
Rôle exact des examinateurs.
Possibilité réglementaire des créneaux individuels.
Données/documents que l’ATT peut stocker.
