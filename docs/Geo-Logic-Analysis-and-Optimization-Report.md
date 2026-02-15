# GeoEveryTP — Geographic Information Logic Analysis and Optimization Report

## 1. Project Implementation Overview

GeoEveryTP is an Android geographic-information app with the following main features:

- **Location** — Latitude and longitude (high accuracy, manual refresh).
- **Altitude (two sources)** — Elevation by coordinates via DEM (Open-Elevation API), and from device sensor/GPS.
- **Pressure** — Device barometric pressure sensor (hPa).
- **Timezone and local time** — System default timezone with `java.time` formatting.
- **Log** — Room-backed local storage: save, list, detail, and delete.

Tech stack: Kotlin, Jetpack Compose, FusedLocationProvider, Room, Open-Elevation API, SensorManager.

---

## 2. Geographic Information Acquisition and Calculation Logic

### 2.1 Location

| Item | Description |
|------|-------------|
| **Implementation** | `LocationHelper.kt` |
| **Method** | `FusedLocationProviderClient.getCurrentLocation()` |
| **Parameters** | `Priority.PRIORITY_HIGH_ACCURACY`, `MAX_UPDATE_AGE_MS = 60_000` (accepts cache up to 60 seconds) |
| **Threading** | Invoked via `Dispatchers.IO` in the ViewModel |
| **Permission** | Caller must hold `ACCESS_FINE_LOCATION` |
| **Failure** | When result is `null`, main screen shows "Location unavailable" |

**Summary:** Only the “current location” path is used; there is no “last known location” fast path. When there is no cache or when positioning is slow, the user either waits or sees a direct failure.

---

### 2.2 Elevation (by coordinates, DEM)

| Item | Description |
|------|-------------|
| **Implementation** | `ElevationHelper.kt` |
| **Data source** | Open-Elevation API: `https://api.open-elevation.com/api/v1/lookup?locations=lat,lon` |
| **Request** | HTTP GET, `connectTimeout` / `readTimeout` = 5000 ms |
| **Parsing** | JSON `results[0].elevation` (metres) |
| **Cache** | Keyed by grid: `CACHE_GRID = 0.001` (~100 m grid); `ConcurrentHashMap<String, Double?>` with **no capacity limit or eviction** |
| **When called** | After `MainViewModel.refreshLocation()` obtains a location, async `fetchElevationFromCoordinates(lat, lon)` |

**Summary:** Depends on network; cache key is rounded by grid so coordinates in the same cell are reused; cache is never evicted, so long-running use can increase memory usage.

---

### 2.3 Elevation (from device)

| Item | Description |
|------|-------------|
| **Implementation** | `formatAltitude(Location)` in `MainViewModel` |
| **Data source** | `Location.altitude` when `Location.hasAltitude() == true` |
| **Unit** | Metres; shows "N/A" when no sensor or no altitude |

**Summary:** Purely from device/system; no extra computation.

---

### 2.4 Pressure

| Item | Description |
|------|-------------|
| **Implementation** | `PressureHelper.kt` |
| **Data source** | `SensorManager`, `Sensor.TYPE_PRESSURE` |
| **Sampling** | `SENSOR_DELAY_NORMAL`; **first** `onSensorChanged` is used then `unregisterListener` (one-shot read) |
| **Threading** | `Dispatchers.Main.immediate` (listener must be registered on main thread) |
| **Unit** | hPa; returns `null` when no sensor, UI shows "N/A" |

**Summary:** A single reading is enough for “current pressure” display; NORMAL delay is ~200 ms+ per sample, slightly slower for the first read but acceptable.

---

### 2.5 Timezone and time

| Item | Description |
|------|-------------|
| **Implementation** | `TimeZoneHelper.kt` |
| **Timezone** | `ZoneId.systemDefault().id` (system’s current timezone; **not** derived from coordinates) |
| **Time** | `Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault()).format(...)` |
| **Format** | `yyyy-MM-dd HH:mm:ss`, follows system locale |

**Summary:** Displays the device’s timezone and local time; no timezone lookup from current latitude/longitude (e.g. Google Time Zone API or offline TZ data).

---

## 3. Optimization Options (Literature and Best Practices)

### 3.1 Location

- **Official guidance (Android docs):** Try `getLastLocation()` first to use cached location; if it is null or stale, then use `getCurrentLocation()`. This allows immediate display when cache exists and avoids long waits or direct failure.
- **Practice:** `getLastLocation()` can return null when the device has never located, location is off, or Play services restarted; falling back to `getCurrentLocation()` is a common strategy.
- **Optimization:** Add a “last then current” flow in `LocationHelper`; treat last known location as valid only up to a max age (e.g. same 60 s as today), and request a fresh fix when older.

### 3.2 Elevation (DEM) cache

- **Issue:** `ConcurrentHashMap` has no upper bound; long-running use or many refreshes in different places can grow the cache without limit.
- **Practice:** Android recommends `LruCache` for bounded caches (by entry count or size) to avoid unbounded memory growth.
- **Optimization:** Replace `ConcurrentHashMap` with an `LruCache` (e.g. max 200–500 entries), evicting least recently used when full.

### 3.3 Elevation (DEM) network and API

- Open-Elevation is an open-source service; the public API has no strict documented monthly limit, but heavy use can still be limited by the server or network.
- The app already uses a 5 s timeout and grid-based caching; optional improvements include limited retry on failure (e.g. once) or leaving behaviour as-is and only optimizing cache capacity.

### 3.4 Timezone and coordinates

- To show “timezone for current coordinates” (instead of device system timezone), an extra data source is needed: e.g. Google Time Zone API (API key and network) or offline timezone boundaries (e.g. TZ shapefiles + point-in-polygon).
- The app is currently oriented to “device’s system time at current place”; coordinate→timezone is out of scope for this round and is only noted for future work.

### 3.5 Pressure sensor

- A single barometric reading is sufficient; the difference between `SENSOR_DELAY_NORMAL` and `SENSOR_DELAY_UI` for first-read latency is small (on the order of tens of ms), so no change was made.

---

## 4. Feasibility and Rationale

### 4.1 Location: getLastLocation fast path + getCurrentLocation fallback

| Aspect | Conclusion |
|--------|------------|
| **Feasibility** | High. `FusedLocationProviderClient` already provides `lastLocation` (getLastLocation); it can be combined with `getCurrentLocation()` in a “last then current” flow. |
| **Rationale** | High. Aligns with official guidance, keeps “high accuracy first” semantics, and only requests a new fix when cache is missing or too old; better UX with no extra permissions or dependencies. |
| **Decision** | **Approved; implemented.** |

### 4.2 Elevation cache: ConcurrentHashMap → LruCache

| Aspect | Conclusion |
|--------|------------|
| **Feasibility** | High. Android provides `android.util.LruCache` (or `androidx.collection.LruCache`); only the cache container and get/put logic need to change. |
| **Rationale** | High. Keeps “cache by grid” semantics, adds a cap and LRU eviction to avoid memory growth over time; impact on hit rate is bounded (e.g. 300 entries cover 300 grid cells). |
| **Decision** | **Approved; implemented.** |

### 4.3 Other items (timezone from coordinates, pressure sampling rate, elevation retry)

- **Timezone from coordinates:** Requires an API key or large offline dataset and does not match the current “system timezone” product scope; not implemented in this round.
- **Pressure:** Current implementation is considered adequate; no change.
- **Elevation retry:** Can be added in a later iteration if needed; not included here.

---

## 5. Implemented Optimizations (Summary)

The following two changes were implemented after passing both feasibility and rationale:

1. **LocationHelper**  
   - Call `getLastLocation()` first; if the result is non-null and within `MAX_UPDATE_AGE_MS`, return it immediately.  
   - Otherwise call `getCurrentLocation()`.  
   - Behaviour remains compatible with the previous single `getCurrentLocation()` call; only the fast path and fallback were added.

2. **ElevationHelper**  
   - Replace `ConcurrentHashMap<String, Double?>` with an `LruCache`-based cache.  
   - Set a maximum entry count (e.g. 300) with LRU eviction when exceeded.  
   - Grid key and request/parse logic are unchanged; only the cache implementation was updated.

See the corresponding source files and comments for the exact code changes.
