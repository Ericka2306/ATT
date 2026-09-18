package mg.itu.att.data

import mg.itu.att.metier.formatDate

/**
 * Synchronisation offline-first des résultats validés vers le serveur central de l'ATT (cours S7).
 *
 * « La base d'abord, le réseau ensuite » : un résultat est validé EN BASE (visible tout de suite, réseau ou pas) ;
 * la remontée au serveur vient après, et son échec est sans gravité : le résultat reste dans la file d'attente
 * (`WHERE synchronisee = 0`) et sera renvoyé à la prochaine tentative, automatique ou par le bouton « Synchroniser ».
 *
 * Partagée par le ViewModel des résultats (après une validation) et par l'écran de synchronisation.
 */
object Synchronisation {

    /** "20.0" → "20", "12.5" → "12,5" (même règle que l'affichage). */
    private fun formatNombre(x: Double): String = if (x == x.toLong().toDouble()) x.toLong().toString() else x.toString().replace('.', ',')

    /**
     * Remonte au serveur tout ce qui est en attente, dans l'ordre. Sans réseau : s'arrête au premier échec, ne casse rien.
     * @return le nombre de résultats effectivement remontés.
     */
    suspend fun synchroniserResultats(db: AppDatabase): Int {
        var envoyes = 0
        for (resultat in db.resultatDao().enAttenteDeSynchronisation()) {
            val recu = FauxServeurATT.envoyer(libelleServeur(db, resultat))
            if (!recu) break // réseau coupé : on s'arrête, on réessaiera
            db.resultatDao().marquerSynchronise(resultat.id)
            envoyes += 1
        }
        return envoyes
    }

    /** Ce que le serveur central reçoit : le résultat, sans le nom du candidat (numéro d'appel seulement). */
    private suspend fun libelleServeur(db: AppDatabase, r: Resultat): String {
        val tentative = db.tentativeDao().parId(r.tentativeId)
        val inscription = tentative?.let { db.inscriptionDao().parId(it.inscriptionId) }
        val epreuve = tentative?.let { db.typeEpreuveDao().parId(it.typeEpreuveId) }
        return "n° ${inscription?.numeroAnonymat ?: "?"} · ${epreuve?.libelle ?: "épreuve"} · ${formatNombre(r.noteObtenue)} / ${formatNombre(r.noteMax)} · ${if (r.reussi) "réussi" else "échec"} · validé le ${r.dateValidation?.let(::formatDate) ?: "?"}"
    }
}
