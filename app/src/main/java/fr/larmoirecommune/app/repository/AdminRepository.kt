package fr.larmoirecommune.app.repository

import fr.larmoirecommune.app.model.Consommable
import fr.larmoirecommune.app.model.CreateConsommableRequest
import fr.larmoirecommune.app.model.UpdateConsommableRequest
import fr.larmoirecommune.app.model.UpdateLieuRequest
import fr.larmoirecommune.app.model.UpdateObjectRequest
import fr.larmoirecommune.app.model.CreateLieuRequest
import fr.larmoirecommune.app.model.CreateObjectRequest
import fr.larmoirecommune.app.model.Fermeture
import fr.larmoirecommune.app.model.Objet
import fr.larmoirecommune.app.model.ObjetWithReservation
import fr.larmoirecommune.app.model.Reservation
import fr.larmoirecommune.app.model.ScanResult
import fr.larmoirecommune.app.model.SetFermeturesRequest
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

    suspend fun createObject(
        nom: String,
        description: String,
        tagIds: List<Int> = emptyList(),
        consommableIds: List<Int> = emptyList(),
        stockage: String? = null
    ): Objet? {
        return try {
            val request = CreateObjectRequest(
                nom = nom,
                description = description,
                tagIds = tagIds,
                consommableIds = consommableIds,
                disponibiliteGlobale = true,
                stockage = stockage
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

    /** Fiche complète avec token : inclut le champ `stockage`, réservé aux admins. */
    suspend fun getObjetAdmin(objetId: Int): Objet? {
        return try {
            ApiClient.client.get(ApiClient.getUrl("/objets/$objetId")).body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateObject(objetId: Int, request: UpdateObjectRequest): Objet? {
        return try {
            val response = ApiClient.client.put(ApiClient.getUrl("/admin/objets/$objetId")) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status.value in 200..299) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- LIEUX (édition) ---

    suspend fun updateLieu(lieuId: Int, request: UpdateLieuRequest): Boolean {
        return try {
            val response = ApiClient.client.put(ApiClient.getUrl("/admin_meta/lieux/$lieuId")) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- CONSOMMABLES ---

    suspend fun getConsommables(): List<Consommable> {
        return try {
            ApiClient.client.get(ApiClient.getUrl("/admin_meta/consommables")).body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createConsommable(request: CreateConsommableRequest): Consommable? {
        return try {
            val response = ApiClient.client.post(ApiClient.getUrl("/admin_meta/consommables")) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status.value in 200..299) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateConsommable(id: Int, request: UpdateConsommableRequest): Boolean {
        return try {
            val response = ApiClient.client.put(ApiClient.getUrl("/admin_meta/consommables/$id")) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteConsommable(id: Int): Boolean {
        return try {
            val response = ApiClient.client.delete(ApiClient.getUrl("/admin_meta/consommables/$id"))
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun uploadConsommableImage(consommableId: Int, imageBytes: ByteArray, mimeType: String): Boolean =
        uploadImage("/admin_meta/consommables/$consommableId/image", imageBytes, mimeType)

    suspend fun uploadObjetImage(objetId: Int, imageBytes: ByteArray, mimeType: String): Boolean =
        uploadImage("/admin/objets/$objetId/image", imageBytes, mimeType)

    private suspend fun uploadImage(path: String, imageBytes: ByteArray, mimeType: String): Boolean {
        return try {
            val ext = if (mimeType == "image/png") "png" else "jpg"
            val response = ApiClient.client.post(ApiClient.getUrl(path)) {
                setBody(MultiPartFormDataContent(formData {
                    append("file", imageBytes, Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"image.$ext\"")
                    })
                }))
            }
            response.status.value in 200..299
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
            val response = ApiClient.client.post(ApiClient.getUrl("/admin/objets/$objetId/clear-alert"))
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getMaintenanceObjects(): List<Objet> {
        return try {
            ApiClient.client.get(ApiClient.getUrl("/admin/objets/maintenance")).body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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

    suspend fun avancerReservation(reservationId: Int): Boolean {
        return try {
            val response = ApiClient.client.post(ApiClient.getUrl("/admin/reservations/$reservationId/avancer"))
            response.status.value in 200..299
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

    // --- SCAN QR ---

    suspend fun scanObjet(objetId: Int): ScanResult? {
        return try {
            ApiClient.client.post(ApiClient.getUrl("/objets/$objetId/scan")).body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- VÉRIFICATION ---

    suspend fun getVerificationObjects(): List<ObjetWithReservation> {
        return try {
            ApiClient.client.get(ApiClient.getUrl("/admin/objets/verification")).body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun validerObjet(objetId: Int): Boolean {
        return try {
            ApiClient.client.post(ApiClient.getUrl("/admin/objets/$objetId/valider"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun mettreEnMaintenance(objetId: Int): Boolean {
        return try {
            ApiClient.client.post(ApiClient.getUrl("/admin/objets/$objetId/mettre-en-maintenance"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- CONGÉS ADMIN (FERMETURES) ---

    suspend fun getFermetures(): List<Fermeture> {
        return try {
            ApiClient.client.get(ApiClient.getUrl("/fermetures")).body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Remplace l'ensemble des semaines fermées par la liste fournie (ajouts + désélections en un appel). */
    suspend fun setFermetures(semainesIso: List<String>): List<Fermeture>? {
        return try {
            ApiClient.client.put(ApiClient.getUrl("/admin/fermetures")) {
                contentType(ContentType.Application.Json)
                setBody(SetFermeturesRequest(semaines = semainesIso))
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
