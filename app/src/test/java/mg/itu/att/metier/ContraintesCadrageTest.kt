package mg.itu.att.metier

import mg.itu.att.data.Bareme
import mg.itu.att.data.CategoriePermis
import mg.itu.att.data.Inscription
import mg.itu.att.data.Session
import mg.itu.att.data.StatutDossier
import mg.itu.att.data.StatutInscription
import mg.itu.att.data.StatutPresence
import mg.itu.att.data.StatutResultat
import mg.itu.att.data.StatutSession
import mg.itu.att.data.StatutTentative
import mg.itu.att.data.Tentative
import mg.itu.att.metier.CalculResultat.LigneConduite
import mg.itu.att.metier.ReglesInscription.ContexteInscription
import mg.itu.att.metier.ReglesRepassage.EtatEpreuve
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Étape D1 : un test par contrainte du cahier de cadrage §11, pour prouver que la règle est bien tenue
 * par le code métier. Les contraintes qui ne relèvent pas d'un calcul (candidat sans smartphone,
 * panne réseau, examinateur indisponible) sont vérifiées par scénario sur émulateur, voir
 * le tableau de `docs/COMPTES_RENDUS.md` → « Étape D1 ».
 */
class ContraintesCadrageTest {

    private val bareme = Bareme(id = 1, typeEpreuveId = 1, version = 1, noteMax = 20.0, seuilReussite = 12.0, dateDebutValidite = "2026-01-01")

    private val session = Session(
        id = 1, categorieId = 1, typeEpreuveId = 1, centreId = 1, date = "2026-09-20",
        heureConvocation = "08:00", capacite = 2, dureeCreneauMin = 15, margeMin = 5,
        statut = StatutSession.OUVERTE, creeParId = 1,
    )

    private fun inscription(id: Int, sessionId: Int = 1, statut: StatutInscription = StatutInscription.INSCRIT) =
        Inscription(id = id, candidatId = id, dossierId = id, sessionId = sessionId, statut = statut, dateInscription = "2026-09-18", numeroAnonymat = "%03d".format(id))

    private fun contexte(
        inscriptionsSession: List<Inscription> = emptyList(),
        inscriptionsCandidat: List<Pair<Inscription, Session>> = emptyList(),
        dossierValide: Boolean = true,
        derniereTentative: String? = null,
        delai: Int = 0,
        nombreTentatives: Int = 0,
        tentativesMax: Int = 0,
        theorieRequise: Boolean = false,
        theorieReussie: Boolean = false,
    ) = ContexteInscription(
        session = session, inscriptionsSession = inscriptionsSession, inscriptionsCandidat = inscriptionsCandidat,
        dossierValide = dossierValide, regionAutoEcole = 1, regionCentre = 1, examenDansRegion = false,
        derniereTentative = derniereTentative, delaiRepassageJours = delai,
        nombreTentatives = nombreTentatives, tentativesMax = tentativesMax,
        theorieRequise = theorieRequise, theorieReussie = theorieReussie,
    )

    // §11 — « Dossier incomplet / refusé »

    @Test
    fun `contrainte - un dossier incomplet ou refuse peut etre repris, un dossier valide ne se rouvre pas`() {
        // Un dossier refusé ou incomplet n'empêche pas d'en ouvrir un nouveau pour la même catégorie…
        assertNull(ReglesDossier.peutOuvrirDossier(listOf(dossier(StatutDossier.REFUSE)), categorieId = 1))
        // …mais un dossier déjà validé, si.
        assertNotNull(ReglesDossier.peutOuvrirDossier(listOf(dossier(StatutDossier.VALIDE)), categorieId = 1))
    }

    private fun dossier(statut: StatutDossier) = mg.itu.att.data.Dossier(id = 1, candidatId = 1, categorieId = 1, statut = statut)

    // §11 — « Candidat non éligible »

    @Test
    fun `contrainte - candidat non eligible par l age ou sans dossier valide`() {
        val categorie = CategoriePermis(id = 1, code = "B", libelle = "Véhicules légers", ageMinimum = 18)
        assertNotNull("un candidat de 17 ans ne passe pas le B", ReglesDossier.verifierEligibilite("2009-09-19", categorie, "2026-09-18"))
        assertNull("à 18 ans révolus, il passe", ReglesDossier.verifierEligibilite("2008-09-17", categorie, "2026-09-18"))
        assertNotNull("sans dossier validé, pas d'inscription", ReglesInscription.verifier(contexte(dossierValide = false)))
    }

    // §11 — « Session complète »

    @Test
    fun `contrainte - session complete refusee`() {
        val pleine = listOf(inscription(1), inscription(2))
        val refus = ReglesInscription.verifier(contexte(inscriptionsSession = pleine))
        assertNotNull(refus)
        assertTrue(refus!!.contains("complète"))
        // Les inscriptions annulées libèrent la place.
        assertNull(ReglesInscription.verifier(contexte(inscriptionsSession = listOf(inscription(1), inscription(2, statut = StatutInscription.ANNULE)))))
    }

    // §11 — « Absence, retard, report, annulation »

    @Test
    fun `contrainte - retard dans la tolerance, absence selon la regle configuree`() {
        assertEquals(StatutPresence.PRESENT, ReglesPresence.statutArrivee("08:00", "08:10", toleranceMin = 15))
        assertEquals(StatutPresence.EN_RETARD, ReglesPresence.statutArrivee("08:00", "08:20", toleranceMin = 15))
        assertTrue(ReglesPresence.absentReporteAutomatiquement("REPORT_AUTO"))
        assertFalse(ReglesPresence.absentReporteAutomatiquement("NOUVELLE_INSCRIPTION"))
        // Un report ou une annulation libère la place et n'est plus une inscription active.
        assertEquals(1, ReglesInscription.actives(listOf(inscription(1), inscription(2, statut = StatutInscription.REPORTE))).size)
    }

    // §11 — « Erreur de notation, correction traçable »

    @Test
    fun `contrainte - une correction exige un motif et ne touche pas au resultat d origine`() {
        assertNotNull("pas de correction sans motif", CalculResultat.verifierCorrection("", StatutResultat.VALIDE_ATT))
        assertNull(CalculResultat.verifierCorrection("erreur de report", StatutResultat.VALIDE_ATT))
        // Un résultat déjà remplacé ne se corrige pas une seconde fois : c'est la nouvelle ligne qui vit.
        assertNotNull(CalculResultat.verifierCorrection("erreur de report", StatutResultat.ANNULE))
    }

    // §11 — « Échec et nouvelle tentative »

    @Test
    fun `contrainte - apres un echec, nouveau numero de passage et delai de repassage`() {
        val passees = listOf(
            Tentative(id = 1, candidatId = 1, inscriptionId = 1, typeEpreuveId = 1, numero = 1, statut = StatutTentative.TERMINEE, dateHeure = "2026-08-01T08:00"),
        )
        assertEquals("le numéro n'est jamais réutilisé", 2, ReglesTentatives.numeroSuivant(passees))

        // Le délai de repassage bloque une inscription trop proche du dernier passage…
        val tropTot = ReglesInscription.verifier(contexte(derniereTentative = "2026-09-15", delai = 25))
        assertNotNull(tropTot)
        assertTrue(tropTot!!.contains("Délai de repassage"))
        // …et le laisse passer une fois écoulé.
        assertNull(ReglesInscription.verifier(contexte(derniereTentative = "2026-07-01", delai = 25)))

        // L'épreuve échouée est bien listée comme à repasser, avec sa date de réinscription.
        val aRepasser = ReglesRepassage.epreuvesARepasser(
            listOf(EtatEpreuve(1, "Épreuve théorique", dateReussite = null, dateDernierPassage = "2026-09-18")),
            conservationJours = 365, delaiRepassageJours = 25, aujourdHui = "2026-09-18",
        )
        assertEquals(1, aRepasser.size)
        assertEquals("2026-10-13", aRepasser.single().inscriptibleLe)
    }

    // §11 — « Examinateur indisponible »

    @Test
    fun `contrainte - aucune affectation examinateur n est exigee pour ouvrir un passage`() {
        // Le seul prérequis est la présence du candidat : n'importe quel examinateur connecté peut saisir (R5).
        assertNull(ReglesTentatives.peutOuvrir(StatutPresence.PRESENT, tentativeDeCetteInscription = null, nombreTentatives = 0, tentativesMax = 0))
        assertNotNull(ReglesTentatives.peutOuvrir(StatutPresence.ABSENT, tentativeDeCetteInscription = null, nombreTentatives = 0, tentativesMax = 0))
    }

    // §11 — « Conflit de créneaux »

    @Test
    fun `contrainte - conflit de creneaux a la creation et a l inscription`() {
        // À la création : une autre session le même jour dans le même centre → avertissement.
        assertNotNull(ReglesPlanification.avertissementConflit(nombreSessionsMemeJour = 1))
        assertNull(ReglesPlanification.avertissementConflit(nombreSessionsMemeJour = 0))

        // À l'inscription : deux sessions le même jour pour le même candidat → refus.
        val autre = session.copy(id = 2)
        val refus = ReglesInscription.verifier(contexte(inscriptionsCandidat = listOf(inscription(9, sessionId = 2) to autre)))
        assertNotNull(refus)
        assertTrue(refus!!.contains("conflit de créneaux"))
    }

    // §11 — « Faute éliminatoire » (règle d'évaluation liée à l'erreur de notation)

    @Test
    fun `contrainte - une faute eliminatoire fait perdre l epreuve quel que soit le total`() {
        val lignes = listOf(
            LigneConduite(10.0, 10.0, critereEliminatoire = false, fauteCochee = false),
            LigneConduite(10.0, 10.0, critereEliminatoire = true, fauteCochee = true),
        )
        val calcul = CalculResultat.calculerConduite(lignes, bareme)
        assertTrue(calcul.fauteEliminatoire)
        assertFalse("même avec 10 points sur 20, l'épreuve est perdue", calcul.reussi)
    }

    // §11 — « Candidat sans smartphone » : rien dans le parcours n'exige un compte candidat.

    @Test
    fun `contrainte - le parcours reste faisable sans compte candidat`() {
        // Aucun contrôle d'inscription ne dépend d'un compte : seul le dossier validé compte.
        assertNull(ReglesInscription.verifier(contexte()))
        // Et l'ATT comme l'auto-école peuvent tout faire à la place du candidat (UC12).
        assertTrue(ReglesConsultation.peutGererCandidat(mg.itu.att.data.Role.AUTO_ECOLE))
        assertTrue(ReglesConsultation.peutGererCandidat(mg.itu.att.data.Role.ADMIN_ATT))
        assertFalse(ReglesConsultation.peutGererCandidat(mg.itu.att.data.Role.CANDIDAT))
    }

    // §11 — « Panne réseau / électricité » : la liste d'appel imprimée sert de secours.

    @Test
    fun `contrainte - la liste d appel imprimee permet de tenir la session sans application`() {
        val html = DocumentsImpression.listeAppel(
            listOf(DocumentsImpression.LigneAppel("001", "RAKOTO Jean", "08:00")),
            "B", "Épreuve théorique", "Centre ATT Soarano", "2026-09-20", "08:00",
            avecNoms = true, dateEdition = "2026-09-18",
        )
        assertTrue("colonnes à cocher pour l'appel papier", html.contains("<th>Présent</th>") && html.contains("<th>Absent</th>"))
        assertTrue(html.contains("secours en cas de panne"))
    }
}
