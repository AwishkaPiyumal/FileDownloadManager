package com.piumal.filedownloadmanager.ui.downloads.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import com.piumal.filedownloadmanager.R

/**
 * Selection Header Component
 *
 * Displays when user is in selection mode.
 * Shows the count of selected items, select all option, delete button, and a close button to exit selection mode.
 *
 * @param selectedCount Number of currently selected items
 * @param totalCount Total number of items available for selection
 * @param onClose Callback when close button is clicked (exits selection mode)
 * @param onDelete Callback when delete button is clicked (deletes selected items)
 * @param onSelectAll Callback when "All" is clicked (selects/deselects all items)
 * @param modifier Optional modifier for styling
 */
@Composable
fun SelectionHeader(
    selectedCount: Int,
    totalCount: Int,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onSelectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine if all items are selected
    val isAllSelected = totalCount > 0 && selectedCount == totalCount

    // Determine delete icon opacity based on selection state
    val deleteIconAlpha = if (selectedCount > 0) 1f else 0.4f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side - Select All option with check circle
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable { onSelectAll() }
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Check circle icon - changes based on selection state
            Icon(
                painter = painterResource(
                    id = if (isAllSelected) R.drawable.check_circle_24px else R.drawable.radio_button_unchecked_24px
                ),
                contentDescription = if (isAllSelected) "All selected" else "Select all",
                tint = if (isAllSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.size(24.dp)
            )

            // "All" text
            Text(
                text = "All",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Center - Selection count text (uses weight to take remaining space and center)
        Text(
            text = "$selectedCount Selected",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Right side - Action buttons row
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


