package com.example.geoeverytp.pressure

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Reads current barometric pressure (hPa) from device sensor.
 * Returns null if no pressure sensor. Must run on Main for [SensorManager.registerListener].
 */
object PressureHelper {

    /** Returns pressure in hPa, or null if sensor unavailable. */
    suspend fun getCurrentPressureHpa(context: Context): Float? = withContext(Dispatchers.Main.immediate) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return@withContext null
        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) ?: return@withContext null
        suspendCancellableCoroutine { cont ->
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type == Sensor.TYPE_PRESSURE) {
                        sensorManager.unregisterListener(this)
                        cont.resume(event.values[0])
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) { /* not used */ }
            }
            sensorManager.registerListener(listener, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)
            cont.invokeOnCancellation { sensorManager.unregisterListener(listener) }
        }
    }
}
