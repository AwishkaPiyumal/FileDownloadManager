package com.piumal.filedownloadmanager.ui.downloads.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme

enum class SortCategory {
    DATE,
    NAME,
    SIZE
}

enum class SortOrder {
    ASCENDING,
    DESCENDING
}

data class SortOption(
    val category: SortCategory,
    val order: SortOrder
) {
    companion object {
        val DATE_ASC = SortOption(SortCategory.DATE, SortOrder.ASCENDING)
        val DATE_DESC = SortOption(SortCategory.DATE, SortOrder.DESCENDING)
        val NAME_ASC = SortOption(SortCategory.NAME, SortOrder.ASCENDING)
        val NAME_DESC = SortOption(SortCategory.NAME, SortOrder.DESCENDING)
        val SIZE_ASC = SortOption(SortCategory.SIZE, SortOrder.ASCENDING)
        val SIZE_DESC = SortOption(SortCategory.SIZE, SortOrder.DESCENDING)
    }
}

private fun getOrderSubtitle(category: SortCategory, order: SortOrder): String {
    return when (category) {
        SortCategory.DATE -> if (order == SortOrder.ASCENDING) "Oldest First" else "Newest First"
        SortCategory.NAME -> if (order == SortOrder.ASCENDING) "A-Z" else "Z-A"
        SortCategory.SIZE -> if (order == SortOrder.ASCENDING) "Smallest First" else "Largest First"
    }
}

@Composable
private fun SortCategoryItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected)
                MaterialTheme.colorScheme.secondary
            else
                MaterialTheme.colorScheme.onSurface
        )

        if (isSelected) {
            RadioButton(
                selected = true,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }
}

@Composable
private fun SortOrderItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (isSelected) {
            RadioButton(
                selected = true,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    onDismiss: () -> Unit,
    selectedOption: SortOption,
    onSortSelected: (SortOption) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(selectedOption.category) }
    var selectedOrder by remember { mutableStateOf(selectedOption.order) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "Sort By",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

            // Sort Category Section
            SortCategoryItem(
                title = "By Date",
                isSelected = selectedCategory == SortCategory.DATE,
                onClick = {
                    selectedCategory = SortCategory.DATE
                    onSortSelected(SortOption(SortCategory.DATE, selectedOrder))
                }
            )

            SortCategoryItem(
                title = "By Name",
                isSelected = selectedCategory == SortCategory.NAME,
                onClick = {
                    selectedCategory = SortCategory.NAME
                    onSortSelected(SortOption(SortCategory.NAME, selectedOrder))
                }
            )

            SortCategoryItem(
                title = "By Size",
                isSelected = selectedCategory == SortCategory.SIZE,
                onClick = {
                    selectedCategory = SortCategory.SIZE
                    onSortSelected(SortOption(SortCategory.SIZE, selectedOrder))
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

            // Sort Order Section
            Text(
                text = "Order",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            SortOrderItem(
                title = "Ascending",
                subtitle = getOrderSubtitle(selectedCategory, SortOrder.ASCENDING),
                isSelected = selectedOrder == SortOrder.ASCENDING,
                onClick = {
                    selectedOrder = SortOrder.ASCENDING
                    onSortSelected(SortOption(selectedCategory, SortOrder.ASCENDING))
                }
            )

            SortOrderItem(
                title = "Descending",
                subtitle = getOrderSubtitle(selectedCategory, SortOrder.DESCENDING),
                isSelected = selectedOrder == SortOrder.DESCENDING,
                onClick = {
                    selectedOrder = SortOrder.DESCENDING
                    onSortSelected(SortOption(selectedCategory, SortOrder.DESCENDING))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(name = "Light Mode - Name Ascending")
@Composable
fun SortBottomSheetLightPreview() {
    FileDownloadManagerTheme(darkTheme = false) {
        SortBottomSheet(
            onDismiss = {},
            selectedOption = SortOption.NAME_ASC,
            onSortSelected = {}
        )
    }
}

@Preview(name = "Dark Mode - Date Descending")
@Composable
fun SortBottomSheetDarkPreview() {
    FileDownloadManagerTheme(darkTheme = true) {
        SortBottomSheet(
            onDismiss = {},
            selectedOption = SortOption.DATE_DESC,
            onSortSelected = {}
        )
    }
}

@Preview(name = "Light Mode - Size Ascending")
@Composable
fun SortBottomSheetSizePreview() {
    FileDownloadManagerTheme(darkTheme = false) {
        SortBottomSheet(
            onDismiss = {},
            selectedOption = SortOption.SIZE_ASC,
            onSortSelected = {}
        )
    }
}

