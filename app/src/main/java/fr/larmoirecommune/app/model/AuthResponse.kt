package fr.larmoirecommune.app.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val access_token: String,
    val token_type: String
)
