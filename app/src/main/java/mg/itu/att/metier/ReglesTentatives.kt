package mg.itu.att.metier

import mg.itu.att.data.StatutPresence
import mg.itu.att.data.StatutTentative
import mg.itu.att.data.Tentative

/**
 * Règles des tentatives (UC09, cadrage §9) : numérotation jamais réutilisée, ouverture réservée aux candidats
 * présents, nombre maximal configurable. Fonctions pures, testées par JUnit.
 */
object ReglesTentatives {

    /** Numéro de la prochaine tentative d'un candidat pour une épreuve (les tentatives ne s'effacent jamais). */
    fun numeroSuivant(tentativesExistantes: List<Tentative>): Int = (tentativesExistantes.maxOfOrNull { it.numero } ?: 0) + 1

    /**
     * Peut-on ouvrir une tentative pour cette inscription ?
     * @param tentativeDeCetteInscription la tentative déjà liée à cette inscription, s'il y en a une.
     * @return un message d'erreur, ou null si l'ouverture est possible.
     */
    fun peutOuvrir(statutPresence: StatutPresence?, tentativeDeCetteInscription: Tentative?, nombreTentatives: Int, tentativesMax: Int): String? = when {
        tentativeDeCetteInscription != null -> when (tentativeDeCetteInscription.statut) {
            StatutTentative.EN_COURS -> null // reprise de la tentative en cours
            else -> "Un passage existe déjà pour cette inscription (n° ${tentativeDeCetteInscription.numero}, ${tentativeDeCetteInscription.statut})."
        }
        statutPresence != StatutPresence.PRESENT && statutPresence != StatutPresence.EN_COURS -> "Le candidat n'est pas marqué présent (appel)."
        tentativesMax > 0 && nombreTentatives >= tentativesMax -> "Nombre maximal de passages atteint (${tentativesMax})."
        else -> null
    }
}
