package com.piumal.filedownloadmanager.ui.downloads.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.piumal.filedownloadmanager.R
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

/**
 * Composable representing a single download item in the list
 * Follows Material Design 3 guidelines and Google Play Store policies
 *
 * @param downloadItem Download data to display
 * @param actionsEnabled Whether the context menu actions are enabled
 * @param onMoreClick Callback when more options button is clicked
 * @param onPauseClick Callback when pause button is clicked
 * @param onResumeClick Callback when resume button is clicked
 * @param onRetryClick Callback when retry button is clicked
 * @param isSelectionMode Whether selection mode is active
 * @param isSelected Whether this item is currently selected
 * @param onLongPress Callback when item is long pressed
 * @param onItemClick Callback when item is clicked (used for selection toggle in selection mode)
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DownloadItemCard(
    modifier: Modifier = Modifier,
    downloadItem: DownloadItem,
    actionsEnabled: Boolean = false,
    onMoreClick: (DownloadItem) -> Unit,
    onPauseClick: (String) -> Unit = {},
    onResumeClick: (String) -> Unit = {},
    onRetryClick: (String) -> Unit = {},
    onOpen: (String) -> Unit = {},
    onShare: (String) -> Unit = {},
    onRename: (String, String) -> Unit = { id, name -> },
    onDelete: (String) -> Unit = {},
    onShowInFolder: (String) -> Unit = {},
    onShowInfo: (String) -> Unit = {},
    onCopyTo: (String) -> Unit = {},
    onRemoveFromList: (String) -> Unit = {},
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongPress: (String) -> Unit = {},
    onItemClick: (String) -> Unit = {},

) {
    var showMoreMenu by remember { mutableStateOf(false) }
    // Theme colors
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    // Calculate data directly without ViewModel
    val progressPercentage = downloadItem.getProgressPercentage()
    val formattedSize = remember(downloadItem) {
        formatFileSize(downloadItem)
    }

    // Background color based on selection state
    val backgroundColor = if (isSelected) {
        colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        colorScheme.surface
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        // In selection mode, clicking toggles selection
                        onItemClick(downloadItem.id)
                    }
                    // In normal mode, clicking does nothing (user uses more options)
                },
                onLongClick = {
                    // Long press enters selection mode and selects this item
                    onLongPress(downloadItem.id)
                }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Row 1: File name and More button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File name (takes most of the space)
            Text(
                text = downloadItem.fileName,
                style = typography.bodyLarge,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // More options button with menu positioned relative to it
            Box {
                IconButton(
                    onClick = { showMoreMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.more_vert_24px),
                        contentDescription = "More options",
                        tint = colorScheme.onSurface
                    )
                }

                // Menu positioned relative to the IconButton
                DownloadItemMoreOption(
                    expanded = showMoreMenu,
                    onDismiss = { showMoreMenu = false },
                    onMenuItemClick = { action ->
                        when(action){
                            is DownloadItemMoreMenuAction.Open -> onOpen(downloadItem.id)
                            is DownloadItemMoreMenuAction.ShowInFolder -> onShowInFolder(downloadItem.id)
                            is DownloadItemMoreMenuAction.ShareFile -> onShare(downloadItem.id)
                            is DownloadItemMoreMenuAction.ShowInfo -> onShowInfo(downloadItem.id)
                            is DownloadItemMoreMenuAction.RenameFile -> onRename(downloadItem.id, downloadItem.fileName)
                            is DownloadItemMoreMenuAction.Copyto -> onCopyTo(downloadItem.id)
                            is DownloadItemMoreMenuAction.Deletefile -> onDelete(downloadItem.id)
                            is DownloadItemMoreMenuAction.Removefromlist -> onRemoveFromList(downloadItem.id)
                        }
                    },
                    enabled = actionsEnabled
                )
            }
        }


        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: Status and Progress bar (if applicable)
        if (downloadItem.isInProgress() || downloadItem.isPaused()) {
            // Show progress bar with percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress bar
                LinearProgressIndicator(
                    progress = { progressPercentage / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp),
                    color = colorScheme.primary,
                    trackColor = colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Percentage
                Text(
                    text = "${progressPercentage}%",
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        // Row 3: Status label, action button, and file size
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Status label and action button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status label
                Text(
                    text = getStatusText(downloadItem.status),
                    style = typography.bodySmall,
                    color = getStatusColor(downloadItem.status, colorScheme)
                )

                // Action button based on status
                when (downloadItem.status) {
                    DownloadStatus.DOWNLOADING -> {
                        // Show Pause button
                        IconButton(
                            onClick = { onPauseClick(downloadItem.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.pause_circle_24px),
                                contentDescription = "Pause download",
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    DownloadStatus.PAUSED -> {
                        // Show Resume (Play) button
                        IconButton(
                            onClick = { onResumeClick(downloadItem.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.play_circle_24px),
                                contentDescription = "Resume download",
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    DownloadStatus.FAILED -> {
                        // Show Retry (Refresh) button
                        IconButton(
                            onClick = { onRetryClick(downloadItem.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.refresh_24px),
                                contentDescription = "Retry download",
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    else -> {
                        // No action button for COMPLETED, QUEUED, etc.
                    }
                }
            }

            // File size information
            Text(
                text = formattedSize,
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Divider line
        HorizontalDivider(
            thickness = 1.dp,
            color = colorScheme.outlineVariant
        )
    }
}

/**
 * Format file size based on download status
 */
private fun formatFileSize(item: DownloadItem): String {
    return if (item.isCompleted()) {
        formatBytes(item.totalSize)
    } else {
        "${formatBytes(item.downloadedSize)} / ${formatBytes(item.totalSize)}"
    }
}

/**
 * Convert bytes to human-readable format
 */
private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()

    val size = bytes / 1024.0.pow(digitGroups.toDouble())

    return String.format(Locale.US, "%.2f %s", size, units[digitGroups])
}

private fun getStatusText(status: DownloadStatus): String {
    return when (status) {
        DownloadStatus.PENDING -> "Pending"
        DownloadStatus.DOWNLOADING -> "Downloading"
        DownloadStatus.PAUSED -> "Paused"
        DownloadStatus.COMPLETED -> "Completed"
        DownloadStatus.FAILED -> "Failed"
        DownloadStatus.QUEUED -> "Queued"
        DownloadStatus.CANCELLED -> "Cancelled"
    }
}

@Composable
private fun getStatusColor(status: DownloadStatus, colorScheme: ColorScheme): androidx.compose.ui.graphics.Color {
    return when (status) {
        DownloadStatus.PENDING -> colorScheme.primary
        DownloadStatus.DOWNLOADING -> colorScheme.secondary
        DownloadStatus.PAUSED -> colorScheme.tertiaryContainer
        DownloadStatus.COMPLETED -> colorScheme.tertiary
        DownloadStatus.FAILED -> colorScheme.error
        DownloadStatus.QUEUED -> colorScheme.onSurfaceVariant
        DownloadStatus.CANCELLED -> colorScheme.outline
    }
}


