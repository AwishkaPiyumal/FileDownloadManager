package com.piumal.filedownloadmanager.ui.settings.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme

/**
 * SettingsDivider Component
 *
 * A thin horizontal line used to visually separate setting items.
 */
@Composable
fun SettingsDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        thickness = 1.5.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsDividerPreview() {
    FileDownloadManagerTheme {
        SettingsDivider()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsDividerDarkPreview() {
    FileDownloadManagerTheme {
        SettingsDivider()
    }
}

