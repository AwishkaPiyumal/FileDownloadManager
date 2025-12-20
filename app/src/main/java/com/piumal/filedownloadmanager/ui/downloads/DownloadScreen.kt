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
import com.piumal.filedownloadmanager.domain.model.DownloadItem
import com.piumal.filedownloadmanager.domain.model.DownloadStatus
import com.piumal.filedownloadmanager.ui.downloads.components.AddDownloadDialog
import com.piumal.filedownloadmanager.ui.downloads.components.DownloadFAB
import com.piumal.filedownloadmanager.ui.downloads.components.DownloadList
import com.piumal.filedownloadmanager.ui.downloads.components.SortBottomSheet
import com.piumal.filedownloadmanager.ui.downloads.components.SortOption


@Composable
fun DownloadScreen() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showAddDownloadDialog by remember { mutableStateOf(false) }
    var selectedSortOption by remember { mutableStateOf(SortOption.DATE_DESC) }
    val tabs = listOf("All", "Active", "Completed")

    // Sample downloads for UI testing
    val sampleDownloads = remember {
        listOf(
            DownloadItem(
                id = "1",
                fileName = "video_tutorial_android_jetpack_compose.mp4",
                downloadedSize = 25 * 1024 * 1024, // 25 MB
                totalSize = 100 * 1024 * 1024,     // 100 MB
                status = DownloadStatus.DOWNLOADING,
                url = "https://example.com/video.mp4",
                filePath = "/storage/emulated/0/Download/FileDownloadManager/"
            ),
            DownloadItem(
                id = "2",
                fileName = "document_presentation.pdf",
                downloadedSize = 50 * 1024 * 1024, // 50 MB
                totalSize = 50 * 1024 * 1024,      // 50 MB
                status = DownloadStatus.COMPLETED,
                url = "https://example.com/document.pdf",
                filePath = "/storage/emulated/0/Download/FileDownloadManager/"
            ),
            DownloadItem(
                id = "3",
                fileName = "large_archive_backup.zip",
                downloadedSize = 150 * 1024 * 1024, // 150 MB
                totalSize = 500 * 1024 * 1024,      // 500 MB
                status = DownloadStatus.PAUSED,
                url = "https://example.com/archive.zip",
                filePath = "/storage/emulated/0/Download/FileDownloadManager/"
            ),
            DownloadItem(
                id = "4",
                fileName = "image_collection.jpg",
                downloadedSize = 2 * 1024 * 1024, // 2 MB
                totalSize = 10 * 1024 * 1024,     // 10 MB
                status = DownloadStatus.FAILED,
                url = "https://example.com/image.jpg",
                filePath = "/storage/emulated/0/Download/FileDownloadManager/"
            ),
            DownloadItem(
                id = "5",
                fileName = "software_installer.exe",
                downloadedSize = 80 * 1024 * 1024,  // 80 MB
                totalSize = 200 * 1024 * 1024,      // 200 MB
                status = DownloadStatus.DOWNLOADING,
                url = "https://example.com/software.exe",
                filePath = "/storage/emulated/0/Download/FileDownloadManager/"
            ),
            DownloadItem(
                id = "6",
                fileName = "music_album.mp3",
                downloadedSize = 120 * 1024 * 1024, // 120 MB
                totalSize = 120 * 1024 * 1024,      // 120 MB
                status = DownloadStatus.COMPLETED,
                url = "https://example.com/music.mp3",
                filePath = "/storage/emulated/0/Download/FileDownloadManager/"
            )
        )
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

            // Display download list based on selected tab
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> DownloadList(
                        downloads = sampleDownloads,
                        onMoreClick = { item ->
                            // TODO: Show options menu for this item
                        }
                    )
                    1 -> DownloadList(
                        downloads = sampleDownloads.filter {
                            it.status == DownloadStatus.DOWNLOADING ||
                            it.status == DownloadStatus.QUEUED
                        },
                        onMoreClick = { item ->
                            // TODO: Show options menu for this item
                        }
                    )
                    2 -> DownloadList(
                        downloads = sampleDownloads.filter {
                            it.status == DownloadStatus.COMPLETED
                        },
                        onMoreClick = { item ->
                            // TODO: Show options menu for this item
                        }
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
