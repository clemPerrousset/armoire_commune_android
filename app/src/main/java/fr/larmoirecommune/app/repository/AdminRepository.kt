package fr.larmoirecommune.app.repository

import fr.larmoirecommune.app.model.Reservation
import fr.larmoirecommune.app.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AdminRepository {
    suspend fun createLieu(nom: String, lat: Double, long: Double, adresse: String): Boolean {
        return try {
            ApiClient.client.post(ApiClient.getUrl("/admin_meta/lieux")) {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "nom" to nom,
                    "lat" to lat,
                    "long" to long,
                    "adresse" to adresse
                ))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun createObject(nom: String, description: String, quantite: Int, tagId: Int, consommableIds: List<Int>): Boolean {
        return try {
            ApiClient.client.post(ApiClient.getUrl("/objets")) {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "nom" to nom,
                    "description" to description,
                    "quantite" to quantite,
                    "tag_id" to tagId,
                    "consommable_ids" to consommableIds,
                    "disponibilite_globale" to true
                ))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllReservations(): List<Reservation> {
        return try {
             ApiClient.client.get(ApiClient.getUrl("/admin/reservations")).body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun returnObject(reservationId: Int): Boolean {
        return try {
            ApiClient.client.post(ApiClient.getUrl("/admin/reservations/$reservationId/return"))
            true
        } catch (e: Exception) {
            false
        }
    }
}
