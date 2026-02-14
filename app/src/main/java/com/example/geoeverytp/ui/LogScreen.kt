package com.example.geoeverytp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.geoeverytp.R
import com.example.geoeverytp.data.GeoLogEntity
import com.example.geoeverytp.timezone.TimeZoneHelper
import com.example.geoeverytp.viewmodel.MainViewModel

/** Log list screen: back button, empty state or [LazyColumn] of [LogItem]. */
@Composable
fun LogScreen(
    viewModel: MainViewModel,
    padding: PaddingValues,
    onLogClick: (GeoLogEntity) -> Unit,
    onBack: () -> Unit
) {
    val logs by viewModel.logList.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            androidx.compose.material3.TextButton(onClick = onBack) {
                Text(stringResource(R.string.back))
            }
        }
        if (logs.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.log_empty),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(logs, key = { it.id }) { item ->
                    LogItem(
                        entity = item,
                        onClick = { onLogClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LogItem(
    entity: GeoLogEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(
            entity.name,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            "${entity.latitude}, ${entity.longitude} • ${TimeZoneHelper.formatTimeWithMillis(entity.recordedAtMillis)}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
