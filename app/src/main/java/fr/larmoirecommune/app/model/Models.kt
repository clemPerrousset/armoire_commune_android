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
    val prix: Double
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
    val consommables: List<Consommable> = emptyList()
)

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
    @SerialName("tag_id") val tagId: Int? = null,
    @SerialName("consommable_ids") val consommableIds: List<Int>,
    @SerialName("disponibilite_globale") val disponibiliteGlobale: Boolean = true
)

@Serializable
data class CreateReservationRequest(
    @SerialName("objet_id") val objetId: Int,
    @SerialName("lieu_id") val lieuId: Int,
    @SerialName("date_debut") val dateDebut: String,
    @SerialName("nb_semaines") val nbSemaines: Int = 1
)

@Serializable
data class SignupRequest(
    val nom: String,
    val prenom: String,
    val email: String,
    @SerialName("password") val password: String,
    @SerialName("association_id") val associationId: Int
)
