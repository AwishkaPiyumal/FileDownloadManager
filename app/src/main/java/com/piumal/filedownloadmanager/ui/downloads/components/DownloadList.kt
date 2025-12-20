package com.piumal.filedownloadmanager.ui.downloads.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.piumal.filedownloadmanager.domain.model.DownloadItem

/**
 * Composable that displays a list of download items
 * Used in DownloadScreen to show All/Active/Completed downloads
 *
 * @param downloads List of download items to display
 * @param onMoreClick Callback when more options is clicked on an item
 * @param modifier Optional modifier for customization
 */
@Composable
fun DownloadList(
    downloads: List<DownloadItem>,
    onMoreClick: (DownloadItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (downloads.isEmpty()) {
        // Empty state
        EmptyDownloadState(modifier = modifier)
    } else {
        // Download list
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = downloads,
                key = { it.id }
            ) { downloadItem ->
                DownloadItemCard(
                    downloadItem = downloadItem,
                    onMoreClick = onMoreClick
                )
            }
        }
    }
}

/**
 * Empty state when no downloads are available
 */
@Composable
private fun EmptyDownloadState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No Downloads",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tap the + button to add a download",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

