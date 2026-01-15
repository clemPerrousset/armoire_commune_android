package fr.larmoirecommune.app.utils

import fr.larmoirecommune.app.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.net.URLEncoder

object GeoUtils {
    // Uses api-adresse.data.gouv.fr (France only) as requested or similar

    suspend fun searchAddress(query: String): GeoPoint? {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://api-adresse.data.gouv.fr/search/?q=$encoded&limit=1"
            val response: String = ApiClient.client.get(url).body()

            val json = JSONObject(response)
            val features = json.optJSONArray("features")
            if (features != null && features.length() > 0) {
                val coords = features.getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates")
                // GeoJSON is [lon, lat]
                GeoPoint(coords.getDouble(1), coords.getDouble(0))
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun reverseGeocode(lat: Double, lon: Double): String? {
        return try {
            val url = "https://api-adresse.data.gouv.fr/reverse/?lon=$lon&lat=$lat"
            val response: String = ApiClient.client.get(url).body()

            val json = JSONObject(response)
            val features = json.optJSONArray("features")
            if (features != null && features.length() > 0) {
                val props = features.getJSONObject(0).getJSONObject("properties")
                props.optString("label")
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}