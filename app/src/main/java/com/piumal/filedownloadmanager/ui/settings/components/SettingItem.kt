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
 * - Optional subtitle/value text
 */
@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Title and subtitle column
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

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

        // Chevron for navigation items
        if (hasChevron) {
            Text(
                text = ">",
                style = MaterialTheme.typography.bodySmall,
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

