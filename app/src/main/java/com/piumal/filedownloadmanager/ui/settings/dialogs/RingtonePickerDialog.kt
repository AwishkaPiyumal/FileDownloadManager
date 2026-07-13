package com.piumal.filedownloadmanager.ui.settings.dialogs

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * RingtonePickerDialog
 *
 * Dialog for selecting notification ringtone with preview functionality.
 * Features:
 * - Device default ringtone
 * - System notification sounds (built-in only)
 * - Custom MP3 from internal storage
 * - Preview/playback when selected
 *
 * MVVM Architecture:
 * - UI Layer: This Composable
 * - ViewModel: SettingsViewModel handles state
 * - Data Layer: SharedPreferences stores selection
 */

data class RingtoneInfo(
    val uri: String,
    val displayName: String,
    val isCustom: Boolean = false
)

@Composable
fun RingtonePickerDialog(
    currentRingtone: String,
    title: String,
    onRingtoneSelected: (Pair<String, String>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val ringtones = remember { mutableStateOf<List<RingtoneInfo>>(emptyList()) }
    val selectedRingtone = remember { mutableStateOf(currentRingtone) }
    val isLoading = remember { mutableStateOf(true) }
    val mediaPlayer = remember { MediaPlayer() }
    val showCustomFileDialog = remember { mutableStateOf(false) }

    // File picker for custom MP3 files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val customRingtone = RingtoneInfo(
                uri = uri.toString(),
                displayName = "Custom Sound",
                isCustom = true
            )
            // Add to list if not already there
            val newList = ringtones.value.toMutableList()
            newList.removeAll { it.isCustom }
            newList.add(customRingtone)
            ringtones.value = newList
            selectedRingtone.value = uri.toString()
            playRingtone(context, mediaPlayer, uri.toString())
        }
        showCustomFileDialog.value = false
    }

    // Load available ringtones
    LaunchedEffect(Unit) {
        val ringtoneList = loadSystemNotificationRingtones(context)
        ringtones.value = ringtoneList
        isLoading.value = false
    }

    // Cleanup media player on dismiss
    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer.stop()
                mediaPlayer.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontSize = 18.sp)
        },
        text = {
            if (isLoading.value) {
                Text("Loading ringtones...")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(ringtones.value) { ringtone ->
                            RingtoneListItem(
                                ringtone = ringtone,
                                isSelected = selectedRingtone.value == ringtone.uri,
                                onSelect = {
                                    selectedRingtone.value = ringtone.uri
                                    playRingtone(context, mediaPlayer, ringtone.uri)
                                }
                            )
                        }
                    }

                    // Custom file picker button
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            filePickerLauncher.launch("audio/mpeg")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("+ Select Custom MP3", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        mediaPlayer.stop()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    // Find the display name for the selected ringtone
                    val selectedRingtoneInfo = ringtones.value.find { it.uri == selectedRingtone.value }
                    val displayName = selectedRingtoneInfo?.displayName ?: "Unknown Ringtone"
                    android.util.Log.d("RingtonePickerDialog", "Selected ringtone: uri=${selectedRingtone.value}, name=$displayName")
                    // Pass both URI and display name
                    onRingtoneSelected(selectedRingtone.value to displayName)
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    try {
                        mediaPlayer.stop()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    onDismiss()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RingtoneListItem(
    ringtone: RingtoneInfo,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Text(
            text = ringtone.displayName,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp
        )
    }
}

/**
 * Load ONLY system notification ringtones (built-in device sounds)
 * Excludes external ringtones and music files
 */
private fun loadSystemNotificationRingtones(context: Context): List<RingtoneInfo> {
    val ringtones = mutableListOf<RingtoneInfo>()

    // Add device default option
    ringtones.add(
        RingtoneInfo(
            uri = "DEFAULT",
            displayName = "Device Default Notification",
            isCustom = false
        )
    )

    try {
        // Get ONLY notification ringtones from system
        val ringtoneManager = RingtoneManager(context)
        ringtoneManager.setType(RingtoneManager.TYPE_NOTIFICATION)
        val cursor = ringtoneManager.cursor

        cursor?.use { c ->
            while (c.moveToNext()) {
                try {
                    val titleColumn = RingtoneManager.TITLE_COLUMN_INDEX
                    val uriColumn = RingtoneManager.ID_COLUMN_INDEX

                    val title = c.getString(titleColumn)
                    val uriId = c.getString(uriColumn)

                    // Construct URI for system notification sound
                    val uri = "content://media/internal/audio/media/$uriId"

                    // Add to list with simple name
                    ringtones.add(
                        RingtoneInfo(
                            uri = uri,
                            displayName = title,
                            isCustom = false
                        )
                    )
                } catch (e: Exception) {
                    android.util.Log.e("RingtonePickerDialog", "Error loading ringtone", e)
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("RingtonePickerDialog", "Error loading system ringtones", e)
    }

    return ringtones
}

/**
 * Play ringtone preview
 */
private fun playRingtone(context: Context, mediaPlayer: MediaPlayer, ringtoneUri: String) {
    try {
        mediaPlayer.reset()

        when {
            ringtoneUri == "DEFAULT" -> {
                // Play device default notification sound
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                mediaPlayer.setDataSource(context, defaultUri)
            }
            ringtoneUri.startsWith("content://") -> {
                // Play system ringtone
                mediaPlayer.setDataSource(context, Uri.parse(ringtoneUri))
            }
            else -> {
                // Play custom MP3 file
                mediaPlayer.setDataSource(context, Uri.parse(ringtoneUri))
            }
        }

        mediaPlayer.prepare()
        mediaPlayer.start()
    } catch (e: Exception) {
        android.util.Log.e("RingtonePickerDialog", "Error playing ringtone: ${e.message}", e)
    }
}

/**
 * Format ringtone URI for display
 */
fun getRingtoneDisplayName(uri: String): String {
    return when {
        uri == "DEFAULT" || uri.isEmpty() -> "Device Default"
        uri.contains("content://") -> "System Sound"
        else -> "Custom Sound"
    }
}

