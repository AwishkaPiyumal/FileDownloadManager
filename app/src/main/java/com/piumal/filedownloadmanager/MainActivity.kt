@file:OptIn(ExperimentalMaterial3Api::class)
package com.piumal.filedownloadmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.navigation.compose.rememberNavController
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import com.piumal.filedownloadmanager.ui.MainScreen


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FileDownloadManagerTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                MainScreen(navController = navController, drawerState = drawerState)
            }
        }
    }
}