/**
 * Génère le rapport technique Word du projet ATT : `node generer_rapport.js` → `Rapport_technique_ATT.docx`.
 *
 * Le contenu vit dans ce fichier (une fonction par section) ; les captures d'écran sont lues dans `captures/`
 * quand elles existent, sinon un encadré « capture à insérer » prend leur place. Bibliothèque : docx (npm), locale
 * au dossier (`npm install` si `node_modules` manque).
 */
const fs = require("fs");
const path = require("path");
const {
  Document, Packer, Paragraph, TextRun, HeadingLevel, AlignmentType, Table, TableRow, TableCell, WidthType,
  ShadingType, BorderStyle, PageBreak, TableOfContents, Header, Footer, PageNumber, LevelFormat, ImageRun,
  VerticalAlign, TabStopType,
} = require("./node_modules/docx");

// ---------- IDENTITÉ ----------

const BLEU = "0F4C81";
const BLEU_FONCE = "0B2E4F";
const GRIS = "5E6B7C";
const GRIS_CLAIR = "EAEFF5";
const AMBRE_CLAIR = "FFF1D6";
const POLICE = "Calibri";
const AUTEURS = ["MAMINIAINA Ericka", "ONINIAINA Mahery"];
const ETABLISSEMENT = "ITUniversity — Antananarivo";
const MODULE = "Master 1 — Initiation au développement mobile natif Kotlin, assistée par IA";
const ANNEE = "Année universitaire 2026";
const DOSSIER_CAPTURES = path.join(__dirname, "captures");

// ---------- OUTILS D'ÉCRITURE ----------

const texte = (t, opts = {}) => new TextRun({ text: t, font: POLICE, size: 22, ...opts });
const para = (t, opts = {}) => new Paragraph({ children: [typeof t === "string" ? texte(t) : t].flat(), spacing: { after: 120, line: 300 }, ...opts });
const paras = (...ts) => ts.map((t) => para(t));
const titre1 = (t) => new Paragraph({ heading: HeadingLevel.HEADING_1, children: [texte(t, { bold: true, size: 32, color: BLEU })], spacing: { before: 360, after: 200 }, pageBreakBefore: true });
const titre2 = (t) => new Paragraph({ heading: HeadingLevel.HEADING_2, children: [texte(t, { bold: true, size: 26, color: BLEU_FONCE })], spacing: { before: 280, after: 140 } });
const titre3 = (t) => new Paragraph({ heading: HeadingLevel.HEADING_3, children: [texte(t, { bold: true, size: 23, color: BLEU_FONCE })], spacing: { before: 200, after: 100 } });
const puce = (t) => new Paragraph({ children: [typeof t === "string" ? texte(t) : t].flat(), numbering: { reference: "puces", level: 0 }, spacing: { after: 60, line: 300 } });
const puces = (...ts) => ts.map(puce);
const numero = (t) => new Paragraph({ children: [typeof t === "string" ? texte(t) : t].flat(), numbering: { reference: "numeros", level: 0 }, spacing: { after: 60, line: 300 } });
const saut = () => new Paragraph({ children: [new PageBreak()] });
const gras = (t) => texte(t, { bold: true });
const code = (t) => new Paragraph({
  children: [new TextRun({ text: t, font: "Consolas", size: 18 })],
  shading: { type: ShadingType.CLEAR, fill: "F3F6FA", color: "auto" },
  spacing: { after: 0, line: 260 }, indent: { left: 200, right: 200 },
});
const bloc = (lignes) => lignes.map(code);

/** Un encadré jaune : texte à compléter pendant le rejeu. Facile à retrouver dans Word (surlignage). */
const aCompleter = (t) => new Paragraph({
  children: [new TextRun({ text: "À compléter — " + t, font: POLICE, size: 20, italics: true, color: "7A4B00", highlight: "yellow" })],
  spacing: { after: 120 },
});

/** Consigne de rédaction pour la section (grise, en italique) : à supprimer une fois la section écrite. */
const consigne = (t) => new Paragraph({
  children: [new TextRun({ text: t, font: POLICE, size: 19, italics: true, color: GRIS })],
  spacing: { after: 120 }, indent: { left: 200 },
  border: { left: { style: BorderStyle.SINGLE, size: 12, color: "B7C3D1", space: 8 } },
});

/** Une figure : la capture si le fichier existe, sinon un cadre « capture à insérer ». Légende numérotée. */
let numFigure = 0;
const figure = (nomFichier, legende, largeurCm = 7) => {
  numFigure += 1;
  const chemin = path.join(DOSSIER_CAPTURES, nomFichier);
  const contenu = [];
  if (fs.existsSync(chemin)) {
    const donnees = fs.readFileSync(chemin);
    // Dimensions lues dans l'en-tête PNG (octets 16 à 24) : la figure garde les proportions de l'écran capturé.
    const largeurPng = donnees.readUInt32BE(16);
    const hauteurPng = donnees.readUInt32BE(20);
    const px = Math.round(largeurCm * 37.8);
    contenu.push(new Paragraph({
      alignment: AlignmentType.CENTER,
      children: [new ImageRun({ type: "png", data: donnees, transformation: { width: px, height: Math.round(px * hauteurPng / largeurPng) } })],
    }));
  } else {
    contenu.push(new Table({
      width: { size: 4000, type: WidthType.DXA }, columnWidths: [4000],
      rows: [new TableRow({ children: [new TableCell({
        width: { size: 4000, type: WidthType.DXA }, verticalAlign: VerticalAlign.CENTER,
        shading: { type: ShadingType.CLEAR, fill: AMBRE_CLAIR, color: "auto" },
        margins: { top: 1200, bottom: 1200, left: 200, right: 200 },
        children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [texte("Capture à insérer : " + nomFichier, { italics: true, color: "7A4B00" })] })],
      })] })],
    }));
  }
  contenu.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 80, after: 240 }, children: [texte(`Figure ${numFigure} — ${legende}`, { italics: true, size: 19, color: GRIS })] }));
  return contenu;
};

/** Tableau avec en-tête bleu ; `largeurs` en centièmes de la largeur utile (somme 100). */
const tableau = (entetes, lignes, largeurs) => {
  const total = 9020;
  const dxa = largeurs.map((l) => Math.round(total * l / 100));
  const cellule = (t, i, entete = false) => new TableCell({
    width: { size: dxa[i], type: WidthType.DXA },
    shading: entete ? { type: ShadingType.CLEAR, fill: BLEU, color: "auto" } : undefined,
    margins: { top: 80, bottom: 80, left: 100, right: 100 },
    children: [new Paragraph({ children: [texte(String(t), { bold: entete, color: entete ? "FFFFFF" : undefined, size: 20 })], spacing: { after: 0, line: 270 } })],
  });
  return new Table({
    width: { size: total, type: WidthType.DXA }, columnWidths: dxa,
    rows: [
      new TableRow({ tableHeader: true, children: entetes.map((e, i) => cellule(e, i, true)) }),
      ...lignes.map((l, r) => new TableRow({ children: l.map((c, i) => new TableCell({
        width: { size: dxa[i], type: WidthType.DXA },
        shading: r % 2 === 1 ? { type: ShadingType.CLEAR, fill: "F3F6FA", color: "auto" } : undefined,
        margins: { top: 80, bottom: 80, left: 100, right: 100 },
        children: [new Paragraph({ children: [texte(String(c), { size: 20 })], spacing: { after: 0, line: 270 } })],
      })) })),
    ],
  });
};
const espace = () => new Paragraph({ spacing: { after: 160 }, children: [] });

// ---------- LECTURES DANS LE PROJET (tests) ----------

const RACINE_PROJET = path.resolve(__dirname, "..", "..");
function classesDeTest() {
  const dossier = path.join(RACINE_PROJET, "app", "src", "test", "java");
  const noms = [];
  const parcourir = (d) => { for (const f of fs.readdirSync(d)) { const c = path.join(d, f); if (fs.statSync(c).isDirectory()) parcourir(c); else if (f.endsWith("Test.kt")) noms.push(f.replace(".kt", "")); } };
  if (fs.existsSync(dossier)) parcourir(dossier);
  return noms.sort();
}
function nombreTests() {
  const dossier = path.join(RACINE_PROJET, "app", "build", "test-results", "testDebugUnitTest");
  if (!fs.existsSync(dossier)) return "plus de cent";
  let total = 0;
  for (const f of fs.readdirSync(dossier)) if (f.endsWith(".xml")) { const m = fs.readFileSync(path.join(dossier, f), "utf8").match(/tests="(\d+)"/); if (m) total += Number(m[1]); }
  return total || "plus de cent";
}

// ---------- PAGE DE GARDE ----------

function pageDeGarde() {
  const ligne = (t, opts, before = 0, after = 120) => new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before, after }, children: [texte(t, opts)] });
  return [
    ligne(ETABLISSEMENT, { size: 24, color: GRIS }, 600),
    ligne(MODULE, { size: 22, color: GRIS }, 0, 2400),
    new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 200 }, border: { bottom: { style: BorderStyle.SINGLE, size: 18, color: BLEU, space: 12 } }, children: [texte("ATT", { bold: true, size: 96, color: BLEU })] }),
    ligne("Digitalisation des examens du permis de conduire", { bold: true, size: 40, color: BLEU_FONCE }, 200, 100),
    ligne("Agence des Transports Terrestres — Madagascar", { size: 26, color: GRIS }, 0, 900),
    ligne("Rapport technique", { bold: true, size: 32, color: BLEU }, 0, 100),
    ligne("Application Android — Kotlin, Jetpack Compose, Room", { size: 22, color: GRIS }, 0, 2400),
    ligne("Réalisé par", { size: 22, color: GRIS }, 0, 80),
    ...AUTEURS.map((a) => ligne(a, { bold: true, size: 28, color: BLEU_FONCE }, 0, 60)),
    ligne(ANNEE, { size: 22, color: GRIS }, 900, 0),
  ];
}

// ---------- SECTIONS ----------

function introduction() {
  return [
    titre1("1. Introduction"),
    ...paras(
      "À Madagascar, l'Agence des Transports Terrestres (ATT) organise les examens du permis de conduire : réception des dossiers déposés par les auto-écoles, planification des sessions, appel des candidats, épreuves théorique et de conduite, calcul et publication des résultats. Le Centre d'Immatriculation (CIM) prend ensuite le relais pour délivrer le permis.",
      "Ce rapport présente l'application mobile que nous avons conçue et développée pour outiller cette chaîne, de la constitution du dossier jusqu'au résultat validé et imprimable. Il expose la problématique observée, la solution proposée point par point, le parcours complet dans l'application, l'architecture technique retenue et la démarche de travail.",
    ),
    titre2("1.1 Ce que couvre ce document"),
    ...puces(
      "la problématique et les objectifs (chapitres 2 et 3) ;",
      "la solution, module par module, en réponse à chaque difficulté relevée (chapitre 4) ;",
      "les acteurs et le parcours de bout en bout, illustré par des captures d'écran (chapitres 5 et 6) ;",
      "l'architecture, les règles configurables, la qualité, le déroulement du projet et l'application des cours du module (chapitres 7 à 11) ;",
      "les limites, les perspectives et les annexes : captures, extraits de code, sources, glossaire.",
    ),
    titre2("1.2 Cadre du projet"),
    ...paras(
      "Projet universitaire de première année de master, réalisé en binôme dans le cadre du module « Initiation au développement mobile natif Kotlin, assistée par IA ». Le cahier de cadrage fourni fixe le périmètre : uniquement l'ATT, sans le CIM, sans notification ni paiement, avec des règles métier configurables et une attention particulière aux candidats sans smartphone.",
    ),
  ];
}

function problematique() {
  return [
    titre1("2. Problématique"),
    ...paras("Passer l'examen du permis à Madagascar reste aujourd'hui un parcours long et opaque, pour le candidat comme pour l'administration. Les difficultés relevées sont les suivantes."),
    titre2("2.1 Pour le candidat et l'auto-école"),
    ...puces(
      [gras("Attente le jour de l'examen : "), texte("les candidats sont convoqués tôt pour l'appel puis attendent parfois plusieurs heures avant leur passage, sans heure estimée.")],
      [gras("Dossier papier : "), texte("les pièces sont rassemblées par l'auto-école et déposées physiquement ; un dossier incomplet peut n'être découvert que le jour de l'examen. Nous l'avons vu : lors d'un appel, il manquait une pièce au dossier d'un candidat, qui a été renvoyé sans passer l'épreuve, après s'être déplacé et avoir attendu.")],
      [gras("Manque de visibilité : "), texte("l'auto-école et le candidat ne savent pas où en est le dossier, l'inscription ou le résultat.")],
      [gras("Candidats sans smartphone : "), texte("toute solution numérique doit rester utilisable par ceux qui n'ont ni téléphone ni connexion.")],
    ),
    titre2("2.2 Pour l'ATT"),
    ...puces(
      [gras("Sessions et capacités gérées à la main : "), texte("risque de dépassement de capacité, de double inscription et de conflits de créneaux.")],
      [gras("Présences, retards et absences : "), texte("appel à la voix devant une foule, statut noté une fois pour toutes, sans règle appliquée uniformément ni trace.")],
      [gras("Évaluations et résultats : "), texte("feuilles d'examen papier, calcul manuel, corrections non tracées, résultats jamais consolidés.")],
      [gras("Anonymat et intégrité : "), texte("l'appel par nom expose l'examinateur à la pression ; l'ATT a engagé un passage à l'anonymat des candidats.")],
      [gras("Règles administratives dispersées : "), texte("barèmes, seuils, délais de repassage et âges minimums ne sont pas publiés de façon accessible et peuvent changer.")],
    ),
    titre2("2.3 Ce que nous avons observé nous-mêmes"),
    ...paras(
      "L'une de nous a passé l'examen pendant le projet. Deux observations ont directement orienté la conception :",
    ),
    ...puces(
      [gras("L'épreuve théorique est orale : "), texte("l'examinateur pose des questions, note la réponse donnée et attribue des points sur une feuille, avec un total en bas ; il n'y a pas de questionnaire à choix multiples, contrairement à ce que laissait penser la presse.")],
      [gras("Le dossier se vérifie trop tard : "), texte("un candidat appelé avec une pièce manquante est renvoyé sur-le-champ. Le contrôle doit donc avoir lieu avant l'inscription à une session, et si possible sur pièces numériques, pour éviter le déplacement inutile.")],
      [gras("L'appel se fait dans la foule : "), texte("avec autant de candidats convoqués à la même heure, la personne qui fait l'appel n'entend pas toujours le « présent » et marque le candidat absent aussitôt, sans retour possible. Convoquer par créneau, avec peu de candidats à la fois, et pouvoir corriger un statut de présence règlent ces deux problèmes.")],
    ),
  ];
}

function objectifs() {
  return [
    titre1("3. Objectifs et périmètre"),
    titre2("3.1 Objectifs"),
    ...puces(
      "Centraliser candidats, auto-écoles, dossiers, sessions, évaluations et résultats dans un outil unique.",
      "Réduire l'attente et les déplacements inutiles par des créneaux et des heures de passage estimées.",
      "Rendre chaque étape visible et traçable : qui a fait quoi, quand, avec quel motif.",
      "Calculer les résultats automatiquement selon des barèmes configurables, jamais codés en dur.",
      "Gérer les cas réels : absence, retard, report, annulation, échec et nouvelle tentative, erreur de notation corrigée.",
      "Ne pas exclure les candidats sans smartphone : tout est faisable par l'ATT ou l'auto-école, et imprimable.",
    ),
    titre2("3.2 Périmètre du MVP"),
    tableau(["Dans le périmètre", "Hors périmètre"], [
      ["Authentification et rôles (Super Admin, Admin ATT, auto-école, examinateur, candidat)", "Délivrance et renouvellement du permis (CIM)"],
      ["Auto-écoles, candidats, dossiers et pièces", "Notifications push ou SMS"],
      ["Catégories, épreuves, barèmes, questions, critères, règles configurables", "Paiement en ligne"],
      ["Centres, sessions, créneaux, inscriptions, convocations", "Serveur et synchronisation réseau"],
      ["Appel et présence, tentatives, évaluation théorique et de conduite", ""],
      ["Calcul, validation et correction des résultats", ""],
      ["Consultation par rôle, historique, impression", ""],
    ], [55, 45]),
    espace(),
    titre2("3.3 Contraintes imposées"),
    ...puces(
      "Application Android native, hors ligne, base locale : aucune dépendance à un serveur.",
      "Technologies du cours uniquement : Kotlin, Jetpack Compose, Navigation Compose, ViewModel et StateFlow, Room, coroutines.",
      "Aucune règle administrative inventée : toute valeur inconnue est une configuration marquée « à confirmer ».",
      "Une tentative ou un résultat ne s'écrase jamais : toute correction crée une nouvelle ligne tracée.",
    ),
  ];
}

function solution() {
  return [
    titre1("4. Solution proposée, point par point"),
    ...paras("Chaque difficulté du chapitre 2 est reprise ci-dessous avec la réponse apportée par l'application et le module concerné. Les captures correspondantes sont au chapitre 6."),
    tableau(["Problème observé", "Réponse de l'application", "Module"], [
      ["Attente le jour de l'examen", "Sessions découpées en créneaux, heure de passage estimée imprimée sur la convocation, appel par créneau", "Sessions, Inscriptions, Impression"],
      ["Dossier incomplet découvert le jour de l'examen, candidat renvoyé", "Dossier constitué et soumis dans l'application par l'auto-école, avec une photo ou un PDF par pièce si elle le souhaite ; pièces attendues configurables, décision de l'ATT avec motif (validé, incomplet, refusé) ; seul un dossier validé permet l'inscription à une session, donc personne n'est convoqué avec un dossier incomplet", "Candidats et dossiers, Inscriptions"],
      ["Manque de visibilité", "Chaque rôle voit ce qui le concerne : l'auto-école ses candidats et leurs inscriptions, le candidat son parcours, l'ATT tout", "Consultation"],
      ["Candidats sans smartphone", "Compte candidat facultatif ; convocation, liste d'appel et relevé imprimables ; tout est saisi par l'ATT ou l'auto-école", "Impression, Comptes"],
      ["Capacités et doublons", "Contrôles à l'inscription : capacité de session et de créneau, double inscription, chevauchement le même jour, éligibilité du dossier", "Inscriptions"],
      ["Appel dans la foule, absent marqué par erreur", "Appel par créneau avec peu de candidats à la fois, statuts présent, en retard, absent ; un statut se corrige ; tolérance de retard et sort de l'absent configurables ; tout tracé", "Sessions, Appel"],
      ["Feuilles d'examen et calcul manuel", "Feuille d'examen à l'écran (questions posées, réponses, points), calcul automatique selon le barème, validation par l'ATT, correction en nouvelle ligne", "Évaluation, Résultats"],
      ["Anonymat", "L'examinateur travaille par numéro d'appel, sans nom ; aucune affectation préalable examinateur → candidat", "Appel, Évaluation"],
      ["Règles dispersées ou inconnues", "Écran de configuration du Super Admin : catégories, épreuves, barèmes versionnés, questions, critères, règles ; chaque valeur porte un drapeau « à confirmer »", "Configuration"],
      ["Absence de trace", "Historique de toutes les modifications sensibles, avec auteur, date, motif, ancienne et nouvelle valeur", "Historique"],
      ["Connectivité incertaine dans les centres", "Application hors ligne d'abord : la base locale est la source de vérité ; les résultats validés sont remontés au serveur central quand le réseau le permet, sans jamais bloquer l'agent", "Synchronisation"],
    ], [26, 52, 22]),
  ];
}

function acteurs() {
  return [
    titre1("5. Acteurs et rôles"),
    ...paras("Cinq rôles, chacun avec son menu et ses droits. Un utilisateur ne voit que les actions autorisées ; les contrôles sont faits dans les ViewModels, pas seulement à l'écran."),
    tableau(["Rôle", "Ce qu'il fait", "Ce qu'il ne voit pas"], [
      ["Super administrateur", "Configuration : catégories, épreuves, barèmes, questions, critères, règles, centres ; comptes administrateurs", "—"],
      ["Administrateur ATT (national ou régional)", "Auto-écoles et examinateurs, dossiers, sessions et créneaux, inscriptions, appel, validation des résultats, impression, historique", "La configuration"],
      ["Auto-école", "Ses candidats, leurs dossiers, leurs inscriptions et résultats validés", "Les autres auto-écoles, la configuration"],
      ["Examinateur", "Sessions du jour, passages des candidats présents (par numéro), saisie des épreuves", "Les noms des candidats, les dossiers"],
      ["Candidat", "Son parcours : dossier, convocations, passages, résultats validés", "Tout le reste"],
    ], [24, 50, 26]),
    espace(),
    ...figure("05_accueil_admin.png", "Accueil de l'administrateur ATT : le menu du rôle"),
  ];
}

function parcours() {
  const etape = (n, titre, texte, fichier, legende) => [titre2(`6.${n} ${titre}`), ...paras(texte), ...figure(fichier, legende)];
  return [
    titre1("6. Le parcours dans l'application, de A à Z"),
    ...paras("Ce chapitre suit un candidat de son inscription en auto-école jusqu'à son résultat, dans l'ordre réel des opérations. Chaque étape est illustrée par une capture prise sur un petit téléphone (720 × 1280) ; les comptes utilisés sont ceux de l'annexe E."),
    ...etape(1, "Connexion et accueil par rôle",
      "L'application s'ouvre sur un écran de connexion. Le mot de passe n'est jamais stocké en clair : une empreinte avec sel est comparée à celle saisie. Une fois connecté, chaque rôle arrive sur son propre accueil, un menu en tuiles qui ne montre que ce qu'il a le droit de faire (figure 1 du chapitre 5 pour l'administrateur ATT). La session vit en mémoire : fermer l'application demande une nouvelle connexion.",
      "06_01_connexion.png", "Écran de connexion"),
    ...etape(2, "Configuration par le Super Admin",
      "Avant tout examen, le Super Admin décrit ce que l'ATT attend : les catégories de permis (A', A, B, C, D, E) avec leur âge minimum, les épreuves de chaque catégorie, le barème de chaque épreuve (note maximale, seuil), les questions orales avec leurs points et une réponse attendue facultative, les critères de conduite, et les règles générales (délais, capacités, tolérance de retard). Un barème est versionné : créer une nouvelle version ferme l'ancienne à la date du jour, et un résultat déjà calculé garde la version qui a servi. Sur la figure, le barème de l'épreuve théorique B a été porté à 20 points avec un seuil de 12 ; l'ancienne version reste lisible. Chaque valeur qui n'a pas été validée par l'ATT porte un drapeau « à confirmer », que le Super Admin lève d'un bouton.",
      "06_02_configuration.png", "Épreuve théorique de la catégorie B : barème versionné et questions"),
    ...etape(3, "Centres d'examen",
      "Un centre d'examen appartient toujours à une région : les vingt-quatre régions de Madagascar sont préchargées. Le centre porte une capacité par défaut, reprise à la création des sessions. Un administrateur régional ne voit que les centres de sa région.",
      "06_03_centre.png", "Liste des centres, rattachés à leur région"),
    ...etape(4, "Auto-écoles et comptes",
      "L'ATT crée les auto-écoles agréées et leur compte de connexion : une auto-école ne s'inscrit pas elle-même, parce que l'agrément est une décision de l'ATT et que l'application n'a pas de serveur pour recevoir des demandes. La fiche montre la région, l'adresse, l'agrément, les comptes rattachés et l'historique des modifications. Une auto-école ne se supprime jamais : elle se désactive, et son compte suit.",
      "06_04_auto_ecole.png", "Fiche d'une auto-école et ses comptes"),
    ...etape(5, "Examinateurs",
      "L'administrateur ATT crée l'examinateur et son compte en une seule opération : il n'existe pas d'examinateur sans compte ni de compte orphelin. L'identifiant et le mot de passe initial sont communiqués à l'examinateur, qui le change depuis « Mon mot de passe ». Aucun examinateur n'est affecté à l'avance à un candidat : c'est une exigence du cadrage, cohérente avec l'anonymat.",
      "06_05_examinateur.png", "Création d'un examinateur avec son compte"),
    ...etape(6, "Candidat et dossier",
      "L'auto-école crée ses candidats et, pour chacun, ouvre un dossier dans une catégorie. Les pièces attendues viennent de la configuration (la liste officielle du portail Torolalana, différente pour les catégories qui exigent déjà le permis B). L'auto-école coche ce qu'elle fournit puis soumet le dossier à l'ATT ; l'âge minimum de la catégorie est contrôlé à la soumission. À chaque pièce, l'auto-école peut joindre une photo prise avec le téléphone ou un fichier PDF : l'ATT vérifie alors sur écran, sans attendre le papier ; sans fichier, la vérification se fait sur le dossier apporté, donc plus tard. Un dossier ne se supprime pas : il passe par les statuts brouillon, soumis, validé, incomplet ou refusé, tous tracés.",
      "06_06_dossier.png", "Dossier constitué par l'auto-école, prêt à être soumis"),
    ...paras("Sur la figure suivante, l'auto-école a joint une photo à la première pièce depuis les fichiers du téléphone : la pièce passe en « Photo jointe », avec les actions ouvrir, remplacer, retirer, et l'ATT pourra l'examiner sur écran. La dernière figure montre la pièce ouverte, avec les mêmes droits que le dossier."),
    ...figure("06_06b_piece_jointe.png", "Pièce du dossier avec un fichier joint : ouvrir, remplacer, retirer"),
    ...figure("06_06c_piece_ouverte.png", "La pièce jointe ouverte par l'ATT ou l'auto-école"),
    ...etape(7, "Décision de l'ATT sur le dossier",
      "L'administrateur ATT retrouve les dossiers soumis dans « Dossiers à traiter ». Il valide, ou marque le dossier incomplet ou refusé avec un motif obligatoire, que l'auto-école lit sur sa propre fiche. C'est ce contrôle en amont qui évite le cas vécu d'un candidat renvoyé le jour de l'examen : seul un dossier validé permet une inscription.",
      "06_07_decision.png", "Décision de l'ATT : valider, incomplet ou refuser avec motif"),
    ...etape(8, "Session et créneaux",
      "Une session est une épreuve d'une catégorie, dans un centre, à une date, avec une heure de convocation et une capacité. L'administrateur indique combien de candidats passent par créneau, la durée d'un créneau et la marge entre deux ; l'application calcule les créneaux et les affiche avant même la création. Une seconde session le même jour dans le même centre déclenche un avertissement. La session passe ensuite par les statuts planifiée, ouverte aux inscriptions, complète, en cours, terminée ou annulée avec motif.",
      "06_08_session.png", "Création d'une session : les créneaux se calculent à la saisie"),
    ...etape(9, "Inscription et convocation",
      "L'administrateur inscrit un candidat éligible : dossier validé pour la catégorie, capacité de la session et du créneau non dépassées, pas de double inscription, pas d'autre session le même jour, délai de repassage écoulé, théorie réussie si l'épreuve est la conduite. Le candidat reçoit un numéro d'appel (001, 002…) et un créneau, donc une heure de passage estimée. La convocation s'imprime depuis cette liste ; une inscription se reporte ou s'annule avec motif, et la place est libérée.",
      "06_09_inscription.png", "Inscriptions d'une session : numéro d'appel, créneau, actions"),
    ...etape(10, "Appel et présence",
      "Le jour de l'examen, l'agent fait l'appel par créneau, avec peu de candidats à la fois. Chaque candidat est marqué présent ou absent ; au-delà de la tolérance de retard configurée, l'application propose d'accepter ou de refuser le retard. Un statut se corrige, ce qui répond au cas observé du « présent » non entendu dans la foule. La liste d'appel s'imprime en secours ; l'examinateur en reçoit une version sans les noms.",
      "06_10_appel.png", "Appel d'une session : présents, retards, absents"),
    ...etape(11, "Passages et vue de l'examinateur",
      "L'examinateur connecté voit les sessions du jour et, pour chacune, les candidats présents identifiés par leur seul numéro d'appel. Il ouvre un passage : le n-ième pour ce candidat et cette épreuve, jamais réutilisé, jamais effacé, tracé dans l'historique. Un passage déjà terminé se consulte en lecture seule.",
      "06_11_passages.png", "Vue de l'examinateur : numéros d'appel, sans les noms"),
    ...etape(12, "Épreuve théorique orale",
      "L'épreuve théorique est orale, comme nous l'avons vécue. L'écran reproduit la feuille de l'examinateur : le sujet est tiré au sort par l'application jusqu'à la note maximale du barème, ou choisi question par question par l'examinateur selon la règle configurée. Pour chaque question, il écrit la réponse donnée par le candidat et les points attribués, de zéro au maximum de la question ; la réponse attendue s'affiche en aide-mémoire. Le total se calcule au fil de la saisie. « Terminer l'épreuve » fige la feuille, clôt le passage et calcule aussitôt le résultat.",
      "06_12_theorie.png", "Feuille d'examen théorique : questions posées, réponses, points"),
    ...etape(13, "Épreuve de conduite",
      "Pour la conduite, l'application propose une grille : un critère par ligne avec ses points, une case « faute éliminatoire » quand le critère l'admet, une observation. La grille elle-même n'est pas imposée : elle vient de la configuration et reste à confirmer par l'ATT. Une faute éliminatoire vaut zéro sur le critère et fait perdre l'épreuve quel que soit le total, ce que l'écran annonce en rouge.",
      "06_13_conduite.png", "Grille de conduite : points par critère, faute éliminatoire, observations"),
    ...etape(14, "Résultat, validation et correction",
      "Le résultat est calculé automatiquement à la clôture de l'épreuve, avec la version du barème en vigueur, copiée dans le résultat pour rester lisible même si le barème change. Il attend la validation de l'administrateur ATT ; jusque-là, ni l'auto-école ni le candidat ne le voient. Une erreur de notation se corrige en rouvrant la feuille de l'examinateur puis en recalculant : l'application crée un nouveau résultat avec un motif, l'ancien reste en base et lisible, marqué « remplacé ».",
      "06_14_resultat.png", "Détail d'un résultat avant validation par l'ATT"),
    ...etape(15, "Échec et nouvelle tentative",
      "En cas d'échec, l'écran indique les épreuves à repasser et la date à partir de laquelle le candidat peut être réinscrit, d'après le délai de repassage configuré ; une épreuve réussie reste acquise pendant la durée configurée. Le prochain passage portera le numéro suivant : rien n'est écrasé, le parcours complet reste visible.",
      "06_15_repassage.png", "Résultat en échec : épreuves à repasser et date de réinscription"),
    ...etape(16, "Consultation par rôle",
      "Chacun voit ce qui le concerne. L'auto-école suit ses candidats, leurs inscriptions et leurs résultats validés ; le candidat qui dispose d'un compte lit son parcours, dossier, convocations, passages et résultats ; l'ATT voit tout et filtre par région. Un candidat sans smartphone n'est pas pénalisé : son auto-école et l'ATT ont la même information, et les documents s'impriment.",
      "06_16_parcours.png", "Parcours du candidat, vu avec son compte"),
    ...etape(17, "Impression",
      "Quatre documents s'impriment depuis l'application, sans réseau : la convocation individuelle avec l'heure de passage estimée et les pièces à apporter, la liste d'appel d'une session (avec ou sans noms selon le rôle), la liste des admis et le relevé de résultat. La page est produite en HTML, montrée en aperçu, puis confiée au service d'impression d'Android, qui permet aussi d'enregistrer un PDF.",
      "06_17_releve.png", "Relevé de résultat, prêt à imprimer"),
    ...etape(18, "Historique",
      "Toute modification sensible est enregistrée avec son auteur, sa date, l'action, l'ancienne et la nouvelle valeur et le motif, dans la même transaction que la modification elle-même. L'historique se lit depuis chaque fiche et depuis un écran global, filtrable par période, par objet et par utilisateur.",
      "06_18_historique.png", "Journal des modifications, filtrable"),
    ...etape(19, "Synchronisation avec le serveur central",
      "L'application fonctionne entièrement hors ligne : la base locale est la source de vérité. Les résultats validés sont remontés à un serveur central de l'ATT quand le réseau le permet ; sinon ils restent en file d'attente, sans rien bloquer, et « Synchroniser maintenant » les renvoie plus tard. Sur la figure, le réseau est coupé par l'interrupteur de démonstration : un résultat attend, l'autre a déjà été envoyé.",
      "06_19_synchronisation.png", "Synchronisation : réseau coupé, un résultat en attente, un envoyé"),
  ];
}

function architecture() {
  return [
    titre1("7. Architecture technique"),
    titre2("7.1 Vue d'ensemble"),
    ...paras("L'application suit l'architecture vue en cours : l'écran affiche et signale, le ViewModel porte l'état et décide, le DAO stocke. Les règles métier sont des fonctions pures, sans dépendance Android, testées par JUnit."),
    ...bloc([
      "Ecran* (Compose)  →  XxxViewModel (StateFlow<EtatXxx>)  →  XxxDao (Room)  →  AppDatabase",
      "      ↑ l'état descend (lecture seule)        ↓ les événements remontent (fonctions)",
      "                    ↘ metier/ : fonctions pures (calculs, contrôles), testées par JUnit",
    ]),
    espace(),
    tableau(["Couche", "Rôle", "Exemples"], [
      ["ui/", "Écrans Compose et ViewModels, un dossier par fonctionnalité", "ui/candidats, ui/sessions, ui/evaluation, ui/resultats"],
      ["ui/communs", "Composants partagés : cadre d'écran, champs, sélecteurs, cartes, encarts, pastilles", "EcranStandard, ChampTexte, CarteIcone, EncartInfo"],
      ["metier/", "Règles pures : inscriptions, présence, tentatives, théorie, conduite, calcul du résultat, repassage", "ReglesInscription, CalculResultat, ReglesTheorie"],
      ["data/", "Entités Room, DAO, base, données initiales, traçage", "Entites*.kt, *Dao.kt, AppDatabase, Tracage"],
      ["securite/", "Hachage des mots de passe (PBKDF2 + sel)", "MotDePasse"],
      ["Navigation.kt", "Un seul NavHost, routes en chaînes, identifiants en argument, écrans recevant des lambdas", "graphSessions, graphResultats"],
    ], [18, 44, 38]),
    espace(),
    titre2("7.2 Modèle de données"),
    ...paras("Vingt-cinq entités Room, reliées par des clés étrangères, en six familles. Le schéma complet, avec chaque attribut et chaque statut, est dans le document docs/03 du dépôt ; il est exporté par Room dans app/schemas à chaque version."),
    tableau(["Famille", "Entités", "Choix de conception"], [
      ["Référentiels", "Region, CategoriePermis, TypeEpreuve, Bareme, Question, CriterePratique, RegleConfig, Centre", "Tout ce qui est une règle administrative est une ligne de table, jamais une constante ; le barème est versionné (une version se ferme, ne se modifie pas)"],
      ["Acteurs", "Utilisateur, AutoEcole, Examinateur, Candidat", "Un compte porte son rôle et, selon le rôle, l'auto-école, l'examinateur ou le candidat qu'il représente ; le compte candidat est facultatif"],
      ["Dossiers", "Dossier, PieceDossier", "Un dossier par catégorie, avec ses pièces déclarées ; statuts brouillon, soumis, validé, incomplet, refusé"],
      ["Planification", "Session, Creneau, Inscription, Presence", "Une session = une épreuve, un centre, une date ; l'inscription porte le numéro d'appel et le créneau ; la présence est une ligne à part, corrigeable"],
      ["Examens", "Tentative, Evaluation, ReponseCandidat, EvaluationCritere, Resultat", "Une tentative n'est jamais effacée ; l'évaluation fige la version du barème ; un résultat ne se modifie pas, une correction en crée un nouveau qui pointe vers l'ancien"],
      ["Traçabilité", "Historique", "Auteur, date, action, ancienne et nouvelle valeur, motif : écrit dans la même transaction que la modification"],
    ], [18, 34, 48]),
    titre2("7.3 Flux d'un écran"),
    ...paras("Exemple type : l'écran des inscriptions collecte un StateFlow construit par combine sur les flux Room ; chaque action appelle une fonction du ViewModel qui vérifie la règle pure puis écrit dans une transaction avec sa ligne d'historique."),
    ...paras("L'annexe B montre les quatre briques de ce flux : l'écriture et sa ligne d'historique dans une même transaction (B.1), une règle pure et son test (B.2), un ViewModel qui combine les flux de Room en un seul état (B.3) et le tirage du sujet avec le hasard injecté (B.4)."),
    titre2("7.4 Sécurité et hors ligne"),
    ...puces(
      "Mots de passe jamais en clair : empreinte PBKDF2 avec sel, comparaison d'empreintes.",
      "Session utilisateur en mémoire, reconnexion à chaque lancement ; chaque ViewModel filtre ses requêtes selon le rôle.",
      "Base Room locale, source de vérité ; aucune fonction ne dépend du réseau.",
    ),
    titre2("7.5 Hors ligne d'abord et synchronisation"),
    ...paras(
      "Les centres d'examen n'ont pas tous une connexion fiable. L'application applique le principe du cours : l'écran lit toujours la base locale, qui est la source de vérité ; le réseau ne fait que la nourrir. Une coupure n'est pas une panne, c'est un retard de synchronisation.",
      "Concrètement, un résultat validé par l'ATT porte un drapeau « synchronisé » à faux. La validation l'écrit en base puis tente aussitôt de le remonter au serveur central ; si le réseau manque, il reste dans la file d'attente, qui n'est rien d'autre qu'une requête sur ce drapeau. L'écran de synchronisation montre la file, le bouton « Synchroniser maintenant » et ce que le serveur a reçu. Le serveur est simulé en mémoire, avec un interrupteur réseau pour la démonstration ; une vraie interface réseau le remplacerait sans changer le reste du code.",
    ),
    ...bloc([
      "suspend fun synchroniserResultats(db: AppDatabase): Int {",
      "    var envoyes = 0",
      "    for (resultat in db.resultatDao().enAttenteDeSynchronisation()) {   // la file = WHERE synchronisee = 0",
      "        val recu = FauxServeurATT.envoyer(libelleServeur(db, resultat))",
      "        if (!recu) break                                                  // réseau coupé : on réessaiera",
      "        db.resultatDao().marquerSynchronise(resultat.id)",
      "        envoyes += 1",
      "    }",
      "    return envoyes",
      "}",
    ]),
    espace(),
    titre2("7.6 Environnement de développement"),
    tableau(["Élément", "Version"], [
      ["Kotlin (intégré à AGP)", "2.2.10"], ["Android Gradle Plugin", "9.3.2"], ["Jetpack Compose (BOM)", "2026.08.00, Material 3"],
      ["Navigation Compose", "2.10.1"], ["Lifecycle ViewModel", "2.11.0"], ["Room (KSP)", "2.8.5"], ["Java", "17"], ["Android minimum", "8.0 (API 26)"],
    ], [50, 50]),
  ];
}

function regles() {
  return [
    titre1("8. Règles configurables et valeurs à confirmer"),
    ...paras("Aucun barème, seuil, délai, capacité ou catégorie n'est écrit dans le code. Tout est en base, modifiable par le Super Admin, et chaque valeur non validée par l'ATT porte le drapeau « à confirmer », visible à l'écran."),
    tableau(["Règle", "Valeur par défaut", "Statut"], [
      ["Nombre maximal de passages par épreuve", "0 (illimité)", "à confirmer"],
      ["Délai minimal entre deux passages", "25 jours", "à confirmer"],
      ["Durée d'acquisition d'une épreuve réussie", "365 jours", "à confirmer"],
      ["Tolérance de retard", "15 minutes", "à confirmer"],
      ["Sort d'un absent", "nouvelle inscription", "à confirmer"],
      ["Capacité d'une session / d'un créneau", "30 / 5", "à confirmer"],
      ["Durée et marge d'un créneau", "15 / 5 minutes", "à confirmer"],
      ["Théorie réussie avant la conduite", "oui", "à confirmer"],
      ["Mode de choix des questions", "tirage au sort", "à confirmer"],
      ["Pièces du dossier", "liste du portail officiel Torolalana", "officiel"],
      ["Catégories et âges minimums", "A', A, B, C, D, E ; 16 / 18 / 21 ans", "liste officielle, âges à confirmer"],
    ], [44, 32, 24]),
    espace(),
  ];
}

function qualite() {
  return [
    titre1("9. Qualité, tests et traçabilité"),
    titre2("9.1 Tests unitaires"),
    ...paras("Les règles métier sont testées par JUnit, sans émulateur : tirage du sujet reproductible, contrôles d'inscription, calcul du résultat, règles de repassage, contraintes du cadrage. Le tirage au sort reçoit son générateur de hasard en paramètre, ce qui le rend reproductible en test."),
    ...paras(`Au moment de la rédaction, ${nombreTests()} tests unitaires passent. Les classes de test, une par famille de règles :`),
    ...puces(...classesDeTest()),
    titre2("9.2 Cas particuliers du cadrage"),
    tableau(["Contrainte", "Comment elle est couverte"], [
      ["Dossier incomplet ou refusé", "Décision avec motif obligatoire, resoumission possible, historique"],
      ["Candidat non éligible", "Âge minimum de la catégorie, dossier validé de la bonne catégorie"],
      ["Session complète", "Capacité de session et de créneau contrôlées à l'inscription"],
      ["Absence, retard, report, annulation", "Statuts de présence, tolérance configurable, report et annulation avec motif, place libérée"],
      ["Erreur de notation", "Correction en nouvelle ligne de résultat, l'ancienne reste lisible"],
      ["Échec et nouvelle tentative", "Numéro de passage jamais réutilisé, délai et nombre maximal configurables"],
      ["Examinateur indisponible", "Aucune affectation préalable : tout examinateur connecté peut évaluer"],
      ["Conflit de créneaux", "Avertissement à la création de session, refus d'une seconde inscription le même jour"],
      ["Candidat sans smartphone", "Compte facultatif, documents imprimables, saisie par l'ATT ou l'auto-école"],
      ["Panne réseau ou électricité", "Application hors ligne, listes imprimées avant la session"],
    ], [34, 66]),
    espace(),
    titre2("9.3 Traçabilité"),
    ...paras("Chaque écriture sensible est enregistrée dans la même transaction que sa ligne d'historique : soit les deux sont écrites, soit aucune. L'historique conserve l'auteur, la date, l'action, le motif, l'ancienne et la nouvelle valeur, et se consulte depuis chaque fiche et depuis un écran global filtrable."),
  ];
}

function deroulement() {
  return [
    titre1("10. Déroulement du projet"),
    titre2("10.1 Méthode"),
    ...puces(
      "Cadrage d'abord : règles de développement, plan de travail, modèle de données et cas d'utilisation validés avant la première ligne de code.",
      "Une étape à la fois, chacune sur sa branche Git avec une pull request relue avant fusion, un compte rendu et un critère de fin vérifié.",
      "Usage de l'IA encadré par le protocole du module : chaque contribution est relue, commentée et jugée dans un journal.",
      "Vérification systématique sur émulateur, avec un pilote qui rejoue les scénarios par le texte des boutons.",
    ),
    titre2("10.2 Étapes réalisées"),
    tableau(["Étape", "Contenu"], [
      ["Cadrage", "Analyse des exigences, acteurs et permissions, modèle de données, cas d'utilisation et écrans"],
      ["Socle", "Projet Gradle et Compose, base Room et données initiales, historique et transactions"],
      ["Authentification et comptes", "Connexion, rôles, hachage, comptes administrateurs, examinateurs, auto-écoles, changement de mot de passe"],
      ["Auto-écoles, candidats, dossiers", "Fiches, pièces, soumission, décision de l'ATT avec motif"],
      ["Configuration", "Catégories, épreuves, barèmes versionnés, questions, critères, règles, centres"],
      ["Sessions et créneaux", "Création, génération des créneaux, statuts, annulation"],
      ["Inscriptions et présence", "Contrôles d'éligibilité et de capacité, numéro d'appel, convocation, appel, retards et absences"],
      ["Tentatives et évaluation", "Passages numérotés, épreuve théorique orale, structure de l'épreuve de conduite"],
      ["Résultats", "Calcul selon le barème, validation, correction traçable, règles de repassage"],
      ["Consultation, historique, impression", "Vues par rôle, journal filtrable, convocation, listes, relevé"],
      ["Tests et qualité", "Tests des contraintes du cadrage, relecture croisée, schéma figé, refonte de l'interface"],
    ], [30, 70]),
    espace(),
    titre2("10.3 Durée réelle et bilan"),
    ...paras(
      "Le cadrage (règles, plan, modèle de données, cas d'utilisation) a été écrit avant la première ligne de code, en une journée, et n'a presque pas bougé ensuite : c'est ce qui a permis d'enchaîner les étapes sans revenir en arrière. Le socle technique et les premières fonctionnalités ont suivi en deux jours ; les évaluations, les résultats, la consultation et l'impression en deux jours de plus ; puis les tests, la relecture croisée, la refonte de l'interface et ce rapport.",
      "Ce qui a pris plus de temps que prévu : comprendre le vrai déroulement de l'examen. La première version de l'épreuve théorique était un questionnaire à choix multiples, construit d'après la presse ; elle a été refaite le jour même, après notre propre passage de l'examen, en feuille orale. La leçon vaut pour tout le projet : un témoignage de terrain vaut plus qu'un article, et ce que l'on ne sait pas doit rester une configuration marquée « à confirmer », pas une hypothèse gravée dans le code.",
    ),
  ];
}

function coursAppliques() {
  return [
    titre1("11. Les cours appliqués dans le projet"),
    ...paras("Le module a été construit comme « une application, sept couches, une couche par séance ». Ce chapitre montre, séance par séance, où et comment chaque notion est mise en œuvre dans l'application."),
    tableau(["Séance", "Notions du cours", "Où et comment dans l'application"], [
      ["1 — Kotlin essentiel", "data class, val et copy, null safety sans !!, when, collections (map, filter, groupBy, sumOf, sortedBy)", "Toutes les entités et tous les états d'écran sont des data class immuables modifiées par copy ; les règles métier (metier/) sont écrites sans boucle explicite quand une fonction de collection suffit : tirage du sujet, total des points, filtrage des candidats éligibles ; l'opérateur !! est interdit par nos règles"],
      ["2 — Coroutines", "suspend, launch, Flow ; jamais bloquer le thread de l'interface ; runBlocking réservé aux tests", "Toute écriture passe par viewModelScope.launch ; les DAO exposent des fonctions suspend pour les écritures et des Flow pour les lectures ; le faux serveur simule l'aller-retour réseau par delay, sans bloquer l'écran"],
      ["3 — Anatomie Android", "Activity, cycle de vie, manifeste, Intents, Logcat, pile de retour", "Une seule MainActivity ; l'état survit à la rotation parce qu'il vit dans les ViewModels ; l'impression utilise le service d'Android ; la pile de retour est gérée par Navigation Compose (popBackStack, popUpTo à la connexion et à la déconnexion)"],
      ["4 — Jetpack Compose", "L'interface est une fonction de l'état ; @Composable, Column, Row, Card, LazyColumn, Modifier, remember pour l'état purement local", "Tous les écrans sont des composables qui affichent un état et signalent des gestes ; les listes sont des LazyColumn ; l'ouverture d'un menu déroulant est le seul état local (remember) ; un jeu de composants communs (cadre d'écran, champs, cartes, encarts) évite la répétition"],
      ["5 — Navigation", "Navigation Compose, routes en chaînes, identifiant en argument, écrans qui reçoivent des lambdas", "Un NavHost, des routes comme session/{sessionId}/appel, l'identifiant relu par toIntOrNull sans plantage ; aucun écran ne connaît le navController : l'écran signale, la navigation décide"],
      ["6 — Architecture MVVM", "UI, ViewModel, source de données ; StateFlow et flux unidirectionnel garanti par les types ; ViewModel partagé", "Un ViewModel par fonctionnalité, un StateFlow d'état immuable par écran, MutableStateFlow privé ; ViewModel de session partagé au-dessus de la navigation, ViewModels de sous-parcours partagés entre liste, fiche et formulaire"],
      ["7 — Room et hors ligne d'abord", "Entity, DAO, Database, Flow ou suspend, SQL vérifié à la compilation, combine et stateIn, base locale source de vérité, synchronisation avec drapeau", "Vingt-cinq entités avec clés étrangères et index, requêtes Flow combinées par combine puis stateIn ; la base locale est la seule source ; les résultats validés portent un drapeau synchronisé et sont remontés au serveur central par une file d'attente, exactement comme la démonstration du cours"],
      ["8 — Synthèse", "Une application, sept couches", "Le projet parcourt les sept couches, du langage aux données, avec une étape du plan par couche puis par fonctionnalité"],
    ], [17, 33, 50]),
    espace(),
    titre2("11.1 Ce qui dépasse le cours, et pourquoi"),
    ...paras("Quelques notions non vues en cours ont été nécessaires ; chacune est expliquée dans un document du projet avant son usage, avec l'équivalent vu en cours et la justification :"),
    ...puces(
      "clés étrangères et index Room, transactions (une écriture et sa ligne d'historique ensemble ou pas du tout) ;",
      "hachage des mots de passe avec sel (bibliothèque standard Java) ;",
      "flatMapLatest (une fiche qui suit un identifiant qui change), menus déroulants, grille de tuiles, rangées qui passent à la ligne ;",
      "affichage d'une page HTML pour l'impression et service d'impression d'Android ;",
      "fonctions pures testées par JUnit, avec le hasard injecté pour rendre le tirage reproductible.",
    ),
    ...paras(
      "Le tableau complet, avec pour chaque notion l'étape où elle est apparue, l'équivalent vu en cours et la décision du binôme, est le fichier docs/HORS_COURS.md du dépôt (vingt-sept entrées). Deux exemples de ce que nous avons refusé ou remplacé : le Repository, proposé au départ pour « faire propre », a été écarté parce que le cours n'en a jamais écrit et que les ViewModels appellent les DAO directement sans perdre en clarté ; les boutons radio de la première épreuve théorique ont disparu avec le questionnaire à choix multiples, remplacés par les champs de texte du cours.",
    ),
  ];
}

function limites() {
  return [
    titre1("12. Limites et perspectives"),
    titre2("11.1 Limites"),
    ...puces(
      "Les barèmes, seuils, délais et la grille de conduite sont des valeurs d'exemple en attente de validation par l'ATT.",
      "Le serveur central est simulé en mémoire : la synchronisation est démontrée, pas raccordée à une infrastructure réelle.",
    ),
    titre2("11.2 Perspectives"),
    ...puces(
      "Photos des pièces : relire l'orientation enregistrée par l'appareil, chiffrer le stockage si l'ATT le demande.",
      "Remplacer le serveur simulé par un vrai serveur de l'ATT : la file d'attente et la boucle de synchronisation restent les mêmes.",
      "Interface en malgache.",
      "Passage de relais au CIM après validation du résultat.",
    ),
    titre1("13. Conclusion"),
    ...paras(
      "Nous étions partis d'un cahier de cadrage et d'une question simple : pourquoi passer l'examen du permis reste-t-il si long et si opaque, pour le candidat comme pour l'administration ? En suivant un candidat de son dossier à son résultat, nous avons construit une application qui couvre toute la chaîne de l'ATT : dossiers vérifiés avant la convocation, sessions découpées en créneaux, appel par numéro, feuille d'examen à l'écran, résultat calculé selon un barème configurable, validé, corrigeable sans rien effacer, imprimable, et remonté à un serveur central quand le réseau le permet.",
      "Ce projet nous a beaucoup intéressées, et d'abord parce qu'il est vrai. Ce que nous avons observé nous-mêmes le jour de l'examen, un candidat renvoyé pour une pièce manquante, un « présent » que personne n'entend dans la foule, trouve une réponse concrète dans ce que nous avons construit. Nous avons aussi appris, au passage, à ne pas inventer une règle administrative que nous ne connaissions pas : tout ce qui reste incertain est marqué « à confirmer » et attend l'ATT.",
      "Il faut aussi être honnêtes : ce que nous livrons est le minimum qu'un projet universitaire, mené en quelques semaines à deux, pouvait atteindre. Ça pourrait être tellement mieux. Les barèmes et la grille de conduite sont des exemples ; le serveur est simulé ; les pièces du dossier ne sont pas encore jointes en photo ; l'interface mériterait d'être testée avec de vrais agents dans un vrai centre. Aucune de ces limites n'est un mur : chacune est une étape déjà nommée, et l'architecture a été pensée pour les accueillir sans tout reprendre.",
      "Nous espérons qu'un jour l'État, à travers l'ATT, se saisira d'un outil de ce genre, celui-ci ou un autre, parce que le besoin est là et que les candidats, les auto-écoles et les agents y gagneraient tous. Si ce travail a au moins montré que c'était possible avec des moyens modestes, en respectant les règles et en gardant le candidat sans smartphone au centre, alors il aura servi à quelque chose.",
    ),
  ];
}

function annexes() {
  return [
    titre1("Annexe A — Captures d'écran"),
    ...paras("Les captures du chapitre 6 ont été prises sur l'émulateur Android d'un petit téléphone (720 × 1280), avec les comptes de démonstration, en rejouant le parcours complet depuis une base vide. Elles sont regroupées ici par étape ; les fichiers se trouvent dans le dossier docs/rapport/captures du dépôt."),
    tableau(["Figure", "Écran", "Fichier"], [
      ["1", "Accueil de l'administrateur ATT", "05_accueil_admin.png"],
      ["2", "Connexion", "06_01_connexion.png"], ["3", "Configuration : épreuve théorique B", "06_02_configuration.png"], ["4", "Centres d'examen", "06_03_centre.png"],
      ["5", "Fiche d'une auto-école", "06_04_auto_ecole.png"], ["6", "Création d'un examinateur", "06_05_examinateur.png"], ["7", "Dossier du candidat", "06_06_dossier.png"],
      ["8", "Décision de l'ATT sur le dossier", "06_07_decision.png"], ["9", "Création d'une session", "06_08_session.png"], ["10", "Inscriptions", "06_09_inscription.png"],
      ["11", "Appel", "06_10_appel.png"], ["12", "Passages (examinateur)", "06_11_passages.png"], ["13", "Épreuve théorique", "06_12_theorie.png"],
      ["14", "Épreuve de conduite", "06_13_conduite.png"], ["15", "Résultat à valider", "06_14_resultat.png"], ["16", "Échec et repassage", "06_15_repassage.png"],
      ["17", "Parcours du candidat", "06_16_parcours.png"], ["18", "Relevé imprimable", "06_17_releve.png"], ["19", "Historique", "06_18_historique.png"], ["20", "Synchronisation", "06_19_synchronisation.png"],
    ], [12, 50, 38]),
    titre1("Annexe B — Extraits de code commentés"),
    titre2("B.1 Écriture et historique dans une même transaction"),
    ...bloc([
      "db.withTransaction {",
      "    val modifiee = presence.copy(statut = StatutPresence.PRESENT)",
      "    db.presenceDao().modifier(modifiee)",
      "    db.tracer(EntitesHistorique.PRESENCE, presence.id, ActionsHistorique.MODIFICATION,",
      "        utilisateur.id, ancienneValeur = presence.resume(), nouvelleValeur = modifiee.resume())",
      "}",
    ]),
    espace(),
    ...paras("La modification et sa trace sont dans le même bloc : Room les écrit ensemble ou n'écrit rien. L'historique ne peut donc jamais raconter autre chose que ce qui s'est passé."),
    titre2("B.2 Une règle pure et son test"),
    ...bloc([
      "// metier/ReglesTentatives.kt",
      "fun peutOuvrir(statutPresence: StatutPresence?, tentativeDeCetteInscription: Tentative?,",
      "               nombreTentatives: Int, tentativesMax: Int): String? = when {",
      "    tentativeDeCetteInscription != null -> when (tentativeDeCetteInscription.statut) {",
      "        StatutTentative.EN_COURS -> null                         // reprise du passage en cours",
      "        else -> \"Un passage existe déjà pour cette inscription.\"",
      "    }",
      "    statutPresence != StatutPresence.PRESENT && statutPresence != StatutPresence.EN_COURS ->",
      "        \"Le candidat n'est pas marqué présent (appel).\"",
      "    tentativesMax > 0 && nombreTentatives >= tentativesMax -> \"Nombre maximal de passages atteint.\"",
      "    else -> null",
      "}",
      "",
      "// test/ReglesTentativesTest.kt",
      "@Test fun `ouverture reservee aux presents`() {",
      "    assertNull(ReglesTentatives.peutOuvrir(StatutPresence.PRESENT, null, 0, 0))",
      "    assertNotNull(ReglesTentatives.peutOuvrir(StatutPresence.ABSENT, null, 0, 0))",
      "}",
    ]),
    ...paras("La fonction ne connaît ni Android ni la base : elle reçoit des valeurs et rend un message d'erreur ou null. Le ViewModel fait les requêtes, la fonction fait le jugement, le test JUnit tourne sans téléphone."),
    titre2("B.3 Un ViewModel branché sur Room"),
    ...bloc([
      "val etat: StateFlow<EtatTentatives> =",
      "    idSession.flatMapLatest { id ->",
      "        combine(db.sessionDao().parIdEnDirect(id), db.inscriptionDao().parSession(id),",
      "                db.presenceDao().parSession(id), db.tentativeDao().parSession(id)) { s, i, p, t -> ... }",
      "    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatTentatives())",
    ]),
    ...paras("L'écran collecte un seul état immuable, recalculé par Room à chaque changement d'une des tables : aucune lecture manuelle, aucun rafraîchissement à déclencher."),
    titre2("B.4 Le tirage du sujet, reproductible"),
    ...bloc([
      "fun tirerSujet(questionsActives: List<Question>, noteMax: Double, aleatoire: Random = Random.Default): List<Question> {",
      "    val sujet = mutableListOf<Question>(); var total = 0.0",
      "    for (q in questionsActives.filter { it.actif && it.points > 0 }.shuffled(aleatoire)) {",
      "        if (total >= noteMax) break",
      "        if (total + q.points <= noteMax) { sujet += q; total += q.points }",
      "    }",
      "    return sujet",
      "}",
    ]),
    ...paras("Le hasard est un paramètre : en production, le générateur par défaut ; en test, une graine fixe, et le test vérifie que le sujet atteint la note maximale sans la dépasser ni répéter une question."),
    titre1("Annexe C — Sources"),
    tableau(["Sujet", "Source", "Fiabilité"], [
      ["Pièces du dossier, frais, étapes officielles", "Portail Torolalana (gouvernement) — torolalana.gov.mg", "Officiel"],
      ["Code de la route, catégories", "Loi n° 2017-002 du 6 juillet 2017 ; décret 2026-974", "Officiel"],
      ["Statut de l'ATT", "Décret n° 2006-279 du 25 avril 2006", "Officiel"],
      ["Anonymat, lutte anti-corruption, digitalisation", "L'Express de Madagascar, Newsmada, Studio Sifaka (2024–2026)", "Presse"],
      ["Volumes : candidats, centres, examinateurs", "Newsmada, juillet 2024", "Presse"],
      ["Déroulement des épreuves, délai de repassage", "Sites d'auto-écoles (fiarakodia, terraformalis), témoignage du binôme", "Secondaire / témoignage"],
      ["Régions de Madagascar", "Loi 2004-001, lois 2021 et 2023 ; Wikipédia pour la liste", "Officiel / secondaire"],
      ["Android, Kotlin, Compose, Room", "developer.android.com, kotlinlang.org, supports du cours", "Officiel"],
    ], [30, 50, 20]),
    espace(),
    ...paras("Adresses principales :"),
    ...puces(
      "torolalana.gov.mg/fr/services/obtenir-son-permis-de-conduire (portail officiel : étapes, pièces, frais)",
      "lexpress.mg, newsmada.com, studiosifaka.org (articles 2020 à 2026 : anonymat, digitalisation, volumes, formation des examinateurs)",
      "fiarakodia.com, terraformalis.com (sites d'auto-écoles : déroulement, délais, catégories)",
      "fr.wikipedia.org/wiki/Région_de_Madagascar (liste des 24 régions)",
      "developer.android.com, m3.material.io, kotlinlang.org (documentation technique)",
    ),
    ...paras("Le détail des sources, avec le niveau de fiabilité et la citation exacte, est dans le fichier docs/references/RECHERCHE_ATT_ET_ANDROID.md du dépôt."),
    titre1("Annexe D — Glossaire"),
    tableau(["Terme", "Sens dans l'application"], [
      ["Passage (tentative)", "Un candidat passe une épreuve une fois ; numéroté, jamais effacé"],
      ["Inscription", "Convocation d'un candidat à une session, avec numéro d'appel et créneau"],
      ["Session", "Une épreuve d'une catégorie, dans un centre, à une date, avec des créneaux"],
      ["Dossier", "Les pièces du candidat pour une catégorie, validées par l'ATT"],
      ["Barème", "Note maximale et seuil d'une épreuve, versionné"],
      ["À confirmer", "Valeur d'exemple en attente de validation par l'ATT"],
    ], [30, 70]),
    espace(),
    titre1("Annexe E — Comptes de démonstration"),
    ...paras("Comptes disponibles au premier lancement d'une version de développement ; les trois derniers n'existent pas dans la version livrée."),
    tableau(["Rôle", "Identifiant"], [
      ["Super administrateur", "superadmin"], ["Administrateur ATT", "admin"], ["Auto-école", "autoecole"], ["Examinateur", "examinateur"], ["Candidat", "candidat"],
    ], [50, 50]),
    espace(),
    titre1("Annexe F — Questions à valider avec l'ATT"),
    ...paras("Aucune de ces règles n'a été inventée : chacune a une valeur par défaut « à confirmer », modifiable dans l'application, et une question à poser à l'ATT."),
    tableau(["Question", "Ce que nous avons retenu en attendant"], [
      ["Q1 — Épreuve théorique : note maximale, seuil, notation", "Orale (témoignage) ; questions à points variables ; barème d'exemple 20 / 12"],
      ["Q1 bis — Choix des questions", "Tirage au sort par l'application, ou choix de l'examinateur (règle MODE_THEORIE)"],
      ["Q2 — Épreuve de conduite : grille", "Aucune grille préchargée ; critères, points et fautes éliminatoires configurables"],
      ["Q2 bis — Sujet ou grille qui ne totalise pas la note max", "Note rapportée au barème (règle de trois)"],
      ["Q3 — Repassage", "25 jours entre deux passages, nombre illimité, épreuve réussie acquise 365 jours"],
      ["Q4 — Appel, retard, absence", "Tolérance 15 minutes ; absent = nouvelle inscription ; appel par numéro"],
      ["Q5 — Sessions", "Capacité 30, créneaux de 15 minutes avec 5 de marge, 5 candidats par créneau"],
      ["Q6 — Examinateur", "Aucune affectation préalable ; l'ATT valide chaque résultat"],
      ["Q7 — Dossier", "Pièces du portail Torolalana ; photo ou PDF joint par pièce, facultatif, stocké dans l'application"],
      ["Q8 — Qui inscrit", "L'ATT ; l'auto-école peut demander si la règle l'autorise"],
      ["Q9 — Théorie avant conduite", "Oui (règle configurable)"],
      ["Q10 — Catégories et âges", "A', A, B, C, D, E ; 16 / 18 / 21 ans, à confirmer"],
      ["Q11 — Régions", "24 régions ; centres et auto-écoles rattachés ; examen hors région autorisé par défaut"],
      ["Q12 — Formats locaux", "CIN et téléphone en texte, validation souple"],
      ["Q14 — Auto-école qui demande son agrément", "Non dans le MVP : créée par l'ATT"],
    ], [42, 58]),
  ];
}

// ---------- DOCUMENT ----------

const document = new Document({
  creator: AUTEURS.join(", "),
  title: "ATT — Rapport technique",
  description: "Digitalisation des examens du permis de conduire — application Android",
  features: { updateFields: true },
  styles: {
    default: { document: { run: { font: POLICE, size: 22 } } },
    paragraphStyles: [
      { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true, run: { font: POLICE, size: 32, bold: true, color: BLEU }, paragraph: { spacing: { before: 360, after: 200 }, outlineLevel: 0 } },
      { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true, run: { font: POLICE, size: 26, bold: true, color: BLEU_FONCE }, paragraph: { spacing: { before: 280, after: 140 }, outlineLevel: 1 } },
      { id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true, run: { font: POLICE, size: 23, bold: true, color: BLEU_FONCE }, paragraph: { spacing: { before: 200, after: 100 }, outlineLevel: 2 } },
    ],
  },
  numbering: {
    config: [
      { reference: "puces", levels: [{ level: 0, format: LevelFormat.BULLET, text: "•", alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 560, hanging: 280 } } } }] },
      { reference: "numeros", levels: [{ level: 0, format: LevelFormat.DECIMAL, text: "%1.", alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 560, hanging: 280 } } } }] },
    ],
  },
  sections: [
    {
      properties: { page: { margin: { top: 1440, bottom: 1440, left: 1440, right: 1440 } }, titlePage: true },
      headers: {
        default: new Header({ children: [new Paragraph({ alignment: AlignmentType.RIGHT, border: { bottom: { style: BorderStyle.SINGLE, size: 6, color: "B7C3D1", space: 4 } }, children: [texte("ATT — Digitalisation des examens du permis de conduire · Rapport technique", { size: 17, color: GRIS })] })] }),
        first: new Header({ children: [new Paragraph({ children: [] })] }),
      },
      footers: {
        default: new Footer({ children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [texte("Page ", { size: 17, color: GRIS }), new TextRun({ children: [PageNumber.CURRENT], font: POLICE, size: 17, color: GRIS }), texte(" / ", { size: 17, color: GRIS }), new TextRun({ children: [PageNumber.TOTAL_PAGES], font: POLICE, size: 17, color: GRIS })] })] }),
        first: new Footer({ children: [new Paragraph({ children: [] })] }),
      },
      children: [
        ...pageDeGarde(),
        saut(),
        new Paragraph({ children: [texte("Sommaire", { bold: true, size: 32, color: BLEU })], spacing: { after: 240 } }),
        new TableOfContents("Sommaire", { hyperlink: true, headingStyleRange: "1-2" }),
        ...introduction(),
        ...problematique(),
        ...objectifs(),
        ...solution(),
        ...acteurs(),
        ...parcours(),
        ...architecture(),
        ...regles(),
        ...qualite(),
        ...deroulement(),
        ...coursAppliques(),
        ...limites(),
        ...annexes(),
      ],
    },
  ],
});

/**
 * Word (surtout sur Mac) refuse d'afficher les images quand plusieurs partagent le même identifiant `wp:docPr`
 * (« Nous ne pouvons pas afficher l'image »). La bibliothèque les numérote toutes 1 : on renumérote après coup
 * et on donne un nom à chaque image.
 */
async function numeroterImages(buffer) {
  const JSZip = require("./node_modules/jszip");
  const zip = await JSZip.loadAsync(buffer);
  const chemin = "word/document.xml";
  let xml = await zip.file(chemin).async("string");
  let n = 0;
  xml = xml.replace(/<wp:docPr id="\d+" name=""/g, () => { n += 1; return `<wp:docPr id="${n}" name="Figure ${n}"`; });
  xml = xml.replace(/<pic:cNvPr id="0" name=""/g, () => `<pic:cNvPr id="0" name="Image"`);
  zip.file(chemin, xml);
  console.log("Images renumérotées :", n);
  return zip.generateAsync({ type: "nodebuffer", compression: "DEFLATE" });
}

Packer.toBuffer(document).then(numeroterImages).then((buffer) => {
  const sortie = path.join(__dirname, "Rapport_technique_ATT.docx");
  fs.writeFileSync(sortie, buffer);
  console.log("Écrit :", sortie, Math.round(buffer.length / 1024), "Ko");
});
