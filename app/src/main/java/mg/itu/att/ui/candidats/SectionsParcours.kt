package mg.itu.att.ui.candidats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.att.data.StatutTentative
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.appel.PastillePresence
import mg.itu.att.ui.appel.libelle
import mg.itu.att.ui.communs.PastilleStatut
import mg.itu.att.ui.communs.TitreSection
import mg.itu.att.ui.inscriptions.PastilleInscription

/**
 * Les sections du parcours d'un candidat (UC12) : inscriptions (convocations) et passages d'épreuve.
 * Partagées par la fiche candidat (ATT, auto-école) et par « Mon parcours » (candidat) :
 * des `item`/`items` à poser dans une `LazyColumn`, comme les autres sections des fiches.
 */

fun StatutTentative.libelle(): String = when (this) {
    StatutTentative.EN_COURS -> "En cours"
    StatutTentative.TERMINEE -> "Terminé"
    StatutTentative.VALIDEE -> "Validé"
    StatutTentative.ANNULEE -> "Annulé"
}

@Composable
fun PastilleTentative(statut: StatutTentative) {
    val (fond, texte) = when (statut) {
        StatutTentative.TERMINEE, StatutTentative.VALIDEE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        StatutTentative.EN_COURS -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        StatutTentative.ANNULEE -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    PastilleStatut(statut.libelle(), fond, texte)
}

/** Carte à deux lignes avec une pastille à droite, commune aux deux sections. */
@Composable
private fun CarteParcours(titre: String, detail: String, pastille: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(titre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                pastille()
            }
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Inscriptions du candidat : session, centre, créneau et heure de passage estimée, numéro d'appel, présence. */
fun LazyListScope.sectionInscriptions(lignes: List<InscriptionParcours>) {
    item {
        TitreSection("Inscriptions et convocations")
        if (lignes.isEmpty()) Text("Aucune inscription à une session.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    items(lignes) { l ->
        val i = l.inscription
        val passage = listOfNotNull(
            l.heureCreneau?.let { "créneau $it" },
            i.heurePassageEstimee?.let { "passage estimé $it" },
        ).joinToString(", ")
        CarteParcours(
            titre = l.libelleSession,
            detail = listOfNotNull(
                l.nomCentre,
                "n° d'appel ${i.numeroAnonymat}",
                passage.ifBlank { null },
                l.presence?.let { "présence : ${it.statut.libelle()}" },
                i.motif?.let { "motif : $it" },
            ).joinToString(" · "),
        ) {
            val p = l.presence
            if (p != null && !l.aVenir) PastillePresence(p.statut) else PastilleInscription(i.statut)
        }
    }
}

/** Passages d'épreuve (tentatives) du candidat, tous conservés, du plus récent au plus ancien. */
fun LazyListScope.sectionPassages(lignes: List<PassageParcours>) {
    item {
        TitreSection("Passages d'épreuve")
        if (lignes.isEmpty()) Text("Aucun passage d'épreuve.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    items(lignes) { l ->
        val t = l.tentative
        CarteParcours(
            titre = "Passage n° ${t.numero} — ${l.libelleEpreuve}",
            detail = "${formatDate(t.dateHeure)} · ${l.libelleSession}",
        ) { PastilleTentative(t.statut) }
    }
}
