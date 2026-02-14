package com.example.geoeverytp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.geoeverytp.R
import com.example.geoeverytp.data.GeoLogEntity
import com.example.geoeverytp.timezone.TimeZoneHelper
import com.example.geoeverytp.viewmodel.MainViewModel

/** Log detail: all fields + delete and back. */
@Composable
fun LogDetailScreen(
    entity: GeoLogEntity,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    padding: androidx.compose.foundation.layout.PaddingValues
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.log_delete_confirm)) },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteLog(entity)
                    showDeleteConfirm = false
                    onBack()
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(entity.name, style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.latitude) + ": " + formatDouble(entity.latitude),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            stringResource(R.string.longitude) + ": " + formatDouble(entity.longitude),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            stringResource(R.string.altitude_from_coordinates) + ": " + (entity.altitudeFromCoordinates?.let { formatDouble(it) } ?: "N/A"),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            stringResource(R.string.altitude_from_device) + ": " + (entity.altitude?.let { formatDouble(it) } ?: "N/A"),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            stringResource(R.string.pressure_hpa) + ": " + (entity.pressureHpa?.let { formatDouble(it) } ?: "N/A"),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            stringResource(R.string.timezone) + ": " + entity.timeZoneId,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            stringResource(R.string.recorded_at) + " " + TimeZoneHelper.formatTimeWithMillis(entity.recordedAtMillis),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
        Button(
            onClick = { showDeleteConfirm = true },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.delete))
        }
        Button(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) {
            Text(stringResource(R.string.back))
        }
    }
}

/** Formats double for display (trim trailing zeros). */
private fun formatDouble(value: Double): String {
    val s = String.format(java.util.Locale.US, "%.15f", value)
    return s.trimEnd('0').trimEnd('.')
}
