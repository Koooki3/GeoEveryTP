package com.example.geoeverytp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.geoeverytp.R
import com.example.geoeverytp.viewmodel.MainViewModel

/** Main screen: geo info card, refresh/save/log/settings, altitude source card. */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onRequestPermission: () -> Unit,
    onNavigateToLog: () -> Unit,
    onNavigateToSettings: () -> Unit,
    padding: androidx.compose.foundation.layout.PaddingValues
) {
    val state by viewModel.geoState.collectAsState()
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveName by remember { mutableStateOf("") }
    LaunchedEffect(state.error) {
        if (state.error == "Permission denied") onRequestPermission() // Keep in sync with ViewModel
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(stringResource(R.string.save_to_log)) },
            text = {
                OutlinedTextField(
                    value = saveName,
                    onValueChange = { saveName = it },
                    label = { Text(stringResource(R.string.name_hint)) },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (saveName.isNotBlank()) {
                        viewModel.saveToLog(saveName.trim())
                        saveName = ""
                        showSaveDialog = false
                    }
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                Button(onClick = { showSaveDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.guide_title),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            stringResource(R.string.guide_message),
            style = MaterialTheme.typography.bodySmall
        )
        if (state.error != null) {
            Text(
                state.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            GeoInfoCard(state)
            Button(onClick = { viewModel.refreshLocation(onRequestPermission) }) {
                Text(stringResource(R.string.refresh))
            }
            Button(
                onClick = { showSaveDialog = state.location != null },
                enabled = state.location != null
            ) {
                Text(stringResource(R.string.save_to_log))
            }
            Button(onClick = onNavigateToLog) {
                Text(stringResource(R.string.log))
            }
            Button(onClick = onNavigateToSettings) {
                Text(stringResource(R.string.settings))
            }
            AltitudeLogicCard()
        }
    }
}

/** Card describing how the two altitude values are obtained (DEM API vs device sensor). */
@Composable
private fun AltitudeLogicCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.altitude_logic_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                stringResource(R.string.altitude_logic_coords),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                stringResource(R.string.altitude_logic_device),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
