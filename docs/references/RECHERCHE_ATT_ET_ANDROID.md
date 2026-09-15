# Recherche documentaire — Projet « ATT » (digitalisation des examens du permis de conduire, Madagascar)

Date de la recherche : 15 septembre 2026.
Méthode : recherche web (WebSearch) + lecture des pages (WebFetch). Chaque affirmation est suivie de son URL source. Les niveaux de fiabilité utilisés sont :

- **[OFFICIEL]** : site gouvernemental malgache (torolalana.gov.mg, assemblee-nationale.mg…), documentation officielle Android/Kotlin (developer.android.com, kotlinlang.org, blogs officiels Google/JetBrains).
- **[PRESSE]** : presse malgache (L'Express de Madagascar, Newsmada, La Vérité, Moov.mg, 2424.mg, Studio Sifaka, allAfrica qui republie L'Express/Midi).
- **[SECONDAIRE]** : sites d'auto-écoles, blogs, annuaires, guides privés (terraformalis.com, fiarakodia.com, bureaudepoche.fr, vol-direct.net, Wikipédia).
- **[NON TROUVÉ]** : aucune source exploitable trouvée ; à valider auprès de l'ATT / de l'enseignant. Rien n'a été inventé.

Remarque importante : plusieurs sites officiels étaient inaccessibles au moment de la recherche (att.mg : nom de domaine non résolu ; transport.gov.mg : DNS introuvable ; mtm.gov.mg et mid.gov.mg : certificat TLS invalide ; PDF de la loi 2017-002 sur assemblee-nationale.mg et textes Lexxika : HTTP 403 ; article La Vérité : HTTP 500). Pour ces sources, seuls les extraits fournis par le moteur de recherche ont pu être utilisés, ce qui est signalé au cas par cas.

---

# PARTIE A — Règles métier du permis de conduire à Madagascar (ATT)

## A.0 Cadre juridique général

| Élément | Ce que dit la recherche | Fiabilité | Sources |
|---|---|---|---|
| Loi de base | Loi n° 2017-002 du 6 juillet 2017 portant Code de la route à Madagascar (validée par la HCC le 5 juillet 2017). Tout véhicule motorisé doit être conduit par un titulaire d'un permis de conduire ; les véhicules non motorisés relèvent d'une « autorisation de conduite ». | OFFICIEL (existence, HCC) / PRESSE (contenu) | https://www.assemblee-nationale.mg/wp-content/uploads/2020/10/Loi-n%C2%B02017-002_Code-de-la-route-%C3%A0-Madagascar.pdf (403 lors de la lecture) ; http://www.hcc.gov.mg/?p=2952 ; https://fr.allafrica.com/stories/202605260570.html |
| Renvoi aux textes réglementaires | D'après l'extrait indexé de la loi (article cité comme L3.2-2), « les différentes catégories de permis et d'autorisations de conduire, les conditions et modalités de leur délivrance, la durée de leur validité, ainsi que les infractions pouvant entraîner interdiction de délivrance, suspension, retrait temporaire ou définitif » sont fixées par textes réglementaires du ministère chargé des Transports et/ou du ministère de l'Intérieur. | PRESSE + extrait de moteur de recherche (le texte intégral n'a pas pu être lu) | https://www.lexpress.mg/2026/04/securite-routiere-le-nouveau-code-de-la.html ; https://textes.lexxika.com/lois-malagasy/loi-n2017-002-du-06-juillet-2016-portant-code-de-la-route-a-madagascar/ (403) |
| Décret d'application | Décret n° 2026-974 du 23 avril 2026 (cité par L'Express/allAfrica). L'ATT a précisé « que ces dispositions ne seraient pas appliquées dans l'immédiat », plusieurs étapes de préparation restant nécessaires. Un site privé (Lexxika) référence aussi un « Décret n° 2026-002 » d'application de la même loi ; le numéro exact n'a pas pu être vérifié (page en 403). | PRESSE | https://www.lexpress.mg/2026/05/circulation-les-deux-roues-soumis-un.html ; https://fr.allafrica.com/stories/202605260570.html ; https://textes.lexxika.com/lois-malagasy/decret-n2026-002-determinant-les-modalites-dapplication-de-la-loi-n2017-002-du-6-juillet-2017-portant-code-de-la-route-a-madagascar/ |
| Statut de l'ATT | Créée par le décret n° 2006-279 du 25 avril 2006 « portant création de l'Agence des Transports Terrestres (ATT), fixant ses statuts, son fonctionnement et ses modalités de financement » ; établissement public ; autorité déléguée de régulation du sous-secteur transports terrestres (route et rail). L'une de ses missions listées en 2010 : « amélioration de l'organisation des examens de Permis de Conduire ». | SECONDAIRE (titre du décret sur Lexxika, page en 403) + PRESSE | https://textes.lexxika.com/lois-malagasy/decret-n2006-279-du-25-avril-2006-portant-creation-de-lagence-des-transports-terrestres-att-fixant-ses-statuts-son-fonctionnement-et-ses-modalites-de-financement/ ; https://www.madagascar-tribune.com/Etat-ATT-la-repartition-des-roles,15123.html |

## A.1 Catégories de permis

| Catégorie | Définition trouvée | Fiabilité | Source |
|---|---|---|---|
| A' | Motos de moins de 125 cm³ (accordée « dès l'âge de 16 ans » selon Studio Sifaka) | PRESSE | https://www.lexpress.mg/2026/05/circulation-les-deux-roues-soumis-un.html ; https://www.studiosifaka.org/articles/actualites/item/2425-ce-qu-il-faut-savoir-sur-les-procedures-pour-l-obtention-du-permis-de-conduire.html |
| A | Motos de plus de 125 cm³ | PRESSE | https://www.lexpress.mg/2026/05/circulation-les-deux-roues-soumis-un.html |
| B | « Autorise à conduire les automobiles légères transportant jusqu'à 9 personnes » (18 ans+) | SECONDAIRE | https://terraformalis.com/permis-conduire-madagascar/ |
| C | Poids lourds > 3,5 t (21 ans+, permis B requis) — extension de B, certificat médical exigé | SECONDAIRE (définition) / OFFICIEL (C, D, E = extensions nécessitant certificat médical + copie du permis B) | https://terraformalis.com/permis-conduire-madagascar/ ; https://torolalana.gov.mg/fr/services/obtenir-son-permis-de-conduire/ |
| D | Transport en commun de personnes (21 ans+, certificat médical) | SECONDAIRE | https://terraformalis.com/permis-conduire-madagascar/ |
| E | Remorques / semi-remorques (21 ans+, permis B et C requis) | SECONDAIRE | https://terraformalis.com/permis-conduire-madagascar/ |
| F, AM | Mentionnées seulement dans des résumés de moteur de recherche (« A, B, C, D, E, F et AM ») ; aucune page consultée ne définit ces catégories. | NON TROUVÉ | — |

Ce qui est confirmé par une source officielle : l'existence des catégories A, A', B, C, D et E (le portail Torolalana distingue « A, A' et B » et « C, D et E »), sans définition. Les définitions ci-dessus viennent de la presse (A/A') et d'un guide privé (B/C/D/E). Les catégories F et AM restent **non trouvées**. Le décret 2026-974 pourrait redéfinir la nomenclature ; son texte n'a pas pu être lu.

## A.2 Déroulement de l'examen

### Épreuve théorique (« code »)

| Point | Ce que dit la recherche | Fiabilité | Source |
|---|---|---|---|
| Ordre des épreuves | « Deux examens consécutifs, l'épreuve théorique et pratique, à l'ATT » ; « la réussite de l'épreuve théorique est obligatoire avant de passer à l'autre épreuve ». | PRESSE | https://www.studiosifaka.org/articles/actualites/item/2425-ce-qu-il-faut-savoir-sur-les-procedures-pour-l-obtention-du-permis-de-conduire.html |
| Format | « 30 questions auxquelles les candidats doivent répondre dans un délai de 30 minutes », « généralement sous forme de QCM ». Même chiffre (30 questions / 30 minutes) chez fiarakodia.com. | SECONDAIRE (deux sites privés, concordants mais non officiels) | https://www.bureaudepoche.fr/tout-savoir-sur-lexamen-theorique-du-code-de-la-route-a-madagascar/ ; https://www.fiarakodia.com/en/posts/comment-obtenir-votre-permis-de-conduire-a-madagascar |
| Seuil de réussite | « Un score minimum défini par la réglementation en vigueur » — valeur **non trouvée**. | NON TROUVÉ | https://www.bureaudepoche.fr/tout-savoir-sur-lexamen-theorique-du-code-de-la-route-a-madagascar/ |
| Support | Historiquement manuel ; l'ATT a développé une « application spécialisée » couvrant « l'ensemble du processus d'examen du permis de conduire, de l'inscription à la publication des résultats », avec des « salles d'examen modernes » aménagées ; le système était « en phase de simulation technique » et devait être déployé progressivement. Le détail (tablettes, PC, papier) n'est pas précisé. | PRESSE | https://www.moov.mg/article/111378-examen-du-permis-de-conduire-une-application-en-phase-de-simulation-technique |
| Thèmes | « principes généraux de la circulation, règles applicables aux usagers et conducteurs, normes concernant les véhicules » ; manuel officiel « conforme à la loi n° 2017-002 ». | SECONDAIRE | https://www.bureaudepoche.fr/tout-savoir-sur-lexamen-theorique-du-code-de-la-route-a-madagascar/ |

### Épreuve pratique (conduite)

| Point | Ce que dit la recherche | Fiabilité | Source |
|---|---|---|---|
| Nature | « Ici l'examen se fait dans la voiture » ; « un vrai test de conduite pour vérifier si l'apprenant est apte ou pas à conduire ». | SECONDAIRE | https://www.fiarakodia.com/en/posts/passer-son-permis-a-madagascar ; https://www.vol-direct.net/comment-passer-son-code-de-la-route-a-madagascar.html |
| Contenu observé | Témoignage (2020) : « On m'a demandé un simple démarrage en côte » ; formation parfois réduite à « huit cours de conduite de quinze minutes ». | PRESSE (témoignage, pas une règle) | https://lexpress.mg/07/02/2020/auto-ecoles-les-formations-laissent-a-desirer/ |
| Grille de notation, fautes éliminatoires, manœuvres imposées (créneau, etc.) | **Non trouvé** pour Madagascar. Attention : certains résultats de recherche (stationnement à 40 cm du trottoir, « droit à deux tentatives ») proviennent d'un site **marocain** (maroctl.com) et ne doivent pas être utilisés. | NON TROUVÉ | — |

## A.3 Acteurs

| Acteur | Ce que dit la recherche | Fiabilité | Source |
|---|---|---|---|
| Examinateurs | « Des examinateurs assermentés au sein de l'ATT se chargent d'évaluer chaque candidat » (extrait de recherche, Studio Sifaka). En juillet 2024 : formation de renforcement de capacités de « 31 examinateurs » à Alarobia, organisée par le MTM et l'ATT ; « 22 centres d'examen répartis à travers le pays ». | PRESSE | https://www.studiosifaka.org/articles/actualites/item/2425-ce-qu-il-faut-savoir-sur-les-procedures-pour-l-obtention-du-permis-de-conduire.html ; https://newsmada.com/2024/07/24/examen-pour-lobtention-du-permis-de-conduire-pres-de-60-000-candidats-recenses-chaque-annee/ |
| Volumes | « Environ 60 000 candidats se présentent chaque année » ; taux de réussite « 53 % » ; « 173 auto-écoles » dont « 100 à Antananarivo » (2024). | PRESSE | https://newsmada.com/2024/07/24/examen-pour-lobtention-du-permis-de-conduire-pres-de-60-000-candidats-recenses-chaque-annee/ |
| Auto-écoles : obligation | Étape 1 officielle : « s'inscrire dans une auto-école » (leçons théoriques et pratiques) ; les auto-écoles « paient les frais d'examen pour le compte des candidats » à l'ATT. Aucune procédure de « candidat libre » trouvée. Le syndicat SECOM s'est opposé en 2024 à un projet « permis pour tous » (« un permis de conduire n'est pas une carte d'identité nationale qu'on distribue gratuitement »). | OFFICIEL (obligation) / PRESSE (SECOM) | https://torolalana.gov.mg/fr/services/obtenir-son-permis-de-conduire/ ; https://fr.allafrica.com/stories/202405260078.html (403, extrait de recherche) |
| Auto-écoles : agrément | Les auto-écoles doivent « obtenir les visas provinciaux nécessaires » (Studio Sifaka, 2020) ; un extrait de recherche parle de « visas de la province d'Antananarivo ». Une auto-école agréée dispose d'un numéro d'agrément (extrait). Texte d'agrément (arrêté) **non trouvé**. Presse 2026 : des structures « clandestines » opèrent sans autorisation, véhicules sans double pédale ; la présidente du syndicat (nommé SECAM dans cet article, SECOM ailleurs) parle d'« usurpation de fonction ». | PRESSE | https://www.studiosifaka.org/articles/actualites/item/2425-ce-qu-il-faut-savoir-sur-les-procedures-pour-l-obtention-du-permis-de-conduire.html ; https://www.lexpress.mg/2026/06/securite-routiere-des-auto-ecoles.html |
| Centres d'examen (Antananarivo) | Torolalana : examens « auprès de l'Agence des Transports Terrestres de Tsimbazaza » ; Studio Sifaka : ATT « sise à Tsaralalana » (« petite vitesse ») ; La Vérité (extrait) : « à partir du 17 octobre, le centre d'examen a été transféré à Soarano, dans les locaux de Madarail » ; L'Express (26/07/2023) : le siège ATT d'Ampasampito déménage à la gare de Soarano au 31 juillet 2023, y compris « ceux qui vont soumettre une copie d'examen pour obtenir un permis de conduire ». Liste nationale des 22 centres : **non trouvée**. | OFFICIEL / PRESSE | https://torolalana.gov.mg/fr/services/obtenir-son-permis-de-conduire/ ; https://www.studiosifaka.org/articles/actualites/item/2425-ce-qu-il-faut-savoir-sur-les-procedures-pour-l-obtention-du-permis-de-conduire.html ; https://laverite.mg/societe/item/17278-examen-d%E2%80%99obtention-du-permis-de-conduire%20-des-mesures-strictes-pour-%C3%A9radiquer-la-corruption.html (500) ; https://lexpress.mg/26/07/2023/latt-ampasampito-demenagera-a-la-gare-de-soarano/ |
| Tutelle | Ministère des Transports et de la Météorologie (MTM) pour l'ATT ; CIM rattaché au Ministère de l'Intérieur et de la Décentralisation. | PRESSE | https://newsmada.com/2024/07/24/examen-pour-lobtention-du-permis-de-conduire-pres-de-60-000-candidats-recenses-chaque-annee/ ; https://fr.allafrica.com/stories/202605260570.html |

## A.4 Dossier candidat, âge, éligibilité

Source officielle principale : portail Torolalana (gouvernement) — https://torolalana.gov.mg/fr/services/obtenir-son-permis-de-conduire/

| Point | Ce que dit la recherche | Fiabilité |
|---|---|---|
| Pièces A, A' et B | Carte scolaire (A/A') ou copie certifiée de la CIN (B), copie d'acte de naissance, certificat de résidence, 5 photos d'identité. | OFFICIEL (Torolalana) ; concordant avec Studio Sifaka (PRESSE) |
| Pièces C, D, E (extensions) | Pièces de B + copie CIN, certificat médical du Bureau Municipal d'Hygiène (BMH), copie certifiée du permis B. | OFFICIEL (Torolalana) |
| Frais d'examen ATT | « 10 000 Ar pour l'obtention des permis de catégories A, A' et B » ; « 15 000 Ar pour C, D et E » ; payés par l'auto-école. | OFFICIEL (Torolalana) |
| Frais CIM (délivrance) | 38 000 Ar (dossier dans une « chemise cartonnée verte », photo prise sur place, attestation provisoire). | OFFICIEL (Torolalana) ; concordant 2424.mg et vol-direct |
| Frais de formation (indicatif) | Minimum conventionnel 100 000 Ar (A, A') / 150 000 Ar (C, D, E) en 2020 ; terraformalis cite 200 000 à 500 000 Ar selon catégorie (2025-2026). | PRESSE (Studio Sifaka) / SECONDAIRE (terraformalis) — https://terraformalis.com/permis-conduire-madagascar/ |
| Âge minimum | A/A' « dès l'âge de 16 ans » (Studio Sifaka) ; « âge légal 18 ans » avec possibilité à 16 ans avec accord parental (vol-direct, fiarakodia, extraits) ; C/D/E 21 ans (terraformalis). Pas de texte officiel lu. | PRESSE / SECONDAIRE — https://www.vol-direct.net/comment-passer-son-code-de-la-route-a-madagascar.html ; https://www.fiarakodia.com/en/posts/comment-obtenir-votre-permis-de-conduire-a-madagascar |
| Certificat médical pour B | Non exigé par Torolalana pour A/A'/B ; exigé par certains guides privés (fiarakodia : « certificat médical de moins de 3 mois »). Contradiction à trancher. | À VALIDER |
| « Permis d'apprenti » | fiarakodia mentionne « un permis d'apprenti qui a été utilisé avant 12 mois » pour l'examen théorique — formulation confuse, non confirmée ailleurs. | NON CONFIRMÉ |

## A.5 Règles en cas d'échec

| Point | Ce que dit la recherche | Fiabilité | Source |
|---|---|---|---|
| Délai avant repassage | « En cas d'échec, une nouvelle tentative est possible après un délai déterminé » (délai non précisé) ; fiarakodia : « il faut attendre environ 25 jours ». | SECONDAIRE (faible) | https://www.bureaudepoche.fr/tout-savoir-sur-lexamen-theorique-du-code-de-la-route-a-madagascar/ ; https://www.fiarakodia.com/en/posts/passer-son-permis-a-madagascar |
| Conservation du code réussi | Un extrait de recherche indique qu'après réussite du code « vous avez un temps limité pour compléter la partie pratique », sans durée. | NON TROUVÉ (durée) | — |
| Nombre de tentatives | **Non trouvé**. | NON TROUVÉ | — |
| Frais de réinscription | Extrait de recherche : « entre 10 000 et 15 000 Ar selon le type de permis » (correspond aux droits d'examen ATT). | SECONDAIRE | — |

## A.6 Organisation pratique des sessions et digitalisation

| Point | Ce que dit la recherche | Fiabilité | Source |
|---|---|---|---|
| Mesures anti-corruption (appel, anonymat) | Après réunion ATT / responsables d'auto-écoles / MTM / BIANCO : « les candidats et les examinateurs doivent désormais se présenter dans l'anonymat » ; auparavant les candidats « étaient accompagnés par leur moniteur d'auto-école » ; « l'appel des candidats par leur nom » a été supprimé ; « chaque candidat devra se présenter avec un numéro que l'examinateur ne doit pas connaître » ; transfert du centre à Soarano (Madarail) « à partir du 17 octobre ». (Article inaccessible, extraits de moteur de recherche ; année non déterminée, probablement 2023.) | PRESSE (extraits) | https://laverite.mg/societe/item/17278-examen-d%E2%80%99obtention-du-permis-de-conduire%20-des-mesures-strictes-pour-%C3%A9radiquer-la-corruption.html |
| Digitalisation ATT | Annonce de la DG de l'ATT, Mirambololona Ratovohery (article daté 16 janvier ; l'article lu indique 2026, les résumés de recherche disent 2025 — à vérifier) : application couvrant « de l'inscription à la publication des résultats », objectif « éliminer toute manipulation manuelle des dossiers », partenariat BIANCO et DCAC, « salles d'examen modernes », phase de « simulation technique », déploiement progressif ; extension aux licences d'exploitation et autorisations spéciales « au cours de 2026 ». | PRESSE | https://www.moov.mg/article/111378-examen-du-permis-de-conduire-une-application-en-phase-de-simulation-technique |
| Convocation, fréquence des sessions, capacité par session, retard/absence | **Non trouvé** (aucune règle publiée). | NON TROUVÉ | — |
| Files d'attente (contexte) | Files documentées surtout au **CIM** (délivrance) : « cent cinquante personnes par jour » en temps normal, 380 personnes un vendredi de juillet 2021 après une panne (600 rendez-vous reportés) ; prise de rendez-vous obligatoire au CIM depuis novembre 2020 (« guichet de prise de rendez-vous… tous les jours de 8h à 16h »). Pour l'ATT, seule la mention des 60 000 candidats/an et de 31 examinateurs donne l'ordre de grandeur. | PRESSE | https://lexpress.mg/21/07/2021/permis-de-conduire-la-file-dattente-revient-au-centre-dimmatriculation/ ; https://2424.mg/centre-immatriculateur-les-dossiers-relatifs-aux-permis-de-conduire-a-deposer-sur-rendez-vous/ |
| Publication des résultats | Extrait de recherche : le CIM délivre le permis « suivant la liste des admis publiée par l'ATT » ; l'auto-école remet ensuite « un bordereau d'envoi à l'apprenant et son rôle s'arrête là ». | PRESSE / SECONDAIRE | https://www.studiosifaka.org/... ; https://www.vol-direct.net/comment-passer-son-code-de-la-route-a-madagascar.html |
| Autres projets numériques | Permis **biométrique** délivré par le CIM (prestataires cités : Hephalu Mada en 2021, Cetis pour les anciens permis biométriques) ; pénurie de cartes fin 2025 (SECOM : « certificat de capacité provisoire… n'est pas reconnu par la majorité des entreprises »). Le portail Torolalana propose aussi « Permis de conduire (Médical) » et « Certificat d'authenticité de permis de conduire ». | PRESSE / OFFICIEL | https://newsmada.com/2025/11/15/stock-de-permis-de-conduire-epuise-le-secom-hausse-le-ton/ ; https://fr.allafrica.com/stories/202511250386.html ; https://torolalana.gov.mg/fr/services/permis-de-conduire-medical/ ; https://torolalana.gov.mg/services/certificat-d-authenticite-de-permis-de-conduire/?category=conduite-et-transport |

## A.7 ATT vs CIM (délimitation du périmètre)

| Rôle | Organisme | Fiabilité | Source |
|---|---|---|---|
| Formation | Auto-écoles (agréées) | PRESSE/OFFICIEL | https://fr.allafrica.com/stories/202605260570.html ; Torolalana |
| Organisation des examens (théorie + pratique), examinateurs, publication de la liste des admis, encaissement des droits d'examen | **ATT** (tutelle MTM) | OFFICIEL (Torolalana) + PRESSE | https://torolalana.gov.mg/fr/services/obtenir-son-permis-de-conduire/ ; https://fr.allafrica.com/stories/202605260570.html |
| Après réussite : « fiche de renvoi » / bordereau pour prendre rendez-vous au CIM | Auto-école → candidat → CIM | OFFICIEL | Torolalana (étape 3) |
| Dépôt du dossier (38 000 Ar), photo, attestation provisoire, fabrication et remise du permis biométrique | **CIM** d'Ambohidahy (Ministère de l'Intérieur) et centres immatriculateurs de province (ex. Mahajanga, Ampisikina) | OFFICIEL + PRESSE | Torolalana (étapes 4-5) ; https://fr.allafrica.com/stories/202511250386.html |
| Contrôles routiers | Police / Gendarmerie | PRESSE | https://fr.allafrica.com/stories/202605260570.html |

Conclusion pour le périmètre : l'application ATT peut s'arrêter à la **liste des admis / attestation de réussite** transmise au candidat et à l'auto-école ; tout ce qui suit (rendez-vous CIM, 38 000 Ar, biométrie, carte) est hors périmètre, conformément au cahier de cadrage.

## A.8 Organisation territoriale de l'ATT (complément demandé)

| Question | Ce que dit la recherche | Fiabilité | Source |
|---|---|---|---|
| Directions / agences régionales de l'ATT | Aucun organigramme officiel trouvé (site att.mg inaccessible, décret 2006-279 en 403). Indices d'implantations : siège à **Soarano** (gare Madarail, depuis le 31/07/2023, ex-Ampasampito) ; guichet à la station **Fasany Karana** (« dédiée aux lignes régionales », autorisations spéciales) ; bureau ATT à **Toamasina** (« Villa Eglee, angle Boulevard de la Libération », annuaire madayp) ; « Agence des Transports Terrestres Mahajanga » (bureau du gouvernement, Mapcarta) ; un post Orange Actu évoque « un bureau permanent » de l'ATT (non lisible). Liste exhaustive des antennes : **non trouvée**. | PRESSE (Soarano, Fasany Karana) / SECONDAIRE (annuaires) | https://lexpress.mg/26/07/2023/latt-ampasampito-demenagera-a-la-gare-de-soarano/ ; https://moov.mg/actualite/nationale/94722-noel-et-nouvel-an-lagence-des-transports-terrestres-assure-la-fluidite-du-transport-a-antananarivo ; https://fr.madayp.com/company/3693/Att_agence_Des_Transports_Terrestres ; https://mapcarta.com/fr/N8544469869 |
| Centres d'examen par région | « 22 centres d'examen répartis à travers le pays » (2024), **31 examinateurs** au total — ce qui implique nécessairement des centres hors Antananarivo, mais la liste des villes et le caractère fixe/itinérant des sessions ne sont **pas documentés**. | PRESSE (chiffres) / NON TROUVÉ (liste) | https://newsmada.com/2024/07/24/examen-pour-lobtention-du-permis-de-conduire-pres-de-60-000-candidats-recenses-chaque-annee/ |
| Agrément des auto-écoles par région | Les auto-écoles doivent « obtenir les visas provinciaux nécessaires » (formulation 2020) ; « 173 auto-écoles réparties dans le pays, dont 100 à Antananarivo ». Le niveau exact (province historique, région, ATT centrale) n'est pas précisé. | PRESSE | Studio Sifaka ; Newsmada 2024 |
| Le candidat doit-il passer dans la région de son auto-école ? | **Non trouvé.** Aucune règle publiée. Indice indirect : les auto-écoles paient les droits et disposent de visas territoriaux, ce qui suggère un rattachement territorial, sans preuve. | NON TROUVÉ | — |
| Sessions itinérantes vs centres fixes | **Non trouvé.** | NON TROUVÉ | — |
| Référentiel géographique à utiliser | Madagascar compte **24 régions** (loi 2004-001 : 22 régions ; loi 010/2021 du 9 juin 2021 : scission Vatovavy / Fitovinany ; loi 2023-012 du 29 juin 2023 : création d'Ambatosoa à partir d'Analanjirofo). Les 6 anciennes provinces (faritany) ont été dissoutes le 4 octobre 2009 mais restent utilisées comme regroupement (et dans le vocabulaire « visa provincial »). | SECONDAIRE (Wikipédia, cohérent avec la presse) | https://fr.wikipedia.org/wiki/R%C3%A9gion_de_Madagascar ; https://fr.wikipedia.org/wiki/R%C3%A9gions_de_Madagascar ; https://lexpress.mg/11/08/2021/creation-de-la-23eme-region/ ; https://midi-madagasikara.mg/maroantsetra-mananara-la-creation-de-la-24eme-region-adoptee-en-conseil-des-ministres/ |

Liste des 24 régions (chef-lieu, ancienne province) — utilisable comme table de référence `Region` dans Room (source : https://fr.wikipedia.org/wiki/R%C3%A9gion_de_Madagascar) :

| Ancienne province | Régions (chef-lieu) |
|---|---|
| Antananarivo | Analamanga (Antananarivo), Bongolava (Tsiroanomandidy), Itasy (Miarinarivo), Vakinankaratra (Antsirabe) |
| Antsiranana | Diana (Antsiranana), Sava (Sambava) |
| Fianarantsoa | Amoron'i Mania (Ambositra), Atsimo-Atsinanana (Farafangana), Fitovinany (Manakara), Haute Matsiatra (Fianarantsoa), Ihorombe (Ihosy), Vatovavy (Mananjary) |
| Mahajanga | Betsiboka (Maevatanana), Boeny (Mahajanga), Melaky (Maintirano), Sofia (Antsohihy) |
| Toamasina | Alaotra-Mangoro (Ambatondrazaka), Ambatosoa (Maroantsetra), Analanjirofo (Fenoarivo Atsinanana), Atsinanana (Toamasina) |
| Toliara | Androy (Ambovombe), Anôsy (Tôlanaro), Atsimo-Andrefana (Toliara), Menabe (Morondava) |

Recommandation de modélisation (prudente) : prévoir une entité `Region` (24 lignes, seed) et une entité `CentreExamen` rattachée à une région, avec les auto-écoles rattachées à une région ; ne pas coder en dur la contrainte « examen dans la région de l'auto-école » tant qu'elle n'est pas confirmée (la rendre paramétrable).

## A.9 Tableau récapitulatif « Point à valider → recherche → fiabilité → question à poser »

| Point à valider (cahier de cadrage) | Ce que dit la recherche | Fiabilité | Question à poser à l'ATT / à l'enseignant |
|---|---|---|---|
| Barème et seuil de l'épreuve théorique | 30 questions / 30 min (QCM) ; seuil « défini par la réglementation », valeur inconnue | SECONDAIRE / NON TROUVÉ | Combien de bonnes réponses pour être admis ? Nombre de questions et durée officiels ? Existe-t-il une banque de questions officielle et une variante par catégorie ? |
| Barème de l'épreuve pratique | Aucune grille officielle trouvée ; épreuve en véhicule ; témoignage « démarrage en côte » | NON TROUVÉ | Quelle grille (points, fautes éliminatoires) ? Y a-t-il une phase manœuvres + une phase circulation ? Durée ? Le résultat est-il binaire (apte/inapte) ou noté ? |
| Déroulement exact (ordre, même jour ou non) | Théorie obligatoire avant pratique ; « deux examens consécutifs » | PRESSE | Théorie et pratique le même jour ou sessions séparées ? Délai minimal entre les deux ? |
| Support de l'épreuve théorique | Application ATT en simulation technique ; salles modernisées | PRESSE | L'épreuve théorique est-elle passée sur écran ou sur papier corrigé ensuite ? L'app doit-elle gérer le QCM lui-même ou seulement le résultat ? |
| Repassage et conservation de la théorie réussie | « délai déterminé » ; ~25 jours (source faible) ; « temps limité » pour passer la pratique, durée inconnue | SECONDAIRE faible | Combien de temps un code réussi reste-t-il valable ? Combien de tentatives de pratique avant de repasser le code ? Délai minimal entre deux tentatives ? Faut-il re-payer 10 000/15 000 Ar ? |
| Appel / présence / retard / absence | Appel par numéro anonyme (plus par nom) ; moniteur non admis | PRESSE (extraits) | Un retardataire est-il reporté ou absent ? Une absence consomme-t-elle une tentative ? Qui note la présence (examinateur, agent d'accueil) ? |
| Durée / capacité des sessions | Non trouvé ; ordre de grandeur : 60 000 candidats/an, 31 examinateurs, 22 centres | NON TROUVÉ | Combien de candidats par session théorique / pratique ? Combien de sessions par semaine et par centre ? |
| Rôle exact des examinateurs | Examinateurs « assermentés » de l'ATT, formés par le MTM/ATT | PRESSE | Un examinateur fait-il théorie et pratique ? Affectation par tirage au sort ? Peut-il modifier un résultat après saisie ? Double signature ? |
| Créneaux individuels | Non trouvé ; la logique actuelle est celle de sessions collectives par numéro | NON TROUVÉ | Un créneau horaire individuel est-il réglementairement possible pour la pratique ? |
| Données / documents que l'ATT peut stocker | Pièces du dossier connues (CIN, acte de naissance, résidence, photos, certificat BMH pour C/D/E) ; aucune règle de protection des données trouvée | OFFICIEL (pièces) / NON TROUVÉ (droit) | Quelles pièces l'ATT conserve-t-elle (scan ou simple attestation de dépôt) ? Durée de conservation ? Le numéro de CIN peut-il servir d'identifiant ? |
| Catégories à gérer | A, A', B, C, D, E confirmées ; F et AM non confirmées ; décret 2026-974 pourrait changer la liste | OFFICIEL / NON TROUVÉ | Liste officielle actuelle des catégories et des extensions (C, D, E exigent B) ? Faut-il prévoir F/AM ? |
| Âges minimaux | 16 ans (A/A'), 18 ans (B), 21 ans (C/D/E) selon presse/guides | PRESSE / SECONDAIRE | Confirmer les âges par catégorie et la règle d'accord parental. |
| Organisation territoriale | 22 centres, auto-écoles avec « visas provinciaux », 24 régions officielles | PRESSE / NON TROUVÉ | Liste des centres d'examen et de leur région ; le candidat est-il lié à la région de son auto-école ; les examinateurs sont-ils affectés à un centre ? |
| Lien avec le CIM | ATT publie la liste des admis, le CIM délivre | OFFICIEL | Quel document exact l'ATT remet-elle (copie d'examen, fiche de renvoi, attestation) et qui le signe ? |

---

# PARTIE B — Bonnes pratiques Android / Kotlin (septembre 2026)

## B.1 Versions stables et compatibilité avec AGP 9.3.x

### Tableau récapitulatif des versions (toutes vérifiées le 15/09/2026)

| Composant | Version stable | Notes de compatibilité | Source |
|---|---|---|---|
| Android Gradle Plugin | **9.3.0** (juillet 2026) ; 9.4.x existe déjà (page « About AGP ») | Gradle ≥ 9.5.0, JDK ≥ 17, SDK Build Tools ≥ 36.0.0, API max 37 | https://developer.android.com/build/releases/agp-9-3-0-release-notes ; https://developer.android.com/build/releases/about-agp |
| Gradle | **9.7.1** (20 août 2026) ; 9.5.0 minimum pour AGP 9.3 | KGP 2.4.20 supporte Gradle 7.6.3 → 9.7.0 (rester ≤ 9.7.x) | https://docs.gradle.org/current/release-notes.html ; https://kotlinlang.org/docs/gradle-configure-project.html |
| Android Studio | **Quail 3** (juillet 2026, AGP 7.1–9.3) ; Quail 4 (AGP 7.1–9.4) | | https://developer.android.com/studio/releases/past-releases/as-quail-3-release-notes ; https://developer.android.com/build/releases/about-agp |
| Kotlin / KGP | **2.4.20** (7 sept. 2026) | KGP 2.4.20 : AGP 8.5.2 → **9.3.1** ; KGP 2.4.0–2.4.10 : AGP ≤ 9.1.0 seulement | https://kotlinlang.org/docs/releases.html ; https://kotlinlang.org/docs/gradle-configure-project.html |
| KGP intégré à AGP (built-in Kotlin) | AGP 9.x embarque une dépendance sur KGP **2.2.10** par défaut ; version supérieure déclarable | Voir B.1.2 | https://developer.android.com/build/releases/agp-9-0-0-release-notes |
| KSP | **2.3.12** (dernière release listée ; versionnage indépendant de Kotlin depuis 2.3.0 ; correctif de compatibilité Kotlin 2.4.0 dans 2.3.10) | AGP minimum 8.12.0 | https://github.com/google/ksp/releases ; https://kotlinlang.org/docs/ksp-quickstart.html |
| Compose Compiler plugin | `org.jetbrains.kotlin.plugin.compose` **= version de Kotlin** (2.4.20) | Fait partie du dépôt Kotlin depuis 2.0 | https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler |
| Compose BOM | **2026.08.00** → ui/foundation/runtime/animation **1.12.0**, material3 **1.4.0**, material3-adaptive 1.3.0 | Compose 1.12 exige **AGP ≥ 9.1.1** et **compileSdk 37** | https://developer.android.com/develop/ui/compose/bom/bom-mapping ; https://android-developers.googleblog.com/2026/08/jetpack-compose-august-2026-release.html |
| Lifecycle (viewmodel-compose, runtime-compose) | **2.11.0** (17 juin 2026) ; 2.12.0-alpha03 | minSdk 23 depuis 2.10.0 | https://developer.android.com/jetpack/androidx/releases/lifecycle |
| Navigation 2.x (navigation-compose) | **2.10.1** (9 sept. 2026) — **mode maintenance** (« will only receive critical fixes ») | | https://developer.android.com/jetpack/androidx/releases/navigation |
| Navigation 3 | **1.1.7** (26 août 2026) ; 1.2.0-beta01 | Compose-first, NavKey sérialisables, minSdk 23 ; stable depuis nov. 2025 | https://developer.android.com/jetpack/androidx/releases/navigation3 ; https://android-developers.googleblog.com/2025/11/jetpack-navigation-3-is-stable.html |
| Room 2.x | **2.8.5** (9 sept. 2026) — entre en **mode maintenance** | KSP recommandé, Kotlin 2.0+ | https://developer.android.com/jetpack/androidx/releases/room |
| Room 3 | **3.0.3** (9 sept. 2026), artefacts `androidx.room3:room3-*`, plugin `androidx.room3` | **KSP obligatoire**, Kotlin only, DAO `suspend`/`Flow` uniquement | https://developer.android.com/jetpack/androidx/releases/room3 ; https://android-developers.googleblog.com/2026/03/room-30-modernizing-room.html |
| kotlinx-coroutines | **1.11.0** (compilé contre Kotlin 2.2.20) | | https://github.com/Kotlin/kotlinx.coroutines/releases |
| DataStore | **1.2.1** (11 mars 2026) | Pour la session utilisateur | https://developer.android.com/jetpack/androidx/releases/datastore |
| WorkManager | **2.11.2** (25 mars 2026) ; minSdk 23 | Perspective offline-first | https://developer.android.com/jetpack/androidx/releases/work |
| compileSdk / targetSdk | **37** (Android 17, sorti le 16 juin 2026) ; API 37 exige AGP ≥ 9.1.1 et Studio Panda 3+ | | https://developer.android.com/about/versions/17/setup-sdk ; https://developer.android.com/build/releases/about-agp |
| JDK | **17** minimum pour AGP 9.x ; toolchain 17 recommandé | | https://developer.android.com/build/releases/agp-9-3-0-release-notes |

Remarque : les dates de release affichées par GitHub pour KSP et kotlinx-coroutines ont été extraites de façon incohérente par l'outil de lecture (années 2023-2024 alors que les versions citent Kotlin 2.2.20 / AGP 8.12) ; seuls les **numéros de version** sont à retenir, les dates sont à vérifier sur les pages GitHub.

### B.1.1 Built-in Kotlin dans AGP 9 : oui, et comment le déclarer

- « Android Gradle plugin 9.0 introduces built-in Kotlin support and enables it by default. That means you no longer have to apply the `org.jetbrains.kotlin.android` (or `kotlin-android`) plugin in your build files to compile Kotlin source files. » — https://developer.android.com/build/releases/agp-9-0-0-release-notes
- « AGP 9.0 now has a runtime dependency on Kotlin Gradle plugin (KGP) 2.2.10 … if you use a KGP version lower than 2.2.10, Gradle will automatically upgrade your KGP version to 2.2.10. » ; de même KSP est relevé à 2.2.10-2.0.2 si plus ancien. — même source.
- Pour utiliser une KGP plus récente (ex. 2.4.20), la doc prescrit de la déclarer dans le `buildscript` du build racine :

```kotlin
// build.gradle.kts (racine)
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.12")
    }
}
```
(source : https://developer.android.com/build/releases/agp-9-0-0-release-notes). Le plugin `org.jetbrains.kotlin.android` « is not compatible with the new DSL » ; opt-out possible via `android.builtInKotlin=false` dans `gradle.properties`, flag qui sera supprimé dans AGP 10 (https://blog.jetbrains.com/kotlin/2026/01/update-your-projects-for-agp9/).
- Guide de migration officiel (https://developer.android.com/build/migrate-to-built-in-kotlin) : supprimer `alias(libs.plugins.kotlin.android)` du module et du build racine et l'entrée `kotlin-android` du catalogue ; remplacer `android.kotlinOptions {}` par :

```kotlin
kotlin {
    compilerOptions {
        // jvmTarget optionnel : « jvmTarget par défaut prend la valeur de android.compileOptions.targetCompatibility »
        // jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}
```
  Les sources Kotlin additionnelles se déclarent via `android.sourceSets.named("main") { kotlin.directories += ... }` ; `kapt` devient `com.android.legacy-kapt` (inutile ici puisque Room est en KSP) ; un module sans Kotlin peut poser `android { enableKotlin = false }`.
- Le plugin Compose Compiler reste à appliquer explicitement (la page de migration ne le couvre pas) : `id("org.jetbrains.kotlin.plugin.compose") version "<version Kotlin>"` + `android { buildFeatures { compose = true } }` — https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler

### B.1.2 Squelette de build recommandé pour le projet (synthèse des sources ci-dessus)

```toml
# gradle/libs.versions.toml
[versions]
agp = "9.3.0"
kotlin = "2.4.20"
ksp = "2.3.12"
composeBom = "2026.08.00"
lifecycle = "2.11.0"
navigation = "2.10.1"        # ou navigation3 = "1.1.7"
room = "2.8.5"               # ou room3 = "3.0.3"
coroutines = "1.11.0"
datastore = "1.2.1"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "room" }
```

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}
android {
    compileSdk = 37
    defaultConfig { minSdk = 26; targetSdk = 37 }   // 26 : PBKDF2WithHmacSHA256 disponible (voir B.5)
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    buildFeatures { compose = true }
    room { schemaDirectory("$projectDir/schemas") }
}
dependencies {
    val bom = platform(libs.compose.bom); implementation(bom); androidTestImplementation(bom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.navigation:navigation-compose:2.10.1")
    implementation("androidx.room:room-runtime:2.8.5"); ksp("androidx.room:room-compiler:2.8.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
}
```
Avec built-in Kotlin, aucune ligne `org.jetbrains.kotlin.android` ; la version de KGP 2.4.20 est imposée via le `buildscript` racine (B.1.1). Gradle wrapper : 9.5.0 minimum, 9.7.x recommandé (≤ 9.7.0 pour rester dans la plage testée par KGP 2.4.20).

### B.1.3 Navigation 2 ou Navigation 3 ? Room 2 ou Room 3 ?

- **Navigation** : Navigation 2.x est officiellement en maintenance (« This library is in maintenance mode and will only receive critical fixes; new features are not planned ») ; Navigation 3 est stable (1.1.7). Pour un projet étudiant démarré en 2026, Navigation 3 est le choix aligné sur la roadmap (back stack = liste de `NavKey` `@Serializable`, `NavDisplay`, intégration ViewModel via `lifecycle-viewmodel-navigation3`), mais navigation-compose 2.10.1 reste parfaitement fonctionnel et mieux documenté dans les tutoriels ; les deux sont acceptables. Sources : https://developer.android.com/jetpack/androidx/releases/navigation ; https://developer.android.com/jetpack/androidx/releases/navigation3 ; https://developer.android.com/guide/navigation/navigation-3/get-started
- **Room** : Room 2.8.5 reste stable et passe en maintenance ; Room 3.0.3 est la ligne d'avenir (« Since the development of Room will be focused on Room 3, the current Room 2.x version enters maintenance mode »), avec **KSP obligatoire**, génération Kotlin, DAO `suspend` obligatoires (« DAO functions have to be suspending unless they are returning a reactive type, such as a Flow ») et nouvelles API de transaction (`withWriteTransaction`). La migration 2 → 3 « mostly involves updating symbol references » (`androidx.room` → `androidx.room3`). Pour un projet qui utilise déjà KSP + coroutines, **Room 3.0.3 est raisonnable** ; Room 2.8.5 reste le choix le plus sûr si l'enseignant/les supports de cours sont sur Room 2. Sources : https://android-developers.googleblog.com/2026/03/room-30-modernizing-room.html ; https://developer.android.com/jetpack/androidx/releases/room3

## B.2 Architecture officielle et adaptation « projet étudiant sans Hilt »

Principes du Guide to app architecture (https://developer.android.com/topic/architecture) :
- Au moins deux couches : **UI layer** et **data layer**, **domain layer** optionnelle.
- « The most important principle is separation of concerns » ; « drive your UI from data models, preferably persistent models » ; **Single source of truth** (« only the SSOT can modify or mutate it ») ; **Unidirectional Data Flow** (« state flows in only one direction… events… flow in the opposite direction »).
- Recommandations : ne pas stocker de données dans les composants Android, rendre chaque partie testable isolément, persister localement, « Types are responsible for their concurrency policy » (main-safety).

UI layer (https://developer.android.com/topic/architecture/ui-layer) :
- UI state = « immutable snapshot of the data needed for the UI to render fully » ; une `data class XxxUiState` par écran, ou une `sealed interface` Loading/Success/Error (pattern Now in Android : https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md).
- « The ViewModel type is the recommended implementation for the management of screen-level UI state » ; exposition via `StateFlow` (ou `mutableStateOf`) ; conversion des flux Room avec `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue)` (https://developer.android.com/topic/architecture/ui-layer/state-production ; https://developer.android.com/topic/architecture/data-layer/offline-first).
- Collecte côté Compose avec `collectAsStateWithLifecycle()` (artefact `lifecycle-runtime-compose`) : « Don't make the UI observe the UI state when the composable isn't being displayed to the user. »
- Toute logique métier dans data/domain, jamais dans les composables ; « Make sure all work performed in a ViewModel is main-safe ».

Data layer : un repository par type de donnée, exposant des `Flow` en lecture et des `suspend fun` en écriture ; les entités Room ne remontent pas jusqu'à l'UI (mapping `asExternalModel()`), pattern documenté dans https://developer.android.com/topic/architecture/data-layer/offline-first.

Injection sans Hilt — approche officielle « manual DI » (https://developer.android.com/training/dependency-injection/manual) :
```kotlin
class AppContainer(context: Context) {
    val db = AppDatabase.getInstance(context)
    val candidatRepository = CandidatRepository(db.candidatDao())
    val sessionRepository = SessionRepository(db.sessionDao(), db.inscriptionDao())
}
class AttApplication : Application() { val appContainer by lazy { AppContainer(this) } }
```
puis dans un composable `val vm: SessionViewModel = viewModel(factory = SessionViewModelFactory(container.sessionRepository))` (ou `viewModel { SessionViewModel(container.sessionRepository) }` avec `lifecycle-viewmodel-compose`). La doc précise que Hilt est recommandé « when possible », mais reconnaît le conteneur manuel comme approche valide ; ses limites (boilerplate de factories, gestion manuelle des scopes) sont acceptables pour un projet à un seul module.

Structure de packages suggérée (dérivée de Now in Android) : `data/local/{entity,dao,AppDatabase}`, `data/repository`, `domain/model` (+ éventuels use cases pour les règles d'examen), `ui/{login,sessions,candidats,examens,impression}` avec un `XxxScreen` + `XxxViewModel` + `XxxUiState` par écran, `di/AppContainer.kt`.

## B.3 Room pour ~20 entités très reliées

Références : https://developer.android.com/training/data-storage/room/defining-data ; https://developer.android.com/training/data-storage/room/relationships ; https://developer.android.com/training/data-storage/room/accessing-data ; https://developer.android.com/training/data-storage/room/migrating-db-versions ; https://developer.android.com/training/data-storage/room/prepopulate ; https://developer.android.com/training/data-storage/room/testing-db

1. **Entités, clés, index** : `@Entity(tableName=…, foreignKeys=[ForeignKey(entity=…, parentColumns=…, childColumns=…, onDelete=RESTRICT)], indices=[Index("candidat_id"), Index(value=["session_id","candidat_id"], unique=true)])`. Room émet un avertissement lint si une colonne FK n'est pas indexée ; indexer systématiquement les colonnes FK et poser des index `unique` pour les contraintes métier (un candidat inscrit une seule fois par session). Utiliser `@PrimaryKey(autoGenerate = true)` ou des UUID string.
2. **Relations** : la doc recommande, sauf besoin d'objets intermédiaires, les **requêtes JOIN avec retour multimap** (`Map<Session, List<Inscription>>`) : « If you don't have a specific reason to use intermediate data classes, we recommend using the multimap return type approach. » `@Embedded` + `@Relation` (avec `@Junction` pour le many-to-many) restent pratiques pour les écrans « fiche » ; toute méthode DAO qui renvoie un `@Relation` doit être annotée `@Transaction`. Room 3 ajoute les clés composites dans `@Relation`.
3. **DAO** : `suspend fun` pour les écritures, `Flow<List<…>>` pour les listes observées, `@Insert(onConflict = OnConflictStrategy.ABORT)` par défaut (ne pas utiliser REPLACE sur des tables historisées), `@Upsert` pour les référentiels, `@Query` avec paramètres de collection (`IN (:ids)`).
4. **Transactions** : Room 2 → `@Transaction` sur une méthode DAO ou `db.withTransaction { … }` ; Room 3 → `db.withWriteTransaction { … }` / `withReadTransaction`. Cas d'usage : « enregistrer un résultat + créer la ligne d'historique + clôturer l'inscription » en une seule transaction.
5. **Historique / audit, « ne jamais écraser un ancien résultat »** (synthèse à partir des API ci-dessus ; pas une règle officielle Room) :
   - modéliser `ResultatEpreuve` en **append-only** : une ligne par tentative, avec `tentativeNumero`, `dateHeure`, `examinateurId`, `statut`, et un index unique `(inscriptionId, epreuve, tentativeNumero)` ; jamais de `@Update` sur cette table, seulement `@Insert` ;
   - dériver l'état courant par requête (`MAX(tentativeNumero)` ou `ORDER BY dateHeure DESC LIMIT 1`) plutôt que par une colonne mutable ;
   - table `JournalAudit(id, dateHeure, utilisateurId, action, entite, entiteId, ancienneValeur, nouvelleValeur)` alimentée dans la même transaction que la modification, depuis le repository ;
   - pour une « correction », insérer une nouvelle ligne avec `annuleResultatId` référençant l'ancienne (soft-delete, `onDelete = RESTRICT` sur les FK pour interdire la suppression physique).
6. **Seed initial** : deux options officielles — `createFromAsset("database/att-seed.db")` (base pré-remplie, validée contre le schéma exporté) ou un `RoomDatabase.Callback().onCreate` qui lance l'insertion des référentiels (régions, catégories, centres, compte admin) via une coroutine sur `Dispatchers.IO`. `createFromAsset` n'est pas supporté sur les bases in-memory des tests.
7. **Migrations** : activer le plugin Gradle Room (`schemaDirectory`) pour exporter `schemas/<version>.json` dès la v1 ; `@AutoMigration(from, to)` pour les ajouts de colonnes/tables ; `Migration` manuelle avec `execSQL`/`executeSQL` pour les changements complexes ; tester avec `MigrationTestHelper` (`room-testing`). Éviter `fallbackToDestructiveMigration()` hors développement (« permanently deletes all user data »).

## B.4 Impression (convocations, listes) — approche la plus simple

Options du Print Framework (Android 4.4+) : https://developer.android.com/training/printing

| Approche | Principe | Complexité | Source |
|---|---|---|---|
| **WebView + HTML** (recommandée pour le projet) | Générer une chaîne HTML (convocation, liste d'appel), `webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)`, puis dans `WebViewClient.onPageFinished` : `printManager.print(jobName, webView.createPrintDocumentAdapter(jobName), PrintAttributes.Builder().build())`. L'utilisateur choisit imprimante **ou « Enregistrer en PDF »** dans la boîte de dialogue système. Pièges officiels : appeler la création du job seulement dans `onPageFinished`, garder une référence au WebView jusqu'à la remise de l'adapter, pas d'en-têtes/pieds de page ni de numéros de page, un job par WebView. | Faible | https://developer.android.com/training/printing/html-docs |
| **PrintDocumentAdapter + PrintedPdfDocument** | Implémenter `onLayout` (calcul du nombre de pages) et `onWrite` (dessin sur `page.canvas` en points 1/72 pouce, `pdfDocument.writeTo(...)`). Contrôle total de la mise en page, mais beaucoup de code. | Élevée | https://developer.android.com/training/printing/custom-docs |
| **PdfDocument seul + partage** | Générer un fichier PDF avec `android.graphics.pdf.PdfDocument` dans `filesDir`, l'exposer par un `FileProvider` (`res/xml/filepaths.xml`) et l'envoyer via `ShareCompat.IntentBuilder`/`ACTION_SEND` avec `type = "application/pdf"` et `FLAG_GRANT_READ_URI_PERMISSION`. Utile pour envoyer une convocation par messagerie. | Moyenne | https://developer.android.com/training/sharing/send |

Recommandation : pour un projet étudiant, **HTML → WebView → PrintManager** est l'approche la plus simple et couvre à la fois l'impression et l'export PDF (via l'imprimante virtuelle « Enregistrer au format PDF ») ; le CSS inline suffit pour un tableau de liste d'appel. Le `PrintDocumentAdapter` personnalisé n'est utile que si l'on exige numéros de page ou mise en page pixel-perfect.

## B.5 Authentification locale, rôles, stockage de session, tests

### Hachage des mots de passe (sans backend)
- Approche standard SDK : **PBKDF2** via `SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")` (disponible **API 26+** ; `PBKDF2WithHmacSHA1` sur les API plus anciennes), sel aléatoire de 16 octets (`SecureRandom`) stocké avec le hash, longueur de clé ≥ 128 bits (256 recommandé), nombre d'itérations élevé (l'auteur cite les recommandations OWASP : 310 000 itérations pour HMAC-SHA256, mais mesurées ~2 s sur mobile ; un compromis de quelques dizaines de milliers d'itérations est courant sur téléphone — à documenter dans le rapport comme choix assumé). Sources : https://www.danielhugenroth.com/posts/2021_06_password_hashing_on_android/ ; https://blog.codersee.com/kotlin-pbkdf2-secure-password-hashing/
- Alternative plus forte : **Argon2id** via la bibliothèque `argon2kt` (binding JNI, ~60 ms avec 37 MiB / 1 itération) — https://github.com/lambdapioneer/argon2kt. Pour un projet étudiant, PBKDF2 (aucune dépendance) est « simple et correct » ; comparer les hachages en temps constant (`MessageDigest.isEqual`).
- Ne jamais stocker le mot de passe en clair ni un simple SHA-256 sans sel.

Exemple minimal :
```kotlin
object PasswordHasher {
    private const val ITER = 120_000; private const val KEY_BITS = 256
    fun hash(pwd: CharArray, salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(pwd, salt, ITER, KEY_BITS)).encoded
    fun newSalt() = ByteArray(16).also { SecureRandom().nextBytes(it) }
    fun verify(pwd: CharArray, salt: ByteArray, expected: ByteArray) = MessageDigest.isEqual(hash(pwd, salt), expected)
}
```
(entité Room `Utilisateur(id, login, role, salt: ByteArray, hash: ByteArray, actif)`).

### Rôles et session
- Rôles (ADMIN, AGENT, EXAMINATEUR…) : enum stocké dans l'entité utilisateur ; vérification côté ViewModel/repository (jamais seulement dans l'UI).
- Session : garder l'utilisateur connecté en mémoire (`StateFlow<Utilisateur?>` dans un `SessionManager` du `AppContainer`) et, si l'on veut survivre au redémarrage, persister l'`id` utilisateur + horodatage dans **Preferences DataStore 1.2.1** (https://developer.android.com/jetpack/androidx/releases/datastore). **Ne pas utiliser `EncryptedSharedPreferences`** : `androidx.security:security-crypto` est déprécié depuis 1.1.0-alpha07 (avril 2025) ; la recommandation communautaire est DataStore (+ Tink/Keystore si chiffrement nécessaire) — https://www.droidcon.com/2025/12/16/goodbye-encryptedsharedpreferences-a-2026-migration-guide/ ; https://github.com/ed-george/encrypted-shared-preferences. Pour un projet local sans secret réseau, stocker uniquement un identifiant de session (jamais le mot de passe) suffit.

### Tests
- **JUnit 4** reste la base officielle des tests Android (`createComposeRule`, `MainDispatcherRule` sont des `TestRule` JUnit4) ; le support JUnit 5 n'existe qu'à travers le plugin communautaire mannodermaus, dont la configuration avec AGP 9 est encore en cours (issue #404, janvier 2026) — https://github.com/mannodermaus/android-junit-framework/issues/404. Recommandation : JUnit 4.13.2.
- **ViewModel + coroutines** (https://developer.android.com/kotlin/coroutines/test) : `testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")`, tests en `runTest { }`, injection d'un `CoroutineDispatcher` dans les repositories, remplacement de `Dispatchers.Main` par la règle officielle :
```kotlin
class MainDispatcherRule(val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) { Dispatchers.setMain(testDispatcher) }
    override fun finished(description: Description) { Dispatchers.resetMain() }
}
```
  Avec `StandardTestDispatcher`, appeler `advanceUntilIdle()` ; pour les `StateFlow` produits par `stateIn(WhileSubscribed)`, collecter dans `backgroundScope`.
- **Room** (https://developer.android.com/training/data-storage/room/testing-db) : la doc actuelle recommande des **tests JVM locaux** avec `Room.inMemoryDatabaseBuilder<AppDatabase>().setDriver(BundledSQLiteDriver()).build()` (Room KMP, sans émulateur) et indique explicitement de **ne pas** utiliser Robolectric pour Room (« Don't use: Android local unit tests with Robolectric »). Les tests instrumentés `@RunWith(AndroidJUnit4::class)` avec `ApplicationProvider.getApplicationContext()` restent possibles. Tester les migrations avec `MigrationTestHelper`.
- **Compose UI** (https://developer.android.com/develop/ui/compose/testing) : `androidTestImplementation("androidx.compose.ui:ui-test-junit4")`, `debugImplementation("androidx.compose.ui:ui-test-manifest")`, `@get:Rule val composeTestRule = createComposeRule()`, `setContent { … }`, `onNodeWithText("Valider").performClick()`, `assertIsDisplayed()`. Le BOM 2026.08.00 ajoute `hasPendingWork()` / `runWithoutImplicitWait()` pour réduire la flakiness (https://android-developers.googleblog.com/2026/08/jetpack-compose-august-2026-release.html).

## B.6 Perspectives : offline-first et WorkManager

- L'application est déjà « offline » par construction (Room = source de vérité). Le guide officiel (https://developer.android.com/topic/architecture/data-layer/offline-first) formalise la suite : « The local data source is the exclusive source of truth », lectures via `Flow`, écritures `suspend`, stratégies d'écriture (online-only / queued / lazy), synchronisation pull ou push, résolution de conflit « last write wins » par horodatage.
- Pour une future synchronisation vers un serveur ATT central : `WorkManager 2.11.2` (`androidx.work:work-runtime`, `CoroutineWorker`, contraintes `NetworkType.CONNECTED`, `Result.retry()` avec backoff exponentiel), pattern `SyncWorker` de Now in Android. Source : https://developer.android.com/jetpack/androidx/releases/work
- Ceci reste hors périmètre du projet ; à mentionner en « perspectives » uniquement.

---

## Sources principales (rappel)

Partie A : https://torolalana.gov.mg/fr/services/obtenir-son-permis-de-conduire/ · https://www.studiosifaka.org/articles/actualites/item/2425-ce-qu-il-faut-savoir-sur-les-procedures-pour-l-obtention-du-permis-de-conduire.html · https://newsmada.com/2024/07/24/examen-pour-lobtention-du-permis-de-conduire-pres-de-60-000-candidats-recenses-chaque-annee/ · https://www.moov.mg/article/111378-examen-du-permis-de-conduire-une-application-en-phase-de-simulation-technique · https://www.lexpress.mg/2026/05/circulation-les-deux-roues-soumis-un.html · https://fr.allafrica.com/stories/202605260570.html · https://www.lexpress.mg/2026/04/securite-routiere-le-nouveau-code-de-la.html · https://www.lexpress.mg/2026/06/securite-routiere-des-auto-ecoles.html · https://lexpress.mg/07/02/2020/auto-ecoles-les-formations-laissent-a-desirer/ · https://lexpress.mg/21/07/2021/permis-de-conduire-la-file-dattente-revient-au-centre-dimmatriculation/ · https://lexpress.mg/26/07/2023/latt-ampasampito-demenagera-a-la-gare-de-soarano/ · https://2424.mg/centre-immatriculateur-les-dossiers-relatifs-aux-permis-de-conduire-a-deposer-sur-rendez-vous/ · https://newsmada.com/2025/11/15/stock-de-permis-de-conduire-epuise-le-secom-hausse-le-ton/ · https://fr.allafrica.com/stories/202511250386.html · https://laverite.mg/societe/item/17278-examen-d%E2%80%99obtention-du-permis-de-conduire%20-des-mesures-strictes-pour-%C3%A9radiquer-la-corruption.html · https://fr.allafrica.com/stories/202405260078.html · https://www.bureaudepoche.fr/tout-savoir-sur-lexamen-theorique-du-code-de-la-route-a-madagascar/ · https://terraformalis.com/permis-conduire-madagascar/ · https://www.fiarakodia.com/en/posts/comment-obtenir-votre-permis-de-conduire-a-madagascar · https://www.fiarakodia.com/en/posts/passer-son-permis-a-madagascar · https://www.vol-direct.net/comment-passer-son-code-de-la-route-a-madagascar.html · https://www.madagascar-tribune.com/Etat-ATT-la-repartition-des-roles,15123.html · https://fr.wikipedia.org/wiki/R%C3%A9gion_de_Madagascar · https://www.assemblee-nationale.mg/wp-content/uploads/2020/10/Loi-n%C2%B02017-002_Code-de-la-route-%C3%A0-Madagascar.pdf · http://www.hcc.gov.mg/?p=2952

Partie B : https://developer.android.com/build/releases/agp-9-3-0-release-notes · https://developer.android.com/build/releases/agp-9-0-0-release-notes · https://developer.android.com/build/releases/about-agp · https://developer.android.com/build/migrate-to-built-in-kotlin · https://blog.jetbrains.com/kotlin/2026/01/update-your-projects-for-agp9/ · https://kotlinlang.org/docs/releases.html · https://kotlinlang.org/docs/gradle-configure-project.html · https://github.com/google/ksp/releases · https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler · https://developer.android.com/develop/ui/compose/bom/bom-mapping · https://android-developers.googleblog.com/2026/08/jetpack-compose-august-2026-release.html · https://developer.android.com/jetpack/androidx/releases/lifecycle · https://developer.android.com/jetpack/androidx/releases/navigation · https://developer.android.com/jetpack/androidx/releases/navigation3 · https://developer.android.com/jetpack/androidx/releases/room · https://developer.android.com/jetpack/androidx/releases/room3 · https://android-developers.googleblog.com/2026/03/room-30-modernizing-room.html · https://github.com/Kotlin/kotlinx.coroutines/releases · https://developer.android.com/jetpack/androidx/releases/datastore · https://developer.android.com/jetpack/androidx/releases/work · https://docs.gradle.org/current/release-notes.html · https://developer.android.com/about/versions/17/setup-sdk · https://developer.android.com/topic/architecture · https://developer.android.com/topic/architecture/ui-layer · https://developer.android.com/training/dependency-injection/manual · https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md · https://developer.android.com/training/data-storage/room/defining-data · https://developer.android.com/training/data-storage/room/relationships · https://developer.android.com/training/data-storage/room/accessing-data · https://developer.android.com/training/data-storage/room/migrating-db-versions · https://developer.android.com/training/data-storage/room/prepopulate · https://developer.android.com/training/data-storage/room/testing-db · https://developer.android.com/training/printing · https://developer.android.com/training/printing/html-docs · https://developer.android.com/training/printing/custom-docs · https://developer.android.com/training/sharing/send · https://www.danielhugenroth.com/posts/2021_06_password_hashing_on_android/ · https://github.com/lambdapioneer/argon2kt · https://developer.android.com/kotlin/coroutines/test · https://developer.android.com/develop/ui/compose/testing · https://github.com/mannodermaus/android-junit-framework/issues/404 · https://www.droidcon.com/2025/12/16/goodbye-encryptedsharedpreferences-a-2026-migration-guide/ · https://developer.android.com/topic/architecture/data-layer/offline-first
