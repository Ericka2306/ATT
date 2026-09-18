package mg.itu.att.data

import kotlinx.coroutines.delay
import mg.itu.att.metier.maintenantIso

/**
 * FAUX SERVEUR CENTRAL DE L'ATT — tout est en mémoire, aucune infrastructure réseau (cours S7, démo `demosync`).
 *
 * Pourquoi un faux serveur plutôt qu'un vrai appel réseau ?
 * - l'application est locale par choix (pas de serveur dans le MVP, cadrage §13) ;
 * - l'interrupteur « réseau » est simulé : la démonstration ne dépend pas du mode avion de l'appareil ;
 * - le comportement observé est le même : une écriture distante qui réussit ou qui échoue.
 *
 * Dans une vraie application, cet objet serait remplacé par une interface Retrofit ; le reste du code
 * (la file d'attente en base, la boucle de synchronisation) ne changerait pas.
 */
object FauxServeurATT {

    /** L'interrupteur de la démonstration, basculé depuis l'écran de synchronisation. */
    @Volatile var reseauDisponible: Boolean = true

    /** Ce que le serveur a effectivement reçu, dans l'ordre : s'affiche dans le panneau « serveur ». */
    private val recus = mutableListOf<String>()

    /** Date et heure de la dernière synchronisation réussie, ou null. */
    @Volatile var derniereSynchronisation: String? = null
        private set

    /**
     * Envoie un résultat validé au serveur central.
     * @return true si le serveur l'a reçu, false si le réseau est coupé.
     */
    suspend fun envoyer(libelle: String): Boolean {
        delay(600) // le temps d'un aller-retour réseau : suspend, ça ne bloque rien
        if (!reseauDisponible) return false
        recus.add(libelle)
        derniereSynchronisation = maintenantIso()
        return true
    }

    fun contenu(): List<String> = recus.toList()
}
