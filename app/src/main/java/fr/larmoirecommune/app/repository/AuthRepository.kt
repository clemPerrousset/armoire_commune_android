package fr.larmoirecommune.app.repository

import android.util.Log
import fr.larmoirecommune.app.model.AuthResponse
import fr.larmoirecommune.app.model.SignupRequest
import fr.larmoirecommune.app.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.submitForm
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType

class AuthRepository {

    suspend fun login(email: String, password: String): Boolean {
        return try {
            // Utilise le client sans Auth — le plugin bearer de Ktor 3.x interfère
            // avec les requêtes qui n'ont pas encore de token
            val response: AuthResponse = ApiClient.unauthenticatedClient.submitForm(
                url = ApiClient.getUrl("/auth/login"),
                formParameters = Parameters.build {
                    append("username", email)
                    append("password", password)
                }
            ).body()

            ApiClient.setTokenAndParse(response.accessToken)
            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed: ${e::class.simpleName} — ${e.message}", e)
            false
        }
    }

    suspend fun signup(
        nom: String,
        prenom: String,
        email: String,
        password: String,
        associationId: Int
    ): Boolean {
        return try {
            val request = SignupRequest(
                nom = nom,
                prenom = prenom,
                email = email,
                password = password,
                associationId = associationId
            )
            ApiClient.unauthenticatedClient.post(ApiClient.getUrl("/auth/signup")) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Signup failed: ${e::class.simpleName} — ${e.message}", e)
            false
        }
    }
}
