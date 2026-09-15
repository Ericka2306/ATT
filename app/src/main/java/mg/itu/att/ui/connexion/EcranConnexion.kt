package mg.itu.att.ui.connexion

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import mg.itu.att.R
import mg.itu.att.ui.communs.TexteErreur

/**
 * Écran de connexion (UC01). En haut, une photo de route malgache (RN44, Wikimedia Commons, CC0)
 * avec le logo et le titre ; en bas, la carte de saisie.
 * L'état de saisie vit dans le ViewModel, pas dans l'écran (règle S6).
 * `OutlinedTextField` = un Text dont la valeur est un état et qui signale `onValueChange` (HORS_COURS n° 6).
 */
@Composable
fun EcranConnexion(viewModel: ConnexionViewModel, onConnecte: () -> Unit) {
    val etat by viewModel.uiState.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // ---------- BANDEAU PHOTO ----------
        Box(
            Modifier
                .fillMaxWidth()
                .height(300.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.photo_route_rn44),
                contentDescription = "Route nationale 44, Madagascar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            // Voile bleu dégradé pour la lisibilité du titre.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x660F4C81), Color(0xCC0B2E4F)),
                        ),
                    ),
            )
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_logo_att),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        "ATT",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Agence des Transports Terrestres",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Text(
                    "Gestion des examens du permis de conduire",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFD6E4F5),
                )
            }
        }

        // ---------- CARTE DE CONNEXION ----------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Connexion", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Identifiez-vous avec le compte fourni par l'ATT ou votre auto-école.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = etat.identifiant,
                    onValueChange = viewModel::changerIdentifiant,
                    label = { Text("Identifiant") },
                    singleLine = true,
                    enabled = !etat.enCours,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = etat.motDePasse,
                    onValueChange = viewModel::changerMotDePasse,
                    label = { Text("Mot de passe") },
                    singleLine = true,
                    enabled = !etat.enCours,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                TexteErreur(etat.erreur)
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.seConnecter(onSucces = onConnecte) },
                    enabled = !etat.enCours,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                ) {
                    Text(if (etat.enCours) "Vérification…" else "Se connecter")
                }
            }
        }

        Text(
            "Projet universitaire M1 — les règles administratives affichées sont à confirmer par l'ATT.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        // Marge basse = hauteur de la barre de navigation du téléphone (mode bord à bord).
        Spacer(Modifier.navigationBarsPadding())
    }
}
