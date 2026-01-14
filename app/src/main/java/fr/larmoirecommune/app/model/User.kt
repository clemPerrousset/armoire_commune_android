package fr.larmoirecommune.app.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int? = null,
    val nom: String,
    val prenom: String,
    val email: String,
    val is_admin: Boolean = false
)
