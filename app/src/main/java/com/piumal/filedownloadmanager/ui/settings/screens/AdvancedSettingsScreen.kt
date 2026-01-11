package com.piumal.filedownloadmanager.ui.settings.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.piumal.filedownloadmanager.ui.settings.SettingsUiState
import com.piumal.filedownloadmanager.ui.settings.SettingsViewModel
import com.piumal.filedownloadmanager.ui.settings.components.SettingItem
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme

/**
 * AdvancedSettingsScreen
 *
 * Detail screen for Advanced Settings options.
 * Contains advanced configuration settings.
 */
@Composable
fun AdvancedSettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AdvancedSettingsContent(
        uiState = uiState
    )
}

@Composable
fun AdvancedSettingsContent(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        SettingItem(
            title = "Max retry count",
            subtitle = "${uiState.maxRetryCount} times",
            hasChevron = true,
            onClick = { /* Open retry count picker */ }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AdvancedSettingsScreenPreview() {
    FileDownloadManagerTheme {
        AdvancedSettingsContent(
            uiState = SettingsUiState()
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AdvancedSettingsScreenDarkPreview() {
    FileDownloadManagerTheme {
        AdvancedSettingsContent(
            uiState = SettingsUiState(darkMode = true)
        )
    }
}

