package mg.itu.att.metier

import mg.itu.att.data.CategoriePermis
import mg.itu.att.data.Dossier
import mg.itu.att.data.StatutDossier

/**
 * Règles des dossiers (UC04, UC05) : éligibilité par l'âge, un seul dossier en cours par catégorie.
 * Fonctions pures : les valeurs de configuration (âge minimum) viennent de la base, jamais du code.
 */
object ReglesDossier {

    /** Âge révolu à `dateRef` pour une naissance à `dateNaissance` (dates ISO) ; null si une date est invalide. */
    fun ageA(dateNaissance: String, dateRef: String): Int? {
        if (!ValidationCandidat.dateValide(dateNaissance) || !ValidationCandidat.dateValide(dateRef)) return null
        val (an, mn, jn) = dateNaissance.split('-').map { it.toInt() }
        val (ar, mr, jr) = dateRef.split('-').map { it.toInt() }
        val anniversairePasse = mr > mn || (mr == mn && jr >= jn)
        return ar - an - if (anniversairePasse) 0 else 1
    }

    /**
     * Le candidat peut-il soumettre un dossier pour cette catégorie à la date donnée ?
     * @return un message d'erreur, ou null si éligible. Un âge minimum null (non renseigné) ne bloque pas.
     */
    fun verifierEligibilite(dateNaissance: String, categorie: CategoriePermis, dateRef: String): String? {
        val ageMinimum = categorie.ageMinimum ?: return null
        val age = ageA(dateNaissance, dateRef) ?: return "Date de naissance invalide."
        return if (age < ageMinimum) {
            "Âge insuffisant pour la catégorie ${categorie.code} : $age ans, minimum $ageMinimum ans (valeur à confirmer)."
        } else {
            null
        }
    }

    /** Un seul dossier ouvert (ou validé) par catégorie : rend l'erreur, ou null si on peut en ouvrir un nouveau. */
    fun peutOuvrirDossier(dossiersExistants: List<Dossier>, categorieId: Int): String? {
        val memeCategorie = dossiersExistants.filter { it.categorieId == categorieId }
        return when {
            memeCategorie.any { it.statut == StatutDossier.VALIDE } -> "Un dossier validé existe déjà pour cette catégorie."
            memeCategorie.any { it.statut != StatutDossier.REFUSE } -> "Un dossier est déjà en cours pour cette catégorie."
            else -> null
        }
    }

    /** Vrai si toutes les pièces attendues sont fournies (information affichée, pas bloquante : l'ATT décide). */
    fun dossierComplet(piecesFournies: List<Boolean>): Boolean = piecesFournies.isNotEmpty() && piecesFournies.all { it }

    /** Les pièces attendues, à partir de la valeur de la règle `PIECES_DOSSIER`. */
    fun piecesDepuisRegle(valeur: String?, separateur: String): List<String> =
        valeur.orEmpty().split(separateur).map { it.trim() }.filter { it.isNotEmpty() }
}
