package com.piumal.filedownloadmanager.ui.downloads.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import java.text.DateFormat
import java.util.Date

@Composable
fun DownloadInfoDialog(
    downloadItem: DownloadItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = {
            Text(text = "File info", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow(label = "Name", value = downloadItem.fileName)
                InfoRow(label = "Path", value = downloadItem.filePath)
                InfoRow(label = "Status", value = downloadItem.status.name)
                InfoRow(label = "Downloaded", value = "${downloadItem.downloadedSize/(1024*1024)} MB")
                InfoRow(label = "Total size", value = "${downloadItem.totalSize/(1024*1024)} MB")
                InfoRow(label = "Created", value = DateFormat.getDateTimeInstance().format(Date(downloadItem.createdAt)))
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 16.dp))
    }
}

