package fr.larmoirecommune.app.repository


import fr.larmoirecommune.app.model.CreateLieuRequest
import fr.larmoirecommune.app.model.CreateObjectRequest
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
            val request = CreateLieuRequest(
                nom = nom,
                lat = lat,
                long = long,
                adresse = adresse
            )

            ApiClient.client.post(ApiClient.getUrl("/admin_meta/lieux")) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun createObject(nom: String, description: String, quantite: Int, tagId: Int, consommableIds: List<Int>): Boolean {
        return try {
            val request = CreateObjectRequest(
                nom = nom,
                description = description,
                quantite = quantite,
                tagId = tagId,
                consommableIds = consommableIds,
                disponibiliteGlobale = true
            )

            ApiClient.client.post(ApiClient.getUrl("/objets")) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getAllReservations(): List<Reservation> {
        return try {
            ApiClient.client.get(ApiClient.getUrl("/admin/reservations")).body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun returnObject(reservationId: Int): Boolean {
        return try {
            ApiClient.client.post(ApiClient.getUrl("/admin/reservations/$reservationId/return"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}