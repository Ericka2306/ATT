package mg.itu.att.ui.evaluation

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import mg.itu.att.ui.communs.CarteIcone
import mg.itu.att.ui.communs.EcranStandard
import mg.itu.att.ui.communs.EncartInfo
import mg.itu.att.ui.sessions.PastilleSession

/** Sessions à évaluer (examinateur, UC09) : du jour et à venir, avec présents et tentatives ouvertes. */
@Composable
fun EcranSessionsExaminateur(viewModel: TentativesViewModel, onOuvrir: (Int) -> Unit, onRetour: () -> Unit) {
    val sessions by viewModel.sessionsAEvaluer.collectAsState()

    EcranStandard(titre = "Sessions à évaluer", onRetour = onRetour) {
        EncartInfo("Aucune affectation préalable : tout examinateur connecté peut évaluer un candidat présent.")
        if (sessions.isEmpty()) Text("Aucune session ouverte aujourd'hui ou à venir.", style = MaterialTheme.typography.bodyLarge)
        LazyColumn {
            items(sessions) { s ->
                CarteIcone(
                    icone = Icons.Filled.DateRange,
                    titre = s.libelle,
                    description = "${s.presents} présent(s) · ${s.tentatives} passage(s) ouvert(s)",
                    onClick = { onOuvrir(s.session.id) },
                    complement = { PastilleSession(s.session.statut) },
                )
            }
        }
    }
}
