package com.piumal.filedownloadmanager.ui.downloads.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme

/**
 * FileExistsDialog Component
 *
 * Shows a confirmation dialog when user tries to download a file that already exists
 * Allows user to either cancel the download or continue with a renamed file
 *
 * MVVM Architecture:
 * - UI Layer component (presentation only)
 * - Business logic handled by ViewModel
 *
 * Clean Code:
 * - Single responsibility: Show confirmation UI
 * - Clear callbacks for user actions
 *
 * @param fileName The name of the file that already exists
 * @param onDismiss Callback when dialog should be dismissed (cancel button or outside click)
 * @param onContinue Callback when user wants to continue with renamed file
 */
@Composable
fun FileExistsDialog(
    fileName: String,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "File Already Exists",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // Message
                Text(
                    text = "\"$fileName\" is already downloaded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Do you want to download it again?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    // Continue Button
                    Button(
                        onClick = onContinue,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "Continue",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * Preview for Light Theme
 */
@Preview(showBackground = true)
@Composable
fun FileExistsDialogPreview() {
    FileDownloadManagerTheme {
        FileExistsDialog(
            fileName = "sample_document.pdf",
            onDismiss = {},
            onContinue = {}
        )
    }
}

/**
 * Preview for Dark Theme
 */
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FileExistsDialogDarkPreview() {
    FileDownloadManagerTheme {
        FileExistsDialog(
            fileName = "sample_document.pdf",
            onDismiss = {},
            onContinue = {}
        )
    }
}

