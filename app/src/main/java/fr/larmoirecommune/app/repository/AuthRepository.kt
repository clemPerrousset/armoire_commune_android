package fr.larmoirecommune.app.repository

import fr.larmoirecommune.app.model.AuthResponse
import fr.larmoirecommune.app.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthRepository {
    suspend fun login(email: String, password: String): Boolean {
        try {
            val response: AuthResponse = ApiClient.client.submitForm(
                url = ApiClient.getUrl("/auth/login"),
                formParameters = Parameters.build {
                    append("username", email)
                    append("password", password)
                }
            ).body()

            ApiClient.setTokenAndParse(response.access_token)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun signup(nom: String, prenom: String, email: String, password: String): Boolean {
        try {
            ApiClient.client.post(ApiClient.getUrl("/auth/signup")) {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "nom" to nom,
                    "prenom" to prenom,
                    "email" to email,
                    "password" to password
                ))
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
