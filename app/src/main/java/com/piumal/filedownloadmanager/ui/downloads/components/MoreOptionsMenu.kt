package com.piumal.filedownloadmanager.ui.downloads.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme

/**
 * MoreOptionsMenu Component
 *
 * A dropdown menu that appears near the more_vert icon button in the Top App Bar.
 * Displays various download management options.
 *
 * @param expanded Whether the menu is currently visible
 * @param onDismiss Callback when the menu should be dismissed (clicking outside)
 * @param onMenuItemClick Callback when a menu item is clicked, receives the menu action
 * @param modifier Modifier for styling
 */
@Composable
fun MoreOptionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onMenuItemClick: (MoreMenuAction) -> Unit,
    modifier: Modifier = Modifier
) {
    // DropdownMenu automatically positions itself relative to its parent (the IconButton)
    // It uses Surface color from MaterialTheme by default
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        // Menu Item: Select
        DropdownMenuItem(
            text = {
                Text(
                    text = "Select",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            onClick = {
                onMenuItemClick(MoreMenuAction.Select)
                onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        )

        // Menu Item: Pause All
        DropdownMenuItem(
            text = {
                Text(
                    text = "Pause All",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            onClick = {
                onMenuItemClick(MoreMenuAction.PauseAll)
                onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        )

        // Menu Item: Resume All
        DropdownMenuItem(
            text = {
                Text(
                    text = "Resume All",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            onClick = {
                onMenuItemClick(MoreMenuAction.ResumeAll)
                onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        )

        // Menu Item: Retry All
        DropdownMenuItem(
            text = {
                Text(
                    text = "Retry All",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            onClick = {
                onMenuItemClick(MoreMenuAction.RetryAll)
                onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        )

        // Menu Item: Remove All
        DropdownMenuItem(
            text = {
                Text(
                    text = "Remove All",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            onClick = {
                onMenuItemClick(MoreMenuAction.RemoveAll)
                onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        )

        // Menu Item: Delete All
        DropdownMenuItem(
            text = {
                Text(
                    text = "Delete All",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            onClick = {
                onMenuItemClick(MoreMenuAction.DeleteAll)
                onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        )

        // Menu Item: How to download?
        DropdownMenuItem(
            text = {
                Text(
                    text = "How to download?",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            onClick = {
                onMenuItemClick(MoreMenuAction.HowToDownload)
                onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        )
    }
}

/**
 * Sealed class representing all possible menu actions
 * This provides type-safe handling of menu selections
 */
sealed class MoreMenuAction {
    object Select : MoreMenuAction()
    object PauseAll : MoreMenuAction()
    object ResumeAll : MoreMenuAction()
    object RetryAll : MoreMenuAction()
    object RemoveAll : MoreMenuAction()
    object DeleteAll : MoreMenuAction()
    object HowToDownload : MoreMenuAction()
}

@Preview(showBackground = true)
@Composable
fun MoreOptionsMenuPreview() {
    FileDownloadManagerTheme {
        MoreOptionsMenu(
            expanded = true,
            onDismiss = {},
            onMenuItemClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MoreOptionsMenuDarkPreview() {
    FileDownloadManagerTheme {
        MoreOptionsMenu(
            expanded = true,
            onDismiss = {},
            onMenuItemClick = {}
        )
    }
}

