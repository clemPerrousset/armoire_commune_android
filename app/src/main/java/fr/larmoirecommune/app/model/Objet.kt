package fr.larmoirecommune.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Objet(
    val id: Int? = null,
    val nom: String,
    val description: String,
    val image: String? = null,
    val quantite: Int = 1,
    val disponibilite_globale: Boolean = true,
    val tag_id: Int? = null,
    val tag: Tag? = null,
    val consommables: List<Consommable> = emptyList()
)
