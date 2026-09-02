package com.piumal.filedownloadmanager.ui.downloads.components

import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.piumal.filedownloadmanager.R

import com.piumal.filedownloadmanager.domain.model.DownloadConfig
import com.piumal.filedownloadmanager.domain.util.DownloadStoragePaths
import com.piumal.filedownloadmanager.ui.downloads.viewmodel.AddDownloadViewModel


/**
 * Dialog for adding new downloads with URL, file path, and scheduling options
 * UI Layer - Only handles composable rendering
 * Business logic is handled by AddDownloadViewModel
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onDownload Callback when download button is clicked with the download configuration
 * @param viewModel ViewModel for managing dialog state and business logic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDownloadDialog(
    onDismiss: () -> Unit,
    onDownload: (DownloadConfig) -> Unit,
    viewModel: AddDownloadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    // Folder picker launcher using Storage Access Framework
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            // Take persistable permission
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)

            // Convert URI to readable path
            val folderPath = runCatching {
                val docId = DocumentsContract.getTreeDocumentId(uri)
                val split = docId.split(":")
                if (split.size >= 2) {
                    if (split[0] == "primary") {
                        "${'$'}{android.os.Environment.getExternalStorageDirectory().absolutePath}/${'$'}{split[1]}"
                    } else {
                        "/storage/${'$'}{split[0]}/${'$'}{split[1]}"
                    }
                } else {
                    uri.path ?: DownloadStoragePaths.DEFAULT_UI_FOLDER_LABEL
                }
            }.getOrDefault(DownloadStoragePaths.DEFAULT_UI_FOLDER_LABEL)

            viewModel.onFilePathChanged(folderPath)

            // After user selected folder, proceed with download
            if (viewModel.isValid()) {
                onDownload(viewModel.getDownloadConfig())
                viewModel.reset()
            }
        }
    }

    // Local state for validation errors
    var validationError by remember { mutableStateOf<String?>(null) }

    // Local state for schedule picker dialog
    var showSchedulePicker by remember { mutableStateOf(false) }

    // Initialize on first composition
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    Dialog(
        onDismissRequest = {
            viewModel.reset()
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Add Download",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // URL Input Field
                OutlinedTextField(
                    value = uiState.url,
                    onValueChange = { viewModel.onUrlChanged(it) },
                    label = { Text("URL", color = MaterialTheme.colorScheme.onSurface) },
                    placeholder = { Text("Enter or paste download URL") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // File Path Input Field with Folder Icon
                OutlinedTextField(
                    value = uiState.filePath,
                    onValueChange = { viewModel.onFilePathChanged(it) },
                    label = { Text("Save Location", color = MaterialTheme.colorScheme.onSurface) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            // Open folder picker to select save location
                            val initialUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download")
                            folderPickerLauncher.launch(initialUri)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.folder_24px),
                                contentDescription = "Select Folder",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // File Name Input Field
                OutlinedTextField(
                    value = uiState.fileName,
                    onValueChange = { viewModel.onFileNameChanged(it) },
                    label = { Text("File Name", color = MaterialTheme.colorScheme.onSurface) },
                    placeholder = { Text("Enter file name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            showSchedulePicker = true
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.calendar_clock_24px),
                                contentDescription = "Schedule Download",
                                tint = if (uiState.scheduleTime != null)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    singleLine = true
                )

                // Show schedule info if set
                if (uiState.scheduleTime != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 ${'$'}{formatDateTime(uiState.scheduleTime!!)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { viewModel.onScheduleTimeChanged(null) }
                        ) {
                            Text("Clear", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Show validation error if exists
                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Download Button
                Button(
                    onClick = {
                        if (viewModel.isValid()) {
                            // Validate URL with ContentValidator (Google Policy Compliance)
                            val validation = com.piumal.filedownloadmanager.domain.util.ContentValidator
                                .validateDownloadUrl(uiState.url)

                            if (!validation.isValid) {
                                // Show error message
                                validationError = validation.message
                            } else if (validation.requiresWarning && validation.warningMessage != null) {
                                // Show warning as error message (no dialog)
                                validationError = validation.warningMessage
                            } else {
                                // Check ask-download-folder setting via ViewModel
                                val isAsk = viewModel.isAskDownloadFolderEnabled()

                                if (isAsk) {
                                    // Launch folder picker to get destination path, download continues in launcher callback
                                    val initialUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download")
                                    folderPickerLauncher.launch(initialUri)
                                } else {
                                    // Proceed with download immediately
                                    onDownload(viewModel.getDownloadConfig())
                                    viewModel.reset()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = viewModel.isValid()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.download_24px),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Download",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    // Schedule Picker Dialog
    if (showSchedulePicker) {
        SchedulePickerDialog(
            initialDateTime = uiState.scheduleTime,
            onDismiss = {
                showSchedulePicker = false
            },
            onConfirm = { selectedTime ->
                viewModel.onScheduleTimeChanged(selectedTime)
                showSchedulePicker = false
            }
        )
    }
}

