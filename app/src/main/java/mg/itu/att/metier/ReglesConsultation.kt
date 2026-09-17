package mg.itu.att.metier

import mg.itu.att.data.Role

/**
 * Consultation par rôle (UC12, matrice docs/01 §6) : qui peut voir la fiche d'un candidat,
 * qui peut agir sur ses dossiers et ses comptes. Fonctions pures, sans Android.
 */
object ReglesConsultation {

    /**
     * Vrai si l'utilisateur connecté peut voir la fiche complète d'un candidat :
     * le Super Admin voit tout ; l'Admin ATT régional sa région ; l'auto-école ses candidats ;
     * le candidat lui-même ; l'examinateur ne consulte pas les fiches (il voit des numéros d'appel).
     */
    fun peutVoirCandidat(
        role: Role,
        regionIdSession: Int?,
        autoEcoleIdSession: Int?,
        candidatIdSession: Int?,
        candidatId: Int,
        autoEcoleIdCandidat: Int,
        regionIdAutoEcole: Int?,
    ): Boolean = when (role) {
        Role.SUPER_ADMIN -> true
        Role.ADMIN_ATT -> regionIdSession == null || regionIdSession == regionIdAutoEcole
        Role.AUTO_ECOLE -> autoEcoleIdSession == autoEcoleIdCandidat
        Role.CANDIDAT -> candidatIdSession == candidatId
        Role.EXAMINATEUR -> false
    }

    /** L'ATT et l'auto-école gèrent la fiche et les dossiers ; le candidat ne fait que lire (docs/01 §6). */
    fun peutGererCandidat(role: Role): Boolean =
        role == Role.SUPER_ADMIN || role == Role.ADMIN_ATT || role == Role.AUTO_ECOLE

    /** Le compte de connexion d'un candidat (facultatif, cadrage §10) est créé par l'ATT ou par son auto-école. */
    fun peutCreerCompteCandidat(role: Role): Boolean = peutGererCandidat(role)

    /** Une inscription est « à venir » tant que la date de sa session n'est pas passée. */
    fun estAVenir(dateSession: String, aujourdHui: String): Boolean = dateSession >= aujourdHui

    /** Libellé d'une session dans un parcours : « 17/09/2026 à 08:00 — permis B, Épreuve théorique ». */
    fun libelleSession(date: String, heureConvocation: String, codeCategorie: String, libelleEpreuve: String): String =
        "${formatDate(date)} à $heureConvocation — permis $codeCategorie, $libelleEpreuve"
}
