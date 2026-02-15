package com.example.geoeverytp.elevation

import android.util.LruCache
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Fetches ground elevation (m) by coordinates from Open-Elevation API (DEM data).
 * No API key; free tier with rate limits. Results are cached by grid (~0.001° ≈ 100 m) to reduce calls.
 * Cache is capped with LRU eviction to avoid unbounded memory growth.
 */
object ElevationHelper {

    private const val BASE_URL = "https://api.open-elevation.com/api/v1/lookup"
    private const val TIMEOUT_MS = 5000
    /** Grid size for cache key; same cell reuses cached elevation. */
    private const val CACHE_GRID = 0.001
    /** Max cache entries; evicts least recently used when exceeded. */
    private const val MAX_CACHE_ENTRIES = 300

    private object FailedMarker

    private val cache = object : LruCache<String, Any>(MAX_CACHE_ENTRIES) {
        override fun sizeOf(key: String, value: Any): Int = 1
    }

    /**
     * Returns ground elevation (m) at (latitude, longitude). Cached by grid; null on network error.
     */
    suspend fun getElevationFromCoordinates(latitude: Double, longitude: Double): Double? {
        val key = cacheKey(latitude, longitude)
        when (val cached = cache.get(key)) {
            null -> { /* not in cache */ }
            is Double -> return cached
            else -> return null /* cached failure (FailedMarker) */
        }
        val value = fetch(latitude, longitude)
        cache.put(key, value ?: FailedMarker)
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
