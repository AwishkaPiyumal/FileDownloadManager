package com.piumal.filedownloadmanager.ui.downloads.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DownloadItemMoreOption(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onMenuItemClick: (DownloadItemMoreMenuAction) -> Unit,
    modifier: Modifier = Modifier
) {

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = "Open",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            onClick = {
                onMenuItemClick(DownloadItemMoreMenuAction.Open)
                onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = "Show in Folder",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            onClick = {
                onMenuItemClick(DownloadItemMoreMenuAction.ShowInFolder)
                onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = "Share file",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            onClick = {
                onMenuItemClick(DownloadItemMoreMenuAction.ShareFile)
                onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )

        DropdownMenuItem(
            text = {
                Text(
                    text = "Show info",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            onClick = {
                onMenuItemClick(DownloadItemMoreMenuAction.ShowInfo)
                onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )

        DropdownMenuItem(
            text = {
                Text(
                    text = "Rename file",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            onClick = {
                onMenuItemClick(DownloadItemMoreMenuAction.RenameFile)
                onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = "Move to...",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            onClick = {
                onMenuItemClick(DownloadItemMoreMenuAction.Moveto)
                onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = "Delete file",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            onClick = {
                onMenuItemClick(DownloadItemMoreMenuAction.Deletefile)
                onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = "Remove from list",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            onClick = {
                onMenuItemClick(DownloadItemMoreMenuAction.Removefromlist)
                onDismiss()
            },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        )
    }
}
sealed class DownloadItemMoreMenuAction {
    object Open : DownloadItemMoreMenuAction()
    object ShowInFolder : DownloadItemMoreMenuAction()
    object ShareFile : DownloadItemMoreMenuAction()
    object ShowInfo : DownloadItemMoreMenuAction()
    object RenameFile : DownloadItemMoreMenuAction()
    object Moveto : DownloadItemMoreMenuAction()
    object Deletefile : DownloadItemMoreMenuAction()
    object Removefromlist : DownloadItemMoreMenuAction()
}