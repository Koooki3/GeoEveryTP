package com.example.geoeverytp.elevation

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Fetches ground elevation (m) by coordinates from Open-Elevation API (DEM data).
 * No API key; free tier with rate limits. Results are cached by grid (~0.001° ≈ 100 m) to reduce calls.
 */
object ElevationHelper {

    private const val BASE_URL = "https://api.open-elevation.com/api/v1/lookup"
    private const val TIMEOUT_MS = 5000
    /** Grid size for cache key; same cell reuses cached elevation. */
    private const val CACHE_GRID = 0.001

    private val cache = ConcurrentHashMap<String, Double?>()

    /**
     * Returns ground elevation (m) at (latitude, longitude). Cached by grid; null on network error.
     */
    suspend fun getElevationFromCoordinates(latitude: Double, longitude: Double): Double? {
        val key = cacheKey(latitude, longitude)
        cache[key]?.let { return it }
        val value = fetch(latitude, longitude)
        cache[key] = value
        return value
    }

    private fun cacheKey(lat: Double, lon: Double): String {
        val g = CACHE_GRID
        return "${(lat / g).toLong() * g}_${(lon / g).toLong() * g}"
    }

    private fun fetch(latitude: Double, longitude: Double): Double? {
        return try {
            val url = URL("$BASE_URL?locations=${formatCoord(latitude)},${formatCoord(longitude)}")
            (url.openConnection() as? HttpURLConnection)?.run {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                doInput = true
                try {
                    if (responseCode == 200) {
                        inputStream.bufferedReader().use { reader ->
                            parseElevation(reader.readText())
                        }
                    } else null
                } finally {
                    disconnect()
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun formatCoord(c: Double): String = String.format(Locale.US, "%.6f", c)

    /** Parses Open-Elevation response: {"results":[{"elevation": z}]}. */
    private fun parseElevation(json: String): Double? {
        return try {
            val root = JSONObject(json)
            val results = root.optJSONArray("results") ?: return null
            if (results.length() == 0) return null
            val first = results.getJSONObject(0)
            if (first.has("elevation")) first.getDouble("elevation") else null
        } catch (_: Exception) {
            null
        }
    }
}
