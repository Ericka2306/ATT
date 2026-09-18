package mg.itu.att.metier

/**
 * Les quatre documents imprimables (UC13, cadrage §6 et §10) : convocation, liste d'appel, liste des admis,
 * relevé de résultat. Chaque fonction construit une page HTML à partir de données déjà lues ;
 * l'impression elle-même est confiée au service Android (docs/HORS_COURS.md n° 10).
 *
 * Fonctions pures, sans Android : elles se testent avec JUnit, comme les règles de `metier/`.
 */
object DocumentsImpression {

    /** Mention obligatoire tant que la procédure ATT n'est pas confirmée (docs/04, Q13). */
    const val MENTION = "Document interne, sans valeur officielle tant que la procédure ATT n'est pas confirmée."

    /** Échappe le texte saisi : un nom avec « & » ou « < » ne doit pas casser la page. */
    fun echapper(texte: String): String = texte
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

    /** Squelette commun : titre, styles simples, pied de page avec la mention et la date d'édition. */
    fun page(titre: String, corps: String, dateEdition: String): String = """
        <!DOCTYPE html>
        <html lang="fr"><head><meta charset="utf-8"><meta name="viewport" content="width=640"><title>${echapper(titre)}</title>
        <style>
          body { font-family: sans-serif; font-size: 12pt; margin: 24px; color: #000; }
          h1 { font-size: 16pt; margin: 0 0 4px; }
          .entete { border-bottom: 2px solid #0b3c66; padding-bottom: 8px; margin-bottom: 16px; }
          .agence { color: #0b3c66; font-weight: bold; }
          table { width: 100%; border-collapse: collapse; margin-top: 12px; }
          th, td { border: 1px solid #666; padding: 6px 8px; text-align: left; }
          th { background: #e8eef4; }
          .ligne { margin: 4px 0; }
          .etiquette { display: inline-block; min-width: 190px; color: #444; }
          .pied { margin-top: 24px; font-size: 9pt; color: #555; border-top: 1px solid #999; padding-top: 8px; }
          .case { display: inline-block; width: 18px; height: 18px; border: 1px solid #333; }
        </style></head>
        <body>
          <div class="entete">
            <div class="agence">ATT — Agence des Transports Terrestres</div>
            <h1>${echapper(titre)}</h1>
          </div>
          $corps
          <div class="pied">$MENTION<br>Édité le ${formatDate(dateEdition)}.</div>
        </body></html>
    """.trimIndent()

    /** Une ligne « libellé : valeur » du corps d'un document. */
    fun ligne(etiquette: String, valeur: String): String =
        """<div class="ligne"><span class="etiquette">${echapper(etiquette)} :</span> ${echapper(valeur)}</div>"""

    // ---------- CONVOCATION ----------

    /** Ce qu'il faut imprimer sur une convocation (UC13) ; les pièces viennent du référentiel du dossier. */
    data class Convocation(
        val nomCandidat: String,
        val dateNaissance: String,
        val nomAutoEcole: String,
        val codeCategorie: String,
        val libelleEpreuve: String,
        val nomCentre: String,
        val adresseCentre: String,
        val dateSession: String,
        val heureConvocation: String,
        val heurePassageEstimee: String?,
        val numeroAnonymat: String,
        val pieces: List<String>,
    )

    fun convocation(c: Convocation, dateEdition: String): String {
        val pieces = if (c.pieces.isEmpty()) "<p>Aucune pièce particulière enregistrée pour ce dossier.</p>"
        else "<ul>" + c.pieces.joinToString("") { "<li>${echapper(it)}</li>" } + "</ul>"
        val corps = buildString {
            append(ligne("Candidat", c.nomCandidat))
            append(ligne("Date de naissance", formatDate(c.dateNaissance)))
            append(ligne("Auto-école", c.nomAutoEcole))
            append(ligne("Numéro d'appel", c.numeroAnonymat))
            append(ligne("Épreuve", "permis ${c.codeCategorie} — ${c.libelleEpreuve}"))
            append(ligne("Centre", "${c.nomCentre}, ${c.adresseCentre}"))
            append(ligne("Date", formatDate(c.dateSession)))
            append(ligne("Heure de convocation", c.heureConvocation))
            append(ligne("Heure de passage estimée", c.heurePassageEstimee ?: "communiquée à l'appel"))
            append("<h2>Pièces à apporter</h2>")
            append(pieces)
            append("<p>Présentez-vous à l'heure de convocation. L'appel se fait par numéro.</p>")
        }
        return page("Convocation à l'examen", corps, dateEdition)
    }

    // ---------- LISTE D'APPEL ----------

    /** Une ligne de la liste d'appel : le nom n'est imprimé que sur la version ATT (anonymat, Q4). */
    data class LigneAppel(val numeroAnonymat: String, val nomCandidat: String, val heureCreneau: String?)

    /** Entête commun aux listes d'une session. */
    private fun enteteSession(codeCategorie: String, libelleEpreuve: String, nomCentre: String, dateSession: String, heureConvocation: String): String =
        ligne("Épreuve", "permis $codeCategorie — $libelleEpreuve") +
            ligne("Centre", nomCentre) +
            ligne("Date", "${formatDate(dateSession)} à $heureConvocation")

    /**
     * Liste d'appel d'une session.
     * @param avecNoms vrai pour la version ATT ; faux pour la version examinateur (numéros seuls).
     */
    fun listeAppel(
        lignes: List<LigneAppel>,
        codeCategorie: String,
        libelleEpreuve: String,
        nomCentre: String,
        dateSession: String,
        heureConvocation: String,
        avecNoms: Boolean,
        dateEdition: String,
    ): String {
        val entetes = listOfNotNull("N° d'appel", "Créneau", if (avecNoms) "Candidat" else null, "Présent", "Absent", "Retard")
        val corps = buildString {
            append(enteteSession(codeCategorie, libelleEpreuve, nomCentre, dateSession, heureConvocation))
            append(ligne("Candidats convoqués", lignes.size.toString()))
            append("<table><tr>")
            entetes.forEach { append("<th>${echapper(it)}</th>") }
            append("</tr>")
            lignes.forEach { l ->
                append("<tr>")
                append("<td>${echapper(l.numeroAnonymat)}</td>")
                append("<td>${echapper(l.heureCreneau ?: "—")}</td>")
                if (avecNoms) append("<td>${echapper(l.nomCandidat)}</td>")
                repeat(3) { append("""<td><span class="case"></span></td>""") }
                append("</tr>")
            }
            append("</table>")
            if (!avecNoms) append("<p>Version examinateur : les candidats sont identifiés par leur numéro d'appel.</p>")
            append("<p>Cette liste imprimée sert de secours en cas de panne (cadrage §10) : la saisie se fait ensuite dans l'application.</p>")
        }
        return page(if (avecNoms) "Liste d'appel" else "Liste d'appel (version examinateur)", corps, dateEdition)
    }

    // ---------- LISTE DES ADMIS ----------

    /** Une ligne de la liste des admis : seuls les résultats validés par l'ATT y figurent. */
    data class LigneAdmis(val numeroAnonymat: String, val nomCandidat: String, val note: String, val reussi: Boolean)

    fun listeAdmis(
        lignes: List<LigneAdmis>,
        codeCategorie: String,
        libelleEpreuve: String,
        nomCentre: String,
        dateSession: String,
        heureConvocation: String,
        dateEdition: String,
    ): String {
        val admis = lignes.filter { it.reussi }
        val corps = buildString {
            append(enteteSession(codeCategorie, libelleEpreuve, nomCentre, dateSession, heureConvocation))
            append(ligne("Admis", "${admis.size} sur ${lignes.size} résultat(s) validé(s)"))
            if (lignes.isEmpty()) {
                append("<p>Aucun résultat validé pour cette session.</p>")
            } else {
                append("<table><tr><th>N° d'appel</th><th>Candidat</th><th>Note</th><th>Résultat</th></tr>")
                lignes.forEach { l ->
                    append("<tr>")
                    append("<td>${echapper(l.numeroAnonymat)}</td>")
                    append("<td>${echapper(l.nomCandidat)}</td>")
                    append("<td>${echapper(l.note)}</td>")
                    append("<td>${if (l.reussi) "Admis" else "Non admis"}</td>")
                    append("</tr>")
                }
                append("</table>")
            }
        }
        return page("Liste des admis", corps, dateEdition)
    }

    // ---------- RELEVÉ DE RÉSULTAT ----------

    /** Le relevé remis au candidat ou à son auto-école, une fois le résultat validé (UC13, Q13). */
    data class Releve(
        val nomCandidat: String,
        val dateNaissance: String,
        val nomAutoEcole: String,
        val codeCategorie: String,
        val libelleEpreuve: String,
        val numeroPassage: Int,
        val note: String,
        val reussi: Boolean,
        val dateValidation: String?,
    )

    fun releve(r: Releve, dateEdition: String): String {
        val corps = buildString {
            append(ligne("Candidat", r.nomCandidat))
            append(ligne("Date de naissance", formatDate(r.dateNaissance)))
            append(ligne("Auto-école", r.nomAutoEcole))
            append(ligne("Épreuve", "permis ${r.codeCategorie} — ${r.libelleEpreuve}"))
            append(ligne("Passage", "n° ${r.numeroPassage}"))
            append(ligne("Note", r.note))
            append(ligne("Résultat", if (r.reussi) "Réussi" else "Échec"))
            append(ligne("Validé par l'ATT le", r.dateValidation?.let { formatDate(it) } ?: "en attente de validation"))
        }
        return page("Relevé de résultat", corps, dateEdition)
    }
}
