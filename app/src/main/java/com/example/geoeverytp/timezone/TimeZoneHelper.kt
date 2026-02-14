package com.example.geoeverytp.timezone

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Provides timezone and time formatting using system default timezone.
 * Uses [java.time] for thread-safe formatting (no shared mutable state).
 */
object TimeZoneHelper {

    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /** Current timezone ID (e.g. "Asia/Shanghai"). */
    fun getCurrentTimeZoneId(): String = ZoneId.systemDefault().id

    /** Formats [timeMillis] in system default timezone. */
    fun formatTimeWithMillis(timeMillis: Long): String {
        return Instant.ofEpochMilli(timeMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }

    /** Current time in system default timezone. */
    fun formatNow(): String = formatTimeWithMillis(System.currentTimeMillis())
}
