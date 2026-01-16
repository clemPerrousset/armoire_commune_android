package fr.larmoirecommune.app.repository

import fr.larmoirecommune.app.model.Objet
import fr.larmoirecommune.app.model.Reservation
import fr.larmoirecommune.app.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ObjectRepository {
    // --- GESTION DES OBJETS ---
    private var cachedObjects: List<Objet> = emptyList()

    suspend fun getObjects(available: Boolean = false): List<Objet> {
        return try {
            val url = if (available) "/objets?available=true" else "/objets"
            val list: List<Objet> = ApiClient.client.get(ApiClient.getUrl(url)).body()
            if (!available) cachedObjects = list // On met en cache la liste complète
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getObject(id: Int): Objet? {
        cachedObjects.find { it.id == id }?.let { return it }
        // Si pas trouvé, on recharge
        getObjects(false)
        return cachedObjects.find { it.id == id }
    }

    // --- GESTION DES RÉSERVATIONS ---

    // Ajout d'un cache pour éviter de rappeler l'API quand on clique sur le détail
    private var cachedReservations: List<Reservation> = emptyList()

    suspend fun getMyReservations(): List<Reservation> {
        return try {
            val list: List<Reservation> = ApiClient.client.get(ApiClient.getUrl("/reservations/me")).body()
            cachedReservations = list // Mise à jour du cache
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Nouvelle méthode pour le détail (similaire à getObject)
    suspend fun getReservation(id: Int): Reservation? {
        // 1. On cherche dans le cache actuel
        cachedReservations.find { it.id == id }?.let { return it }

        // 2. Si pas trouvé, on recharge la liste depuis l'API
        getMyReservations()

        // 3. On re-cherche
        return cachedReservations.find { it.id == id }
    }

    suspend fun createReservation(objetId: Int, lieuId: Int, dateDebut: String): Boolean {
        return try {
            ApiClient.client.post(ApiClient.getUrl("/reservations")) {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "objet_id" to objetId,
                    "lieu_id" to lieuId,
                    "date_debut" to dateDebut
                ))
            }
            // On vide le cache car une nouvelle réservation existe peut-être
            cachedReservations = emptyList()
            true
        } catch (e: Exception) {
            false
        }
    }
}