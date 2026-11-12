package fr.larmoirecommune.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- AUTHENTIFICATION ---

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String
)

@Serializable
data class User(
    val id: Int? = null,
    val nom: String,
    val prenom: String,
    val email: String,
    @SerialName("is_admin") val isAdmin: Boolean = false,
    @SerialName("is_point_relais") val isPointRelais: Boolean = false,
    val credits: Int = 100
)

// Statuts de réservation
object ReservationStatus {
    const val EN_PREPARATION   = "en_preparation"
    const val MIS_A_DISPOSITION = "mis_a_disposition"
    const val RETIRE           = "retire"
    const val RESTITUE         = "restitue"
    const val EN_VERIFICATION  = "en_verification"
    const val TERMINEE         = "terminee"
    const val ANNULEE          = "annulee"

    fun label(status: String) = when (status) {
        EN_PREPARATION    -> "En préparation"
        MIS_A_DISPOSITION -> "Mis à disposition"
        RETIRE            -> "Retiré"
        RESTITUE          -> "Restitué"
        EN_VERIFICATION   -> "En vérification"
        TERMINEE          -> "Terminée"
        ANNULEE           -> "Annulée"
        // Pseudo-statuts renvoyés par /objets/{id}/scan quand un objet en maintenance
        // (sans réservation active) est rescanné pour être remis en service.
        "maintenance"     -> "Maintenance"
        "disponible"      -> "Disponible"
        else              -> status
    }

    val EN_COURS = listOf(EN_PREPARATION, MIS_A_DISPOSITION, RETIRE, RESTITUE, EN_VERIFICATION)
}

@Serializable
data class ScanResult(
    @SerialName("objet_id") val objetId: Int,
    @SerialName("objet_nom") val objetNom: String,
    @SerialName("ancien_statut") val ancienStatut: String,
    @SerialName("nouveau_statut") val nouveauStatut: String,
    @SerialName("reservation_id") val reservationId: Int? = null,
    @SerialName("verification_requise") val verificationRequise: Boolean = false
)

@Serializable
data class ObjetWithReservation(
    val id: Int? = null,
    val nom: String,
    val description: String,
    val image: String? = null,
    @SerialName("disponibilite_globale") val disponibiliteGlobale: Boolean = true,
    val reservation: ReservationBrief? = null
)

@Serializable
data class ReservationBrief(
    val id: Int,
    val status: String,
    @SerialName("date_debut") val dateDebut: String,
    @SerialName("date_fin") val dateFin: String,
    @SerialName("nb_semaines") val nbSemaines: Int = 1,
    @SerialName("user_id") val userId: Int? = null
)

// --- ENTITÉS DE BASE ---

@Serializable
data class Tag(
    val id: Int? = null,
    val nom: String
)

@Serializable
data class Lieu(
    val id: Int? = null,
    val nom: String,
    val lat: Double,
    val long: Double,
    val adresse: String,
    val description: String? = null   // horaires d'ouverture
)

@Serializable
data class Consommable(
    val id: Int? = null,
    val nom: String,
    val description: String? = null,
    val quantite: Int = 0,
    val prix: Double,
    val image: String? = null,
    val url: String? = null   // lien d'achat (Yapla pour l'instant)
)

@Serializable
data class AppNotification(
    val id: Int,
    val message: String,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class CreateConsommableRequest(
    val nom: String,
    val description: String? = null,
    val prix: Double,
    val url: String? = null
)

@Serializable
data class UpdateConsommableRequest(
    val nom: String,
    val description: String,   // vide = effacer
    val prix: Double,
    val url: String            // vide = effacer
)

// --- STATISTIQUES (vue admin) ---

@Serializable
data class StatsResponse(
    val periode: String,
    @SerialName("periode_label") val periodeLabel: String,
    val debut: String,
    val fin: String,
    @SerialName("genere_le") val genereLe: String,
    val snapshot: StatsSnapshot,
    val activite: StatsActivite,
    val series: StatsSeries
)

@Serializable
data class StatutCount(val statut: String, val label: String, val nombre: Int)

@Serializable
data class NomCount(val nom: String, val reservations: Int)

@Serializable
data class StatsSnapshot(
    @SerialName("utilisateurs_inscrits") val utilisateursInscrits: Int = 0,
    @SerialName("points_relais") val pointsRelais: Int = 0,
    val administrateurs: Int = 0,
    @SerialName("objets_total") val objetsTotal: Int = 0,
    @SerialName("objets_en_maintenance") val objetsEnMaintenance: Int = 0,
    @SerialName("objets_empruntes") val objetsEmpruntes: Int = 0,
    @SerialName("objets_en_verification") val objetsEnVerification: Int = 0,
    @SerialName("objets_en_retard") val objetsEnRetard: Int = 0,
    val lieux: Int = 0,
    @SerialName("reservations_en_cours") val reservationsEnCours: Int = 0,
    @SerialName("reservations_par_statut") val reservationsParStatut: List<StatutCount> = emptyList()
)

@Serializable
data class StatsActivite(
    val reservations: Int = 0,
    @SerialName("semaines_reservees") val semainesReservees: Int = 0,
    @SerialName("credits_consommes") val creditsConsommes: Int = 0,
    val annulations: Int = 0,
    @SerialName("taux_annulation") val tauxAnnulation: Double = 0.0,
    @SerialName("objets_rendus") val objetsRendus: Int = 0,
    @SerialName("objets_distincts_reserves") val objetsDistinctsReserves: Int = 0,
    @SerialName("emprunteurs_actifs") val emprunteursActifs: Int = 0,
    @SerialName("nouveaux_inscrits") val nouveauxInscrits: Int = 0,
    @SerialName("top_objets") val topObjets: List<NomCount> = emptyList(),
    @SerialName("par_categorie") val parCategorie: List<NomCount> = emptyList(),
    @SerialName("par_lieu") val parLieu: List<NomCount> = emptyList()
)

@Serializable
data class StatsSeries(
    val labels: List<String> = emptyList(),
    val reservations: List<Int> = emptyList(),
    val retours: List<Int> = emptyList()
)

@Serializable
data class UpdateLieuRequest(
    val nom: String? = null,
    val adresse: String? = null,
    val description: String? = null
)

@Serializable
data class UpdateObjectRequest(
    val nom: String,
    val description: String,
    @SerialName("tag_ids") val tagIds: List<Int>,
    @SerialName("consommable_ids") val consommableIds: List<Int>,
    val stockage: String? = null
)

// --- OBJETS ET RÉSERVATIONS ---

@Serializable
data class Objet(
    val id: Int? = null,
    val nom: String,
    val description: String,
    val image: String? = null,
    // quantite supprimée : 1 fiche = 1 calendrier

    @SerialName("disponibilite_globale")
    val disponibiliteGlobale: Boolean = true,

    val alert: Boolean = false,

    @SerialName("tag_id")
    val tagId: Int? = null,

    val tag: Tag? = null,
    val tags: List<Tag> = emptyList(),
    val consommables: List<Consommable> = emptyList(),
    val stockage: String? = null   // renvoyé uniquement aux admins
) {
    /** Toutes les catégories (anciens serveurs : seulement `tag`). */
    val allTags: List<Tag> get() = tags.ifEmpty { listOfNotNull(tag) }
}

// Versions allégées d'Objet/Lieu telles que renvoyées par l'API dans les réservations
// (routers/reservations.py : ObjetBrief/LieuBrief). Ne PAS utiliser Objet/Lieu ici :
// leurs champs requis (description, lat, long...) sont absents de ce JSON et font
// planter la désérialisation (MissingFieldException), vidant silencieusement la liste.
@Serializable
data class ObjetBrief(
    val id: Int? = null,
    val nom: String,
    val image: String? = null
)

@Serializable
data class LieuBrief(
    val id: Int? = null,
    val nom: String,
    val adresse: String,
    val description: String? = null
)

@Serializable
data class Reservation(
    val id: Int? = null,

    @SerialName("date_debut")
    val dateDebut: String,

    @SerialName("date_fin")
    val dateFin: String,

    val status: String = "active",

    @SerialName("nb_semaines")
    val nbSemaines: Int = 1,

    @SerialName("user_id")
    val userId: Int? = null,

    @SerialName("objet_id")
    val objetId: Int? = null,

    @SerialName("lieu_id")
    val lieuId: Int? = null,

    val objet: ObjetBrief? = null,
    val lieu: LieuBrief? = null
)

// Semaine de congé admin : bloque tous les objets à la réservation.
@Serializable
data class Fermeture(
    val id: Int,
    @SerialName("date_debut") val dateDebut: String,
    @SerialName("date_fin") val dateFin: String
)

// --- REQUÊTES ---

@Serializable
data class CreateLieuRequest(
    val nom: String,
    val lat: Double,
    val long: Double,
    val adresse: String,
    val description: String? = null
)

@Serializable
data class CreateObjectRequest(
    val nom: String,
    val description: String,
    // quantite supprimée
    @SerialName("tag_ids") val tagIds: List<Int> = emptyList(),
    @SerialName("consommable_ids") val consommableIds: List<Int>,
    @SerialName("disponibilite_globale") val disponibiliteGlobale: Boolean = true,
    val stockage: String? = null
)

@Serializable
data class CreateReservationRequest(
    @SerialName("objet_id") val objetId: Int,
    @SerialName("lieu_id") val lieuId: Int,
    @SerialName("date_debut") val dateDebut: String,
    @SerialName("nb_semaines") val nbSemaines: Int = 1
)

@Serializable
data class SetFermeturesRequest(
    val semaines: List<String>
)

@Serializable
data class SignupRequest(
    val nom: String,
    val prenom: String,
    val email: String,
    @SerialName("password") val password: String,
    @SerialName("association_id") val associationId: Int
)
