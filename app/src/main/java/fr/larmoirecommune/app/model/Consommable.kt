package fr.larmoirecommune.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Consommable(
    val id: Int? = null,
    val nom: String,
    val description: String? = null,
    val quantite: Int = 0,
    val prix: Double
)
