package fr.larmoirecommune.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Lieu(
    val id: Int? = null,
    val nom: String,
    val lat: Double,
    val long: Double,
    val adresse: String
)
