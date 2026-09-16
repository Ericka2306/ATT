#!/bin/bash
# Pilotage de l'émulateur par le TEXTE des éléments (uiautomator dump), pour rejouer les scénarios
# de vérification et de démonstration sans deviner de coordonnées.
#
# Usage : source outils/pilote_emulateur.sh
#   tap "Se connecter"                 → appuie sur l'élément dont le texte (ou la description) est « Se connecter »
#   saisir "Identifiant" "admin"       → appuie dans le champ « Identifiant » puis tape « admin » (%s = espace)
#   visible "Mot de passe changé"      → vrai si le texte est à l'écran
#   capture nom                        → enregistre docs/captures/nom.png
# Un élément non visible est cherché en faisant défiler l'écran vers le bas (4 essais).

ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
DOSSIER_PROJET="$(cd "$(dirname "${BASH_SOURCE[0]:-$0}")/.." && pwd)"
FICHIER_UI="${TMPDIR:-/tmp}/att_ui.xml"

# centre (x y) de l'élément dont le texte ou la description est $1 (exact d'abord, sinon contient)
centre() {
  $ADB shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
  $ADB shell cat /sdcard/ui.xml > "$FICHIER_UI" 2>/dev/null
  python3 - "$1" "$FICHIER_UI" <<'EOF'
import re, sys, html
cible, chemin = sys.argv[1], sys.argv[2]
xml = open(chemin, encoding="utf-8", errors="ignore").read()
exact, partiel = None, None
for m in re.finditer(r'<node[^>]*>', xml):
    n = m.group(0)
    t = html.unescape(re.search(r'text="([^"]*)"', n).group(1) if re.search(r'text="([^"]*)"', n) else "")
    d = html.unescape(re.search(r'content-desc="([^"]*)"', n).group(1) if re.search(r'content-desc="([^"]*)"', n) else "")
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if not b: continue
    x1, y1, x2, y2 = map(int, b.groups()); c = ((x1 + x2) // 2, (y1 + y2) // 2)
    if cible == t or cible == d:
        exact = exact or c
    elif cible in t or cible in d:
        partiel = partiel or c
c = exact or partiel
if c: print(c[0], c[1]); sys.exit(0)
print("INTROUVABLE", file=sys.stderr); sys.exit(1)
EOF
}

tap() {
  local c essai
  for essai in 1 2 3 4; do
    c=$(centre "$1" 2>/dev/null) && break
    # défilement depuis le haut : la zone basse peut être couverte par le clavier
    $ADB shell input swipe 360 650 360 250 400; sleep 1.2
  done
  [ -z "$c" ] && { echo "!! introuvable : $1"; return 1; }
  $ADB shell input tap $c; sleep "${2:-1.5}"
}

saisir() {
  tap "$1" 1.2 || return 1
  $ADB shell input text "$2"; sleep 0.5
}

visible() { centre "$1" >/dev/null 2>&1; }

capture() {
  mkdir -p "$DOSSIER_PROJET/docs/captures"
  $ADB exec-out screencap -p > "$DOSSIER_PROJET/docs/captures/$1.png"
  echo "capture $1"
}

# redémarre l'application (sans effacer les données) ; `relancer effacer` repart de zéro
relancer() {
  $ADB shell am force-stop mg.itu.att
  [ "$1" = "effacer" ] && $ADB shell pm clear mg.itu.att >/dev/null
  $ADB shell input keyevent KEYCODE_WAKEUP
  $ADB shell am start -n mg.itu.att/.MainActivity >/dev/null 2>&1
  sleep 5
}

connexion() { saisir "Identifiant" "$1"; saisir "Mot de passe" "$2"; tap "Se connecter" 4; }
