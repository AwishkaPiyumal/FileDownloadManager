package com.piumal.filedownloadmanager.ui.downloads.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.piumal.filedownloadmanager.R
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme
import kotlin.math.log10
import kotlin.math.pow

/**
 * Composable representing a single download item in the list
 * Follows Material Design 3 guidelines and Google Play Store policies
 *
 * @param downloadItem Download data to display
 * @param onMoreClick Callback when more options button is clicked
 * @param modifier Optional modifier for customization
 */
@Composable
fun DownloadItemCard(
    downloadItem: DownloadItem,
    onMoreClick: (DownloadItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // Theme colors
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    // Calculate data directly without ViewModel
    val progressPercentage = downloadItem.getProgressPercentage()
    val formattedSize = remember(downloadItem) {
        formatFileSize(downloadItem)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
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

            // More options button
            IconButton(
                onClick = { onMoreClick(downloadItem) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.more_vert_24px),
                    contentDescription = "More options",
                    tint = colorScheme.onSurface
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

        // Row 3: Status label and file size
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status label
            Text(
                text = getStatusText(downloadItem.status),
                style = typography.bodySmall,
                color = getStatusColor(downloadItem.status, colorScheme)
            )

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

    return String.format("%.2f %s", size, units[digitGroups])
}

/**
 * Get human-readable status text
 * Follows Google Play Store policy - clear and honest information
 */
private fun getStatusText(status: DownloadStatus): String {
    return when (status) {
        DownloadStatus.DOWNLOADING -> "Downloading"
        DownloadStatus.PAUSED -> "Paused"
        DownloadStatus.COMPLETED -> "Completed"
        DownloadStatus.FAILED -> "Failed"
        DownloadStatus.QUEUED -> "Queued"
    }
}

/**
 * Get status-specific color
 * Provides visual feedback following Material Design 3 guidelines
 */
@Composable
private fun getStatusColor(status: DownloadStatus, colorScheme: ColorScheme): androidx.compose.ui.graphics.Color {
    return when (status) {
        DownloadStatus.DOWNLOADING -> colorScheme.secondary
        DownloadStatus.PAUSED -> colorScheme.tertiaryContainer
        DownloadStatus.COMPLETED -> colorScheme.tertiary
        DownloadStatus.FAILED -> colorScheme.error
        DownloadStatus.QUEUED -> colorScheme.onSurfaceVariant
    }
}

/**
 * Preview for downloading state
 */
@Preview(showBackground = true, name = "Download In Progress")
@Composable
fun DownloadItemCardDownloadingPreview() {
    FileDownloadManagerTheme {
        Surface {
            DownloadItemCard(
                downloadItem = DownloadItem(
                    id = "1",
                    fileName = "example_video.mp4",
                    downloadedSize = 25 * 1024 * 1024, // 25 MB
                    totalSize = 100 * 1024 * 1024, // 100 MB
                    status = DownloadStatus.DOWNLOADING,
                    url = "https://example.com/video.mp4",
                    filePath = "/storage/emulated/0/Download/FileDownloadManager/"
                ),
                onMoreClick = {}
            )
        }
    }
}

/**
 * Preview for paused state
 */
@Preview(showBackground = true, name = "Download Paused")
@Composable
fun DownloadItemCardPausedPreview() {
    FileDownloadManagerTheme {
        Surface {
            DownloadItemCard(
                downloadItem = DownloadItem(
                    id = "2",
                    fileName = "document_file_with_very_long_name.pdf",
                    downloadedSize = 50 * 1024 * 1024, // 50 MB
                    totalSize = 100 * 1024 * 1024, // 100 MB
                    status = DownloadStatus.PAUSED,
                    url = "https://example.com/document.pdf",
                    filePath = "/storage/emulated/0/Download/FileDownloadManager/"
                ),
                onMoreClick = {}
            )
        }
    }
}

/**
 * Preview for completed state
 */
@Preview(showBackground = true, name = "Download Completed")
@Composable
fun DownloadItemCardCompletedPreview() {
    FileDownloadManagerTheme {
        Surface {
            DownloadItemCard(
                downloadItem = DownloadItem(
                    id = "3",
                    fileName = "presentation.pptx",
                    downloadedSize = 15 * 1024 * 1024, // 15 MB
                    totalSize = 15 * 1024 * 1024, // 15 MB
                    status = DownloadStatus.COMPLETED,
                    url = "https://example.com/presentation.pptx",
                    filePath = "/storage/emulated/0/Download/FileDownloadManager/"
                ),
                onMoreClick = {}
            )
        }
    }
}

/**
 * Preview for failed state
 */
@Preview(showBackground = true, name = "Download Failed")
@Composable
fun DownloadItemCardFailedPreview() {
    FileDownloadManagerTheme {
        Surface {
            DownloadItemCard(
                downloadItem = DownloadItem(
                    id = "4",
                    fileName = "image.jpg",
                    downloadedSize = 2 * 1024 * 1024, // 2 MB
                    totalSize = 10 * 1024 * 1024, // 10 MB
                    status = DownloadStatus.FAILED,
                    url = "https://example.com/image.jpg",
                    filePath = "/storage/emulated/0/Download/FileDownloadManager/"
                ),
                onMoreClick = {}
            )
        }
    }
}

/**
 * Preview for dark theme
 */
@Preview(showBackground = true, name = "Dark Theme", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DownloadItemCardDarkPreview() {
    FileDownloadManagerTheme {
        Surface {
            DownloadItemCard(
                downloadItem = DownloadItem(
                    id = "5",
                    fileName = "archive.zip",
                    downloadedSize = 75 * 1024 * 1024, // 75 MB
                    totalSize = 200 * 1024 * 1024, // 200 MB
                    status = DownloadStatus.DOWNLOADING,
                    url = "https://example.com/archive.zip",
                    filePath = "/storage/emulated/0/Download/FileDownloadManager/"
                ),
                onMoreClick = {}
            )
        }
    }
}

