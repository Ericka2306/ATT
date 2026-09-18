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
const ANNEE = "Année universitaire 2025-2026";
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
const FIGURES = [];
const figure = (nomFichier, legende, largeurCm = 7) => {
  numFigure += 1;
  FIGURES.push({ numero: numFigure, legende, fichier: nomFichier });
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
  return noms.filter((n) => !n.startsWith("Example")).sort();
}
function nombreEntites() {
  const dossier = path.join(RACINE_PROJET, "app", "src", "main", "java", "mg", "itu", "att", "data");
  let n = 0;
  for (const f of fs.readdirSync(dossier)) if (f.endsWith(".kt")) n += (fs.readFileSync(path.join(dossier, f), "utf8").match(/@Entity\(/g) || []).length;
  return n;
}
function nombreHorsCours() {
  const f = path.join(RACINE_PROJET, "docs", "HORS_COURS.md");
  return fs.existsSync(f) ? (fs.readFileSync(f, "utf8").match(/^\| \d+ \|/gm) || []).length : 0;
}
/** Une entrée du journal IA, recopiée telle quelle (le journal est la source, le rapport ne le réécrit pas). */
function entreeJournal(numero) {
  const f = path.join(RACINE_PROJET, "JOURNAL-IA.md");
  if (!fs.existsSync(f)) return [];
  const texteJournal = fs.readFileSync(f, "utf8");
  const debut = texteJournal.indexOf(`## Entrée ${numero} `);
  if (debut < 0) return [];
  const fin = texteJournal.indexOf("\n---", debut);
  const bloc = texteJournal.slice(debut, fin < 0 ? undefined : fin).split("\n").filter((l) => l.trim() !== "");
  // Le verdict du binôme s'écrit dans le fichier, à la main ; tant qu'il n'est pas rédigé, la ligne vide reste hors du rapport.
  const lignes = bloc.filter((l) => !l.includes("<à compléter")).map((l) => l.replace(/^## /, "").replace(/`/g, "").replace(/\*\*/g, ""));
  return [titre3(lignes[0]), ...lignes.slice(1).map((l) => para(texte(l, { size: 20 })))];
}
function nombreTests() {
  const dossier = path.join(RACINE_PROJET, "app", "build", "test-results", "testDebugUnitTest");
  if (!fs.existsSync(dossier)) return "plus de cent";
  let total = 0;
  for (const f of fs.readdirSync(dossier)) if (f.endsWith(".xml") && !f.includes("Example")) { const m = fs.readFileSync(path.join(dossier, f), "utf8").match(/tests="(\d+)"/); if (m) total += Number(m[1]); }
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
      "l'architecture, les règles configurables, la qualité, l'usage de l'IA, le déroulement du projet et l'application des cours du module (chapitres 7 à 12) ;",
      "les limites et perspectives, la conclusion, et les annexes : captures, extraits de code, sources, glossaire, comptes de démonstration, questions à valider, extraits du journal IA.",
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
    ...paras(
      "Passer l'examen du permis à Madagascar reste aujourd'hui un parcours long et opaque, pour le candidat comme pour l'administration. L'échelle donne la mesure du problème : environ 60 000 candidats par an, un taux de réussite d'environ 53 %, 173 auto-écoles dont une centaine à Antananarivo, 22 centres d'examen et 31 examinateurs pour tout le pays (presse, juillet 2024, annexe C). Les difficultés relevées sont les suivantes.",
    ),
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
      [gras("Évaluations et résultats : "), texte("feuilles d'examen papier, calcul manuel, corrections difficiles à retracer, résultats à consolider à la main entre les centres.")],
      [gras("Absence de trace : "), texte("qui a validé ce dossier, qui a corrigé cette note, quand et pourquoi : le papier ne le dit pas.")],
      [gras("Examinateurs : "), texte("une trentaine pour soixante mille candidats, qui se déplacent entre les centres et reportent leurs feuilles après coup ; l'outil doit d'abord leur faire gagner du temps.")],
      [gras("Connectivité et électricité incertaines : "), texte("un centre d'examen ne peut pas dépendre d'une connexion permanente pour fonctionner.")],
      [gras("Anonymat et intégrité : "), texte("l'appel par nom expose l'examinateur à la pression ; l'ATT a engagé un passage à l'anonymat des candidats.")],
      [gras("Règles administratives dispersées : "), texte("barèmes, seuils, délais de repassage et âges minimums ne sont pas publiés de façon accessible et peuvent changer.")],
    ),
    titre2("2.3 Ce que nous avons observé nous-mêmes"),
    ...paras(
      "L'une de nous a passé l'examen pendant le projet. Trois observations ont directement orienté la conception :",
    ),
    ...puces(
      [gras("L'épreuve théorique est orale : "), texte("l'examinateur pose des questions, note la réponse donnée et attribue des points sur une feuille, avec un total en bas ; il n'y a pas de questionnaire à choix multiples, contrairement à ce que laissait penser la presse.")],
      [gras("Le dossier se vérifie trop tard : "), texte("un candidat appelé avec une pièce manquante est renvoyé sur-le-champ. Le contrôle doit donc avoir lieu avant l'inscription à une session, et si possible sur pièces numériques, pour éviter le déplacement inutile.")],
      [gras("L'appel se fait dans la foule : "), texte("avec autant de candidats convoqués à la même heure, la personne qui fait l'appel n'entend pas toujours le « présent » et marque le candidat absent aussitôt, sans retour possible. Convoquer par créneau, avec peu de candidats à la fois, et pouvoir corriger un statut de présence règlent ces deux défauts : le « présent » non entendu et l'absence irrévocable.")],
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
      ["Centres, sessions, créneaux, inscriptions, convocations", "Serveur réel de l'ATT : le serveur central est simulé en mémoire pour démontrer le principe « hors ligne d'abord » de la séance 7"],
      ["Appel et présence, tentatives, évaluation théorique et de conduite", ""],
      ["Calcul, validation et correction des résultats", ""],
      ["Consultation par rôle, historique, impression", ""],
    ], [55, 45]),
    espace(),
    titre2("3.3 Contraintes imposées"),
    ...puces(
      "Application Android native, base locale : aucun serveur n'est nécessaire pour fonctionner.",
      "Technologies du cours uniquement : Kotlin, Jetpack Compose, Navigation Compose, ViewModel et StateFlow, Room, coroutines.",
      "Aucune règle administrative inventée : toute valeur inconnue est une configuration marquée « à confirmer ».",
      "Une tentative ou un résultat ne s'écrase jamais : toute correction crée une nouvelle ligne tracée.",
    ),
  ];
}

function solution() {
  return [
    titre1("4. Solution proposée, point par point"),
    ...paras("Chaque difficulté du chapitre 2 est reprise ci-dessous avec la réponse apportée par l'application et le module concerné. Les modules sont nommés par leur fonction ; leur correspondance avec les dossiers du code est donnée au chapitre 7.1. Les captures sont au chapitre 6."),
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
      ["Connectivité incertaine dans les centres", "Application « hors ligne d'abord » : elle fonctionne sans réseau, la base locale est la source de vérité ; les résultats validés sont remontés au serveur central quand le réseau le permet, sans jamais bloquer l'administrateur", "Synchronisation"],
    ], [26, 52, 22]),
  ];
}

function acteurs() {
  return [
    titre1("5. Acteurs et rôles"),
    ...paras("Cinq rôles (SUPER_ADMIN, ADMIN_ATT, AUTO_ECOLE, EXAMINATEUR, CANDIDAT dans le code), chacun avec son menu et ses droits. Un utilisateur ne voit que les actions autorisées ; les contrôles sont faits dans les ViewModels, pas seulement à l'écran : par exemple, la liste des candidats de l'auto-école est filtrée sur son identifiant avant d'arriver à l'écran, qui ne reçoit jamais la liste complète."),
    tableau(["Rôle", "Ce qu'il fait", "Ce qu'il ne voit pas"], [
      ["Super administrateur", "Configuration : catégories, épreuves, barèmes, questions, critères, règles, centres ; comptes administrateurs ; et tout ce que fait l'administrateur ATT", "—"],
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
    ...paras(
      "Ce chapitre suit un candidat de son inscription en auto-école jusqu'à son résultat, dans l'ordre réel des opérations. Chaque étape est illustrée par une capture prise sur un petit téléphone (720 × 1280) ; les comptes utilisés sont ceux de l'annexe E.",
      "Une précision honnête avant de commencer : ce MVP (produit minimum viable, la version qui couvre le parcours de bout en bout sans les raffinements) fonctionne sur un seul appareil. Tous les rôles se connectent à la même base locale, ce qui correspond à un poste de l'ATT dans un centre, où l'administrateur saisit aussi pour l'auto-école et pour le candidat sans smartphone. Répartir les rôles sur plusieurs téléphones exigerait le serveur central, hors périmètre, qui devrait diffuser la configuration, les dossiers et les inscriptions dans les deux sens ; la synchronisation de la section 6.19 n'en est que le premier pas.",
    ),
    ...etape(1, "Connexion et accueil par rôle",
      "L'application s'ouvre sur un écran de connexion. Le mot de passe n'est jamais stocké en clair : le mot de passe saisi est haché avec le sel enregistré pour ce compte, et c'est l'empreinte obtenue qui est comparée à l'empreinte stockée. Une fois connecté, chaque rôle arrive sur son propre accueil, un menu en tuiles qui ne montre que ce qu'il a le droit de faire (figure 1 du chapitre 5 pour l'administrateur ATT). La session de connexion vit en mémoire : fermer l'application demande une nouvelle connexion.",
      "06_01_connexion.png", "Écran de connexion"),
    ...figure("06_01b_connexion_erreur.png", "Connexion refusée : le message reste volontairement vague sur la cause", 5.5),
    ...paras("Chaque rôle a son accueil : les figures suivantes montrent ceux du super administrateur, de l'auto-école, de l'examinateur et du candidat ; celui de l'administrateur ATT est au chapitre 5."),
    ...figure("05_accueil_superadmin.png", "Accueil du super administrateur : configuration et comptes", 5.5),
    ...figure("05_accueil_auto_ecole.png", "Accueil de l'auto-école : ses candidats, ses inscriptions, ses résultats", 5.5),
    ...figure("05_accueil_examinateur.png", "Accueil de l'examinateur : sessions du jour, évaluations", 5.5),
    ...figure("05_accueil_candidat.png", "Accueil du candidat : son parcours", 5.5),
    ...etape(2, "Configuration par le Super Admin",
      "Avant tout examen, le super administrateur décrit ce que l'ATT attend : les catégories de permis (A', A, B, C, D, E) avec leur âge minimum, les épreuves de chaque catégorie, le barème de chaque épreuve (note maximale, seuil), les questions orales avec leurs points et une réponse attendue facultative, les critères de conduite, et les règles générales (délais, capacités, tolérance de retard). Un barème est versionné : créer une nouvelle version ferme l'ancienne à la date du jour, et un résultat déjà calculé garde la version qui a servi. Sur la figure, le barème de l'épreuve théorique B a été porté à 20 points avec un seuil de 12 ; l'ancienne version reste lisible. Chaque valeur qui n'a pas été validée par l'ATT porte un drapeau « à confirmer », que le super administrateur retire d'un bouton une fois la valeur confirmée.",
      "06_02_configuration.png", "Épreuve théorique de la catégorie B : barème versionné et questions"),
    ...figure("06_02b_categories.png", "Les catégories de permis, point d'entrée de la configuration", 5.5),
    ...figure("05_comptes.png", "Les comptes, vus par le super administrateur", 5.5),
    ...etape(3, "Centres d'examen",
      "Un centre d'examen appartient toujours à une région : les vingt-quatre régions de Madagascar sont préchargées. Le centre porte une capacité par défaut, reprise à la création des sessions. Un administrateur régional ne voit que les centres de sa région.",
      "06_03_centre.png", "Liste des centres, rattachés à leur région"),
    ...etape(4, "Auto-écoles et comptes",
      "L'ATT crée les auto-écoles agréées et leur compte de connexion : une auto-école ne s'inscrit pas elle-même, parce que l'agrément est une décision de l'ATT et que l'application n'a pas de serveur pour recevoir des demandes. La fiche montre la région, l'adresse, l'agrément, les comptes rattachés et l'historique des modifications. Une auto-école ne se supprime jamais : elle se désactive, et son compte suit.",
      "06_04_auto_ecole.png", "Fiche d'une auto-école et ses comptes"),
    ...etape(5, "Examinateurs",
      "L'administrateur ATT crée l'examinateur et son compte en une seule opération : il n'existe pas d'examinateur sans compte ni de compte orphelin. L'identifiant et le mot de passe initial sont communiqués à l'examinateur, qui change ce dernier depuis l'écran « Mon mot de passe ». Aucun examinateur n'est affecté à l'avance à un candidat : c'est une exigence du cadrage, cohérente avec l'anonymat.",
      "06_05_examinateur.png", "Création d'un examinateur avec son compte"),
    ...etape(6, "Candidat et dossier",
      "L'auto-école crée ses candidats et, pour chacun, ouvre un dossier dans une catégorie. Les pièces attendues viennent de la configuration (la liste officielle du portail Torolalana, différente pour les catégories qui exigent déjà le permis B). L'auto-école coche ce qu'elle fournit puis soumet le dossier à l'ATT ; l'âge minimum de la catégorie est contrôlé à la soumission. À chaque pièce, l'auto-école peut joindre une photo prise avec le téléphone ou un fichier PDF : l'ATT vérifie alors à l'écran, sans attendre le papier ; sans fichier, la vérification se fait sur le dossier apporté, donc plus tard. Un dossier ne se supprime pas : il passe par les statuts brouillon, soumis, validé, incomplet ou refusé, tous tracés.",
      "06_06_dossier.png", "Dossier constitué par l'auto-école, prêt à être soumis"),
    ...figure("06_06a_mes_candidats.png", "« Mes candidats » : l'auto-école ne voit que les siens", 5.5),
    ...paras("Sur les deux figures suivantes, l'auto-école a joint une photo à la première pièce depuis les fichiers du téléphone : la pièce passe en « Photo jointe », avec les actions ouvrir, remplacer, retirer, et l'ATT pourra l'examiner à l'écran ; puis la pièce est ouverte, avec les mêmes droits que le dossier."),
    ...figure("06_06b_piece_jointe.png", "Pièce du dossier avec un fichier joint : ouvrir, remplacer, retirer"),
    ...figure("06_06c_piece_ouverte.png", "La pièce jointe ouverte par l'ATT ou l'auto-école"),
    ...etape(7, "Décision de l'ATT sur le dossier",
      "L'administrateur ATT retrouve les dossiers soumis dans « Dossiers à traiter ». Il valide, ou marque le dossier incomplet ou refusé avec un motif obligatoire, que l'auto-école lit sur la fiche du dossier. C'est ce contrôle en amont qui évite le cas vécu d'un candidat renvoyé le jour de l'examen : seul un dossier validé permet une inscription.",
      "06_07_decision.png", "Décision de l'ATT : valider, incomplet ou refuser avec motif"),
    ...figure("06_07a_dossiers_a_traiter.png", "« Dossiers à traiter » : la file des dossiers soumis, vue par l'ATT", 5.5),
    ...etape(8, "Session et créneaux",
      "Une session est une épreuve d'une catégorie, dans un centre, à une date, avec une heure de convocation et une capacité. L'administrateur indique combien de candidats passent par créneau, la durée d'un créneau et la marge entre deux ; l'application calcule les créneaux et les affiche avant même la création. Une seconde session le même jour dans le même centre déclenche un avertissement. La session passe ensuite par les statuts planifiée, ouverte aux inscriptions, complète, en cours, terminée ou annulée avec motif.",
      "06_08_session.png", "Création d'une session : les créneaux se calculent à la saisie"),
    ...figure("06_08a_liste_sessions.png", "Liste des sessions avec ses filtres : à venir, du jour, passées, toutes", 5.5),
    ...figure("06_08b_detail_session.png", "Fiche d'une session : créneaux et remplissage, statut, actions et documents", 5.5),
    ...etape(9, "Inscription et convocation",
      "L'administrateur inscrit un candidat éligible : dossier validé pour la catégorie, capacité de la session et du créneau non dépassées, pas de double inscription, pas d'autre session le même jour, délai de repassage écoulé, théorie réussie si l'épreuve est la conduite. Le candidat reçoit un numéro d'appel (001, 002…) et un créneau, donc une heure de passage estimée. La convocation s'imprime depuis cette liste ; une inscription se reporte ou s'annule avec motif, et la place est libérée.",
      "06_09_inscription.png", "Inscriptions d'une session : numéro d'appel, créneau, actions"),
    ...figure("06_09c_convocation.png", "La convocation imprimable : centre, date, heure de passage estimée, pièces à apporter", 5.5),
    ...etape(10, "Appel et présence",
      "Le jour de l'examen, l'administrateur ATT présent au centre fait l'appel par créneau, avec peu de candidats à la fois. Chaque candidat est marqué présent ou absent ; au-delà de la tolérance de retard configurée, l'application propose d'accepter ou de refuser le retard. Un statut se corrige, ce qui répond au cas observé du « présent » non entendu dans la foule. La liste d'appel s'imprime comme solution de secours ; l'examinateur en reçoit une version sans les noms.",
      "06_10_appel.png", "Appel d'une session : présents, retards, absents"),
    ...etape(11, "Passages et vue de l'examinateur",
      "L'examinateur connecté voit les sessions du jour et, pour chacune, les candidats présents identifiés par leur seul numéro d'appel. Il ouvre un passage : le n-ième passage de ce candidat à cette épreuve, jamais réutilisé, jamais effacé, tracé dans l'historique. Un passage s'appelle « tentative » dans le code et dans le modèle de données (Tentative) : c'est le mot du cahier de cadrage ; l'écran dit « passage », plus naturel pour un agent. Un passage déjà terminé se consulte en lecture seule.",
      "06_11_passages.png", "Vue de l'examinateur : numéros d'appel, sans les noms"),
    ...figure("06_11a_sessions_du_jour.png", "« Sessions du jour » de l'examinateur, avec présents et passages ouverts", 5.5),
    ...etape(12, "Épreuve théorique orale",
      "L'épreuve théorique est orale, comme l'une de nous l'a vécue. L'écran reproduit la feuille de l'examinateur : le sujet est tiré au sort par l'application jusqu'à la note maximale du barème, ou choisi question par question par l'examinateur selon la règle configurée. Pour chaque question, l'examinateur écrit la réponse donnée par le candidat et les points attribués, de zéro au maximum de la question ; la réponse attendue s'affiche en aide-mémoire. Le total se calcule au fil de la saisie. « Terminer l'épreuve » fige la feuille, clôt le passage et calcule aussitôt le résultat.",
      "06_12_theorie.png", "Feuille d'examen théorique : questions posées, réponses, points"),
    ...etape(13, "Épreuve de conduite",
      "Pour la conduite, l'application propose une grille : un critère par ligne avec ses points, une case « faute éliminatoire » quand le critère l'admet, une observation. La grille elle-même n'est pas imposée : elle vient de la configuration et reste à confirmer par l'ATT. Une faute éliminatoire vaut zéro sur le critère et fait perdre l'épreuve quel que soit le total, ce que l'écran annonce en rouge.",
      "06_13_conduite.png", "Grille de conduite : points par critère, faute éliminatoire, observations"),
    ...etape(14, "Résultat, validation et correction",
      "Le résultat est calculé automatiquement à la clôture de l'épreuve, avec la version du barème en vigueur, copiée dans le résultat pour rester lisible même si le barème change. Si le sujet posé ou la grille ne totalise pas la note maximale du barème, la note est rapportée au barème par une règle de trois (question Q2 bis, à confirmer) ; les points attribués et les points proposés restent affichés et imprimés tels quels. Il attend la validation de l'administrateur ATT ; jusque-là, ni l'auto-école ni le candidat ne le voient. Une erreur de notation se corrige en rouvrant la feuille de l'examinateur puis en recalculant : l'application crée un nouveau résultat avec un motif, l'ancien reste en base et lisible, marqué « remplacé ».",
      "06_14_resultat.png", "Détail d'un résultat avant validation par l'ATT"),
    ...figure("06_14b_resultats.png", "« Résultats à valider » : la file de l'ATT, avec l'état d'envoi au serveur", 5.5),
    ...figure("06_14c_resultat_corrige.png", "Résultat corrigé : une nouvelle ligne avec son motif, l'ancienne reste consultable", 5.5),
    ...etape(15, "Échec et nouvelle tentative",
      "En cas d'échec, l'écran indique les épreuves à repasser et la date à partir de laquelle le candidat peut être réinscrit, d'après le délai de repassage configuré ; une épreuve réussie reste acquise pendant la durée configurée. Le prochain passage portera le numéro suivant : rien n'est écrasé, le parcours complet reste visible.",
      "06_15_repassage.png", "Résultat en échec : épreuves à repasser et date de réinscription"),
    ...etape(16, "Consultation par rôle",
      "Chacun voit ce qui le concerne. L'auto-école suit ses candidats, leurs inscriptions et leurs résultats validés ; le candidat qui dispose d'un compte lit son parcours, dossier, convocations, passages et résultats ; l'ATT voit tout et filtre par région. Un candidat sans smartphone n'est pas pénalisé : son auto-école et l'ATT ont la même information, et les documents s'impriment.",
      "06_16_parcours.png", "Parcours du candidat, vu avec son compte"),
    ...figure("06_16b_mes_inscriptions.png", "« Mes inscriptions » de l'auto-école : convocations et présence de ses candidats", 5.5),
    ...figure("06_16c_resultats_auto_ecole.png", "Résultats vus par l'auto-école : seulement les résultats validés", 5.5),
    ...etape(17, "Impression",
      "Quatre documents s'impriment depuis l'application, sans réseau : la convocation individuelle avec l'heure de passage estimée et les pièces à apporter, la liste d'appel d'une session (avec les noms pour l'ATT, sans les noms pour l'examinateur), la liste des admis d'une session (seulement les résultats validés) et le relevé de résultat. La liste des admis est le document que le CIM utilise ensuite pour délivrer le permis ; sa forme officielle et son signataire restent à confirmer avec l'ATT (question Q13). La page est produite en HTML, montrée en aperçu, puis confiée au service d'impression d'Android, qui permet aussi d'enregistrer un PDF.",
      "06_17_releve.png", "Relevé de résultat, prêt à imprimer"),
    ...figure("06_17b_liste_appel.png", "Liste d'appel imprimable d'une session, avec cases à cocher", 5.5),
    ...figure("06_17c_liste_admis.png", "Liste des admis d'une session : seulement les résultats validés", 5.5),
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
    ...paras(`${nombreEntites()} entités Room, reliées par des clés étrangères, en six familles. Le schéma complet, avec chaque attribut et chaque statut, est dans le fichier docs/03_MODELE_DE_DONNEES.md du dépôt ; Room l'exporte dans app/schemas à chaque version.`),
    tableau(["Famille", "Entités", "Choix de conception"], [
      ["Référentiels", "Region, CategoriePermis, TypeEpreuve, Bareme, Question, CriterePratique, RegleConfig, Centre", "Tout ce qui est une règle administrative est une ligne de table, jamais une constante ; le barème est versionné (une version se ferme, ne se modifie pas)"],
      ["Acteurs", "Utilisateur, AutoEcole, Examinateur, Candidat", "Un compte porte son rôle et, selon le rôle, l'auto-école, l'examinateur ou le candidat qu'il représente ; le compte candidat est facultatif"],
      ["Dossiers", "Dossier, PieceDossier", "Un dossier par catégorie, avec ses pièces déclarées ; statuts brouillon, soumis, validé, incomplet, refusé"],
      ["Planification", "Session, Creneau, Inscription, Presence", "Une session = une épreuve, un centre, une date ; l'inscription porte le numéro d'appel et le créneau ; la présence est une ligne à part, corrigeable"],
      ["Examens", "Tentative, Evaluation, ReponseCandidat, EvaluationCritere, Resultat", "Une tentative n'est jamais effacée ; l'évaluation fige la version du barème ; un résultat ne se modifie pas, une correction en crée un nouveau qui pointe vers l'ancien"],
      ["Traçabilité", "Historique", "Auteur, date, action, ancienne et nouvelle valeur, motif : écrit dans la même transaction que la modification"],
    ], [18, 34, 48]),
    titre2("7.3 Flux d'un écran"),
    ...paras(
      "Un exemple de bout en bout. Sur l'écran des inscriptions, l'administrateur choisit un candidat et appuie sur « Inscrire ». L'écran appelle la fonction inscrire du ViewModel avec l'identifiant du candidat. Le ViewModel rassemble ce qu'il faut (session, créneaux, inscriptions existantes, règles configurées) et appelle la fonction pure ReglesInscription.verifier, qui renvoie soit un message (« Session complète », « Le candidat est déjà inscrit »), soit rien. S'il y a un message, il est placé dans l'état et l'écran l'affiche dans un encart rouge. Sinon, le ViewModel ouvre une transaction : insertion de l'inscription avec son numéro d'appel et son créneau, puis la ligne d'historique. Room réémet alors les flux des tables concernées, la fonction combine recalcule l'état, et l'écran se redessine avec la nouvelle ligne et les places restantes : aucune relecture manuelle, aucun rafraîchissement à déclencher.",
    ),
    ...paras("L'annexe B montre les quatre briques de ce flux : l'écriture et sa ligne d'historique dans une même transaction (B.1), une règle pure et son test (B.2), un ViewModel qui combine les flux de Room en un seul état (B.3) et le tirage du sujet avec le hasard injecté (B.4)."),
    titre2("7.4 Sécurité"),
    ...puces(
      "Mots de passe jamais en clair : empreinte PBKDF2 (fonction de dérivation de clé de la bibliothèque Java, plusieurs milliers d'itérations) avec un sel aléatoire par compte ; seule l'empreinte est stockée et comparée.",
      "Session de connexion en mémoire, reconnexion à chaque lancement ; chaque ViewModel filtre ses requêtes selon le rôle du compte connecté, un examinateur qui ouvre l'appel d'une session ne reçoit que les numéros.",
      "Les comptes se désactivent et ne se suppriment pas ; les mots de passe initiaux sont à changer depuis « Mon mot de passe ».",
      "Les fichiers joints sont copiés dans le stockage privé de l'application, inaccessible aux autres applications, mais non chiffré (limite au chapitre 13).",
    ),
    titre2("7.5 Hors ligne d'abord et synchronisation"),
    ...paras(
      "Les centres d'examen n'ont pas tous une connexion fiable. L'application applique le principe du cours : l'écran lit toujours la base locale, qui est la source de vérité ; le réseau ne fait que la nourrir. Une coupure n'est pas une panne, c'est un retard de synchronisation.",
      "Concrètement, un résultat validé par l'ATT porte un drapeau « synchronisée » (le champ synchronisee) à faux. La validation l'écrit en base puis tente aussitôt de le remonter au serveur central ; si le réseau manque, il reste dans la file d'attente, qui n'est rien d'autre qu'une requête sur ce drapeau. L'écran de synchronisation montre la file, le bouton « Synchroniser maintenant » et ce que le serveur a reçu. Le serveur est simulé en mémoire, avec un interrupteur réseau pour la démonstration ; une vraie interface réseau le remplacerait sans changer le reste du code.",
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
    ...paras("Deux différences avec la démonstration du cours, assumées : le drapeau est posé sur le résultat au moment de sa validation par l'ATT, pas à l'insertion, parce que seul un résultat validé a vocation à quitter le centre ; et la remontée est tentée aussitôt après la validation, puis à la demande. Une limite connue : si l'application s'arrêtait entre l'envoi et le marquage, le résultat serait renvoyé ; un vrai serveur devra ignorer les doublons sur l'identifiant du résultat."),
    titre2("7.6 Environnement de développement"),
    tableau(["Élément", "Version"], [
      ["Kotlin (intégré à AGP)", "2.2.10"], ["Android Gradle Plugin", "9.3.2"], ["Jetpack Compose (BOM)", "2026.08.00, Material 3"],
      ["Navigation Compose", "2.10.1"], ["Lifecycle ViewModel", "2.11.0"], ["Room", "2.8.5, code d'accès généré par KSP 2.3.12"], ["JUnit", "4.13.2"], ["Java", "17"], ["Android minimum", "8.0 (niveau d'API 26)"],
    ], [50, 50]),
    espace(),
    ...paras("Ces versions sont celles de l'Android Studio installé en septembre 2026 ; le cours en utilisait de plus anciennes (AGP 8.5, Kotlin 2.0). La seule différence visible dans les fichiers de construction : AGP 9 embarque Kotlin, donc le plugin kotlin.android du cours disparaît. Android 8.0 minimum, au lieu de 7.0, pour disposer du hachage PBKDF2 avec SHA-256 dans la bibliothèque standard. La BOM de Compose est un jeu de versions cohérentes de toutes les bibliothèques Compose."),
    titre2("7.7 Erreurs et cas dégradés"),
    ...paras(
      "Les erreurs métier ne sont pas des exceptions. Une règle non respectée (dossier non validé, capacité atteinte, points hors bornes, motif manquant) est rendue par une fonction de metier/ sous forme de message, ou de null quand tout va bien ; le ViewModel place ce message dans son état et l'écran l'affiche dans un encart rouge. Rien ne plante, rien n'est écrit.",
      "Un identifiant de route invalide (une fiche ouverte sur un identifiant qui n'existe plus) donne un écran « introuvable » sans plantage : l'identifiant est relu par toIntOrNull et la fiche affiche un message si la base ne renvoie rien.",
      "Les erreurs techniques, elles, ne sont pas rattrapées : un échec d'écriture Room ferait remonter l'exception dans la coroutine et fermerait l'application. Nous l'assumons comme limite : le mécanisme qui permettrait de l'intercepter n'a pas été vu en cours, et un tel échec sur une base locale relève de la panne d'appareil. Seule la lecture des fichiers joints est protégée, parce qu'un fichier peut avoir été supprimé du téléphone entre deux ouvertures.",
    ),
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
      ["Durée de l'épreuve théorique", "30 minutes", "à confirmer"],
      ["Examen dans la région de l'auto-école", "non exigé", "à confirmer"],
      ["L'auto-école peut demander une inscription", "non", "à confirmer"],
      ["Pièces du dossier", "liste du portail officiel Torolalana", "officiel, sauf le certificat médical pour la catégorie B, à trancher"],
      ["Catégories et âges minimums", "A', A, B, C, D, E ; 16 / 18 / 21 ans", "liste officielle, âges à confirmer"],
    ], [44, 32, 24]),
    espace(),
    ...figure("08_regles.png", "Configuration → Règles : chaque règle est une ligne modifiable, avec son drapeau", 5.5),
    ...figure("08_regle_delai.png", "Modification d'une règle : la valeur, sa description et la case « à confirmer »", 5.5),
  ];
}

function qualite() {
  return [
    titre1("9. Qualité, tests et traçabilité"),
    titre2("9.1 Tests unitaires"),
    ...paras("Les règles métier sont testées par JUnit, sans émulateur : tirage du sujet reproductible, contrôles d'inscription, calcul du résultat, règles de repassage, contraintes du cadrage. Le tirage au sort reçoit son générateur de hasard en paramètre, ce qui le rend reproductible en test."),
    ...paras(`Au moment de la rédaction, ${nombreTests()} tests unitaires passent, lancés par ./gradlew :app:testDebugUnitTest. Les classes de test :`),
    ...puces(...classesDeTest()),
    ...paras(
      "Trois exemples de ce qu'elles vérifient : CalculResultatTest, la note rapportée au barème, le seuil atteint ou non, la faute éliminatoire ; ReglesInscriptionTest, les refus de la section 6.9 (dossier non validé, session complète, double inscription, même jour, délai de repassage, théorie non réussie) ; ReglesTheorieTest, un sujet tiré avec une graine fixe qui totalise la note maximale sans doublon.",
      "Ce que ces tests couvrent, et ce qu'ils ne couvrent pas : ils portent sur les règles pures de metier/, sur le hachage des mots de passe et sur le menu par rôle (MenuParRoleTest). Les ViewModels, les DAO et les écrans ne sont pas testés automatiquement : les requêtes des DAO reposent sur la vérification du SQL à la compilation par Room, et les écrans ont été vérifiés à la main sur l'émulateur, avec un script (outils/pilote_emulateur.sh) qui enchaîne les actions par le texte des boutons et rejoue chaque scénario à l'identique ; les dix contraintes du cadrage ont chacune leur scénario dans docs/COMPTES_RENDUS.md. Les tests unitaires ne font pas partie du programme de ce module ; nous les avons introduits parce qu'ils sont le seul moyen de prouver un calcul de barème sans téléphone.",
      "Un exemple de défaut trouvé par un test avant l'écran : le tirage du sujet, dans une première version, pouvait dépasser la note maximale quand la dernière question ne tenait plus ; le test « tirage atteint la note max sans la dépasser ni répéter » l'a montré, et la boucle a été corrigée.",
    ),
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
    ...paras(
      "Chaque écriture sensible est enregistrée dans la même transaction que sa ligne d'historique : soit les deux sont écrites, soit aucune. Sont tracés : auto-écoles, comptes, examinateurs, candidats, dossiers, sessions, inscriptions, présences, passages, évaluations, résultats, règles, catégories, épreuves, barèmes, questions, critères et centres ; les actions sont la création, la modification, la désactivation et la réactivation, la validation, le refus, la correction, l'annulation et la création de compte.",
      "Un exemple de ligne réelle, telle qu'elle apparaît dans l'application : « 18/09/2026 13:31 — MODIFICATION — Presence n° 1 — par Administrateur ATT — présence de l'inscription 1 : EN_RETARD, arrivée 13:31 → PRESENT, arrivée 13:31, retard accepté ». L'historique se consulte depuis chaque fiche et depuis un écran global filtrable par période, objet et utilisateur.",
    ),
  ];
}

function usageIA() {
  return [
    titre1("10. Usage de l'IA et jugement porté"),
    ...paras(
      "Le module s'intitule « assistée par IA » : ce chapitre dit ce que nous avons fait avec l'IA, ce qu'elle a fait de bien et de mal, et ce que nous avons refusé.",
      "L'outil : Claude Code, un agent de développement qui lit et écrit les fichiers du projet, lance la construction et les tests, et pilote l'émulateur. Il a été utilisé au niveau « agent » décrit dans l'état de l'art du module, avec un fichier de règles à la racine du dépôt (CLAUDE.md) qui lui impose les technologies du cours, l'interdiction d'inventer une règle administrative, la traçabilité, et un cycle de fin d'étape : construction et tests verts, vérification sur émulateur, compte rendu, entrée dans le journal, puis relecture humaine avant tout commit.",
      "La proportion : l'IA a produit la majorité du code de chaque étape à partir de nos documents de cadrage (règles, plan, modèle de données, cas d'utilisation), que nous avons écrits et validés avant la première ligne. Nous avons relu chaque fichier dans la demande de fusion (pull request) de l'étape avant de la fusionner, et rejoué chaque scénario sur l'émulateur. Le journal du dépôt (JOURNAL-IA.md) consigne pour chaque étape ce qui a été soumis, la remarque principale de l'IA, les points d'alerte, et notre verdict.",
    ),
    titre2("10.1 Ce que nous avons refusé ou corrigé"),
    ...puces(
      [gras("Le patron Repository, refusé. "), texte("Proposé dès le cadrage comme couche intermédiaire entre ViewModel et DAO, « pour faire propre », il a été écarté parce que le cours ne l'a jamais écrit et que les ViewModels appellent les DAO directement sans perdre en clarté.")],
      [gras("Une version de bibliothèque fausse. "), texte("L'IA a d'abord proposé la version de KSP citée par une documentation ; la construction a échoué. La bonne version a été trouvée en lisant le message d'erreur, et la règle « vérifier sur le dépôt de la bibliothèque avant d'écrire une version » a été ajoutée (journal, entrée 2).")],
      [gras("Une contrainte de base contraire à la règle métier. "), texte("Un index unique sur le couple candidat et session empêchait de réinscrire un candidat après un report ; le test unitaire passait, seule la base réelle a révélé la contradiction. Leçon consignée : un invariant qui dépend d'un statut se met dans une fonction pure, pas dans un index (entrée 10).")],
      [gras("Un questionnaire à choix multiples inventé. "), texte("L'IA a construit l'épreuve théorique en QCM d'après la presse ; le passage de l'examen par l'une de nous a montré une épreuve orale. L'IA avait respecté la règle « ne pas inventer » en suivant une source écrite ; c'est le témoignage de terrain qui a tranché, et l'écran a été refait le jour même (entrée 12).")],
      [gras("Un texte qui perdait des lettres. "), texte("Sur trois écrans successifs, l'IA a reproduit le même défaut de saisie (un champ qui relit sa valeur à travers un flux combiné) ; il a été trouvé à chaque fois sur l'émulateur, jamais par les tests. Nous savons maintenant reconnaître le motif et l'expliquer.")],
    ),
    titre2("10.2 Ce que l'IA fait bien, ce qu'elle fait mal"),
    ...paras(
      "Bien : produire vite du code conforme à un patron donné (le duo état et événements d'un ViewModel, un DAO, un écran), écrire les tests d'une règle pure, rédiger les comptes rendus et tenir la trace. Mal : elle ne connaît pas le terrain (le QCM), elle reproduit ses propres défauts d'un écran à l'autre, elle propose volontiers des outils hors programme (Repository, WorkManager, Retrofit) qu'il faut refuser, et elle affirme avec la même assurance ce qui est vérifié et ce qui ne l'est pas.",
      "La question de la dernière séance, « sur quelle tâche auriez-vous appris moins si l'IA avait été autorisée ? », nous y répondons ainsi : sur l'architecture. Si l'IA avait posé seule le modèle de données et la découpe des ViewModels, nous aurions eu une application qui marche sans savoir pourquoi. C'est parce que nous avons écrit le cadrage et relu chaque étape que nous savons expliquer chaque fichier ; trois entrées du journal sont recopiées en annexe G pour en témoigner.",
    ),
  ];
}

function deroulement() {
  return [
    titre1("11. Déroulement du projet"),
    titre2("11.1 Méthode"),
    ...puces(
      "Cadrage d'abord : règles de développement, plan de travail, modèle de données et cas d'utilisation validés avant la première ligne de code.",
      "Une étape à la fois, chacune sur sa branche Git avec une demande de fusion (pull request) relue par l'autre membre avant fusion, un compte rendu et un critère de fin vérifié.",
      "Usage de l'IA encadré par le protocole du module : chaque contribution est relue, commentée et jugée dans un journal (chapitre 10).",
      "Vérification systématique sur émulateur, avec un script qui retrouve les boutons par leur libellé et rejoue chaque scénario à l'identique.",
    ),
    titre2("11.2 Étapes réalisées"),
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
      ["Tests et qualité", "Tests des contraintes du cadrage, relecture croisée, schéma figé"],
      ["Refonte de l'interface", "Thème, police, composants communs, accueil en tuiles, responsivité sur petit écran"],
      ["Pièces jointes numériques", "Photo ou PDF par pièce du dossier, ouverture par l'ATT, dossier en brouillon invisible du candidat"],
      ["Synchronisation hors ligne d'abord", "Drapeau sur les résultats validés, serveur simulé, écran de synchronisation"],
      ["Rejeu de A à Z et rapport", "Parcours complet rejoué, défauts corrigés, captures, rédaction de ce rapport"],
    ], [30, 70]),
    espace(),
    titre2("11.3 Durée réelle et bilan"),
    ...paras(
      "Le plan prévoyait dix semaines ; le développement s'est fait du 15 au 18 septembre 2026, soit quatre jours effectifs à deux, précédés de la recherche documentaire de l'annexe C. Le cadrage (règles, plan, modèle de données, cas d'utilisation) a été écrit le premier jour, avant la première ligne de code, et n'a presque pas bougé ensuite : c'est ce qui a permis d'enchaîner les étapes sans revenir en arrière. Le socle technique et les premières fonctionnalités ont suivi ; puis les évaluations, les résultats, la consultation et l'impression ; enfin les tests, la relecture croisée, la refonte de l'interface et ce rapport.",
      "Ce qui a pris plus de temps que prévu : comprendre le vrai déroulement de l'examen. La première version de l'épreuve théorique était un questionnaire à choix multiples, construit d'après la presse ; elle a été refaite le jour même, après le passage de l'examen par l'une de nous, sous la forme d'une feuille d'examen orale. La leçon vaut pour tout le projet : un témoignage de terrain vaut plus qu'un article, et ce que l'on ne sait pas doit rester une configuration marquée « à confirmer », pas une hypothèse gravée dans le code.",
    ),
  ];
}

function coursAppliques() {
  return [
    titre1("12. Les cours appliqués dans le projet"),
    ...paras("Le module a été construit comme « une application, sept couches, une couche par séance ». Ce chapitre montre, séance par séance, où et comment chaque notion est mise en œuvre dans l'application."),
    tableau(["Séance", "Notions du cours", "Où et comment dans l'application"], [
      ["1 — Kotlin essentiel", "data class, val et copy, null safety sans !!, when, collections (map, filter, groupBy, sumOf, sortedBy)", "Toutes les entités et tous les états d'écran sont des data class immuables modifiées par copy ; les règles métier (metier/) utilisent les fonctions de collection quand elles suffisent : total des points par sumOf, candidats éligibles par filter, meilleur passage par maxByOrNull ; l'opérateur !! est interdit par nos règles et absent du code"],
      ["2 — Coroutines", "suspend, launch, Flow ; jamais bloquer le thread de l'interface ; runBlocking réservé aux tests", "Toute écriture passe par viewModelScope.launch, jamais rien ne bloque le fil d'exécution (thread) de l'interface ; les DAO exposent des fonctions suspend pour les écritures et des Flow pour les lectures ; le faux serveur simule l'aller-retour réseau par delay, sans bloquer l'écran"],
      ["3 — Anatomie Android", "Activity, cycle de vie, manifeste, Intents, Logcat, pile de retour", "Une seule MainActivity ; l'état survit à la rotation parce qu'il vit dans les ViewModels ; l'impression utilise le service d'Android ; la pile de retour est gérée par Navigation Compose (popBackStack, popUpTo à la connexion et à la déconnexion)"],
      ["4 — Jetpack Compose", "L'interface est une fonction de l'état ; @Composable, Column, Row, Card, LazyColumn, Modifier, remember pour l'état purement local", "Tous les écrans sont des composables qui affichent un état et signalent des gestes ; les listes sont des LazyColumn ; l'ouverture d'un menu déroulant est le principal état local (remember), tout le reste vit dans les ViewModels ; un jeu de composants communs (cadre d'écran, champs, cartes, encarts) évite la répétition"],
      ["5 — Navigation", "Navigation Compose, routes en chaînes, identifiant en argument, écrans qui reçoivent des lambdas", "Un NavHost, des routes comme session/{sessionId}/appel, l'identifiant relu par toIntOrNull sans plantage ; aucun écran ne connaît le navController : l'écran signale, la navigation décide"],
      ["6 — Architecture MVVM", "UI, ViewModel, source de données ; StateFlow et flux unidirectionnel garanti par les types ; ViewModel partagé", "Architecture MVVM du cours : un ViewModel par fonctionnalité, un StateFlow d'état immuable par écran, MutableStateFlow privé ; ViewModel de connexion (la session utilisateur) partagé au-dessus de la navigation, ViewModels partagés entre la liste, la fiche et le formulaire d'un même sous-parcours"],
      ["7 — Room et hors ligne d'abord", "Entity, DAO, Database, Flow ou suspend, SQL vérifié à la compilation, combine et stateIn, base locale source de vérité, synchronisation avec drapeau", `${nombreEntites()} entités avec clés étrangères et index, requêtes Flow combinées par combine puis stateIn ; la base locale est la seule source ; les résultats validés portent un drapeau synchronisee et sont remontés au serveur central par une file d'attente, sur le modèle de la démonstration du cours (différences en 7.5)`],
      ["8 — Synthèse", "Une application, sept couches", "Le projet parcourt les sept couches, du langage aux données, avec une étape du plan par couche puis par fonctionnalité"],
    ], [17, 33, 50]),
    espace(),
    titre2("12.1 Une preuve par séance"),
    ...puces(
      [gras("Séance 1. "), texte("metier/ReglesTheorie.kt : le sujet est tiré avec filter et shuffled ; ReglesInscription.kt rend un message ou null avec un when sans if ; aucune entité n'a de propriété mutable ; le mot-clé !! n'apparaît nulle part dans le projet.")],
      [gras("Séance 2. "), texte("Toutes les écritures sont des fonctions suspend appelées dans viewModelScope.launch (par exemple InscriptionsViewModel.inscrire) ; FauxServeurATT.envoyer attend 600 ms par delay, et l'écran reste réactif pendant la synchronisation.")],
      [gras("Séance 3. "), texte("Une seule Activity (MainActivity.kt) ; nous avons vérifié en tournant l'émulateur pendant une saisie d'inscription que le formulaire reste rempli : l'état est dans InscriptionsViewModel, qui survit à la recréation de l'Activity, comme annoncé en cours.")],
      [gras("Séance 4. "), texte("ui/communs/Cadre.kt et Champs.kt : le cadre d'écran, les champs et les cartes sont des composables réutilisés par tous les écrans ; l'ouverture d'un menu déroulant (remember dans Selecteurs.kt) est le principal état local ; le reste vit dans les ViewModels.")],
      [gras("Séance 5. "), texte("Navigation.kt : routes comme tentative/{tentativeId}/theorie, identifiant relu par idArgument (toIntOrNull), écrans qui reçoivent des lambdas onRetour et onOuvrir ; aucun écran ne reçoit le navController.")],
      [gras("Séance 6. "), texte("Chaque ViewModel expose un StateFlow<EtatXxx> construit par combine(...).stateIn(...) sur les flux Room, et garde ses MutableStateFlow privés ; le ViewModel de connexion est créé au-dessus du NavHost et partagé par tous les écrans.")],
      [gras("Séance 7. "), texte("data/ExamensDao.kt : requêtes Flow pour ce qui s'affiche, suspend pour les écritures, SQL vérifié à la compilation ; Synchronisation.kt reprend la file d'attente à drapeau de la démonstration demosync, avec les deux différences expliquées en 7.5.")],
      [gras("Séance 8. "), texte("La synthèse du cours demandait sur quelle tâche nous aurions appris moins avec l'IA : la réponse est au chapitre 10.")],
    ),
    titre2("12.2 Ce qui dépasse le cours, et pourquoi"),
    ...paras("Quelques notions non vues en cours ont été nécessaires ; chacune est expliquée dans un document du projet avant son usage, avec l'équivalent vu en cours et la justification :"),
    ...puces(
      "clés étrangères et index Room, transactions (une écriture et sa ligne d'historique ensemble ou pas du tout) ;",
      "hachage des mots de passe avec sel (bibliothèque standard Java) ;",
      "flatMapLatest (une fiche qui suit un identifiant qui change), menus déroulants, grille de tuiles, rangées qui passent à la ligne ;",
      "affichage d'une page HTML pour l'impression et service d'impression d'Android ;",
      "fonctions pures testées par JUnit, avec le hasard injecté pour rendre le tirage reproductible.",
    ),
    ...paras(`Le tableau complet, avec pour chaque notion l'étape où elle est apparue, l'équivalent vu en cours et la décision du binôme, est le fichier docs/HORS_COURS.md du dépôt (${nombreHorsCours()} entrées). Deux exemples de ce que nous avons refusé ou remplacé : le patron Repository (couche intermédiaire entre ViewModel et DAO), proposé au départ pour « faire propre », a été écarté parce que le cours n'en a jamais écrit et que les ViewModels appellent les DAO directement sans perdre en clarté ; les boutons radio de la première épreuve théorique ont disparu avec le questionnaire à choix multiples, remplacés par les champs de texte du cours.`),
  ];
}

function limites() {
  return [
    titre1("13. Limites et perspectives"),
    titre2("13.1 Ce que l'application ne fait pas encore, et pourquoi c'est bloquant pour un centre"),
    ...paras("Un jury pardonne une limite écrite ; un centre d'examen, lui, ne peut pas travailler avec. Voici ce qui manque avant tout usage réel."),
    ...puces(
      [gras("Un seul appareil. "), texte("Tous les rôles partagent la même base locale ; sans serveur central, une auto-école ou un examinateur sur son propre téléphone ne verrait rien de ce que l'ATT saisit. Le serveur devra diffuser la configuration, les dossiers et les inscriptions dans les deux sens ; la synchronisation actuelle ne remonte que les résultats validés.")],
      [gras("Aucune sauvegarde. "), texte("Un téléphone perdu ou cassé emporte les dossiers, les passages et l'historique du centre. Un export chiffré de la base et sa restauration sont indispensables avant déploiement.")],
      [gras("Données personnelles. "), texte("Photos de pièces d'identité et actes de naissance sont stockés dans l'espace privé de l'application, mais sans chiffrement, sans durée de conservation ni journal de consultation. La loi malgache sur la protection des données à caractère personnel (loi 2014-038) devra être appliquée avec l'ATT.")],
      [gras("Comptes de la version livrée. "), texte("Deux comptes existent au premier lancement avec un mot de passe initial documenté ; l'application ne force pas encore son changement et ne limite pas les tentatives de connexion.")],
      [gras("Schéma et migrations. "), texte("Le schéma Room a changé plusieurs fois pendant le projet et la base est recréée à chaque changement de version ; une vraie migration sera nécessaire dès que des données réelles existeront.")],
      [gras("Tests. "), texte("Les tests automatiques ne couvrent que les règles pures ; les écrans et les ViewModels sont vérifiés à la main. L'application n'a pas été essayée avec de vrais agents dans un centre, ni au-delà de quelques dizaines de candidats ; l'interface n'existe qu'en français.")],
    ),
    titre2("13.2 Ce qui reste à confirmer ou à construire pour coller au terrain"),
    ...puces(
      "Les barèmes, seuils, délais et la grille de conduite sont des valeurs d'exemple en attente de validation par l'ATT (annexe F).",
      "La fin de la chaîne : clôture d'une session, procès-verbal signé par l'examinateur, liste des admis numérotée et non modifiable, bordereau vers le CIM, et une procédure de réclamation avec délai et décision tracée.",
      "Les droits d'examen : la quittance pourrait devenir une pièce du dossier exigée avant l'inscription, sans que l'application gère le paiement.",
      "Le report d'une session entière (véhicule en panne, examinateur absent, intempéries) avec transfert des inscriptions et nouvelles convocations.",
      "Le contrôle d'identité à l'appel (pièce d'identité contre numéro d'appel) et l'unicité des candidats par numéro de pièce d'identité, pour empêcher la substitution et le contournement du délai de repassage.",
      "Un tableau de bord d'intégrité : candidats évalués, taux de réussite et notes moyennes par examinateur et par auto-école, questions posées en mode direct, pour donner à l'ATT les moyens de repérer une anomalie.",
      "Des créneaux calculés d'après le nombre d'examinateurs ou de véhicules disponibles, sans quoi l'heure estimée d'une convocation de conduite est optimiste.",
    ),
    titre2("13.3 Perspectives"),
    ...puces(
      "Remplacer le serveur simulé par un vrai serveur de l'ATT : la file d'attente et la boucle de synchronisation restent les mêmes.",
      "Photos des pièces : relire l'orientation enregistrée par l'appareil photo (métadonnées EXIF) pour les afficher dans le bon sens, et chiffrer le stockage.",
      "Interface en malgache.",
      "Passage de relais au CIM après validation du résultat.",
    ),
    titre1("14. Conclusion"),
    ...paras(
      "Nous étions partis d'un cahier de cadrage et d'une question simple : pourquoi passer l'examen du permis reste-t-il si long et si opaque, pour le candidat comme pour l'administration ? En suivant un candidat de son dossier à son résultat, nous avons construit une application qui couvre toute la chaîne de l'ATT : dossiers vérifiés avant la convocation, sessions découpées en créneaux, appel par numéro, feuille d'examen à l'écran, résultat calculé selon un barème configurable, validé, corrigeable sans rien effacer, imprimable, et remonté à un serveur central quand le réseau le permet.",
      "Ce projet nous a beaucoup intéressés, et d'abord parce qu'il est vrai. Ce que nous avons observé nous-mêmes le jour de l'examen, un candidat renvoyé pour une pièce manquante, un « présent » que personne n'entend dans la foule, trouve une réponse concrète dans ce que nous avons construit. Nous avons aussi appris, au passage, à ne pas inventer une règle administrative que nous ne connaissions pas : tout ce qui reste incertain est marqué « à confirmer » et attend l'ATT.",
      "Il faut aussi être honnêtes : ce que nous livrons est le minimum qu'un projet universitaire, mené en quatre jours à deux, pouvait atteindre. Il peut être largement amélioré. Les barèmes et la grille de conduite sont des exemples ; le serveur est simulé et tout tient sur un seul appareil ; les photos des pièces ne sont pas chiffrées ; l'interface mériterait d'être testée avec de vrais agents dans un vrai centre. Aucune de ces limites n'est un mur : chacune est nommée au chapitre 13, et l'architecture a été pensée pour les accueillir sans tout reprendre.",
      "Nous espérons qu'un jour l'État, à travers l'ATT, se saisira d'un outil de ce genre, celui-ci ou un autre, parce que le besoin est là et que les candidats, les auto-écoles et les agents y gagneraient tous. Si ce travail a au moins montré que c'était possible avec des moyens modestes, en respectant les règles et en gardant le candidat sans smartphone au centre, alors il aura servi à quelque chose.",
    ),
  ];
}

function annexes() {
  return [
    titre1("Annexe A — Captures d'écran"),
    ...paras("Les captures du chapitre 6 ont été prises sur l'émulateur Android d'un petit téléphone (720 × 1280), avec les comptes de démonstration, en rejouant le parcours complet depuis une base vide. Elles sont récapitulées ici dans l'ordre du document ; les fichiers se trouvent dans le dossier docs/rapport/captures du dépôt."),
    tableau(["Figure", "Écran", "Fichier"], FIGURES.map((f) => [String(f.numero), f.legende, f.fichier]), [12, 50, 38]),
    titre1("Annexe B — Extraits de code commentés"),
    titre2("B.1 Écriture et historique dans une même transaction"),
    ...paras("Extrait simplifié de AppelViewModel (la version réelle vérifie d'abord le statut de la session et de la présence)."),
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
    ...paras("Extrait simplifié (les messages réels citent le numéro et le statut du passage). La fonction ne connaît ni Android ni la base : elle reçoit des valeurs et renvoie un message d'erreur ou null. Le ViewModel fait les requêtes, la fonction fait le jugement, le test JUnit tourne sans téléphone."),
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
      ["Code de la route, catégories", "Loi n° 2017-002 du 6 juillet 2017 (officiel) ; décret 2026-974 cité par la presse, texte non consulté", "Officiel / presse"],
      ["Statut de l'ATT", "Décret n° 2006-279 du 25 avril 2006, cité par des sources secondaires (texte inaccessible)", "Secondaire"],
      ["Anonymat, lutte anti-corruption, digitalisation", "L'Express de Madagascar, Newsmada, Studio Sifaka (2020–2026)", "Presse"],
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
      ["Numéro d'appel", "Identifiant du candidat dans une session (numeroAnonymat dans le code), seul visible par l'examinateur"],
      ["Créneau", "Tranche horaire d'une session, avec sa capacité et son heure estimée de passage"],
      ["Session de connexion", "Le compte connecté et son rôle, gardés en mémoire le temps de l'utilisation ; à ne pas confondre avec la session d'examen"],
      ["Règle configurable", "Une valeur métier (délai, capacité, tolérance) stockée en base et modifiable par le super administrateur"],
      ["Faute éliminatoire", "Sur un critère de conduite, faute qui fait perdre l'épreuve quel que soit le total"],
      ["Transaction", "Groupe d'écritures en base enregistrées ensemble ou pas du tout"],
      ["ATT, CIM, CIN", "Agence des Transports Terrestres ; Centre d'Immatriculation, qui délivre le permis ; carte d'identité nationale"],
      ["Torolalana", "Portail officiel des démarches administratives malgaches"],
      ["MVP", "Produit minimum viable : la version qui couvre le parcours de bout en bout sans les raffinements"],
      ["Pull request", "Demande de fusion d'une branche Git, relue par l'autre membre avant d'être intégrée"],
      ["ViewModel", "L'objet qui porte l'état d'un écran et traite ses gestes ; il survit à la rotation de l'appareil"],
      ["StateFlow, combine, stateIn", "Un flux de valeurs dans le temps que l'écran observe ; combine fusionne plusieurs flux en un état, stateIn en fait un état partagé"],
      ["DAO", "Interface d'accès aux données : chaque méthode est une requête SQL vérifiée à la compilation par Room"],
      ["Room, KSP", "La bibliothèque qui gère la base locale SQLite ; l'outil qui génère son code d'accès à partir des annotations"],
      ["NavHost, route", "La table des écrans et les adresses textuelles qui les désignent, comme des adresses web internes"],
      ["PBKDF2, sel, empreinte", "Le mot de passe et un sel aléatoire sont hachés plusieurs milliers de fois ; l'empreinte seule est stockée"],
      ["Graine", "Valeur de départ d'un générateur de hasard : la même graine donne le même tirage, ce qui rend un test reproductible"],
      ["Hors ligne d'abord", "La base locale est la source de vérité ; le réseau la nourrit quand il est là"],
    ], [30, 70]),
    espace(),
    titre1("Annexe E — Comptes de démonstration"),
    ...paras("Comptes disponibles au premier lancement d'une version de développement, rappelés sur l'écran de connexion avec un bouton par rôle ; les trois derniers n'existent pas dans la version livrée, et les deux premiers y ont un mot de passe initial documenté, qu'il faut changer soi-même : l'application ne l'impose pas encore (13.1)."),
    tableau(["Rôle", "Identifiant", "Mot de passe initial"], [
      ["Super administrateur", "superadmin", "ChangezMoi2026"], ["Administrateur ATT", "admin", "admin2026"], ["Auto-école", "autoecole", "autoecole2026"], ["Examinateur", "examinateur", "examinateur2026"], ["Candidat", "candidat", "candidat2026"],
    ], [34, 30, 36]),
    espace(),
    titre1("Annexe F — Questions à valider avec l'ATT"),
    ...paras("Aucune de ces règles n'a été inventée : chacune a une valeur par défaut « à confirmer », modifiable dans l'application, et une question à poser à l'ATT."),
    tableau(["Question", "Ce que nous avons retenu en attendant"], [
      ["Q1 — Épreuve théorique : note maximale, seuil, notation", "Orale (témoignage) ; questions à points variables ; barème d'exemple 30 / 20 dans les données initiales, remplacé par une version 2 à 20 / 12 pendant la démonstration (figure de 6.2)"],
      ["Q1 bis — Choix des questions", "Tirage au sort par l'application, ou choix de l'examinateur (règle MODE_THEORIE)"],
      ["Q2 — Épreuve de conduite : grille", "Aucune grille préchargée ; critères, points et fautes éliminatoires configurables"],
      ["Q2 bis — Sujet ou grille qui ne totalise pas la note maximale", "Note rapportée au barème (règle de trois)"],
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
      ["Q13 — Document final et signature", "Relevé de résultat et liste des admis imprimables ; leur forme officielle et le signataire restent à confirmer"],
      ["Q14 — Auto-école qui demande son agrément", "Non dans le MVP : créée par l'ATT"],
    ], [42, 58]),
    espace(),
    titre1("Annexe G — Trois entrées du journal IA, telles quelles"),
    ...paras("Le journal complet est le fichier JOURNAL-IA.md du dépôt. Les trois entrées ci-dessous sont recopiées sans retouche : la contradiction entre un index SQL et une règle métier (chapitre 10), le passage du QCM à l'épreuve orale sur un témoignage de terrain (chapitres 2 et 10), et la reprise de la démonstration du cours pour la synchronisation (chapitres 7.5 et 12)."),
    ...entreeJournal(10),
    ...entreeJournal(12),
    ...entreeJournal(20),
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
        ...usageIA(),
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
