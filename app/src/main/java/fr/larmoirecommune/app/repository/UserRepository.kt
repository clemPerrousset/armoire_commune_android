package fr.larmoirecommune.app.repository

import fr.larmoirecommune.app.model.AppNotification
import fr.larmoirecommune.app.model.Objet
import fr.larmoirecommune.app.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import fr.larmoirecommune.app.model.PasswordChangeRequest

class UserRepository {

    suspend fun getHistorique(): List<Objet> {
        return try {
            ApiClient.client.get(ApiClient.getUrl("/users/historique")).body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getFavoris(): List<Objet> {
        return try {
            ApiClient.client.get(ApiClient.getUrl("/users/favoris")).body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getNotifications(): List<AppNotification> {
        return try {
            ApiClient.client.get(ApiClient.getUrl("/users/notifications")).body()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun markNotificationRead(id: Int) {
        try {
            ApiClient.client.post(ApiClient.getUrl("/users/notifications/$id/lu"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Retourne null si le changement a réussi, sinon le message d'erreur à afficher. */
    suspend fun changePassword(current: String, new: String): String? {
        return try {
            val response = ApiClient.client.put(ApiClient.getUrl("/users/me/password")) {
                contentType(ContentType.Application.Json)
                setBody(PasswordChangeRequest(current, new))
            }
            if (response.status.isSuccess()) null
            else Regex("\"detail\"\\s*:\\s*\"([^\"]+)\"").find(response.bodyAsText())?.groupValues?.get(1)
                ?: "Échec du changement de mot de passe"
        } catch (e: Exception) {
            e.printStackTrace()
            "Impossible de joindre le serveur"
        }
    }

    suspend fun addFavori(objetId: Int): Boolean {
        return try {
            ApiClient.client.post(ApiClient.getUrl("/users/favoris/$objetId"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun removeFavori(objetId: Int): Boolean {
        return try {
            ApiClient.client.delete(ApiClient.getUrl("/users/favoris/$objetId"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
