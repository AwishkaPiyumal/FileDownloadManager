package com.piumal.filedownloadmanager.ui.downloads.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.piumal.filedownloadmanager.R
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme

/**
 * Selection Header Component
 *
 * Displays when user is in selection mode.
 * Shows the count of selected items, delete button, and a close button to exit selection mode.
 *
 * @param selectedCount Number of currently selected items
 * @param onClose Callback when close button is clicked (exits selection mode)
 * @param onDelete Callback when delete button is clicked (deletes selected items)
 * @param modifier Optional modifier for styling
 */
@Composable
fun SelectionHeader(
    selectedCount: Int,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine delete icon opacity based on selection state
    val deleteIconAlpha = if (selectedCount > 0) 1f else 0.4f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection count text - using onBackground color
        Text(
            text = "$selectedCount Selected",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Action buttons row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Delete button - deletes selected items
            // Lower opacity when no items selected, full opacity when items are selected
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp),
                enabled = selectedCount > 0
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.delete_24px),
                    contentDescription = "Delete selected items",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = deleteIconAlpha)
                )
            }

            // Close button to exit selection mode - using onBackground color
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit selection mode",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectionHeaderPreview() {
    FileDownloadManagerTheme {
        SelectionHeader(
            selectedCount = 3,
            onClose = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SelectionHeaderNoSelectionPreview() {
    FileDownloadManagerTheme {
        SelectionHeader(
            selectedCount = 0,
            onClose = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SelectionHeaderDarkPreview() {
    FileDownloadManagerTheme {
        SelectionHeader(
            selectedCount = 5,
            onClose = {},
            onDelete = {}
        )
    }
}

