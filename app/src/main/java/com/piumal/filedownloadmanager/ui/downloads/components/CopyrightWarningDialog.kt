package com.piumal.filedownloadmanager.ui.downloads.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Copyright Warning Dialog
 *
 * CRITICAL for Google Policy Compliance
 * Shows warning for restricted content downloads
 * User must acknowledge before proceeding
 *
 * @param message Warning message to display
 * @param onAccept User accepts and wants to continue
 * @param onReject User cancels download
 */
@Composable
fun CopyrightWarningDialog(
    message: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Dialog(onDismissRequest = onReject) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Warning Icon and Title
                Text(
                    text = "⚠️ LEGAL WARNING",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Warning Message (scrollable)
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                )

                Spacer(modifier = Modifier.height(24.dp))

                // User Responsibility Statement
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "By continuing, you confirm that you have legal rights to download this content and accept full responsibility for your actions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Cancel")
                    }

                    // Accept Button
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("I Accept Responsibility")
                    }
                }
            }
        }
    }
}

/**
 * Terms of Service Dialog
 * Must be shown on first app launch
 * Google Play requirement for legal protection
 */
@Composable
fun TermsOfServiceDialog(
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val termsText = """
        📋 TERMS OF SERVICE
        
        By using this File Download Manager, you agree to:
        
        1. LEGAL USE ONLY
        • Only download content you have legal rights to access
        • Do NOT download copyrighted material without permission
        • Do NOT use for piracy or illegal activities
        
        2. YOUR RESPONSIBILITY
        • You are solely responsible for all downloads
        • You must comply with copyright laws
        • You must comply with local laws and regulations
        
        3. PROHIBITED CONTENT
        You may NOT download:
        • Pirated movies, music, software, or games
        • Copyrighted content without permission
        • Illegal, harmful, or malicious files
        • Content that violates third-party rights
        
        4. DISCLAIMER
        • This app is provided "AS IS" without warranties
        • Developer is NOT liable for your actions
        • This app does NOT host or distribute content
        • Download at your own risk
        
        5. ENFORCEMENT
        • Violations may result in legal action against YOU
        • We may terminate access for policy violations
        • We cooperate with law enforcement
        
        6. INTENDED USE
        This app is for:
        • Personal documents and files
        • Open-source software downloads
        • Public domain content
        • Files you own or have permission to download
        
        ⚠️ WARNING: Downloading copyrighted content without permission is ILLEGAL and may result in:
        • Criminal prosecution
        • Heavy fines
        • Civil lawsuits
        • Account termination
        
        By clicking "I Accept", you confirm that you have read, understood, and agree to these terms.
    """.trimIndent()

    CopyrightWarningDialog(
        message = termsText,
        onAccept = onAccept,
        onReject = onReject
    )
}

