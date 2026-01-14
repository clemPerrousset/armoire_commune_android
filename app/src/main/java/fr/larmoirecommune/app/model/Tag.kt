package fr.larmoirecommune.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: Int? = null,
    val nom: String
)
