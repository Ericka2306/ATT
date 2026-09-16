package mg.itu.att.data

import mg.itu.att.metier.maintenantIso

/**
 * Traçabilité (étape B2, instructions §7 : « tracer les modifications sensibles »).
 *
 * Chaque écriture sensible appelle [tracer] DANS la même transaction que la modification
 * (`db.withTransaction { … }`, le BEGIN/COMMIT de SQL — docs/HORS_COURS.md n° 4) :
 * soit la modification et sa trace sont enregistrées ensemble, soit aucune des deux.
 */

/** Actions possibles d'une ligne d'historique. */
object ActionsHistorique {
    const val CREATION = "CREATION"
    const val MODIFICATION = "MODIFICATION"
    const val DESACTIVATION = "DESACTIVATION"
    const val REACTIVATION = "REACTIVATION"
    const val VALIDATION = "VALIDATION"
    const val REFUS = "REFUS"
    const val CORRECTION = "CORRECTION"
    const val ANNULATION = "ANNULATION"
    const val CREATION_COMPTE = "CREATION_COMPTE"
}

/** Noms d'entités utilisés dans l'historique (toujours les mêmes chaînes, pour pouvoir filtrer). */
object EntitesHistorique {
    const val AUTO_ECOLE = "AutoEcole"
    const val UTILISATEUR = "Utilisateur"
    const val EXAMINATEUR = "Examinateur"
    const val CANDIDAT = "Candidat"
    const val DOSSIER = "Dossier"
    const val SESSION = "Session"
    const val INSCRIPTION = "Inscription"
    const val PRESENCE = "Presence"
    const val RESULTAT = "Resultat"
    const val REGLE_CONFIG = "RegleConfig"
    const val CATEGORIE = "CategoriePermis"
    const val TYPE_EPREUVE = "TypeEpreuve"
    const val BAREME = "Bareme"
    const val QUESTION = "Question"
    const val CRITERE = "CriterePratique"
    const val CENTRE = "Centre"
}

/** Ajoute une ligne d'historique. À appeler à l'intérieur d'un `withTransaction`. */
suspend fun AppDatabase.tracer(
    entite: String,
    entiteId: Int,
    action: String,
    utilisateurId: Int,
    ancienneValeur: String? = null,
    nouvelleValeur: String? = null,
    motif: String? = null,
) {
    historiqueDao().inserer(
        Historique(
            entite = entite,
            entiteId = entiteId,
            action = action,
            ancienneValeur = ancienneValeur,
            nouvelleValeur = nouvelleValeur,
            utilisateurId = utilisateurId,
            dateHeure = maintenantIso(),
            motif = motif,
        ),
    )
}

/** Résumés texte pour l'historique (jamais l'objet complet). */
fun AutoEcole.resume(): String = "$nom (région $regionId, agrément ${numeroAgrement ?: "—"}, ${if (actif) "active" else "inactive"})"

fun Candidat.resume(): String = "$nom $prenom, né(e) le $dateNaissance, auto-école $autoEcoleId${cin?.let { ", CIN $it" } ?: ""}"

fun Dossier.resume(): String = "dossier n° $id, catégorie $categorieId, statut $statut${motif?.let { ", motif : $it" } ?: ""}"

fun Examinateur.resume(): String = "$nom (matricule ${matricule ?: "—"}, région ${regionId ?: "—"}, ${if (actif) "actif" else "inactif"})"

fun Utilisateur.resume(): String = "compte $identifiant ($role, région ${regionId ?: "nationale"}, ${if (actif) "actif" else "désactivé"})"

fun CategoriePermis.resume(): String = "$code — $libelle (âge min ${ageMinimum ?: "—"}, préalable ${categoriePrealableCode ?: "—"}, ${if (actif) "active" else "inactive"}${if (aConfirmer) ", à confirmer" else ""})"

fun TypeEpreuve.resume(): String = "$code — $libelle (ordre $ordre, durée ${dureeMinutes ?: "—"} min, ${if (actif) "active" else "inactive"})"

fun Bareme.resume(): String = "version $version : note max $noteMax, seuil $seuilReussite, du $dateDebutValidite${dateFinValidite?.let { " au $it" } ?: ""}"

fun RegleConfig.resume(): String = "$cle = $valeur${categorieId?.let { " (catégorie $it)" } ?: ""}${if (aConfirmer) ", à confirmer" else ""}"

fun Question.resume(): String = "« ${enonce.take(60)} » ($points pt, ${if (actif) "active" else "inactive"})"

fun CriterePratique.resume(): String = "$libelle ($points pt${if (eliminatoire) ", éliminatoire" else ""}, ${if (actif) "actif" else "inactif"})"

fun Centre.resume(): String = "$nom (région $regionId, $adresse, capacité ${capaciteParDefaut ?: "—"}, ${if (actif) "actif" else "inactif"})"
