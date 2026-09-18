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
import mg.itu.att.data.ClesRegles
import mg.itu.att.data.EntitesHistorique
import mg.itu.att.data.Evaluation
import mg.itu.att.data.Question
import mg.itu.att.data.ReponseCandidat
import mg.itu.att.data.Role
import mg.itu.att.data.StatutPresence
import mg.itu.att.data.StatutTentative
import mg.itu.att.data.Tentative
import mg.itu.att.data.regleEntier
import mg.itu.att.data.regleTexte
import mg.itu.att.data.resume
import mg.itu.att.data.enregistrerResultat
import mg.itu.att.data.tracer
import mg.itu.att.metier.ModesTheorie
import mg.itu.att.metier.NoteQuestion
import mg.itu.att.metier.ReglesTheorie
import mg.itu.att.metier.maintenantIso
import mg.itu.att.metier.pointsValides
import mg.itu.att.ui.communs.ViewModelAvecSession
import mg.itu.att.ui.communs.formatPoints
import mg.itu.att.ui.connexion.SessionUtilisateur
import kotlin.random.Random

/** Qui peut ouvrir et saisir une évaluation : l'examinateur, et l'ATT en secours (Q6). */
fun SessionUtilisateur?.peutEvaluer(): Boolean = this?.role == Role.EXAMINATEUR || this?.role == Role.ADMIN_ATT || this?.role == Role.SUPER_ADMIN

// ---------- ÉTATS ----------

/** Ce que l'examinateur écrit pour une question : la réponse du candidat et les points (texte tel que saisi). */
data class SaisieQuestion(val reponse: String = "", val points: String = "")

/** Une ligne de la feuille d'examen : la question posée et ce qui a été saisi. */
data class QuestionPosee(val question: Question, val saisie: SaisieQuestion) {
    val note: NoteQuestion get() = NoteQuestion(question.points, saisie.points)
}

data class EtatTheorie(
    val tentative: Tentative? = null,
    val entete: String = "",
    val modeDirect: Boolean = false,
    val noteMax: Double = 0.0,
    val dureeMinutes: Int = 0,
    val questions: List<QuestionPosee> = emptyList(),
    val pointsPoses: Double = 0.0,
    val totalAttribue: Double = 0.0,
    /** Mode direct : la question proposée à l'examinateur, et les valeurs de points encore disponibles. */
    val proposition: Question? = null,
    val pointsDisponibles: List<Double> = emptyList(),
    val pointsVoulus: Double? = null,
    val observations: String = "",
    /** Vrai tant que l'épreuve est ouverte et que l'utilisateur connecté peut saisir. */
    val enCours: Boolean = false,
    val erreur: String? = null,
) {
    val pointsRestants: Double get() = noteMax - pointsPoses
    val sansNote: Int get() = ReglesTheorie.sansNote(questions.map { it.note })
}

// ---------- LE VIEWMODEL ----------

/**
 * Épreuve théorique orale (UC09, étape C9) : l'examinateur pose des questions, écrit la réponse du candidat et
 * les points qu'il lui attribue, comme sur la feuille d'examen. À l'ouverture, l'évaluation est créée sur le barème
 * courant ; selon la règle `MODE_THEORIE` (Q1 bis), le sujet est tiré au sort et figé (TIRAGE) ou construit question
 * par question (DIRECT). « Terminer » fige tout, passe la tentative TERMINEE et la présence TERMINE. Le calcul de la
 * note finale est l'affaire de C11.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EvaluationTheorieViewModel(application: Application) : AndroidViewModel(application), ViewModelAvecSession {

    private val db = AppDatabase.obtenir(application)
    private val session = MutableStateFlow<SessionUtilisateur?>(null)
    private val idTentative = MutableStateFlow(0)
    private val erreur = MutableStateFlow<String?>(null)
    private val entete = MutableStateFlow("")
    private val mode = MutableStateFlow(ModesTheorie.TIRAGE)
    private val bareme = MutableStateFlow<Bareme?>(null)
    private val dureeMinutes = MutableStateFlow(0)
    private val pointsVoulus = MutableStateFlow<Double?>(null)
    /** Change à chaque « une autre proposition » : le tirage de la proposition dépend de cette graine. */
    private val graine = MutableStateFlow(0)
    private val observations = MutableStateFlow("")
    private val _saisies = MutableStateFlow<Map<Int, SaisieQuestion>>(emptyMap())

    /**
     * Ce que l'examinateur tape, par question. Exposé tel quel (sans passer par le `combine` de [etat]) : le champ
     * de texte doit afficher la frappe immédiatement, sinon les lettres se mélangent (même duo `_uiState`/`uiState`
     * que le cours). La base est mise à jour derrière, à chaque frappe.
     */
    val saisies: StateFlow<Map<Int, SaisieQuestion>> = _saisies

    override fun definirSession(session: SessionUtilisateur) {
        if (this.session.value != session) this.session.value = session
    }

    // ----- Ouverture -----

    /** Charge la tentative ; crée l'évaluation (et le sujet en mode TIRAGE) si elle n'existe pas encore. */
    fun afficher(tentativeId: Int) {
        if (idTentative.value == tentativeId) return
        idTentative.value = tentativeId
        erreur.value = null
        _saisies.value = emptyMap()
        viewModelScope.launch {
            val tentative = db.tentativeDao().parId(tentativeId) ?: return@launch
            val inscription = db.inscriptionDao().parId(tentative.inscriptionId)
            val candidat = db.candidatDao().parId(tentative.candidatId)
            val epreuve = db.typeEpreuveDao().parId(tentative.typeEpreuveId)
            val categorieId = db.sessionDao().parId(inscription?.sessionId ?: 0)?.categorieId
            val nomVisible = session.value?.role != Role.EXAMINATEUR
            entete.value = "Passage n° ${tentative.numero} — candidat n° ${inscription?.numeroAnonymat ?: "?"}" +
                if (nomVisible && candidat != null) " (${candidat.nom} ${candidat.prenom})" else ""
            mode.value = db.regleTexte(ClesRegles.MODE_THEORIE, categorieId, defaut = ModesTheorie.TIRAGE).uppercase()
            dureeMinutes.value = epreuve?.dureeMinutes ?: db.regleEntier(ClesRegles.DUREE_THEORIE_MIN, categorieId)
            val baremeCourant = db.baremeDao().courant(tentative.typeEpreuveId)

            val existante = db.evaluationDao().parTentative(tentativeId)
            if (existante != null) {
                bareme.value = db.baremeDao().parId(existante.baremeId) // la version figée à l'ouverture, pas forcément la courante
                observations.value = existante.observations ?: ""
                _saisies.value = db.reponseCandidatDao().listePourEvaluation(existante.id)
                    .associate { it.questionId to SaisieQuestion(it.reponseDonnee ?: "", it.pointsAttribues?.let(::formatPoints) ?: "") }
                return@launch
            }
            bareme.value = baremeCourant
            val utilisateur = session.value
            if (tentative.statut != StatutTentative.EN_COURS || !utilisateur.peutEvaluer() || utilisateur == null) return@launch
            val questionsActives = db.questionDao().activesPourEpreuve(tentative.typeEpreuveId)
            val refus = ReglesTheorie.verifierOuverture(baremeCourant, questionsActives, mode.value)
            if (refus != null || baremeCourant == null) return@launch run { erreur.value = refus }
            val sujet = if (mode.value == ModesTheorie.TIRAGE) ReglesTheorie.tirerSujet(questionsActives, baremeCourant.noteMax) else emptyList()
            db.withTransaction {
                val evaluation = Evaluation(tentativeId = tentativeId, baremeId = baremeCourant.id, dateSaisie = maintenantIso())
                val id = db.evaluationDao().inserer(evaluation).toInt()
                db.reponseCandidatDao().insererToutes(sujet.map { ReponseCandidat(evaluationId = id, questionId = it.id) })
                db.tracer(
                    EntitesHistorique.EVALUATION, id, ActionsHistorique.CREATION, utilisateur.id,
                    nouvelleValeur = evaluation.copy(id = id).resume() + ", mode ${mode.value}" + if (sujet.isNotEmpty()) ", sujet de ${sujet.size} questions tiré au sort" else "",
                )
            }
        }
    }

    // ----- État de l'écran -----

    private data class Locaux(val session: SessionUtilisateur?, val erreur: String?, val modeDirect: Boolean, val bareme: Bareme?, val duree: Int)
    private data class Saisie(val saisies: Map<Int, SaisieQuestion>, val observations: String, val entete: String, val pointsVoulus: Double?, val graine: Int)

    val etat: StateFlow<EtatTheorie> =
        idTentative.flatMapLatest { id ->
            combine(db.tentativeDao().parIdEnDirect(id), db.evaluationDao().parTentativeEnDirect(id)) { t, e -> t to e }
                .flatMapLatest { (tentative, evaluation) ->
                    if (tentative == null) return@flatMapLatest flowOf(EtatTheorie())
                    combine(
                        db.questionDao().parEpreuve(tentative.typeEpreuveId),
                        db.reponseCandidatDao().parEvaluationDansLOrdre(evaluation?.id ?: 0),
                        combine(session, erreur, mode, bareme, dureeMinutes) { s, e, m, b, d -> Locaux(s, e, m == ModesTheorie.DIRECT, b, d) },
                        combine(_saisies, observations, entete, pointsVoulus, graine) { sa, o, en, p, g -> Saisie(sa, o, en, p, g) },
                    ) { questions, lignes, locaux, saisie ->
                        val posees = lignes.mapNotNull { l ->
                            questions.find { it.id == l.questionId }?.let { q ->
                                QuestionPosee(q, saisie.saisies[q.id] ?: SaisieQuestion(l.reponseDonnee ?: "", l.pointsAttribues?.let(::formatPoints) ?: ""))
                            }
                        }
                        val noteMax = locaux.bareme?.noteMax ?: 0.0
                        val pointsPoses = ReglesTheorie.pointsPoses(posees.map { it.question })
                        val enCours = tentative.statut == StatutTentative.EN_COURS && locaux.session.peutEvaluer() && evaluation != null
                        val actives = questions.filter { it.actif }
                        val idsPosees = posees.map { it.question.id }.toSet()
                        EtatTheorie(
                            tentative = tentative,
                            entete = saisie.entete,
                            modeDirect = locaux.modeDirect,
                            noteMax = noteMax,
                            dureeMinutes = locaux.duree,
                            questions = posees,
                            pointsPoses = pointsPoses,
                            totalAttribue = ReglesTheorie.totalAttribue(posees.map { it.note }),
                            proposition = if (enCours && locaux.modeDirect) ReglesTheorie.proposer(actives, idsPosees, noteMax - pointsPoses, saisie.pointsVoulus, Random(saisie.graine)) else null,
                            pointsDisponibles = if (locaux.modeDirect) ReglesTheorie.pointsDisponibles(actives, idsPosees, noteMax - pointsPoses) else emptyList(),
                            pointsVoulus = saisie.pointsVoulus,
                            observations = saisie.observations,
                            enCours = enCours,
                            erreur = locaux.erreur,
                        )
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatTheorie())

    // ----- Saisie -----

    /** Observations générales, exposées directement pour la même raison que [saisies]. */
    val observationsSaisies: StateFlow<String> = observations
    fun changerObservations(texte: String) { observations.value = texte }

    /** Mode direct : filtre des propositions par nombre de points (null = tous). */
    fun choisirPoints(points: Double?) {
        pointsVoulus.value = points
        graine.update { it + 1 }
    }

    /** Mode direct : une autre question au hasard. */
    fun autreProposition() = graine.update { it + 1 }

    /** Mode direct : pose la question proposée au candidat (nouvelle ligne sur la feuille). */
    fun poserQuestion(question: Question) {
        val e = etat.value
        if (!e.enCours || !e.modeDirect || question.points > e.pointsRestants) return
        viewModelScope.launch {
            val evaluation = db.evaluationDao().parTentative(idTentative.value) ?: return@launch
            if (db.reponseCandidatDao().pourQuestion(evaluation.id, question.id) != null) return@launch
            db.reponseCandidatDao().inserer(ReponseCandidat(evaluationId = evaluation.id, questionId = question.id))
            graine.update { it + 1 }
        }
    }

    /** L'examinateur écrit la réponse donnée par le candidat, ou les points qu'il lui attribue ; la base suit. */
    fun saisir(questionId: Int, transformation: (SaisieQuestion) -> SaisieQuestion) {
        val e = etat.value
        if (!e.enCours) return
        val actuelle = _saisies.value[questionId] ?: e.questions.find { it.question.id == questionId }?.saisie ?: SaisieQuestion()
        val nouvelle = transformation(actuelle)
        _saisies.update { it + (questionId to nouvelle) }
        erreur.value = null
        viewModelScope.launch { enregistrerLigne(questionId, nouvelle) }
    }

    private suspend fun enregistrerLigne(questionId: Int, saisie: SaisieQuestion) {
        val evaluation = db.evaluationDao().parTentative(idTentative.value) ?: return
        val ligne = db.reponseCandidatDao().pourQuestion(evaluation.id, questionId) ?: return
        val question = db.questionDao().parId(questionId) ?: return
        db.reponseCandidatDao().modifier(
            ligne.copy(reponseDonnee = saisie.reponse.trim().ifBlank { null }, pointsAttribues = pointsValides(saisie.points, question.points)),
        )
    }

    /**
     * Termine l'épreuve : chaque question posée doit avoir ses points ; l'évaluation est figée (observations),
     * la tentative passe TERMINEE, la présence TERMINE, le tout tracé dans la même transaction.
     */
    fun terminer(onTerminee: () -> Unit) {
        val e = etat.value
        val utilisateur = session.value ?: return
        if (!e.enCours) return
        val refus = ReglesTheorie.verifierFin(e.questions.map { it.note })
        if (refus != null) return run { erreur.value = refus }
        viewModelScope.launch {
            val tentative = db.tentativeDao().parId(idTentative.value) ?: return@launch
            val evaluation = db.evaluationDao().parTentative(tentative.id) ?: return@launch
            val presence = db.presenceDao().parInscription(tentative.inscriptionId)
            db.withTransaction {
                for (q in e.questions) enregistrerLigne(q.question.id, q.saisie) // la feuille complète, au cas où une écriture serait en retard
                val figee = evaluation.copy(dateSaisie = maintenantIso(), observations = e.observations.trim().ifBlank { null })
                db.evaluationDao().modifier(figee)
                db.tracer(
                    EntitesHistorique.EVALUATION, figee.id, ActionsHistorique.VALIDATION, utilisateur.id,
                    nouvelleValeur = figee.resume() + ", ${e.questions.size} questions posées, ${formatPoints(e.totalAttribue)} points attribués sur ${formatPoints(e.pointsPoses)} posés",
                )
                // Ouverte par l'ATT sans examinateur ? C'est celui qui termine la saisie qui est enregistré (décision C8 n° 1).
                val terminee = tentative.copy(statut = StatutTentative.TERMINEE, examinateurId = tentative.examinateurId ?: utilisateur.examinateurId)
                db.tentativeDao().modifier(terminee)
                db.tracer(EntitesHistorique.TENTATIVE, tentative.id, ActionsHistorique.MODIFICATION, utilisateur.id, ancienneValeur = tentative.resume(), nouvelleValeur = terminee.resume())
                if (presence != null) {
                    val modifiee = presence.copy(statut = StatutPresence.TERMINE)
                    db.presenceDao().modifier(modifiee)
                    db.tracer(EntitesHistorique.PRESENCE, presence.id, ActionsHistorique.MODIFICATION, utilisateur.id, ancienneValeur = presence.resume(), nouvelleValeur = modifiee.resume())
                }
                // Le calcul s'enchaîne à la clôture (UC10) : le résultat est CALCULE, l'ATT le valide ensuite.
                db.enregistrerResultat(tentative.id, utilisateur.id)
            }
            erreur.value = null
            onTerminee()
        }
    }
}
