package com.piumal.filedownloadmanager.ui.support

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private const val PROJECT_URL = "https://github.com/AwishkaPiyumal/FileDownloadManager"
private const val ISSUES_URL = "https://github.com/AwishkaPiyumal/FileDownloadManager/issues"

@Composable
fun SupportScreen() {
    val uriHandler = LocalUriHandler.current

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
                text = "Help & Support",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Need help with downloads, permissions, or notifications? Start with the official project links below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            SupportInfoCard(
                title = "Before reporting a problem",
                items = listOf(
                    "Make sure the file URL is reachable.",
                    "Grant storage and notification permissions when prompted.",
                    "Include your device model and Android version when reporting a bug."
                )
            )

            SupportInfoCard(
                title = "Where to get help",
                items = listOf(
                    "Project source code and updates are available on GitHub.",
                    "Open an issue for bugs, crashes, or feature requests.",
                    "Check the README for setup and project information."
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { uriHandler.openUri(PROJECT_URL) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "GitHub")
                }
                Button(
                    onClick = { uriHandler.openUri(ISSUES_URL) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Report issue")
                }
            }
        }
    }
}

@Composable
private fun SupportInfoCard(
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

@Preview(showBackground = true)
@Composable
private fun SupportScreenPreview() {
    SupportScreen()
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SupportScreenDarkPreview() {
    SupportScreen()
}

