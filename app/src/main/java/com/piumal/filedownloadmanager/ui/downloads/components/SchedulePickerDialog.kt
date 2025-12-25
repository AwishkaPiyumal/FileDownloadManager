package com.piumal.filedownloadmanager.ui.downloads.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Schedule Picker Dialog Component
 *
 * Allows users to select date and time for scheduling downloads
 * Uses Material 3 DatePicker and TimePicker
 *
 * @param initialDateTime Initial date-time to show (null for current time)
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when user confirms with selected date-time
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulePickerDialog(
    initialDateTime: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    // State for date picker
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateTime ?: System.currentTimeMillis()
    )

    // State for time picker (Material 3 TimeInput)
    val calendar = remember {
        Calendar.getInstance().apply {
            timeInMillis = initialDateTime ?: System.currentTimeMillis()
        }
    }

    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = false
    )

    // State to show date picker or time picker
    var showDatePicker by remember { mutableStateOf(true) }

    // Coroutine scope for updating time picker state
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = if (showDatePicker) "Select Date" else "Select Time",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (showDatePicker) {
                    // Date Picker
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier.fillMaxWidth(),
                        colors = DatePickerDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                } else {
                    // Enhanced Time Picker with typing AND buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Display selected time
                        Text(
                            text = String.format(
                                Locale.getDefault(),
                                "%02d:%02d %s",
                                if (timePickerState.hour == 0 || timePickerState.hour == 12) 12
                                else timePickerState.hour % 12,
                                timePickerState.minute,
                                if (timePickerState.hour < 12) "AM" else "PM"
                            ),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Hour and Minute selectors with typing AND buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Hour Picker
                            EnhancedTimeUnitPicker(
                                label = "Hour",
                                value = if (timePickerState.hour == 0 || timePickerState.hour == 12) 12
                                else timePickerState.hour % 12,
                                range = 1..12,
                                onValueChange = { newHour ->
                                    val adjustedHour = when {
                                        timePickerState.hour < 12 && newHour == 12 -> 0
                                        timePickerState.hour < 12 -> newHour
                                        newHour == 12 -> 12
                                        else -> newHour + 12
                                    }
                                    // Update the state directly
                                    scope.launch {
                                        timePickerState.hour = adjustedHour
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // Minute Picker
                            EnhancedTimeUnitPicker(
                                label = "Minute",
                                value = timePickerState.minute,
                                range = 0..59,
                                onValueChange = { newMinute ->
                                    scope.launch {
                                        timePickerState.minute = newMinute
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // AM/PM Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            FilterChip(
                                selected = timePickerState.hour < 12,
                                onClick = {
                                    scope.launch {
                                        if (timePickerState.hour >= 12) {
                                            timePickerState.hour = timePickerState.hour - 12
                                        }
                                    }
                                },
                                label = { Text("AM") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = timePickerState.hour >= 12,
                                onClick = {
                                    scope.launch {
                                        if (timePickerState.hour < 12) {
                                            timePickerState.hour = timePickerState.hour + 12
                                        }
                                    }
                                },
                                label = { Text("PM") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Cancel")
                    }

                    // Next/Confirm Button
                    Button(
                        onClick = {
                            if (showDatePicker) {
                                // Move to time picker
                                showDatePicker = false
                            } else {
                                // Combine date and time, then confirm
                                val selectedDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                                val calendar = Calendar.getInstance().apply {
                                    timeInMillis = selectedDate
                                    set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                    set(Calendar.MINUTE, timePickerState.minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                onConfirm(calendar.timeInMillis)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (showDatePicker) "Next" else "Confirm")
                    }
                }

                // Back button when showing time picker
                if (!showDatePicker) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "← Back to Date",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Enhanced Time Unit Picker
 * Supports both typing and up/down buttons
 */
@Composable
private fun EnhancedTimeUnitPicker(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember(value) { mutableStateOf(String.format(Locale.getDefault(), "%02d", value)) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Up Button
        IconButton(
            onClick = {
                val newValue = if (value < range.last) value + 1 else range.first
                onValueChange(newValue)
            }
        ) {
            Text("▲", style = MaterialTheme.typography.titleLarge)
        }

        // Text Input Field
        OutlinedTextField(
            value = textValue,
            onValueChange = { newText ->
                textValue = newText
                // Try to parse and validate
                newText.toIntOrNull()?.let { intValue ->
                    if (intValue in range) {
                        onValueChange(intValue)
                    }
                }
            },
            modifier = Modifier
                .width(90.dp)
                .height(72.dp),
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        // Down Button
        IconButton(
            onClick = {
                val newValue = if (value > range.first) value - 1 else range.last
                onValueChange(newValue)
            }
        ) {
            Text("▼", style = MaterialTheme.typography.titleLarge)
        }
    }
}

/**
 * Format timestamp to readable date-time string
 */
fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Preview
@Preview(showBackground = true)
@Composable
fun SchedulePickerDialogPreview() {
    FileDownloadManagerTheme {
        SchedulePickerDialog(
            initialDateTime = System.currentTimeMillis(),
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SchedulePickerDialogDarkPreview() {
    FileDownloadManagerTheme {
        SchedulePickerDialog(
            initialDateTime = System.currentTimeMillis(),
            onDismiss = {},
            onConfirm = {}
        )
    }
}

