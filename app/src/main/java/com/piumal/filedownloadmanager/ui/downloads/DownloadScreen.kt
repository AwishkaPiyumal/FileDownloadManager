package com.piumal.filedownloadmanager.ui.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.piumal.filedownloadmanager.domain.model.DownloadConfig
import com.piumal.filedownloadmanager.ui.downloads.components.AddDownloadDialog
import com.piumal.filedownloadmanager.ui.downloads.components.DownloadFAB
import com.piumal.filedownloadmanager.ui.downloads.components.SortBottomSheet
import com.piumal.filedownloadmanager.ui.downloads.components.SortOption


@Composable
fun DownloadScreen() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showAddDownloadDialog by remember { mutableStateOf(false) }
    var selectedSortOption by remember { mutableStateOf(SortOption.DATE_DESC) }
    val tabs = listOf("All", "Active", "Completed")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    title,
                                    color = if (selectedTabIndex == index)
                                        MaterialTheme.colorScheme.secondary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                    }
                }

                IconButton(
                    onClick = { showSortSheet = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(com.piumal.filedownloadmanager.R.drawable.sort_24px),
                        contentDescription = "Sort",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (selectedTabIndex) {
                    0 -> Text(
                        text = "All Downloads",
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    1 -> Text(
                        text = "Active Downloads",
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    2 -> Text(
                        text = "Completed Downloads",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        DownloadFAB(
            onClick = { showAddDownloadDialog = true },
            modifier = Modifier
                .padding(end = 24.dp, bottom = 52.dp)
                .align(Alignment.BottomEnd)
        )
    }

    if (showSortSheet) {
        SortBottomSheet(
            onDismiss = { showSortSheet = false },
            selectedOption = selectedSortOption,
            onSortSelected = { option ->
                selectedSortOption = option
                showSortSheet = false
                // TODO: Implement sorting logic based on selected option
            }
        )
    }

    if (showAddDownloadDialog) {
        AddDownloadDialog(
            onDismiss = { showAddDownloadDialog = false },
            onDownload = { config ->
                showAddDownloadDialog = false
                // TODO: Implement download logic with the provided config
                // config contains: url, filePath, fileName, scheduleTime
            }
        )
    }
}

@Preview
@Composable
fun DownloadPreview() {
    DownloadScreen()
}
