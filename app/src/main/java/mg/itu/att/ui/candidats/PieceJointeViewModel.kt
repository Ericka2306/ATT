package mg.itu.att.ui.candidats

import android.app.Application
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mg.itu.att.data.AppDatabase
import mg.itu.att.metier.PiecesJointes
import mg.itu.att.metier.ReglesConsultation
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.connexion.SessionUtilisateur

data class EtatPieceJointe(
    val titre: String = "Pièce jointe",
    val candidat: String = "",
    /** La photo, ou les pages du PDF, prêtes à afficher. */
    val pages: List<ImageBitmap> = emptyList(),
    val estPdf: Boolean = false,
    val chargement: Boolean = true,
    val erreur: String? = null,
)

/**
 * Ouvre le fichier joint à une pièce du dossier (étape D2b), pour que l'ATT vérifie sans attendre le papier.
 * Mêmes droits que le dossier : l'ATT, l'auto-école du candidat, le candidat lui-même une fois le dossier soumis.
 */
class PieceJointeViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private var session: SessionUtilisateur? = null

    private val _uiState = MutableStateFlow(EtatPieceJointe())
    val uiState: StateFlow<EtatPieceJointe> = _uiState

    override fun definirSession(session: SessionUtilisateur) {
        this.session = session
    }

    private var pieceChargee: Int? = null

    /** Charge la pièce une seule fois par identifiant (l'écran appelle à chaque recomposition). */
    fun afficher(pieceId: Int) {
        if (pieceChargee == pieceId) return
        pieceChargee = pieceId
        _uiState.value = EtatPieceJointe()
        viewModelScope.launch {
            val s = session ?: return@launch echec("Connexion requise.")
            val piece = db.pieceDossierDao().parId(pieceId) ?: return@launch echec("Pièce introuvable.")
            val dossier = db.dossierDao().parId(piece.dossierId) ?: return@launch echec("Dossier introuvable.")
            val candidat = db.candidatDao().parId(dossier.candidatId) ?: return@launch echec("Candidat introuvable.")
            val regionAutoEcole = db.autoEcoleDao().parId(candidat.autoEcoleId)?.regionId
            val autorise = ReglesConsultation.peutVoirCandidat(s.role, s.regionId, s.autoEcoleId, s.candidatId, candidat.id, candidat.autoEcoleId, regionAutoEcole) &&
                ReglesConsultation.dossierVisible(s.role, dossier.statut)
            if (!autorise) return@launch echec("Cette pièce ne vous concerne pas.")
            val chemin = piece.fichier ?: return@launch echec("Aucun fichier n'est joint à cette pièce.")
            val pages = lirePieceJointe(getApplication(), chemin) ?: return@launch echec("Le fichier ne peut pas être lu.")
            _uiState.value = EtatPieceJointe(
                titre = piece.typePiece,
                candidat = "${candidat.nom} ${candidat.prenom}",
                pages = pages,
                estPdf = PiecesJointes.estPdf(chemin),
                chargement = false,
            )
        }
    }

    private fun echec(message: String) {
        _uiState.value = EtatPieceJointe(chargement = false, erreur = message)
    }
}
