package com.piumal.filedownloadmanager.ui.settings.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme

/**
 * SettingItem Component
 *
 * A reusable setting item row that displays:
 * - Title text on the left
 * - Toggle switch on the right (for boolean settings)
 *
 * Icons will be added later as guided.
 */
@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    title: String,
    hasSwitch: Boolean = false,
    isEnabled: Boolean = false,
    onToggle: () -> Unit = {},
    hasChevron: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Title text
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Switch for toggle settings
        if (hasSwitch) {
            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        // Chevron for navigation items (placeholder - icon to be added later)
        if (hasChevron) {
            Text(
                text = ">",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingItemWithSwitchPreview() {
    FileDownloadManagerTheme {
        SettingItem(
            title = "Notifications",
            hasSwitch = true,
            isEnabled = true,
            onToggle = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingItemWithChevronPreview() {
    FileDownloadManagerTheme {
        SettingItem(
            title = "Advanced Settings",
            hasChevron = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingItemDarkPreview() {
    FileDownloadManagerTheme {
        SettingItem(
            title = "Dark Mode",
            hasSwitch = true,
            isEnabled = true,
            onToggle = {}
        )
    }
}

