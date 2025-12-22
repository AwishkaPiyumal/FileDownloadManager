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
import androidx.hilt.navigation.compose.hiltViewModel
import com.piumal.filedownloadmanager.domain.model.DownloadConfig
import com.piumal.filedownloadmanager.domain.usecase.DownloadFilterType
import com.piumal.filedownloadmanager.ui.downloads.components.AddDownloadDialog
import com.piumal.filedownloadmanager.ui.downloads.components.DownloadFAB
import com.piumal.filedownloadmanager.ui.downloads.components.DownloadList
import com.piumal.filedownloadmanager.ui.downloads.components.SortBottomSheet
import com.piumal.filedownloadmanager.ui.downloads.viewmodel.DownloadScreenViewModel


@Composable
fun DownloadScreen(
    viewModel: DownloadScreenViewModel = hiltViewModel()
) {
    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Active", "Completed")

    // Update filter type when tab changes
    LaunchedEffect(selectedTabIndex) {
        val filterType = when (selectedTabIndex) {
            0 -> DownloadFilterType.ALL
            1 -> DownloadFilterType.ACTIVE
            2 -> DownloadFilterType.COMPLETED
            else -> DownloadFilterType.ALL
        }
        viewModel.setFilterType(filterType)
    }

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
                    onClick = { viewModel.showSortSheet() },
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

            // Display download list based on selected tab
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                // Show loading indicator while data is being loaded
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // Display filtered and sorted downloads from ViewModel
                    DownloadList(
                        downloads = uiState.displayedDownloads,
                        onMoreClick = { item ->
                            viewModel.onDownloadItemMoreClick(item)
                        }
                    )
                }
            }
        }

        DownloadFAB(
            onClick = { viewModel.showAddDownloadDialog() },
            modifier = Modifier
                .padding(end = 24.dp, bottom = 52.dp)
                .align(Alignment.BottomEnd)
        )
    }

    // Sort Bottom Sheet
    if (uiState.showSortSheet) {
        SortBottomSheet(
            onDismiss = { viewModel.hideSortSheet() },
            selectedOption = uiState.selectedSortOption,
            onSortSelected = { option ->
                viewModel.setSortOption(option)
                viewModel.hideSortSheet()
            }
        )
    }

    // Add Download Dialog
    if (uiState.showAddDownloadDialog) {
        AddDownloadDialog(
            onDismiss = { viewModel.hideAddDownloadDialog() },
            onDownload = { config ->
                viewModel.hideAddDownloadDialog()
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
