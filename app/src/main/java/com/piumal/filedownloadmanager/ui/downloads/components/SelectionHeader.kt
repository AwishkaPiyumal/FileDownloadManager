package com.piumal.filedownloadmanager.ui.downloads.components

import androidx.compose.foundation.border
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
 * Shows the count of selected items, select all option, and a close button to exit selection mode.
 * Delete button is shown in TopAppBar instead.
 *
 * @param selectedCount Number of currently selected items
 * @param totalCount Total number of items available for selection
 * @param onClose Callback when close button is clicked (exits selection mode)
 * @param onSelectAll Callback when "Select All" / "Deselect All" is clicked
 * @param modifier Optional modifier for styling
 */
@Composable
fun SelectionHeader(
    selectedCount: Int,
    totalCount: Int,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine if all items are selected
    val isAllSelected = totalCount > 0 && selectedCount == totalCount

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side - Select All / Deselect All option (weight 1)
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(25.dp))
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(25.dp)
                    )
                    .clickable { onSelectAll() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isAllSelected) R.drawable.check_circle_24px else R.drawable.radio_button_unchecked_24px
                    ),
                    contentDescription = if (isAllSelected) "Deselect all" else "Select all",
                    tint = if (isAllSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isAllSelected) "Deselect All" else "Select All",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Center - Selection count inside bordered box
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp))
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$selectedCount Selected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Right side - Close button (weight 1)
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit selection mode",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
