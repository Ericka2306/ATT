package mg.itu.att.data

/**
 * Lecture typée des règles configurables (R3 : aucune valeur métier dans le code).
 * La règle d'une catégorie prime sur la règle globale ; à défaut, la valeur `defaut` passée par l'appelant
 * n'est qu'un filet de sécurité si la règle a été effacée de la base.
 */
suspend fun AppDatabase.regleEntier(cle: String, categorieId: Int? = null, defaut: Int = 0): Int =
    regleConfigDao().pour(cle, categorieId)?.valeur?.trim()?.toIntOrNull() ?: defaut

suspend fun AppDatabase.regleBooleen(cle: String, categorieId: Int? = null, defaut: Boolean = false): Boolean =
    regleConfigDao().pour(cle, categorieId)?.valeur?.trim()?.equals("true", ignoreCase = true) ?: defaut

suspend fun AppDatabase.regleTexte(cle: String, categorieId: Int? = null, defaut: String = ""): String =
    regleConfigDao().pour(cle, categorieId)?.valeur?.trim() ?: defaut
