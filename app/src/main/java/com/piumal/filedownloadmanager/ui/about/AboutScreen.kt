package com.piumal.filedownloadmanager.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

private const val PROJECT_URL = "https://github.com/AwishkaPiyumal/FileDownloadManager"
private const val APP_VERSION = "1.2.0"
private const val APP_PACKAGE_NAME = "com.piumal.filedownloadmanager"

@Composable
fun AboutScreen() {
    val uriHandler = LocalUriHandler.current
    var showLicensesDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopStart)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "File Download Manager",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Version $APP_VERSION",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "A modern Android download manager built with Jetpack Compose, Material 3, MVVM, and Clean Architecture.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            AboutInfoCard(
                title = "What this app does",
                items = listOf(
                    "Manage file downloads in one place.",
                    "Track active and completed downloads.",
                    "Use dark mode, notifications, and network preferences."
                )
            )

            AboutInfoCard(
                title = "Project details",
                items = listOf(
                    "Package name: $APP_PACKAGE_NAME",
                    "Minimum Android version: API 24 (Android 7.0)",
                    "License: MIT"
                )
            )

            // Open Source Licenses Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Open Source Licenses",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "This app uses open-source components including TAndroidLame (LAME MP3 encoder) under LGPL v2.1.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { showLicensesDialog = true },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(text = "View Open Source Licenses")
                    }
                }
            }

            Button(
                onClick = { uriHandler.openUri(PROJECT_URL) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "View source on GitHub")
            }
        }
    }

    if (showLicensesDialog) {
        OpenSourceLicensesDialog(onDismiss = { showLicensesDialog = false })
    }
}

@Composable
private fun AboutInfoCard(
    title: String,
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            items.forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OpenSourceLicensesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Third-Party Licenses")
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "TAndroidLame / LAME MP3 Encoder",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Licensed under GNU Lesser General Public License v2.1 (LGPL 2.1).\n" +
                            "LAME MP3 encoding library is used for converting audio in downloaded media files.",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Jetpack Compose & AndroidX Libraries",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Licensed under Apache License 2.0.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}