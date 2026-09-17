package mg.itu.att.metier

import mg.itu.att.data.TypeValeur

/**
 * Validation des écrans de configuration (UC02). Fonctions pures, testées par JUnit.
 * Le code ne connaît que la FORME des valeurs (un seuil est un nombre ≤ note max…), jamais leur contenu.
 */
object ValidationConfiguration {

    fun validerCategorie(code: String, libelle: String, ageMinimum: String, codesExistants: List<String>): String? = when {
        code.isBlank() -> "Le code est obligatoire (ex. B)."
        libelle.isBlank() -> "Le libellé est obligatoire."
        codesExistants.any { it.equals(code.trim(), ignoreCase = true) } -> "Ce code existe déjà."
        ageMinimum.isNotBlank() && (ageMinimum.toIntOrNull() ?: -1) !in 10..99 -> "L'âge minimum doit être un nombre entre 10 et 99 (ou vide)."
        else -> null
    }

    fun validerEpreuve(code: String, libelle: String, ordre: String, duree: String, codesExistants: List<String>): String? = when {
        code.isBlank() -> "Le code est obligatoire (ex. THEORIE)."
        libelle.isBlank() -> "Le libellé est obligatoire."
        codesExistants.any { it.equals(code.trim(), ignoreCase = true) } -> "Cette catégorie a déjà une épreuve avec ce code."
        (ordre.toIntOrNull() ?: 0) < 1 -> "L'ordre doit être un entier ≥ 1."
        duree.isNotBlank() && (duree.toIntOrNull() ?: 0) < 1 -> "La durée doit être un entier positif (ou vide)."
        else -> null
    }

    fun validerBareme(noteMax: String, seuil: String): String? {
        val max = noteMax.toDoubleOrNull()
        val s = seuil.toDoubleOrNull()
        return when {
            max == null || max <= 0 -> "La note maximale doit être un nombre positif."
            s == null || s < 0 -> "Le seuil doit être un nombre positif ou nul."
            s > max -> "Le seuil ne peut pas dépasser la note maximale."
            else -> null
        }
    }

    /** La valeur saisie doit respecter le type de la règle. */
    fun validerRegle(valeur: String, type: TypeValeur): String? = when (type) {
        TypeValeur.ENTIER -> if (valeur.trim().toIntOrNull() == null) "La valeur doit être un entier." else null
        TypeValeur.DECIMAL -> if (valeur.trim().toDoubleOrNull() == null) "La valeur doit être un nombre." else null
        TypeValeur.BOOLEEN -> if (valeur.trim().lowercase() !in listOf("true", "false")) "La valeur doit être true ou false." else null
        TypeValeur.TEXTE -> if (valeur.isBlank()) "La valeur ne peut pas être vide." else null
    }

    /** Une question a un énoncé, des points positifs, au moins deux réponses non vides et exactement une bonne. */
    /** Question orale : un énoncé et des points ; la réponse attendue est facultative. */
    fun validerQuestion(enonce: String, points: String): String? = when {
        enonce.isBlank() -> "L'énoncé est obligatoire."
        (points.replace(',', '.').toDoubleOrNull() ?: 0.0) <= 0 -> "Les points doivent être un nombre positif."
        else -> null
    }

    fun validerCritere(libelle: String, points: String): String? = when {
        libelle.isBlank() -> "Le libellé est obligatoire."
        (points.toDoubleOrNull() ?: -1.0) < 0 -> "Les points doivent être un nombre positif ou nul."
        else -> null
    }

    fun validerCentre(nom: String, regionId: Int?, adresse: String, capacite: String): String? = when {
        nom.isBlank() -> "Le nom est obligatoire."
        regionId == null -> "Choisissez la région."
        adresse.isBlank() -> "L'adresse est obligatoire."
        capacite.isNotBlank() && (capacite.toIntOrNull() ?: 0) < 1 -> "La capacité doit être un entier positif (ou vide)."
        else -> null
    }
}
