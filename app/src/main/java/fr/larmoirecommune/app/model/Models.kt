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
    @SerialName("is_admin") val isAdmin: Boolean = false
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
    val adresse: String
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
    val quantite: Int = 1,

    @SerialName("disponibilite_globale")
    val disponibiliteGlobale: Boolean = true,

    @SerialName("tag_id")
    val tagId: Int? = null,

    val tag: Tag? = null,
    val consommables: List<Consommable> = emptyList()
)

@Serializable
data class Reservation(
    val id: Int? = null,

    @SerialName("date_debut")
    val dateDebut: String,

    @SerialName("date_fin")
    val dateFin: String,

    val status: String = "active",

    @SerialName("user_id")
    val userId: Int? = null,

    @SerialName("objet_id")
    val objetId: Int? = null,

    @SerialName("lieu_id")
    val lieuId: Int? = null,

    val objet: Objet? = null,
    val lieu: Lieu? = null
)
