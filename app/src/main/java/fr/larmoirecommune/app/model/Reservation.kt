package fr.larmoirecommune.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Reservation(
    val id: Int? = null,
    val date_debut: String,
    val date_fin: String,
    val status: String = "active",
    val user_id: Int? = null,
    val objet_id: Int? = null,
    val lieu_id: Int? = null,
    val objet: Objet? = null,
    val lieu: Lieu? = null
)
