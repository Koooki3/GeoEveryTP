package com.example.geoeverytp.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoeverytp.GeoApp
import com.example.geoeverytp.data.GeoLogEntity
import com.example.geoeverytp.data.repository.GeoLogRepository
import com.example.geoeverytp.elevation.ElevationHelper
import com.example.geoeverytp.location.LocationHelper
import com.example.geoeverytp.pressure.PressureHelper
import com.example.geoeverytp.timezone.TimeZoneHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/** UI state for the main geo info screen. */
data class GeoUiState(
    val latitude: String = "",
    val longitude: String = "",
    /** Elevation from DEM/API by coordinates (m); "N/A" when offline or failed. */
    val altitudeFromCoordinates: String = "",
    /** Elevation from device barometer/GPS (m); "N/A" when unsupported. */
    val altitudeFromDevice: String = "",
    /** Raw barometric pressure (hPa); empty when no sensor. */
    val pressureHpa: String = "",
    val timeZoneId: String = "",
    val timeFormatted: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val location: Location? = null
)

private const val ERROR_PERMISSION_DENIED = "Permission denied"
private const val ERROR_LOCATION_UNAVAILABLE = "Location unavailable"
private const val NA = "N/A"

/**
 * ViewModel for main screen: location, elevation, pressure, timezone and log CRUD.
 * All IO (location, network, DB) runs on [Dispatchers.IO]; sensor on Main.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val locationHelper = LocationHelper(application)
    private val repo = GeoLogRepository(application)

    private val _geoState = MutableStateFlow(GeoUiState())
    val geoState: StateFlow<GeoUiState> = _geoState.asStateFlow()

    private val _logList = MutableStateFlow<List<GeoLogEntity>>(emptyList())
    val logList: StateFlow<List<GeoLogEntity>> = _logList.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAll().catch { emit(emptyList()) }.collect { _logList.value = it }
        }
    }

    fun refreshLocation(onPermissionMissing: () -> Unit) {
        viewModelScope.launch {
            _geoState.value = _geoState.value.copy(isLoading = true, error = null)
            try {
                val loc = withContext(Dispatchers.IO) { locationHelper.getCurrentLocation() }
                if (loc != null) {
                    val fromDevice = formatAltitude(loc)
                    _geoState.value = _geoState.value.copy(
                        latitude = formatDouble(loc.latitude),
                        longitude = formatDouble(loc.longitude),
                        altitudeFromDevice = fromDevice,
                        timeZoneId = TimeZoneHelper.getCurrentTimeZoneId(),
                        timeFormatted = TimeZoneHelper.formatNow(),
                        location = loc,
                        isLoading = false
                    )
                    fetchElevationFromCoordinates(loc.latitude, loc.longitude)
                    fetchPressure()
                } else {
                    _geoState.value = _geoState.value.copy(
                        isLoading = false,
                        error = ERROR_LOCATION_UNAVAILABLE
                    )
                }
            } catch (e: SecurityException) {
                _geoState.value = _geoState.value.copy(isLoading = false, error = ERROR_PERMISSION_DENIED)
                onPermissionMissing()
            } catch (e: Exception) {
                _geoState.value = _geoState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun saveToLog(name: String) {
        val state = _geoState.value
        val loc = state.location ?: return
        val altDevice = if (loc.hasAltitude()) loc.altitude else null
        val altCoords = state.altitudeFromCoordinates.takeIf { it.isNotBlank() && it != NA }?.toDoubleOrNull()
        val pressure = state.pressureHpa.takeIf { it.isNotBlank() && it != NA }?.toDoubleOrNull()
        viewModelScope.launch(Dispatchers.IO) {
            repo.insert(
                GeoLogEntity(
                    name = name,
                    longitude = loc.longitude,
                    latitude = loc.latitude,
                    altitude = altDevice,
                    altitudeFromCoordinates = altCoords,
                    pressureHpa = pressure,
                    timeZoneId = TimeZoneHelper.getCurrentTimeZoneId(),
                    recordedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteLog(entity: GeoLogEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.delete(entity)
        }
    }

    fun clearError() {
        _geoState.value = _geoState.value.copy(error = null)
    }

    fun setLanguage(tag: String) {
        getApplication<Application>().getSharedPreferences(GeoApp.PREFS_NAME, 0)
            .edit()
            .putString(GeoApp.KEY_LANGUAGE, tag)
            .apply()
        (getApplication<Application>() as? GeoApp)?.applySavedLanguage()
    }

    private fun formatDouble(value: Double): String {
        val s = String.format(Locale.US, "%.15f", value)
        return s.trimEnd('0').trimEnd('.')
    }

    /** Device altitude (m); "N/A" when [Location.hasAltitude] is false. */
    private fun formatAltitude(loc: Location): String {
        if (!loc.hasAltitude()) return NA
        return formatDouble(loc.altitude)
    }

    private fun fetchElevationFromCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            val elev = withContext(Dispatchers.IO) {
                ElevationHelper.getElevationFromCoordinates(lat, lon)
            }
            _geoState.value = _geoState.value.copy(
                altitudeFromCoordinates = elev?.let { formatDouble(it) } ?: NA
            )
        }
    }

    /** Fetches current barometric pressure (hPa) from sensor; updates state with N/A if unavailable. */
    private fun fetchPressure() {
        viewModelScope.launch {
            val hpa = PressureHelper.getCurrentPressureHpa(getApplication())
            _geoState.value = _geoState.value.copy(
                pressureHpa = hpa?.let { formatFloat(it) } ?: NA
            )
        }
    }

    private fun formatFloat(value: Float): String {
        val s = String.format(Locale.US, "%.6f", value)
        return s.trimEnd('0').trimEnd('.')
    }
}
