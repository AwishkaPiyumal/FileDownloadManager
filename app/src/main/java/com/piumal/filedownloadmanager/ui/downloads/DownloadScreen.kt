package com.piumal.filedownloadmanager.ui.downloads

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.piumal.filedownloadmanager.domain.usecase.download.DownloadFilterType
import com.piumal.filedownloadmanager.ui.downloads.components.AddDownloadDialog
import com.piumal.filedownloadmanager.ui.downloads.components.DeleteSelectedConfirmDialog
import com.piumal.filedownloadmanager.ui.downloads.components.DownloadFAB
import com.piumal.filedownloadmanager.ui.downloads.components.DownloadList
import com.piumal.filedownloadmanager.ui.downloads.components.FileExistsDialog
import com.piumal.filedownloadmanager.ui.downloads.components.SelectionHeader
import com.piumal.filedownloadmanager.ui.downloads.components.SortBottomSheet
import com.piumal.filedownloadmanager.ui.downloads.viewmodel.DownloadScreenViewModel
import com.piumal.filedownloadmanager.ui.downloads.viewmodel.MoreOptionsViewModel


/**
 * DownloadScreen - Main screen for displaying download items
 *
 * @param viewModel ViewModel for managing download screen state
 * @param moreOptionsViewModel Shared ViewModel for More Options Menu actions
 *        (shared with MainScreen to enable "Select" menu item)
 */
@Composable
fun DownloadScreen(
    viewModel: DownloadScreenViewModel = hiltViewModel(),
    moreOptionsViewModel: MoreOptionsViewModel = hiltViewModel()
) {
    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Active", "Completed")

    // Observe selection mode event from MoreOptionsViewModel
    LaunchedEffect(Unit) {
        moreOptionsViewModel.selectionModeEvent.collect { shouldEnterSelection ->
            if (shouldEnterSelection) {
                viewModel.enterSelectionMode()
            }
        }
    }

    // Handle delete event from TopAppBar delete button
    LaunchedEffect(Unit) {
        moreOptionsViewModel.deleteSelectedEvent.collect {
            viewModel.showDeleteSelectedDialog()
        }
    }

    // Notify MoreOptionsViewModel when selection mode changes (to hide more_vert in TopAppBar)
    LaunchedEffect(uiState.isSelectionMode) {
        moreOptionsViewModel.setSelectionModeActive(uiState.isSelectionMode)
    }

    // Sync selected count with MoreOptionsViewModel for TopAppBar delete button
    LaunchedEffect(uiState.selectedCount) {
        moreOptionsViewModel.setSelectedCount(uiState.selectedCount)
    }

    // Handle back button press - exit selection mode instead of navigating back
    BackHandler(enabled = uiState.isSelectionMode) {
        viewModel.exitSelectionMode()
    }

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
            // Show SelectionHeader when in selection mode, otherwise show tabs
            if (uiState.isSelectionMode) {
                // Selection Header - shows selected count, select all option, and close button
                SelectionHeader(
                    selectedCount = uiState.selectedCount,
                    totalCount = uiState.displayedDownloads.size,
                    onClose = { viewModel.exitSelectionMode() },
                    onSelectAll = { viewModel.toggleSelectAll() }
                )
            } else {
                // Normal mode - show tabs and sort button
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
                        },
                        onPauseClick = { downloadId ->
                            viewModel.pauseDownload(downloadId)
                        },
                        onResumeClick = { downloadId ->
                            viewModel.resumeDownload(downloadId)
                        },
                        onRetryClick = { downloadId ->
                            viewModel.retryDownload(downloadId)
                        },
                        isSelectionMode = uiState.isSelectionMode,
                        selectedIds = uiState.selectedDownloadIds,
                        onLongPress = { downloadId ->
                            viewModel.onItemLongPress(downloadId)
                        },
                        onItemClick = { downloadId ->
                            viewModel.toggleSelection(downloadId)
                        }
                    )
                }
            }
        }

        // Hide FAB when selection mode is active
        if (!uiState.isSelectionMode) {
            DownloadFAB(
                onClick = { viewModel.showAddDownloadDialog() },
                modifier = Modifier
                    .padding(end = 24.dp, bottom = 52.dp)
                    .align(Alignment.BottomEnd)
            )
        }
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
                // Start the download with Google Policy validation
                viewModel.startDownload(config)
            }
        )
    }

    // File Exists Dialog
    if (uiState.showFileExistsDialog && uiState.existingFileName != null) {
        FileExistsDialog(
            fileName = uiState.existingFileName!!,
            onDismiss = { viewModel.dismissFileExistsDialog() },
            onContinue = { viewModel.continueDownloadWithRename() }
        )
    }

    // Delete Selected Confirmation Dialog
    if (uiState.showDeleteSelectedDialog) {
        DeleteSelectedConfirmDialog(
            selectedCount = uiState.selectedCount,
            onConfirm = { viewModel.confirmDeleteSelected() },
            onDismiss = { viewModel.dismissDeleteSelectedDialog() }
        )
    }

    // Show success snackbar
    if (uiState.downloadSuccess) {
        val message = uiState.successMessage
        if (message != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(message)
            }
        }
    }

    // Show error snackbar
    val errorMessage = uiState.downloadError
    if (errorMessage != null) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            containerColor = MaterialTheme.colorScheme.errorContainer
        ) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

