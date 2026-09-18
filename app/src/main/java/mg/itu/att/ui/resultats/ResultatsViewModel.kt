package mg.itu.att.ui.resultats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mg.itu.att.data.ActionsHistorique
import mg.itu.att.data.AppDatabase
import mg.itu.att.data.Candidat
import mg.itu.att.data.ClesRegles
import mg.itu.att.data.EntitesHistorique
import mg.itu.att.data.Historique
import mg.itu.att.data.Resultat
import mg.itu.att.data.Role
import mg.itu.att.data.StatutResultat
import mg.itu.att.data.Synchronisation
import mg.itu.att.data.Tentative
import mg.itu.att.data.calculerPourTentative
import mg.itu.att.data.enregistrerResultat
import mg.itu.att.data.regleEntier
import mg.itu.att.data.resume
import mg.itu.att.data.tracer
import mg.itu.att.metier.CalculResultat
import mg.itu.att.metier.ReglesRepassage
import mg.itu.att.metier.dateDuJour
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.connexion.SessionUtilisateur

// ---------- ÉTATS ----------

/** Un résultat avec ce qu'il faut pour l'afficher sans rouvrir la base (jointures faites dans le ViewModel). */
data class ResultatLigne(
    val resultat: Resultat,
    val tentative: Tentative?,
    val nomCandidat: String,
    val candidatId: Int,
    val libelleEpreuve: String,
    val codeCategorie: String,
    val nomAutoEcole: String,
)

enum class FiltreResultats { A_VALIDER, VALIDES, TOUS }

data class EtatListeResultats(
    val lignes: List<ResultatLigne> = emptyList(),
    val filtre: FiltreResultats = FiltreResultats.A_VALIDER,
    val total: Int = 0,
    /** Vrai pour l'ATT : elle valide et corrige. Les autres rôles consultent. */
    val peutValider: Boolean = false,
    /** Titre de l'écran, différent selon le rôle (UC12). */
    val titre: String = "Résultats",
)

data class EtatDetailResultat(
    val ligne: ResultatLigne? = null,
    /** Les résultats successifs de la même tentative : corrections comprises, jamais effacées (R6). */
    val versions: List<Resultat> = emptyList(),
    val historique: List<Historique> = emptyList(),
    /** Détail du calcul relu depuis la saisie : sert à expliquer la note affichée. */
    val calcul: CalculResultat.Calcul? = null,
    val epreuvesARepasser: List<ReglesRepassage.ARepasser> = emptyList(),
    val peutValider: Boolean = false,
    val peutCorriger: Boolean = false,
)

/**
 * La saisie du motif de correction, exposée à part : elle ne doit pas passer par le flux du détail,
 * qui relit la base à chaque émission (sinon la frappe saute, défaut constaté en C9).
 */
data class SaisieCorrection(val motif: String = "", val message: String? = null, val erreur: String? = null)

// ---------- LE VIEWMODEL ----------

/**
 * Résultats (UC10, UC11, UC12) : calcul déjà fait à la clôture de l'épreuve, validation par l'ATT,
 * correction traçable (nouvelle ligne, jamais de réécriture), et consultation filtrée par rôle.
 */
@OptIn(ExperimentalCoroutinesApi::class) // flatMapLatest (HORS_COURS n° 18)
class ResultatsViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private val session = MutableStateFlow<SessionUtilisateur?>(null)
    private val filtre = MutableStateFlow(FiltreResultats.A_VALIDER)

    override fun definirSession(session: SessionUtilisateur) {
        if (this.session.value != session) {
            this.session.value = session
            // Un rôle qui ne valide pas n'a que faire de la file « à valider ».
            if (!estAtt(session)) filtre.value = FiltreResultats.VALIDES
        }
    }

    private fun estAtt(s: SessionUtilisateur?) = s?.role == Role.ADMIN_ATT || s?.role == Role.SUPER_ADMIN

    fun filtrer(f: FiltreResultats) { filtre.value = f }

    // ----- Liste -----

    /** Toutes les lignes visibles par l'utilisateur, avant filtre d'écran (matrice docs/01 §6). */
    private val lignesVisibles: kotlinx.coroutines.flow.Flow<List<ResultatLigne>> = combine(
        db.resultatDao().tous(),
        db.tentativeDao().toutes(),
        combine(db.candidatDao().tous(), db.autoEcoleDao().toutes()) { c, a -> c to a },
        combine(db.typeEpreuveDao().toutes(), db.categoriePermisDao().toutes()) { e, c -> e to c },
        session,
    ) { resultats, tentatives, (candidats, autoEcoles), (epreuves, categories), s ->
        resultats.mapNotNull { r ->
            val t = tentatives.find { it.id == r.tentativeId }
            val candidat: Candidat? = candidats.find { it.id == t?.candidatId }
            val autoEcole = autoEcoles.find { it.id == candidat?.autoEcoleId }
            val epreuve = epreuves.find { it.id == t?.typeEpreuveId }
            val visible = when {
                s == null -> false
                s.role == Role.SUPER_ADMIN -> true
                s.role == Role.ADMIN_ATT -> s.regionId == null || s.regionId == autoEcole?.regionId
                s.role == Role.AUTO_ECOLE -> candidat?.autoEcoleId == s.autoEcoleId && r.statut == StatutResultat.VALIDE_ATT
                s.role == Role.CANDIDAT -> candidat?.id == s.candidatId && r.statut == StatutResultat.VALIDE_ATT
                // L'examinateur relit les épreuves qu'il a lui-même saisies (« Mes évaluations »).
                s.role == Role.EXAMINATEUR -> t?.examinateurId != null && t.examinateurId == s.examinateurId
                else -> false
            }
            if (!visible) return@mapNotNull null
            ResultatLigne(
                resultat = r, tentative = t,
                nomCandidat = candidat?.let { "${it.nom} ${it.prenom}" } ?: "?",
                candidatId = candidat?.id ?: 0,
                libelleEpreuve = epreuve?.libelle ?: "?",
                codeCategorie = categories.find { it.id == epreuve?.categorieId }?.code ?: "?",
                nomAutoEcole = autoEcole?.nom ?: "?",
            )
        }
    }

    val liste: StateFlow<EtatListeResultats> =
        combine(lignesVisibles, filtre, session) { lignes, f, s ->
            EtatListeResultats(
                lignes = when (f) {
                    FiltreResultats.A_VALIDER -> lignes.filter { it.resultat.statut == StatutResultat.CALCULE || it.resultat.statut == StatutResultat.CORRIGE }
                    FiltreResultats.VALIDES -> lignes.filter { it.resultat.statut == StatutResultat.VALIDE_ATT }
                    FiltreResultats.TOUS -> lignes
                },
                filtre = f,
                total = lignes.size,
                peutValider = estAtt(s),
                titre = when (s?.role) {
                    Role.EXAMINATEUR -> "Mes évaluations"
                    Role.AUTO_ECOLE, Role.CANDIDAT -> "Résultats"
                    else -> "Résultats"
                },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatListeResultats())

    // ----- Détail -----

    private val idDetail = MutableStateFlow(0)

    private val _saisie = MutableStateFlow(SaisieCorrection())
    /** Duo `_uiState` / `uiState` du cours : la frappe reste locale, la base est lue par ailleurs. */
    val saisie: StateFlow<SaisieCorrection> = _saisie

    fun afficher(resultatId: Int) {
        if (idDetail.value != resultatId) {
            idDetail.value = resultatId
            _saisie.value = SaisieCorrection()
        }
    }

    fun changerMotif(texte: String) = _saisie.update { it.copy(motif = texte, erreur = null) }

    val detail: StateFlow<EtatDetailResultat> =
        idDetail.flatMapLatest { id ->
            combine(
                db.resultatDao().parIdEnDirect(id),
                lignesVisibles,
                db.historiqueDao().pourObjet(EntitesHistorique.RESULTAT, id),
                session,
            ) { resultat, lignes, historique, s ->
                val ligne = lignes.find { it.resultat.id == id }
                EtatDetailResultat(
                    ligne = ligne,
                    historique = historique,
                    // Un résultat corrigé repasse par la validation : une correction ne se publie pas toute seule.
                    peutValider = estAtt(s) && (resultat?.statut == StatutResultat.CALCULE || resultat?.statut == StatutResultat.CORRIGE),
                    peutCorriger = estAtt(s) && (resultat?.statut == StatutResultat.VALIDE_ATT || resultat?.statut == StatutResultat.CORRIGE),
                )
            }.map { etat ->
                val r = etat.ligne?.resultat ?: return@map etat
                // Relecture du calcul et des épreuves à repasser : requêtes ponctuelles, hors du flux.
                etat.copy(
                    versions = db.versionsPourTentative(r.tentativeId),
                    calcul = db.calculPourAffichage(r.tentativeId),
                    epreuvesARepasser = if (r.reussi) emptyList() else db.epreuvesARepasser(etat.ligne.candidatId, etat.ligne.tentative?.typeEpreuveId),
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatDetailResultat())

    // ----- Actions de l'ATT -----

    /** Validation par l'ATT : le résultat devient visible par l'auto-école et le candidat (UC10). */
    fun valider(resultatId: Int) {
        val utilisateur = session.value ?: return
        if (!estAtt(utilisateur)) return
        viewModelScope.launch {
            val r = db.resultatDao().parId(resultatId) ?: return@launch
            if (r.statut != StatutResultat.CALCULE && r.statut != StatutResultat.CORRIGE) return@launch
            val valide = r.copy(statut = StatutResultat.VALIDE_ATT, valideParId = utilisateur.id, dateValidation = dateDuJour())
            db.withTransaction {
                db.resultatDao().modifier(valide)
                db.tracer(EntitesHistorique.RESULTAT, r.id, ActionsHistorique.VALIDATION, utilisateur.id, ancienneValeur = r.resume(), nouvelleValeur = valide.resume())
            }
            _saisie.update { it.copy(message = "Résultat validé : il est désormais visible par l'auto-école et le candidat.") }
            // Offline-first (cours S7) : la base d'abord, le réseau ensuite. L'échec de l'envoi est sans gravité :
            // le résultat reste dans la file d'attente et sera remonté par « Synchroniser » ou à la prochaine validation.
            Synchronisation.synchroniserResultats(db)
        }
    }

    /**
     * Correction (UC10, §11 « erreur de notation ») : on recalcule depuis la saisie corrigée par l'examinateur
     * et on crée une NOUVELLE ligne ; l'ancienne passe en ANNULE mais reste lisible (R6).
     */
    fun corriger(resultatId: Int, onCorrige: (Int) -> Unit) {
        val utilisateur = session.value ?: return
        if (!estAtt(utilisateur)) return
        val texte = _saisie.value.motif.trim()
        viewModelScope.launch {
            val ancien = db.resultatDao().parId(resultatId)
            val refus = CalculResultat.verifierCorrection(texte, ancien?.statut)
            if (refus != null || ancien == null) return@launch run { _saisie.update { it.copy(erreur = refus) } }
            val nouveauId = db.withTransaction {
                val remplace = ancien.copy(statut = StatutResultat.ANNULE)
                db.resultatDao().modifier(remplace)
                db.enregistrerResultat(ancien.tentativeId, utilisateur.id, remplace = ancien, motifCorrection = texte)
            }
            if (nouveauId == null) {
                _saisie.update { it.copy(erreur = "Le recalcul est impossible : vérifiez la saisie de l'épreuve et le barème.") }
                return@launch
            }
            _saisie.value = SaisieCorrection(message = "Correction enregistrée : l'ancien résultat reste consultable dans l'historique.")
            onCorrige(nouveauId)
        }
    }
}

// ---------- LECTURES PONCTUELLES ----------

/** Toutes les versions d'un résultat pour une tentative (corrections comprises). */
private suspend fun AppDatabase.versionsPourTentative(tentativeId: Int): List<Resultat> =
    resultatDao().listePourTentative(tentativeId)

/** Le détail du calcul, relu depuis la saisie : sert à expliquer la note à l'écran. */
private suspend fun AppDatabase.calculPourAffichage(tentativeId: Int): CalculResultat.Calcul? =
    calculerPourTentative(tentativeId)

/** Les épreuves que le candidat doit repasser après un échec (UC11), selon les règles configurées. */
private suspend fun AppDatabase.epreuvesARepasser(candidatId: Int, typeEpreuveId: Int?): List<ReglesRepassage.ARepasser> {
    if (candidatId == 0 || typeEpreuveId == null) return emptyList()
    val epreuve = typeEpreuveDao().parId(typeEpreuveId) ?: return emptyList()
    val epreuvesCategorie = typeEpreuveDao().listeActivesPourCategorie(epreuve.categorieId)
    val conservation = regleEntier(ClesRegles.CONSERVATION_EPREUVE_REUSSIE_JOURS, epreuve.categorieId) ?: 0
    val delai = regleEntier(ClesRegles.DELAI_REPASSAGE_JOURS, epreuve.categorieId) ?: 0
    val etats = epreuvesCategorie.map { e ->
        val tentatives = tentativeDao().listePourCandidatEtEpreuve(candidatId, e.id)
        ReglesRepassage.EtatEpreuve(
            typeEpreuveId = e.id,
            libelle = e.libelle,
            dateReussite = resultatDao().dernierReussi(candidatId, e.id)?.dateCalcul,
            dateDernierPassage = tentatives.maxByOrNull { it.numero }?.dateHeure?.substringBefore('T'),
        )
    }
    return ReglesRepassage.epreuvesARepasser(etats, conservation, delai, dateDuJour())
}
