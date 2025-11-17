package com.piumal.filedownloadmanager.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.piumal.filedownloadmanager.ui.theme.GreenJC

@Composable
fun SettingsScreen() {
    Box(modifier=Modifier.fillMaxSize()) {
        Column( modifier = Modifier.fillMaxSize().align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Text(text = "Settings", color = GreenJC)
        }
    }
}
@Preview
@Composable
fun SettingsPreview() {
    SettingsScreen()
}