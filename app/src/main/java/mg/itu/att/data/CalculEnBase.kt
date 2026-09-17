package mg.itu.att.data

import mg.itu.att.metier.CalculResultat
import mg.itu.att.metier.dateDuJour

/**
 * Le calcul d'un résultat côté base (UC10) : on rassemble la saisie de l'examinateur et le barème figé,
 * la décision revient à la fonction pure `CalculResultat`. Rien n'est écrasé : chaque calcul ajoute une ligne
 * `Resultat` (R6), et une correction pointe sur celle qu'elle remplace.
 *
 * À appeler **dans** un `withTransaction`, comme `tracer`.
 */

/** Le calcul d'un passage terminé, ou null si les données ne permettent pas de calculer (barème absent, aucune saisie). */
suspend fun AppDatabase.calculerPourTentative(tentativeId: Int): CalculResultat.Calcul? {
    val tentative = tentativeDao().parId(tentativeId) ?: return null
    val evaluation = evaluationDao().parTentative(tentativeId) ?: return null
    val bareme = baremeDao().parId(evaluation.baremeId) ?: return null
    val epreuve = typeEpreuveDao().parId(tentative.typeEpreuveId)

    // Théorie : la feuille d'examen. Conduite : la grille de critères.
    val reponses = reponseCandidatDao().listePourEvaluation(evaluation.id)
    if (reponses.isNotEmpty()) {
        val questions = questionDao().listePourEpreuve(epreuve?.id ?: tentative.typeEpreuveId)
        val lignes = reponses.map { r ->
            CalculResultat.LigneTheorie(r.pointsAttribues, questions.find { it.id == r.questionId }?.points ?: 0.0)
        }
        if (CalculResultat.verifierCalcul(tentative.statut, bareme, lignes.size) != null) return null
        return CalculResultat.calculerTheorie(lignes, bareme)
    }

    val notes = evaluationCritereDao().listePourEvaluation(evaluation.id)
    if (notes.isNotEmpty()) {
        val criteres = criterePratiqueDao().listePourEpreuve(epreuve?.id ?: tentative.typeEpreuveId)
        val lignes = notes.map { n ->
            val critere = criteres.find { it.id == n.critereId }
            CalculResultat.LigneConduite(n.note, critere?.points ?: 0.0, critere?.eliminatoire == true, n.fauteEliminatoire)
        }
        if (CalculResultat.verifierCalcul(tentative.statut, bareme, lignes.size) != null) return null
        return CalculResultat.calculerConduite(lignes, bareme)
    }
    return null
}

/**
 * Calcule et enregistre le résultat d'un passage terminé, puis trace l'opération.
 * @param remplace le résultat corrigé, s'il s'agit d'une correction (l'ancien passe en ANNULE, il reste lisible).
 * @return l'identifiant du résultat créé, ou null si le calcul est impossible.
 */
suspend fun AppDatabase.enregistrerResultat(
    tentativeId: Int,
    utilisateurId: Int,
    remplace: Resultat? = null,
    motifCorrection: String? = null,
): Int? {
    val calcul = calculerPourTentative(tentativeId) ?: return null
    val evaluation = evaluationDao().parTentative(tentativeId) ?: return null
    val resultat = Resultat(
        tentativeId = tentativeId,
        baremeId = evaluation.baremeId,
        noteObtenue = calcul.noteObtenue,
        noteMax = calcul.noteMax,
        seuil = calcul.seuil,
        reussi = calcul.reussi,
        statut = if (remplace == null) StatutResultat.CALCULE else StatutResultat.CORRIGE,
        dateCalcul = dateDuJour(),
        remplaceResultatId = remplace?.id,
        motifCorrection = motifCorrection,
    )
    val id = resultatDao().inserer(resultat).toInt()
    tracer(
        EntitesHistorique.RESULTAT, id,
        if (remplace == null) ActionsHistorique.CREATION else ActionsHistorique.CORRECTION,
        utilisateurId,
        ancienneValeur = remplace?.resume(),
        nouvelleValeur = resultat.copy(id = id).resume(),
        motif = motifCorrection,
    )
    return id
}
