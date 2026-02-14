# GeoEveryTP

**Description:** GeoEveryTP is a lightweight Android app for viewing your current position (latitude, longitude), altitude from both coordinates (DEM) and device sensor, barometric pressure (hPa), timezone, and local time. Save snapshots to a named log and manage them locally. Supports English and Chinese with a simple black-and-white UI. Built with Kotlin and Jetpack Compose for Android 16+.

---

## Features

- **Location** — Latitude and longitude with high precision (manual refresh).
- **Altitude (two sources)**  
  - **From coordinates** — Ground elevation (m) from [Open-Elevation](https://www.open-elevation.com/) API using global DEM data. Requires network.  
  - **From device** — Altitude (m) from device barometer/GPS when available; shows N/A on devices without a pressure sensor.
- **Barometric pressure** — Raw pressure in hPa from the device sensor (when available).
- **Timezone & local time** — Current timezone ID and formatted local time.
- **Log** — Save the current geo snapshot with a name; view list, open details, delete. Stored locally with Room.
- **Language** — Follow system, Chinese, or English (in-app setting).
- **UI** — Simple black-and-white theme; on-screen note explaining how the two altitudes are obtained.

## Requirements

- **Android 16 (API 36)** or later (e.g. Redmi K90 with HyperOS 3).
- Location permission (fine).
- Network permission (for elevation-by-coordinates only).

## Tech Stack

- **Kotlin 2.0** + **Jetpack Compose** (Material 3)
- **Google Play Services Location** (FusedLocationProvider) for GPS
- **Room** for local log storage
- **Open-Elevation API** for elevation-by-coordinates (no API key)
- **java.time** for thread-safe time formatting
- **KSP** for Room code generation

## Build & Run

1. Clone the repo:
   ```bash
   git clone https://github.com/Koooki3/GeoEveryTP.git
   cd GeoEveryTP
   ```

2. Open the project in **Android Studio** (recommended) or use the command line:
   ```bash
   ./gradlew assembleDebug
   ```
   Install the APK from `app/build/outputs/apk/debug/` or run on a device/emulator from Android Studio.

3. **Release build** (signed):  
   Use **Build → Generate Signed Bundle / APK** in Android Studio, then install the generated APK (e.g. on Redmi K90 via ADB or file transfer).

## Usage

1. Grant **location** permission when prompted.
2. Tap **Refresh** to get the current position and update all values.
3. **Altitude from coordinates** may appear shortly after (network); **Altitude from device** and **Pressure** show N/A if the device has no barometer.
4. Use **Save to log** to store the current snapshot with a name; open **Log** to see, open, or delete entries.
5. **Settings** → choose **Language** (Follow system / 中文 / English). The app restarts to apply.

## Project Structure (high level)

```
app/src/main/java/com/example/geoeverytp/
├── GeoApp.kt              # Application; applies saved language
├── MainActivity.kt        # Single activity; nav (Home / Log / Log detail / Settings)
├── data/                  # Room entity, DAO, database, repository
├── elevation/             # Open-Elevation API client (cached by grid)
├── location/              # FusedLocationProvider wrapper
├── pressure/             # Barometric pressure from SensorManager
├── timezone/              # Timezone and time formatting (java.time)
├── viewmodel/             # MainViewModel (state, refresh, log CRUD)
└── ui/                    # Compose screens and theme (black/white)
```

## License

Licensed under the [MIT License](https://opensource.org/licenses/MIT). See the [LICENSE](LICENSE) file for details.

## Version

**1.0** — Initial release: location, dual altitude, pressure, timezone/time, log, multi-language, and altitude source note.
