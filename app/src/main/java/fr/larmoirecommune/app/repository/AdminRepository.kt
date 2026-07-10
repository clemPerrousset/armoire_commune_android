package fr.larmoirecommune.app.repository

import fr.larmoirecommune.app.model.CreateLieuRequest
import fr.larmoirecommune.app.model.CreateObjectRequest
import fr.larmoirecommune.app.model.Objet
import fr.larmoirecommune.app.model.Reservation
import fr.larmoirecommune.app.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import fr.larmoirecommune.app.model.Tag

class AdminRepository {

    // --- LIEUX ---

    suspend fun createLieu(nom: String, lat: Double, long: Double, adresse: String, description: String? = null): Boolean {
        return try {
            val request = CreateLieuRequest(nom = nom, lat = lat, long = long, adresse = adresse, description = description)
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

    suspend fun deleteLieu(lieuId: Int): Boolean {
        return try {
            val response = ApiClient.client.delete(ApiClient.getUrl("/admin_meta/lieux/$lieuId"))
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- OBJETS ---

    suspend fun createObject(nom: String, description: String, tagId: Int? = null, consommableIds: List<Int> = emptyList()): Objet? {
        return try {
            val request = CreateObjectRequest(
                nom = nom,
                description = description,
                tagId = tagId,
                consommableIds = consommableIds,
                disponibiliteGlobale = true
            )
            ApiClient.client.post(ApiClient.getUrl("/objets")) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun uploadObjetImage(objetId: Int, imageBytes: ByteArray, mimeType: String): Boolean {
        return try {
            val ext = if (mimeType == "image/png") "png" else "jpg"
            ApiClient.client.post(ApiClient.getUrl("/admin/objets/$objetId/image")) {
                setBody(MultiPartFormDataContent(formData {
                    append("file", imageBytes, Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"image.$ext\"")
                    })
                }))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteObjet(objetId: Int): Boolean {
        return try {
            val response = ApiClient.client.delete(ApiClient.getUrl("/admin/objets/$objetId"))
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun setObjetAvailability(objetId: Int, available: Boolean): Boolean {
        return try {
            val response = ApiClient.client.put(ApiClient.getUrl("/admin/objets/$objetId/available?available=$available"))
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getAlertObjects(): List<Objet> {
        return try {
            ApiClient.client.get(ApiClient.getUrl("/admin/objets/alerts")).body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun clearAlert(objetId: Int): Boolean {
        return try {
            ApiClient.client.post(ApiClient.getUrl("/admin/objets/$objetId/clear-alert"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- TAGS ---

    suspend fun createTag(nom: String): Boolean {
        return try {
            val request = mapOf("nom" to nom)
            ApiClient.client.post(ApiClient.getUrl("/admin_meta/tags")) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteTag(tagId: Int): Boolean {
        return try {
            val response = ApiClient.client.delete(ApiClient.getUrl("/admin_meta/tags/$tagId"))
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- RÉSERVATIONS ---

    suspend fun getAllReservations(status: String? = null): List<Reservation> {
        return try {
            val query = if (status != null) "?status=$status" else ""
            ApiClient.client.get(ApiClient.getUrl("/admin/reservations$query")).body()
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
