package com.piumal.filedownloadmanager.ui.downloads.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun DownloadFAB(
    onClick : () -> Unit,
    modifier: Modifier= Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary

    ) {
        Icon(
            painter = painterResource(com.piumal.filedownloadmanager.R.drawable.add_24px),
            contentDescription = "Add Download Icon",
            modifier = Modifier.size(24.dp)
        )
    }

}