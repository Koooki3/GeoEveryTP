package com.example.geoeverytp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.geoeverytp.R
import com.example.geoeverytp.viewmodel.GeoUiState

/** Displays current geo state: lat/lon, altitudes, pressure, timezone, local time. */
@Composable
fun GeoInfoCard(state: GeoUiState) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.latitude) + ": " + state.latitude,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                stringResource(R.string.longitude) + ": " + state.longitude,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                stringResource(R.string.altitude_from_coordinates) + ": " + (state.altitudeFromCoordinates.ifBlank { "N/A" }),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                stringResource(R.string.altitude_from_device) + ": " + (state.altitudeFromDevice.ifBlank { "N/A" }),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                stringResource(R.string.pressure_hpa) + ": " + (state.pressureHpa.ifBlank { "N/A" }),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                stringResource(R.string.timezone) + ": " + state.timeZoneId,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                stringResource(R.string.local_time) + ": " + state.timeFormatted,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
