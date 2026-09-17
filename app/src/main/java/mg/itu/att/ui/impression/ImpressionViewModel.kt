package mg.itu.att.ui.impression

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mg.itu.att.data.AppDatabase
import mg.itu.att.data.Role
import mg.itu.att.data.StatutResultat
import mg.itu.att.metier.DocumentsImpression
import mg.itu.att.metier.dateDuJour
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.connexion.SessionUtilisateur
import mg.itu.att.ui.resultats.noteLisible

/** Les quatre documents imprimables (UC13). */
enum class TypeDocument { CONVOCATION, LISTE_APPEL, LISTE_ADMIS, RELEVE }

data class EtatImpression(
    val titre: String = "Impression",
    /** La page HTML prête à afficher et à imprimer ; null tant qu'elle n'est pas construite. */
    val html: String? = null,
    val erreur: String? = null,
)

/**
 * Prépare les documents à imprimer (UC13) : le ViewModel rassemble les données en base,
 * la mise en page revient aux fonctions pures de `metier/DocumentsImpression`.
 * L'impression elle-même est confiée au service Android par l'écran (docs/HORS_COURS.md n° 10 et n° 24).
 */
class ImpressionViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private var session: SessionUtilisateur? = null

    private val _uiState = MutableStateFlow(EtatImpression())
    val uiState: StateFlow<EtatImpression> = _uiState

    override fun definirSession(session: SessionUtilisateur) {
        this.session = session
    }

    private fun estAtt() = session?.role == Role.ADMIN_ATT || session?.role == Role.SUPER_ADMIN

    private var dernierDocument: Pair<TypeDocument, Int>? = null

    /** Construit le document demandé une seule fois par identifiant (l'écran appelle à chaque recomposition). */
    fun preparer(type: TypeDocument, id: Int) {
        if (dernierDocument == type to id) return
        dernierDocument = type to id
        _uiState.value = EtatImpression()
        viewModelScope.launch {
            when (type) {
                TypeDocument.CONVOCATION -> convocation(id)
                TypeDocument.LISTE_APPEL -> listeAppel(id)
                TypeDocument.LISTE_ADMIS -> listeAdmis(id)
                TypeDocument.RELEVE -> releve(id)
            }
        }
    }

    private fun echec(message: String) = _uiState.update { it.copy(erreur = message) }

    // ---------- CONVOCATION ----------

    private suspend fun convocation(inscriptionId: Int) {
        val inscription = db.inscriptionDao().parId(inscriptionId) ?: return echec("Inscription introuvable.")
        val candidat = db.candidatDao().parId(inscription.candidatId) ?: return echec("Candidat introuvable.")
        // L'auto-école n'imprime que les convocations de ses candidats (instructions §7).
        if (session?.autoEcoleId != null && candidat.autoEcoleId != session?.autoEcoleId) return echec("Cette convocation ne concerne pas votre auto-école.")
        if (!estAtt() && session?.autoEcoleId == null && session?.candidatId != candidat.id) return echec("Document réservé à l'ATT, à l'auto-école et au candidat concerné.")

        val se = db.sessionDao().parId(inscription.sessionId) ?: return echec("Session introuvable.")
        val centre = db.centreDao().parId(se.centreId)
        val categorie = db.categoriePermisDao().parId(se.categorieId)
        val epreuve = db.typeEpreuveDao().parId(se.typeEpreuveId)
        val creneau = inscription.creneauId?.let { db.creneauDao().parId(it) }
        val pieces = db.pieceDossierDao().listePourDossier(inscription.dossierId).map { it.typePiece }

        _uiState.value = EtatImpression(
            titre = "Convocation — ${candidat.nom} ${candidat.prenom}",
            html = DocumentsImpression.convocation(
                DocumentsImpression.Convocation(
                    nomCandidat = "${candidat.nom} ${candidat.prenom}",
                    dateNaissance = candidat.dateNaissance,
                    nomAutoEcole = db.autoEcoleDao().parId(candidat.autoEcoleId)?.nom ?: "—",
                    codeCategorie = categorie?.code ?: "?",
                    libelleEpreuve = epreuve?.libelle ?: "?",
                    nomCentre = centre?.nom ?: "?",
                    adresseCentre = centre?.adresse ?: "—",
                    dateSession = se.date,
                    heureConvocation = se.heureConvocation,
                    heurePassageEstimee = inscription.heurePassageEstimee ?: creneau?.heureDebut,
                    numeroAnonymat = inscription.numeroAnonymat,
                    pieces = pieces,
                ),
                dateDuJour(),
            ),
        )
    }

    // ---------- LISTES D'UNE SESSION ----------

    /** Les données communes aux deux listes d'une session. */
    private suspend fun contexteSession(sessionId: Int): Triple<mg.itu.att.data.Session, String, Pair<String, String>>? {
        val se = db.sessionDao().parId(sessionId) ?: return null
        val centre = db.centreDao().parId(se.centreId)?.nom ?: "?"
        val categorie = db.categoriePermisDao().parId(se.categorieId)?.code ?: "?"
        val epreuve = db.typeEpreuveDao().parId(se.typeEpreuveId)?.libelle ?: "?"
        return Triple(se, centre, categorie to epreuve)
    }

    private suspend fun listeAppel(sessionId: Int) {
        val ctx = contexteSession(sessionId) ?: return echec("Session introuvable.")
        val (se, centre, libelles) = ctx
        val inscriptions = db.inscriptionDao().listePourSession(sessionId)
            .filter { it.statut != mg.itu.att.data.StatutInscription.ANNULE && it.statut != mg.itu.att.data.StatutInscription.REPORTE }
            .sortedBy { it.numeroAnonymat }
        val creneaux = db.creneauDao().listePourSession(sessionId)
        val lignes = inscriptions.map { i ->
            val candidat = db.candidatDao().parId(i.candidatId)
            DocumentsImpression.LigneAppel(
                numeroAnonymat = i.numeroAnonymat,
                nomCandidat = candidat?.let { "${it.nom} ${it.prenom}" } ?: "?",
                heureCreneau = creneaux.find { it.id == i.creneauId }?.heureDebut,
            )
        }
        // L'examinateur ne doit pas voir les noms (anonymat de l'appel, Q4).
        val avecNoms = estAtt()
        _uiState.value = EtatImpression(
            titre = if (avecNoms) "Liste d'appel" else "Liste d'appel (numéros)",
            html = DocumentsImpression.listeAppel(lignes, libelles.first, libelles.second, centre, se.date, se.heureConvocation, avecNoms, dateDuJour()),
        )
    }

    private suspend fun listeAdmis(sessionId: Int) {
        if (!estAtt()) return echec("La liste des admis est établie par l'ATT.")
        val ctx = contexteSession(sessionId) ?: return echec("Session introuvable.")
        val (se, centre, libelles) = ctx
        val inscriptions = db.inscriptionDao().listePourSession(sessionId)
        val lignes = inscriptions.mapNotNull { i ->
            val tentatives = db.tentativeDao().parInscription(i.id)
            val resultat = tentatives.firstNotNullOfOrNull { t -> db.resultatDao().courantPourTentative(t.id) }
            // Seuls les résultats validés par l'ATT figurent sur la liste (UC10).
            if (resultat == null || resultat.statut != StatutResultat.VALIDE_ATT) return@mapNotNull null
            val candidat = db.candidatDao().parId(i.candidatId)
            DocumentsImpression.LigneAdmis(
                numeroAnonymat = i.numeroAnonymat,
                nomCandidat = candidat?.let { "${it.nom} ${it.prenom}" } ?: "?",
                note = resultat.noteLisible(),
                reussi = resultat.reussi,
            )
        }
        _uiState.value = EtatImpression(
            titre = "Liste des admis",
            html = DocumentsImpression.listeAdmis(lignes, libelles.first, libelles.second, centre, se.date, se.heureConvocation, dateDuJour()),
        )
    }

    // ---------- RELEVÉ ----------

    private suspend fun releve(resultatId: Int) {
        val resultat = db.resultatDao().parId(resultatId) ?: return echec("Résultat introuvable.")
        val tentative = db.tentativeDao().parId(resultat.tentativeId) ?: return echec("Passage introuvable.")
        val candidat = db.candidatDao().parId(tentative.candidatId) ?: return echec("Candidat introuvable.")
        val s = session
        val autorise = estAtt() ||
            (s?.autoEcoleId != null && s.autoEcoleId == candidat.autoEcoleId) ||
            (s?.candidatId != null && s.candidatId == candidat.id)
        if (!autorise) return echec("Ce relevé ne vous concerne pas.")
        if (!estAtt() && resultat.statut != StatutResultat.VALIDE_ATT) return echec("Le relevé est disponible une fois le résultat validé par l'ATT.")

        val epreuve = db.typeEpreuveDao().parId(tentative.typeEpreuveId)
        val categorie = epreuve?.let { db.categoriePermisDao().parId(it.categorieId) }
        _uiState.value = EtatImpression(
            titre = "Relevé — ${candidat.nom} ${candidat.prenom}",
            html = DocumentsImpression.releve(
                DocumentsImpression.Releve(
                    nomCandidat = "${candidat.nom} ${candidat.prenom}",
                    dateNaissance = candidat.dateNaissance,
                    nomAutoEcole = db.autoEcoleDao().parId(candidat.autoEcoleId)?.nom ?: "—",
                    codeCategorie = categorie?.code ?: "?",
                    libelleEpreuve = epreuve?.libelle ?: "?",
                    numeroPassage = tentative.numero,
                    note = resultat.noteLisible(),
                    reussi = resultat.reussi,
                    dateValidation = resultat.dateValidation,
                ),
                dateDuJour(),
            ),
        )
    }
}
