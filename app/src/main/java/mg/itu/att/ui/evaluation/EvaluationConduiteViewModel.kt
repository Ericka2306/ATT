package mg.itu.att.ui.evaluation

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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mg.itu.att.data.ActionsHistorique
import mg.itu.att.data.AppDatabase
import mg.itu.att.data.Bareme
import mg.itu.att.data.CriterePratique
import mg.itu.att.data.EntitesHistorique
import mg.itu.att.data.Evaluation
import mg.itu.att.data.EvaluationCritere
import mg.itu.att.data.Role
import mg.itu.att.data.StatutPresence
import mg.itu.att.data.StatutTentative
import mg.itu.att.data.Tentative
import mg.itu.att.data.resume
import mg.itu.att.data.tracer
import mg.itu.att.metier.NoteCritere
import mg.itu.att.metier.ReglesConduite
import mg.itu.att.metier.maintenantIso
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.connexion.SessionUtilisateur

// ---------- ÉTATS ----------

/** Ce que l'examinateur écrit pour un critère : points (texte tel que tapé), faute éliminatoire, observation. */
data class SaisieCritere(val points: String = "", val faute: Boolean = false, val observation: String = "")

/** Une ligne de la grille de conduite : le critère et ce qui a été saisi. */
data class CritereNote(val critere: CriterePratique, val saisie: SaisieCritere) {
    val note: NoteCritere get() = NoteCritere(critere.points, saisie.points, critere.eliminatoire, saisie.faute)
}

data class EtatConduite(
    val tentative: Tentative? = null,
    val entete: String = "",
    val noteMax: Double = 0.0,
    val pointsGrille: Double = 0.0,
    val criteres: List<CritereNote> = emptyList(),
    val totalAttribue: Double = 0.0,
    val fauteEliminatoire: Boolean = false,
    /** Vrai tant que l'épreuve est ouverte et que l'utilisateur connecté peut saisir. */
    val enCours: Boolean = false,
    val erreur: String? = null,
) {
    val sansNote: Int get() = ReglesConduite.sansNote(criteres.map { it.note })
}

// ---------- LE VIEWMODEL ----------

/**
 * Épreuve de conduite (UC09, étape C10) : structure seulement, la grille vient de la configuration (Q2).
 * À l'ouverture, l'évaluation est créée sur le barème courant avec une ligne par critère actif (grille figée) ;
 * l'examinateur saisit points, faute éliminatoire et observation par critère, comme sur la feuille.
 * « Terminer » fige tout, passe la tentative TERMINEE et la présence TERMINE. Le calcul est l'affaire de C11.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EvaluationConduiteViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private val session = MutableStateFlow<SessionUtilisateur?>(null)
    private val idTentative = MutableStateFlow(0)
    private val erreur = MutableStateFlow<String?>(null)
    private val entete = MutableStateFlow("")
    private val bareme = MutableStateFlow<Bareme?>(null)
    private val observations = MutableStateFlow("")
    private val _saisies = MutableStateFlow<Map<Int, SaisieCritere>>(emptyMap())

    /** La frappe de l'examinateur, exposée directement (même raison que pour la théorie : le champ doit suivre la frappe). */
    val saisies: StateFlow<Map<Int, SaisieCritere>> = _saisies
    val observationsSaisies: StateFlow<String> = observations

    override fun definirSession(session: SessionUtilisateur) {
        if (this.session.value != session) this.session.value = session
    }

    // ----- Ouverture -----

    /** Charge la tentative ; crée l'évaluation et la grille (une ligne par critère actif) si elles n'existent pas encore. */
    fun afficher(tentativeId: Int) {
        if (idTentative.value == tentativeId) return
        idTentative.value = tentativeId
        erreur.value = null
        _saisies.value = emptyMap()
        viewModelScope.launch {
            val tentative = db.tentativeDao().parId(tentativeId) ?: return@launch
            val inscription = db.inscriptionDao().parId(tentative.inscriptionId)
            val candidat = db.candidatDao().parId(tentative.candidatId)
            val nomVisible = session.value?.role != Role.EXAMINATEUR
            entete.value = "Passage n° ${tentative.numero} — candidat n° ${inscription?.numeroAnonymat ?: "?"}" +
                if (nomVisible && candidat != null) " (${candidat.nom} ${candidat.prenom})" else ""
            val baremeCourant = db.baremeDao().courant(tentative.typeEpreuveId)

            val existante = db.evaluationDao().parTentative(tentativeId)
            if (existante != null) {
                bareme.value = db.baremeDao().parId(existante.baremeId)
                observations.value = existante.observations ?: ""
                _saisies.value = db.evaluationCritereDao().listePourEvaluation(existante.id)
                    .associate { it.critereId to SaisieCritere(it.note?.let(::formatPoints) ?: "", it.fauteEliminatoire, it.observation ?: "") }
                return@launch
            }
            bareme.value = baremeCourant
            val utilisateur = session.value
            if (tentative.statut != StatutTentative.EN_COURS || !utilisateur.peutEvaluer() || utilisateur == null) return@launch
            val criteresActifs = db.criterePratiqueDao().actifsPourEpreuve(tentative.typeEpreuveId)
            val refus = ReglesConduite.verifierOuverture(baremeCourant, criteresActifs)
            if (refus != null || baremeCourant == null) return@launch run { erreur.value = refus }
            db.withTransaction {
                val evaluation = Evaluation(tentativeId = tentativeId, baremeId = baremeCourant.id, dateSaisie = maintenantIso())
                val id = db.evaluationDao().inserer(evaluation).toInt()
                db.evaluationCritereDao().insererToutes(criteresActifs.map { EvaluationCritere(evaluationId = id, critereId = it.id) })
                db.tracer(
                    EntitesHistorique.EVALUATION, id, ActionsHistorique.CREATION, utilisateur.id,
                    nouvelleValeur = evaluation.copy(id = id).resume() + ", conduite, grille de ${criteresActifs.size} critères",
                )
            }
        }
    }

    // ----- État de l'écran -----

    private data class Locaux(val session: SessionUtilisateur?, val erreur: String?, val bareme: Bareme?, val entete: String)

    val etat: StateFlow<EtatConduite> =
        idTentative.flatMapLatest { id ->
            combine(db.tentativeDao().parIdEnDirect(id), db.evaluationDao().parTentativeEnDirect(id)) { t, e -> t to e }
                .flatMapLatest { (tentative, evaluation) ->
                    if (tentative == null) return@flatMapLatest flowOf(EtatConduite())
                    combine(
                        db.criterePratiqueDao().parEpreuve(tentative.typeEpreuveId),
                        db.evaluationCritereDao().parEvaluation(evaluation?.id ?: 0),
                        _saisies,
                        combine(session, erreur, bareme, entete) { s, e, b, en -> Locaux(s, e, b, en) },
                    ) { criteres, lignes, saisies, locaux ->
                        val notes = lignes.mapNotNull { l ->
                            criteres.find { it.id == l.critereId }?.let { c ->
                                CritereNote(c, saisies[c.id] ?: SaisieCritere(l.note?.let(::formatPoints) ?: "", l.fauteEliminatoire, l.observation ?: ""))
                            }
                        }
                        EtatConduite(
                            tentative = tentative,
                            entete = locaux.entete,
                            noteMax = locaux.bareme?.noteMax ?: 0.0,
                            pointsGrille = ReglesConduite.pointsGrille(notes.map { it.critere }),
                            criteres = notes,
                            totalAttribue = ReglesConduite.totalAttribue(notes.map { it.note }),
                            fauteEliminatoire = ReglesConduite.fauteEliminatoire(notes.map { it.note }),
                            enCours = tentative.statut == StatutTentative.EN_COURS && locaux.session.peutEvaluer() && evaluation != null,
                            erreur = locaux.erreur,
                        )
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatConduite())

    // ----- Saisie -----

    fun changerObservations(texte: String) { observations.value = texte }

    /** L'examinateur note un critère (points, faute, observation) ; la base suit. */
    fun saisir(critereId: Int, transformation: (SaisieCritere) -> SaisieCritere) {
        val e = etat.value
        if (!e.enCours) return
        val actuelle = _saisies.value[critereId] ?: e.criteres.find { it.critere.id == critereId }?.saisie ?: SaisieCritere()
        val nouvelle = transformation(actuelle)
        _saisies.update { it + (critereId to nouvelle) }
        erreur.value = null
        viewModelScope.launch { enregistrerLigne(critereId, nouvelle) }
    }

    private suspend fun enregistrerLigne(critereId: Int, saisie: SaisieCritere) {
        val evaluation = db.evaluationDao().parTentative(idTentative.value) ?: return
        val ligne = db.evaluationCritereDao().pourCritere(evaluation.id, critereId) ?: return
        val critere = db.criterePratiqueDao().parId(critereId) ?: return
        val note = NoteCritere(critere.points, saisie.points, critere.eliminatoire, saisie.faute)
        db.evaluationCritereDao().modifier(ligne.copy(note = note.points, fauteEliminatoire = saisie.faute, observation = saisie.observation.trim().ifBlank { null }))
    }

    /** Termine l'épreuve : tous les critères notés ; évaluation figée, tentative TERMINEE, présence TERMINE, tracés ensemble. */
    fun terminer(onTerminee: () -> Unit) {
        val e = etat.value
        val utilisateur = session.value ?: return
        if (!e.enCours) return
        val refus = ReglesConduite.verifierFin(e.criteres.map { it.note })
        if (refus != null) return run { erreur.value = refus }
        viewModelScope.launch {
            val tentative = db.tentativeDao().parId(idTentative.value) ?: return@launch
            val evaluation = db.evaluationDao().parTentative(tentative.id) ?: return@launch
            val presence = db.presenceDao().parInscription(tentative.inscriptionId)
            db.withTransaction {
                for (c in e.criteres) enregistrerLigne(c.critere.id, c.saisie)
                val figee = evaluation.copy(dateSaisie = maintenantIso(), observations = observations.value.trim().ifBlank { null })
                db.evaluationDao().modifier(figee)
                db.tracer(
                    EntitesHistorique.EVALUATION, figee.id, ActionsHistorique.VALIDATION, utilisateur.id,
                    nouvelleValeur = figee.resume() + ", conduite, ${formatPoints(e.totalAttribue)} points sur ${formatPoints(e.pointsGrille)}" + if (e.fauteEliminatoire) ", faute éliminatoire" else "",
                )
                val terminee = tentative.copy(statut = StatutTentative.TERMINEE, examinateurId = tentative.examinateurId ?: utilisateur.examinateurId)
                db.tentativeDao().modifier(terminee)
                db.tracer(EntitesHistorique.TENTATIVE, tentative.id, ActionsHistorique.MODIFICATION, utilisateur.id, ancienneValeur = tentative.resume(), nouvelleValeur = terminee.resume())
                if (presence != null) {
                    val modifiee = presence.copy(statut = StatutPresence.TERMINE)
                    db.presenceDao().modifier(modifiee)
                    db.tracer(EntitesHistorique.PRESENCE, presence.id, ActionsHistorique.MODIFICATION, utilisateur.id, ancienneValeur = presence.resume(), nouvelleValeur = modifiee.resume())
                }
            }
            erreur.value = null
            onTerminee()
        }
    }
}
