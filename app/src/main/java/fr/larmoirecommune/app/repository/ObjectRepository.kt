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
    suspend fun getObjects(available: Boolean = false): List<Objet> {
        return try {
            val url = if (available) "/objets?available=true" else "/objets"
            ApiClient.client.get(ApiClient.getUrl(url)).body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getObject(id: Int): Objet? {
        // Not in docs list but assumed standard REST or filter from list
        // Or re-fetch list. Let's assume /objets returns list, we filter locally if needed or if there is an endpoint
        // Docs don't show /objets/{id}. We might have to find it from the list.
        // For now return null or implement logic in ViewModel
        return null
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
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getMyReservations(): List<Reservation> {
        return try {
             ApiClient.client.get(ApiClient.getUrl("/reservations/me")).body()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
