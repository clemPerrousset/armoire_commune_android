package fr.larmoirecommune.app.repository

import fr.larmoirecommune.app.model.Objet
import fr.larmoirecommune.app.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post

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
