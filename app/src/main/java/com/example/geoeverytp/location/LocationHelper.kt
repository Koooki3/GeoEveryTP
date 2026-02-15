package com.example.geoeverytp.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Fetches current location via [FusedLocationProviderClient] with high accuracy.
 * Uses last-known location as fast path when fresh, then falls back to [getCurrentLocation] when null or stale.
 * Caller must hold [android.Manifest.permission.ACCESS_FINE_LOCATION]; otherwise returns null or throws.
 *
 * @param context Application or activity context (used for [LocationServices.getFusedLocationProviderClient]).
 */
class LocationHelper(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Returns a high-accuracy location: first tries last-known location if within [MAX_UPDATE_AGE_MS],
     * otherwise requests a fresh fix via [FusedLocationProviderClient.getCurrentLocation].
     * @return Current [Location] or null on failure / no permission.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(MAX_UPDATE_AGE_MS)
            .build()

        fun requestFresh() {
            fusedClient.getCurrentLocation(request, null)
                .addOnSuccessListener { location -> cont.resume(location) }
                .addOnFailureListener { cont.resume(null) }
        }

        fusedClient.lastLocation
            .addOnSuccessListener { last ->
                if (last != null && (System.currentTimeMillis() - last.time) <= MAX_UPDATE_AGE_MS) {
                    cont.resume(last)
                } else {
                    requestFresh()
                }
            }
            .addOnFailureListener { requestFresh() }
    }

    private companion object {
        const val MAX_UPDATE_AGE_MS = 60_000L
    }
}
