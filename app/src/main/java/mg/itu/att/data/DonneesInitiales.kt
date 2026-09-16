package mg.itu.att.data

import mg.itu.att.securite.MotDePasse

/**
 * Jeu de données inséré au premier lancement, quand la base est vide
 * (même geste que `produitsInitiaux` dans listedetailv3).
 *
 * Contenu : les 24 régions de Madagascar, les comptes de démarrage, et des VALEURS D'EXEMPLE
 * pour les catégories, épreuves, barèmes et règles, toutes marquées `aConfirmer = true`.
 * Aucune d'elles n'est une règle officielle de l'ATT (docs/04_QUESTIONS_A_VALIDER.md).
 * Aucun candidat, aucune session, aucun résultat, aucune question, aucun critère de conduite.
 */
object DonneesInitiales {

    /** Identifiants de départ : à changer dès la première connexion. */
    const val IDENTIFIANT_SUPER_ADMIN = "superadmin"
    const val MOT_DE_PASSE_INITIAL_SUPER_ADMIN = "ChangezMoi2026"
    const val IDENTIFIANT_ADMIN_DEMO = "admin"
    const val MOT_DE_PASSE_INITIAL_ADMIN_DEMO = "admin2026"

    // ---------- RÉGIONS (24, loi 2004-001 + créations 2021 et 2023) ----------

    val regions = listOf(
        Region(code = "ANA", nom = "Analamanga"),
        Region(code = "BON", nom = "Bongolava"),
        Region(code = "ITA", nom = "Itasy"),
        Region(code = "VAK", nom = "Vakinankaratra"),
        Region(code = "DIA", nom = "Diana"),
        Region(code = "SAV", nom = "Sava"),
        Region(code = "AMM", nom = "Amoron'i Mania"),
        Region(code = "ATA", nom = "Atsimo-Atsinanana"),
        Region(code = "FIT", nom = "Fitovinany"),
        Region(code = "HMA", nom = "Haute Matsiatra"),
        Region(code = "IHO", nom = "Ihorombe"),
        Region(code = "VAT", nom = "Vatovavy"),
        Region(code = "BET", nom = "Betsiboka"),
        Region(code = "BOE", nom = "Boeny"),
        Region(code = "MEL", nom = "Melaky"),
        Region(code = "SOF", nom = "Sofia"),
        Region(code = "ALM", nom = "Alaotra-Mangoro"),
        Region(code = "AMB", nom = "Ambatosoa"),
        Region(code = "ANJ", nom = "Analanjirofo"),
        Region(code = "ATS", nom = "Atsinanana"),
        Region(code = "AND", nom = "Androy"),
        Region(code = "ANO", nom = "Anosy"),
        Region(code = "AAN", nom = "Atsimo-Andrefana"),
        Region(code = "MEN", nom = "Menabe"),
    )

    // ---------- CATÉGORIES (A', A, B, C, D, E confirmées ; âges = presse, à confirmer, Q10) ----------

    private val categories = listOf(
        CategoriePermis(code = "A'", libelle = "Motocyclettes de moins de 125 cm³", ageMinimum = 16),
        CategoriePermis(code = "A", libelle = "Motocyclettes de plus de 125 cm³", ageMinimum = 16),
        CategoriePermis(code = "B", libelle = "Véhicules légers (jusqu'à 9 places)", ageMinimum = 18),
        CategoriePermis(code = "C", libelle = "Poids lourds (plus de 3,5 t)", ageMinimum = 21, categoriePrealableCode = "B"),
        CategoriePermis(code = "D", libelle = "Transport en commun", ageMinimum = 21, categoriePrealableCode = "B"),
        CategoriePermis(code = "E", libelle = "Remorques et semi-remorques", ageMinimum = 21, categoriePrealableCode = "B"),
    )

    // ---------- RÈGLES (valeurs par défaut de docs/04, toutes à confirmer) ----------

    private val regles = listOf(
        RegleConfig(cle = ClesRegles.TENTATIVES_MAX, valeur = "0", typeValeur = TypeValeur.ENTIER,
            description = "Nombre maximal de tentatives par épreuve (0 = illimité) — Q3"),
        RegleConfig(cle = ClesRegles.DELAI_REPASSAGE_JOURS, valeur = "25", typeValeur = TypeValeur.ENTIER,
            description = "Délai minimal en jours entre deux tentatives — Q3"),
        RegleConfig(cle = ClesRegles.CONSERVATION_EPREUVE_REUSSIE_JOURS, valeur = "365", typeValeur = TypeValeur.ENTIER,
            description = "Durée en jours pendant laquelle une épreuve réussie reste acquise — Q3"),
        RegleConfig(cle = ClesRegles.TOLERANCE_RETARD_MIN, valeur = "15", typeValeur = TypeValeur.ENTIER,
            description = "Minutes de retard tolérées après l'heure de convocation — Q4"),
        RegleConfig(cle = ClesRegles.REGLE_ABSENCE, valeur = "NOUVELLE_INSCRIPTION", typeValeur = TypeValeur.TEXTE,
            description = "Sort d'un absent : REPORT_AUTO ou NOUVELLE_INSCRIPTION — Q4"),
        RegleConfig(cle = ClesRegles.DUREE_CRENEAU_MIN, valeur = "15", typeValeur = TypeValeur.ENTIER,
            description = "Durée par défaut d'un créneau de passage, en minutes — Q5"),
        RegleConfig(cle = ClesRegles.MARGE_CRENEAU_MIN, valeur = "5", typeValeur = TypeValeur.ENTIER,
            description = "Marge par défaut entre deux créneaux, en minutes — Q5"),
        RegleConfig(cle = ClesRegles.CAPACITE_SESSION_DEFAUT, valeur = "30", typeValeur = TypeValeur.ENTIER,
            description = "Capacité par défaut d'une session — Q5"),
        RegleConfig(cle = ClesRegles.CAPACITE_CRENEAU_DEFAUT, valeur = "5", typeValeur = TypeValeur.ENTIER,
            description = "Capacité par défaut d'un créneau (1 = créneau individuel) — Q5"),
        RegleConfig(cle = ClesRegles.EXAMEN_DANS_REGION_AUTO_ECOLE, valeur = "false", typeValeur = TypeValeur.BOOLEEN,
            description = "Le candidat doit passer dans la région de son auto-école — Q11"),
        RegleConfig(cle = ClesRegles.CONDUITE_APRES_THEORIE_REUSSIE, valeur = "true", typeValeur = TypeValeur.BOOLEEN,
            description = "La conduite exige la théorie réussie — Q9"),
        RegleConfig(cle = ClesRegles.AUTO_ECOLE_PEUT_INSCRIRE, valeur = "false", typeValeur = TypeValeur.BOOLEEN,
            description = "L'auto-école peut demander une inscription à une session — Q8"),
        RegleConfig(cle = ClesRegles.DUREE_THEORIE_MIN, valeur = "30", typeValeur = TypeValeur.ENTIER,
            description = "Durée de l'épreuve théorique, en minutes — Q1"),
        RegleConfig(cle = ClesRegles.MODE_THEORIE, valeur = "TIRAGE", typeValeur = TypeValeur.TEXTE,
            description = "Choix des questions : TIRAGE (sujet tiré au sort par l'application) ou DIRECT (l'examinateur choisit pendant l'épreuve) — Q1 bis"),
        // Pièces du dossier : liste du portail officiel Torolalana pour A, A', B (Q7) ; les catégories C, D, E ont leur surcharge.
        RegleConfig(cle = ClesRegles.PIECES_DOSSIER, typeValeur = TypeValeur.TEXTE,
            valeur = listOf(
                "Copie certifiée de la CIN ou carte scolaire", "Acte de naissance", "Certificat de résidence", "5 photos d'identité",
            ).joinToString(ClesRegles.SEPARATEUR_PIECES),
            description = "Pièces attendues dans un dossier (séparées par « ; ») — Q7"),
    )

    /** Pièces supplémentaires pour les catégories qui exigent déjà le permis B (Torolalana). */
    private val piecesCategoriesLourdes = listOf(
        "Copie certifiée de la CIN", "Acte de naissance", "Certificat de résidence", "5 photos d'identité",
        "Certificat médical (BMH)", "Copie certifiée du permis B",
    ).joinToString(ClesRegles.SEPARATEUR_PIECES)

    // ---------- INSERTION ----------

    /**
     * Insère le jeu de données si la base est vide. Appelé au démarrage dans un `viewModelScope.launch`.
     * @return true si l'insertion a eu lieu.
     */
    suspend fun insererSiVide(db: AppDatabase): Boolean {
        if (db.regionDao().nombre() > 0) return false

        db.regionDao().insererToutes(regions)
        db.regleConfigDao().insererToutes(regles)

        // Catégories, puis pour chacune : théorie (ordre 1) et conduite (ordre 2), avec un barème d'exemple.
        for (categorie in categories) {
            val categorieId = db.categoriePermisDao().inserer(categorie).toInt()
            val theorieId = db.typeEpreuveDao().inserer(
                TypeEpreuve(categorieId = categorieId, code = "THEORIE", libelle = "Épreuve théorique", ordre = 1, dureeMinutes = 30),
            ).toInt()
            val conduiteId = db.typeEpreuveDao().inserer(
                TypeEpreuve(categorieId = categorieId, code = "CONDUITE", libelle = "Épreuve de conduite", ordre = 2, dureeMinutes = null),
            ).toInt()
            db.baremeDao().inserer(
                Bareme(typeEpreuveId = theorieId, version = 1, noteMax = 30.0, seuilReussite = 20.0, dateDebutValidite = DATE_INITIALE),
            )
            db.baremeDao().inserer(
                Bareme(typeEpreuveId = conduiteId, version = 1, noteMax = 20.0, seuilReussite = 12.0, dateDebutValidite = DATE_INITIALE),
            )
            if (categorie.categoriePrealableCode != null) {
                db.regleConfigDao().insererToutes(
                    listOf(
                        RegleConfig(
                            cle = ClesRegles.PIECES_DOSSIER, valeur = piecesCategoriesLourdes, typeValeur = TypeValeur.TEXTE,
                            categorieId = categorieId, description = "Pièces attendues pour la catégorie ${categorie.code} — Q7",
                        ),
                    ),
                )
            }
        }

        // Comptes de départ. Les empreintes sont calculées ici, jamais stockées en clair.
        db.utilisateurDao().inserer(
            Utilisateur(
                identifiant = IDENTIFIANT_SUPER_ADMIN,
                motDePasseHash = MotDePasse.hacher(MOT_DE_PASSE_INITIAL_SUPER_ADMIN),
                nom = "Super administrateur",
                role = Role.SUPER_ADMIN,
            ),
        )
        db.utilisateurDao().inserer(
            Utilisateur(
                identifiant = IDENTIFIANT_ADMIN_DEMO,
                motDePasseHash = MotDePasse.hacher(MOT_DE_PASSE_INITIAL_ADMIN_DEMO),
                nom = "Administrateur ATT (démonstration)",
                role = Role.ADMIN_ATT,
            ),
        )
        return true
    }

    private const val DATE_INITIALE = "2026-09-15"
}
