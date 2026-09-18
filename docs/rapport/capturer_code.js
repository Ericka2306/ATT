/**
 * Fabrique les images des extraits de code de l'annexe A, à partir des VRAIS fichiers du projet,
 * présentées comme dans l'éditeur d'Android Studio (thème clair, gouttière avec numéros de ligne,
 * onglet du fichier). Rendu par Google Chrome en mode sans fenêtre.
 *
 *   node capturer_code.js        → captures/code_A1.png … code_A4.png
 */
const fs = require("fs");
const path = require("path");
const { execFileSync } = require("child_process");

const RACINE = path.resolve(__dirname, "..", "..");
const CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";
const SORTIE = path.join(__dirname, "captures");

/** Les extraits : fichier du projet et plages de lignes (une coupure entre deux plages = région repliée). */
const EXTRAITS = [
  { nom: "code_A1", fichier: "app/src/main/java/mg/itu/att/ui/appel/AppelViewModel.kt", plages: [[157, 168]] },
  { nom: "code_A2a", fichier: "app/src/main/java/mg/itu/att/metier/ReglesTentatives.kt", plages: [[16, 30]] },
  { nom: "code_A2b", fichier: "app/src/test/java/mg/itu/att/metier/ReglesTentativesTest.kt", plages: [[23, 29]] },
  { nom: "code_A3", fichier: "app/src/main/java/mg/itu/att/ui/evaluation/TentativesViewModel.kt", plages: [[121, 131], [153, 155]] },
  { nom: "code_A4", fichier: "app/src/main/java/mg/itu/att/metier/ReglesTheorie.kt", plages: [[29, 45]] },
];

const MOTS_CLES = new Set(("package import class data object fun val var if else when for while do return break continue " +
  "null true false is in as this super private public internal protected override suspend open abstract sealed enum " +
  "interface try catch finally throw typealias by lazy it").split(" "));

const echapper = (t) => t.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");

/** Coloration Kotlin minimale, fidèle aux couleurs du thème clair d'IntelliJ. */
function colorer(ligne) {
  const morceaux = [];
  const motif = /(\/\/.*$)|(\/\*[\s\S]*?\*\/|\/\*\*.*$|^\s*\*.*$)|("(?:[^"\\]|\\.)*")|(@\w+)|(\b\d+(?:\.\d+)?\b)|(`[^`]*`)|(\b[A-Za-z_]\w*\b)|(.)/g;
  let m;
  while ((m = motif.exec(ligne)) !== null) {
    const [tout, commentaire, kdoc, chaine, annotation, nombre, backtick, mot, autre] = m;
    if (commentaire || kdoc) morceaux.push(`<span class="c">${echapper(tout)}</span>`);
    else if (chaine) morceaux.push(`<span class="s">${echapper(tout)}</span>`);
    else if (annotation) morceaux.push(`<span class="a">${echapper(tout)}</span>`);
    else if (nombre) morceaux.push(`<span class="n">${echapper(tout)}</span>`);
    else if (backtick) morceaux.push(`<span class="f">${echapper(tout)}</span>`);
    else if (mot) {
      const precedent = ligne.slice(0, m.index).trimEnd();
      if (MOTS_CLES.has(mot)) morceaux.push(`<span class="k">${mot}</span>`);
      else if (precedent.endsWith("fun")) morceaux.push(`<span class="f">${mot}</span>`);
      else morceaux.push(echapper(mot));
    } else morceaux.push(echapper(autre));
  }
  return morceaux.join("");
}

function html(extrait) {
  const lignes = fs.readFileSync(path.join(RACINE, extrait.fichier), "utf8").split("\n");
  const corps = [];
  extrait.plages.forEach(([debut, fin], i) => {
    if (i > 0) corps.push(`<div class="l repli"><span class="g">…</span><span class="t">    …</span></div>`);
    for (let n = debut; n <= fin; n += 1) {
      const texte = lignes[n - 1] ?? "";
      corps.push(`<div class="l"><span class="g">${n}</span><span class="t">${colorer(texte) || " "}</span></div>`);
    }
  });
  const nomFichier = path.basename(extrait.fichier);
  return `<!doctype html><html><head><meta charset="utf-8"><style>
    html, body { margin: 0; background: #ffffff; }
    body { width: 1400px; font-family: "JetBrains Mono", Menlo, "SF Mono", monospace; font-size: 19px; color: #080808; }
    .onglets { height: 46px; background: #f7f8fa; border-bottom: 1px solid #ebecf0; display: flex; align-items: stretch; font-family: -apple-system, "Segoe UI", sans-serif; font-size: 16px; }
    .onglet { display: flex; align-items: center; gap: 8px; padding: 0 18px; background: #ffffff; border-right: 1px solid #ebecf0; border-bottom: 2px solid #4083c9; color: #1e1e1e; }
    .kt { width: 18px; height: 18px; border-radius: 3px; background: linear-gradient(135deg, #7f52ff, #e44857); color: #fff; font-size: 11px; font-weight: 700; display: flex; align-items: center; justify-content: center; }
    .editeur { padding: 10px 0 14px 0; }
    .l { display: flex; line-height: 30px; white-space: pre-wrap; }
    .g { flex: 0 0 74px; text-align: right; padding-right: 22px; color: #adadad; user-select: none; border-right: 1px solid #ebecf0; margin-right: 18px; }
    .t { flex: 1; padding-right: 24px; text-indent: -4ch; padding-left: 4ch; }
    .repli .t { color: #8c8c8c; background: #f1f4f8; }
    .k { color: #0033b3; } .s { color: #067d17; } .c { color: #8c8c8c; font-style: italic; } .a { color: #9e880d; } .n { color: #1750eb; } .f { color: #00627a; }
  </style></head><body>
    <div class="onglets"><div class="onglet"><span class="kt">K</span>${nomFichier}</div></div>
    <div class="editeur">${corps.join("\n")}</div>
    <script>document.body.setAttribute("data-hauteur", document.body.scrollHeight);</script>
  </body></html>`;
}

fs.mkdirSync(SORTIE, { recursive: true });
for (const extrait of EXTRAITS) {
  const page = path.join(SORTIE, `${extrait.nom}.html`);
  fs.writeFileSync(page, html(extrait));
  // Premier passage : Chrome mesure la hauteur réelle de la page (les lignes longues se replient) ;
  // second passage : capture à cette hauteur exacte, sans blanc en bas.
  const mesure = execFileSync(CHROME, ["--headless=new", "--hide-scrollbars", "--window-size=1400,800", "--dump-dom", `file://${page}`], { stdio: ["ignore", "pipe", "ignore"] }).toString();
  const hauteur = Number((mesure.match(/data-hauteur="(\d+)"/) || [])[1]) || 800;
  const sortie = path.join(SORTIE, `${extrait.nom}.png`);
  execFileSync(CHROME, ["--headless=new", "--hide-scrollbars", "--force-device-scale-factor=1", `--window-size=1400,${hauteur}`, `--screenshot=${sortie}`, `file://${page}`], { stdio: "ignore" });
  fs.unlinkSync(page);
  console.log("capture", extrait.nom);
}
