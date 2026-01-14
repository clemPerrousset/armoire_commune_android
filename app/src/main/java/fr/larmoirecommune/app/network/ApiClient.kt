package fr.larmoirecommune.app.network

import android.util.Base64
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import org.json.JSONObject

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8000"

    var token: String? = null
    var currentUserIsAdmin: Boolean = false

    val client = HttpClient(CIO) {
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
        return "$BASE_URL$path"
    }

    fun setTokenAndParse(newToken: String) {
        token = newToken
        currentUserIsAdmin = try {
            val parts = newToken.split(".")
            if (parts.size == 3) {
                // Base64.decode depends on Android SDK, mocking/robolectric works,
                // but strictly speaking Base64 is android.util.Base64
                // We use Base64.URL_SAFE usually for JWT but DEFAULT works often if padding correct.
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE)) // JWT usually URL_SAFE
                val json = JSONObject(payload)
                json.optBoolean("is_admin") || json.optString("role") == "admin"
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
