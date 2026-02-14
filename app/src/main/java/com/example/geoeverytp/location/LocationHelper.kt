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
 * Caller must hold [android.Manifest.permission.ACCESS_FINE_LOCATION]; otherwise returns null or throws.
 *
 * @param context Application or activity context (used for [LocationServices.getFusedLocationProviderClient]).
 */
class LocationHelper(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Requests a single high-accuracy location; may use cached fix up to [MAX_UPDATE_AGE_MS].
     * @return Current [Location] or null on failure / no permission.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(MAX_UPDATE_AGE_MS)
            .build()
        fusedClient.getCurrentLocation(request, null)
            .addOnSuccessListener { location -> cont.resume(location) }
            .addOnFailureListener { cont.resume(null) }
    }

    private companion object {
        const val MAX_UPDATE_AGE_MS = 60_000L
    }
}
