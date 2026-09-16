package mg.itu.att.metier

import mg.itu.att.data.Creneau
import mg.itu.att.data.Inscription
import mg.itu.att.data.Session
import mg.itu.att.data.StatutInscription
import mg.itu.att.data.StatutSession

/**
 * Contrôles d'une inscription (UC07, cadrage §6 et §11). Fonctions pures : le ViewModel rassemble les faits
 * dans un [ContexteInscription] (lus en base, règles comprises), la fonction juge.
 */
object ReglesInscription {

    /** Tout ce qu'il faut savoir pour décider si un candidat peut s'inscrire à une session. */
    data class ContexteInscription(
        val session: Session,
        val inscriptionsSession: List<Inscription>,
        /** Les inscriptions du candidat, avec la date et l'épreuve de leur session. */
        val inscriptionsCandidat: List<Pair<Inscription, Session>>,
        val dossierValide: Boolean,
        /** Région de l'auto-école du candidat et du centre de la session. */
        val regionAutoEcole: Int?, val regionCentre: Int?, val examenDansRegion: Boolean,
        /** Date ISO de la dernière tentative du candidat pour cette épreuve, null si aucune. */
        val derniereTentative: String?, val delaiRepassageJours: Int,
        val nombreTentatives: Int, val tentativesMax: Int,
        /** Vrai si l'épreuve est la conduite et que la règle exige la théorie réussie. */
        val theorieRequise: Boolean, val theorieReussie: Boolean,
    )

    /** Les inscriptions qui occupent une place. */
    fun actives(inscriptions: List<Inscription>): List<Inscription> =
        inscriptions.filter { it.statut == StatutInscription.DEMANDE || it.statut == StatutInscription.INSCRIT || it.statut == StatutInscription.CONFIRME }

    /** Message d'erreur, ou null si le candidat peut être inscrit. L'ordre des contrôles va du plus bloquant au plus fin. */
    fun verifier(ctx: ContexteInscription): String? {
        val s = ctx.session
        val actives = actives(ctx.inscriptionsSession)
        return when {
            s.statut != StatutSession.OUVERTE && s.statut != StatutSession.COMPLETE -> "La session n'est pas ouverte aux inscriptions (${s.statut})."
            !ctx.dossierValide -> "Le candidat n'a pas de dossier validé pour cette catégorie."
            ctx.inscriptionsCandidat.any { (i, se) -> se.id == s.id && i.statut != StatutInscription.ANNULE && i.statut != StatutInscription.REPORTE } -> "Le candidat est déjà inscrit à cette session."
            ctx.inscriptionsCandidat.any { (i, se) -> se.date == s.date && se.id != s.id && actives(listOf(i)).isNotEmpty() } -> "Le candidat a déjà une session le ${formatDate(s.date)} (conflit de créneaux)."
            actives.size >= s.capacite -> "Session complète (${s.capacite} places)."
            ctx.examenDansRegion && ctx.regionAutoEcole != null && ctx.regionAutoEcole != ctx.regionCentre -> "Le candidat doit passer dans la région de son auto-école (règle EXAMEN_DANS_REGION_AUTO_ECOLE)."
            ctx.tentativesMax > 0 && ctx.nombreTentatives >= ctx.tentativesMax -> "Nombre maximal de tentatives atteint (${ctx.tentativesMax})."
            ctx.derniereTentative != null && ctx.delaiRepassageJours > 0 && joursEntre(ctx.derniereTentative, s.date) < ctx.delaiRepassageJours ->
                "Délai de repassage non écoulé : ${ctx.delaiRepassageJours} jours après la tentative du ${formatDate(ctx.derniereTentative)}."
            ctx.theorieRequise && !ctx.theorieReussie -> "L'épreuve théorique doit être réussie avant la conduite (règle CONDUITE_APRES_THEORIE_REUSSIE)."
            else -> null
        }
    }

    /** Premier créneau avec une place libre, dans l'ordre ; null si tout est plein. */
    fun choisirCreneau(creneaux: List<Creneau>, inscriptionsSession: List<Inscription>): Creneau? {
        val occupation = actives(inscriptionsSession).groupingBy { it.creneauId }.eachCount()
        return creneaux.sortedBy { it.ordre }.firstOrNull { (occupation[it.id] ?: 0) < it.capacite }
    }

    /** Numéro d'appel anonyme dans la session : rang à trois chiffres ("007"), jamais réutilisé. */
    fun numeroAnonymat(inscriptionsSession: List<Inscription>): String {
        val max = inscriptionsSession.mapNotNull { it.numeroAnonymat.toIntOrNull() }.maxOrNull() ?: 0
        return "%03d".format(max + 1)
    }

    /** Nombre de jours entre deux dates ISO (approximation civile suffisante : 1 mois = jours réels via l'époque). */
    fun joursEntre(debut: String, fin: String): Long {
        fun jours(iso: String): Long {
            val (a, m, j) = iso.split('-').map { it.toInt() }
            // Formule des jours juliens (calendrier grégorien), suffisante pour une différence.
            val aa = if (m <= 2) a - 1 else a
            val mm = if (m <= 2) m + 12 else m
            return (365.25 * (aa + 4716)).toLong() + (30.6001 * (mm + 1)).toLong() + j + (2 - aa / 100 + aa / 400) - 1524
        }
        return jours(fin) - jours(debut)
    }
}
