package mg.itu.att.ui.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.itu.att.data.StatutSession
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.communs.CarteFiche
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.LigneHistorique
import mg.itu.att.ui.communs.LigneInfo
import mg.itu.att.ui.communs.TexteErreur
import mg.itu.att.ui.communs.TitreSection

/** Fiche d'une session (UC06) : informations, créneaux et leur remplissage, changement de statut, historique. */
@Composable
fun EcranDetailSession(viewModel: SessionsViewModel, sessionId: Int, onInscrire: () -> Unit, onAppel: () -> Unit, onTentatives: () -> Unit, onRetour: () -> Unit) {
    viewModel.afficherDetail(sessionId)
    val etat by viewModel.detail.collectAsState()
    val l = etat.ligne

    EcranStandard(titre = l?.let { "Session du ${formatDate(it.session.date)}" } ?: "Session", onRetour = onRetour) {
        if (l == null) { Text("Session introuvable."); return@EcranStandard }
        val s = l.session
        LazyColumn {
            item {
                CarteFiche {
                    Row { PastilleSession(s.statut) }
                    Spacer(Modifier.height(8.dp))
                    LigneInfo("Épreuve", "Permis ${l.codeCategorie} — ${l.libelleEpreuve}")
                    LigneInfo("Centre", "${l.nomCentre} (${l.nomRegion})")
                    LigneInfo("Convocation", "${formatDate(s.date)} à ${s.heureConvocation}")
                    LigneInfo("Capacité", "${l.inscrits} inscrit(s) / ${s.capacite}")
                    LigneInfo("Créneaux", "${etat.creneaux.size} × ${s.dureeCreneauMin} min, marge ${s.margeMin} min")
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Les inscriptions sont consultables par tous les rôles autorisés ; les actions de statut restent à l'ATT.
                    if (s.statut != StatutSession.PLANIFIEE) OutlinedButton(onClick = onInscrire) { Text("Inscriptions") }
                    if (s.statut == StatutSession.OUVERTE || s.statut == StatutSession.COMPLETE || s.statut == StatutSession.EN_COURS || s.statut == StatutSession.TERMINEE) {
                        OutlinedButton(onClick = onAppel) { Text("Appel") }
                        OutlinedButton(onClick = onTentatives) { Text("Passages") }
                    }
                    if (etat.peutGerer) {
                        when (s.statut) {
                            StatutSession.PLANIFIEE -> Button(onClick = { viewModel.changerStatut(s.id, StatutSession.OUVERTE) }) { Text("Ouvrir aux inscriptions") }
                            StatutSession.OUVERTE, StatutSession.COMPLETE -> Button(onClick = { viewModel.changerStatut(s.id, StatutSession.EN_COURS) }) { Text("Démarrer") }
                            StatutSession.EN_COURS -> Button(onClick = { viewModel.changerStatut(s.id, StatutSession.TERMINEE) }) { Text("Terminer") }
                            StatutSession.TERMINEE, StatutSession.ANNULEE -> {}
                        }
                    }
                }
                TitreSection("Créneaux")
            }
            items(etat.creneaux) { c ->
                Text(
                    "${c.creneau.ordre}. ${c.creneau.heureDebut} – ${c.creneau.heureFinEstimee} : ${c.inscrits}/${c.creneau.capacite} place(s)",
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp),
                )
            }
            if (etat.peutGerer && s.statut != StatutSession.ANNULEE && s.statut != StatutSession.TERMINEE) {
                item {
                    TitreSection("Annuler la session")
                    ChampTexte(etat.motif, viewModel::changerMotif, "Motif de l'annulation (obligatoire)", uneLigne = false)
                    TexteErreur(etat.erreur)
                    OutlinedButton(onClick = { viewModel.changerStatut(s.id, StatutSession.ANNULEE) }) { Text("Confirmer l'annulation") }
                }
            }
            item { TitreSection("Historique") }
            items(etat.historique) { LigneHistorique(it) }
        }
    }
}
