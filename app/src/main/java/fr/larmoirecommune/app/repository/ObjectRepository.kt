package fr.larmoirecommune.app.repository

import android.util.Log
import fr.larmoirecommune.app.model.CreateReservationRequest
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

    private var cachedObjects: List<Objet> = emptyList()
    private var cachedReservations: List<Reservation> = emptyList()

    // --- LIEUX & TAGS ---

    suspend fun getLieux(): List<fr.larmoirecommune.app.model.Lieu> {
        return try {
            ApiClient.client.get(ApiClient.getUrl("/admin_meta/lieux")).body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getTags(): List<fr.larmoirecommune.app.model.Tag> {
        return try {
            ApiClient.client.get(ApiClient.getUrl("/admin_meta/tags")).body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // --- OBJETS ---

    suspend fun getObjects(available: Boolean = false, nom: String? = null, tagId: Int? = null): List<Objet> {
        return try {
            val params = mutableListOf<String>()
            if (available) params.add("available=true")
            if (!nom.isNullOrBlank()) params.add("nom=${nom.trim()}")
            if (tagId != null) params.add("tag_id=$tagId")

            val query = if (params.isNotEmpty()) "?${params.joinToString("&")}" else ""
            val list: List<Objet> = ApiClient.client.get(ApiClient.getUrl("/objets$query")).body()

            if (!available && nom.isNullOrBlank() && tagId == null) cachedObjects = list
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getObject(id: Int): Objet? {
        return try {
            ApiClient.client.get(ApiClient.getUrl("/objets/$id")).body()
        } catch (e: Exception) {
            Log.e("ObjectRepository", "getObject($id) failed: ${e::class.simpleName} — ${e.message}", e)
            cachedObjects.find { it.id == id }
        }
    }

    // --- RÉSERVATIONS ---

    suspend fun getMyReservations(): List<Reservation> {
        return try {
            val list: List<Reservation> = ApiClient.client.get(ApiClient.getUrl("/reservations/me")).body()
            cachedReservations = list
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Réservations terminées de l'utilisateur — pour l'onglet Historique */
    suspend fun getReservationHistorique(): List<Reservation> {
        return try {
            ApiClient.client.get(ApiClient.getUrl("/reservations/historique")).body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Réservations actives/en_cours sur un objet — pour griser le calendrier */
    suspend fun getReservationsForObjet(objetId: Int): List<Reservation> {
        return try {
            ApiClient.client.get(ApiClient.getUrl("/reservations/objet/$objetId")).body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getReservation(id: Int): Reservation? {
        cachedReservations.find { it.id == id }?.let { return it }
        getMyReservations()
        return cachedReservations.find { it.id == id }
    }

    suspend fun createReservation(objetId: Int, lieuId: Int, dateDebut: String, nbSemaines: Int = 1): Boolean {
        return try {
            val request = CreateReservationRequest(
                objetId = objetId,
                lieuId = lieuId,
                dateDebut = dateDebut,
                nbSemaines = nbSemaines
            )
            ApiClient.client.post(ApiClient.getUrl("/reservations")) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            cachedReservations = emptyList()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun cancelReservation(reservationId: Int): Boolean {
        return try {
            val response = ApiClient.client.post(ApiClient.getUrl("/reservations/$reservationId/cancel"))
            if (response.status.value in 200..299) {
                cachedReservations = emptyList()
                true
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun retirerObjet(objectId: Int): Result<Unit> {
        return try {
            val response = ApiClient.client.post(ApiClient.getUrl("/objets/$objectId/retirer"))
            if (response.status.value in 200..299) Result.success(Unit)
            else Result.failure(Exception("Erreur retrait: ${response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun retournerObjet(objectId: Int, lieuId: Int): Result<Unit> {
        return try {
            val response = ApiClient.client.post(ApiClient.getUrl("/objets/$objectId/retourner?lieu_id=$lieuId"))
            if (response.status.value in 200..299) Result.success(Unit)
            else Result.failure(Exception("Erreur retour: ${response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
