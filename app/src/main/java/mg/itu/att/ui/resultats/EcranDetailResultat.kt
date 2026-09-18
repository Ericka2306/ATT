package mg.itu.att.ui.resultats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import mg.itu.att.metier.formatDate
import mg.itu.att.ui.communs.LigneActions
import mg.itu.att.ui.communs.CarteFiche
import mg.itu.att.ui.communs.ChampTexte
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.EncartInfo
import mg.itu.att.ui.communs.TonEncart
import mg.itu.att.ui.communs.HistoriqueVide
import mg.itu.att.ui.communs.LigneHistorique
import mg.itu.att.ui.communs.LigneInfo
import mg.itu.att.ui.communs.TexteErreur
import mg.itu.att.ui.communs.TitreSection

/**
 * Détail d'un résultat (UC10, UC11) : le calcul expliqué, la validation par l'ATT, la correction traçable
 * (nouvelle ligne, l'ancienne reste lisible) et, en cas d'échec, les épreuves à repasser.
 */
@Composable
fun EcranDetailResultat(
    viewModel: ResultatsViewModel,
    resultatId: Int,
    onImprimer: () -> Unit,
    onCorrige: (Int) -> Unit,
    onRetour: () -> Unit,
) {
    viewModel.afficher(resultatId)
    val etat by viewModel.detail.collectAsState()
    val saisie by viewModel.saisie.collectAsState()
    val ligne = etat.ligne

    EcranStandard(titre = "Résultat", onRetour = onRetour) {
        if (ligne == null) {
            Text("Résultat introuvable.")
            return@EcranStandard
        }
        val r = ligne.resultat
        LazyColumn {
            item {
                CarteFiche {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(ligne.nomCandidat, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        PastilleReussite(r.reussi)
                    }
                    Spacer(Modifier.height(8.dp))
                    LigneInfo("Épreuve", "permis ${ligne.codeCategorie}, ${ligne.libelleEpreuve}")
                    LigneInfo("Passage", "n° ${ligne.tentative?.numero ?: "?"}")
                    LigneInfo("Note", r.noteLisible())
                    LigneInfo("Statut", r.statut.libelle())
                    LigneInfo("Calculé le", formatDate(r.dateCalcul))
                    r.dateValidation?.let { LigneInfo("Validé le", formatDate(it)) }
                    r.motifCorrection?.let { LigneInfo("Motif de correction", it) }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onImprimer) { Text("Imprimer le relevé") }
                }

                // Le détail du calcul : d'où vient la note affichée (barème, points, faute éliminatoire).
                etat.calcul?.let { c ->
                    TitreSection("Détail du calcul")
                    CarteFiche {
                        LigneInfo("Points attribués", "${formatNote(c.totalBrut)} sur ${formatNote(c.totalPropose)} proposés")
                        if (c.totalPropose != c.noteMax) {
                            LigneInfo("Rapporté au barème", "${formatNote(c.totalBrut)} / ${formatNote(c.totalPropose)} × ${formatNote(c.noteMax)} = ${formatNote(c.noteObtenue)}")
                        }
                        LigneInfo("Seuil de réussite", formatNote(c.seuil))
                        if (c.fauteEliminatoire) {
                            Text(
                                "Faute éliminatoire : l'épreuve est perdue quel que soit le total.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }

                if (etat.epreuvesARepasser.isNotEmpty()) {
                    TitreSection("Épreuves à repasser")
                    CarteFiche {
                        etat.epreuvesARepasser.forEach { e ->
                            LigneInfo(e.libelle, e.raison + (e.inscriptibleLe?.let { " · réinscription à partir du ${formatDate(it)}" } ?: ""))
                        }
                        EncartInfo("Délais et conservation viennent de la configuration (valeurs à confirmer par l'ATT).", TonEncart.AVERTISSEMENT)
                    }
                }

                if (etat.peutValider || etat.peutCorriger) {
                    TitreSection("Décision de l'ATT")
                    saisie.message?.let { Text(it, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium) }
                    if (etat.peutCorriger) {
                        ChampTexte(saisie.motif, viewModel::changerMotif, "Motif de la correction *", uneLigne = false)
                    }
                    TexteErreur(saisie.erreur)
                    LigneActions {
                        if (etat.peutValider) Button(onClick = { viewModel.valider(r.id) }) { Text("Valider le résultat") }
                        if (etat.peutCorriger) OutlinedButton(onClick = { viewModel.corriger(r.id, onCorrige) }) { Text("Corriger (recalculer)") }
                    }
                    EncartInfo("Une correction ne modifie jamais ce résultat : elle en crée un nouveau, et celui-ci reste consultable.")
                }

                if (etat.versions.size > 1) {
                    TitreSection("Versions de ce passage")
                    CarteFiche {
                        etat.versions.forEach { v ->
                            LigneInfo(
                                "n° ${v.id}" + if (v.id == r.id) " (affiché)" else "",
                                "${v.noteLisible()} · ${if (v.reussi) "réussi" else "échec"} · ${v.statut.libelle()}",
                            )
                        }
                    }
                }
                TitreSection("Historique")
                if (etat.historique.isEmpty()) HistoriqueVide()
            }
            items(etat.historique) { LigneHistorique(it) }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
