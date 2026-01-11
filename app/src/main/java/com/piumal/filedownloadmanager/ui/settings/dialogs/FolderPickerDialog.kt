package com.piumal.filedownloadmanager.ui.settings.dialogs

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.piumal.filedownloadmanager.R
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme

/**
 * FolderPickerDialog
 *
 * A dialog for selecting the default download folder.
 * Shows current folder path with option to browse and select a new folder.
 *
 * @param currentFolder Current folder path
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when user confirms folder selection with the new path
 * @param onBrowse Callback when user wants to browse folders (opens system folder picker)
 */
@Composable
fun FolderPickerDialog(
    currentFolder: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onBrowse: () -> Unit
) {
    var folderPath by remember { mutableStateOf(currentFolder) }

    // Update internal state when currentFolder changes (from external picker)
    LaunchedEffect(currentFolder) {
        folderPath = currentFolder
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Title
                Text(
                    text = "Default Download Folder",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Folder path input with browse button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = folderPath,
                        onValueChange = { folderPath = it },
                        label = { Text("Folder Path") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        readOnly = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onBrowse,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.folder_24px),
                            contentDescription = "Browse folders",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "Cancel",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onConfirm(folderPath) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(text = "Confirm")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FolderPickerDialogPreview() {
    FileDownloadManagerTheme {
        FolderPickerDialog(
            currentFolder = "Download/FileDownloadManager",
            onDismiss = {},
            onConfirm = {},
            onBrowse = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FolderPickerDialogDarkPreview() {
    FileDownloadManagerTheme {
        FolderPickerDialog(
            currentFolder = "Download/FileDownloadManager",
            onDismiss = {},
            onConfirm = {},
            onBrowse = {}
        )
    }
}

