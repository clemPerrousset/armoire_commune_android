package fr.larmoirecommune.app.network

import android.util.Base64
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.json.JSONObject

object ApiClient {
    private const val BASE_URL = "http://51.83.99.103/"

    var token: String? = null
    var currentUserIsAdmin: Boolean = false
    var currentUserEmail: String? = null

    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }

        install(Logging) {
            level = LogLevel.ALL
        }

        install(Auth) {
            bearer {
                loadTokens {
                    token?.let { BearerTokens(it, "") }
                }
                refreshTokens {
                    token?.let { BearerTokens(it, "") }
                }
            }
        }
    }

    fun getUrl(path: String): String {
        val cleanPath = if (path.startsWith("/")) path.substring(1) else path
        return "$BASE_URL$cleanPath"
    }

    // VOICI LA FONCTION RESTAURÉE
    // Elle décode le token immédiatement (utile pour l'affichage rapide)
    // Mais l'appel /users/me dans MainActivity viendra confirmer/écraser ces valeurs ensuite (sécurité)
    fun setTokenAndParse(newToken: String) {
        token = newToken
        try {
            // Le token JWT est en 3 parties séparées par des points
            val parts = newToken.split(".")
            if (parts.size == 3) {
                // On décode la partie centrale (payload)
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                val json = JSONObject(payload)

                // On lit les infos
                currentUserIsAdmin = json.optBoolean("is_admin") || json.optString("role") == "admin"
                currentUserEmail = json.optString("sub")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // En cas d'erreur de parsing, on laisse par défaut,
            // l'appel API /users/me corrigera ça plus tard.
        }
    }
}