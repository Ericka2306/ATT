package mg.itu.att.ui.connexion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mg.itu.att.data.AppDatabase
import mg.itu.att.data.DonneesInitiales
import mg.itu.att.data.Role
import mg.itu.att.securite.MotDePasse

/**
 * L'utilisateur connecté, tel que les autres ViewModels le reçoivent pour filtrer leurs données
 * (instructions §7 : chaque rôle ne voit que ce qui le concerne).
 */
data class SessionUtilisateur(
    val id: Int,
    val nom: String,
    val role: Role,
    val regionId: Int? = null,
    val autoEcoleId: Int? = null,
    val examinateurId: Int? = null,
    val candidatId: Int? = null,
)

/** État de l'écran de connexion : les champs saisis, l'erreur éventuelle, le travail en cours. */
data class EtatConnexion(
    val identifiant: String = "",
    val motDePasse: String = "",
    val erreur: String? = null,
    val enCours: Boolean = false,
)

/**
 * Porte la session utilisateur pour toute l'application : créé au-dessus du NavHost,
 * il survit aux changements d'écran et à la rotation (cours S6).
 * Au premier lancement, il remplit la base (même geste que listedetailv3).
 */
class ConnexionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.obtenir(application)

    private val _uiState = MutableStateFlow(EtatConnexion())
    val uiState: StateFlow<EtatConnexion> = _uiState

    private val _session = MutableStateFlow<SessionUtilisateur?>(null)
    /** null = personne n'est connecté. */
    val session: StateFlow<SessionUtilisateur?> = _session

    init {
        viewModelScope.launch { DonneesInitiales.insererSiVide(db) }
    }

    fun changerIdentifiant(valeur: String) = _uiState.update { it.copy(identifiant = valeur, erreur = null) }

    fun changerMotDePasse(valeur: String) = _uiState.update { it.copy(motDePasse = valeur, erreur = null) }

    /**
     * Vérifie les identifiants puis appelle [onSucces] (l'écran signale, la navigation décide).
     * Le message d'erreur est volontairement le même pour « identifiant inconnu » et « mot de passe faux ».
     */
    fun seConnecter(onSucces: () -> Unit) {
        val etat = _uiState.value
        if (etat.identifiant.isBlank() || etat.motDePasse.isBlank()) {
            _uiState.update { it.copy(erreur = "Saisissez votre identifiant et votre mot de passe.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(enCours = true) }
            val utilisateur = db.utilisateurDao().parIdentifiant(etat.identifiant.trim())
            when {
                // Identifiant inconnu OU mot de passe faux : même message, pour ne pas révéler lequel.
                utilisateur == null || !MotDePasse.verifier(etat.motDePasse, utilisateur.motDePasseHash) ->
                    _uiState.update { it.copy(enCours = false, erreur = "Identifiant ou mot de passe incorrect.") }
                !utilisateur.actif ->
                    _uiState.update { it.copy(enCours = false, erreur = "Ce compte est désactivé.") }
                else -> {
                    _session.value = SessionUtilisateur(
                        id = utilisateur.id,
                        nom = utilisateur.nom,
                        role = utilisateur.role,
                        regionId = utilisateur.regionId,
                        autoEcoleId = utilisateur.autoEcoleId,
                        examinateurId = utilisateur.examinateurId,
                        candidatId = utilisateur.candidatId,
                    )
                    // On ne garde pas le mot de passe en mémoire une fois connecté.
                    _uiState.value = EtatConnexion()
                    onSucces()
                }
            }
        }
    }

    fun seDeconnecter() {
        _session.value = null
        _uiState.value = EtatConnexion()
    }
}
