package com.example.geoeverytp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.geoeverytp.GeoApp
import com.example.geoeverytp.R
import com.example.geoeverytp.viewmodel.MainViewModel

/** Settings: language options (follow system / zh / en); applies and recreates on change. */
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    padding: androidx.compose.foundation.layout.PaddingValues,
    onBack: () -> Unit,
    onLanguageChanged: () -> Unit
) {
    val prefs = LocalContext.current.getSharedPreferences(GeoApp.PREFS_NAME, 0)
    var selected by remember {
        mutableStateOf(prefs.getString(GeoApp.KEY_LANGUAGE, GeoApp.VALUE_FOLLOW_SYSTEM) ?: GeoApp.VALUE_FOLLOW_SYSTEM)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.TextButton(onClick = onBack) {
                Text(stringResource(R.string.back))
            }
        }
        Text(
            stringResource(R.string.language),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        listOf(
            GeoApp.VALUE_FOLLOW_SYSTEM to stringResource(R.string.follow_system),
            GeoApp.VALUE_LANG_ZH to stringResource(R.string.chinese),
            GeoApp.VALUE_LANG_EN to stringResource(R.string.english)
        ).forEach { (tag, label) ->
            Row(
                Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == tag,
                    onClick = {
                        selected = tag
                        viewModel.setLanguage(tag)
                        onLanguageChanged()
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
