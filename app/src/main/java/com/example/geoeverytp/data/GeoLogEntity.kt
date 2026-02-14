package com.example.geoeverytp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room entity for a single geo log entry (saved location + optional altitudes and pressure). */
@Entity(tableName = "geo_log")
data class GeoLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val longitude: Double,
    val latitude: Double,
    /** Device/barometer altitude (m); null if unavailable. */
    val altitude: Double?,
    /** Elevation from coordinates/DEM (m); null if not fetched. */
    val altitudeFromCoordinates: Double? = null,
    /** Barometric pressure (hPa); null if no sensor. */
    val pressureHpa: Double? = null,
    val timeZoneId: String,
    val recordedAtMillis: Long,
    val note: String = ""
)
