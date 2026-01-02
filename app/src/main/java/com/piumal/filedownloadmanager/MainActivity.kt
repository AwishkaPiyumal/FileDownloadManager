@file:OptIn(ExperimentalMaterial3Api::class)
package com.piumal.filedownloadmanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import com.piumal.filedownloadmanager.ui.MainScreen


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Permission launcher for POST_NOTIFICATIONS (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.w("MainActivity", "Notification permission denied")
        }
    }

    // Permission launcher for storage permissions (Android 9 and below)
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Log.d("MainActivity", "Storage permissions granted")
        } else {
            Log.w("MainActivity", "Storage permissions denied")
        }
    }

    // Launcher for MANAGE_EXTERNAL_STORAGE settings (Android 11+)
    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Log.d("MainActivity", "MANAGE_EXTERNAL_STORAGE permission granted")
            } else {
                Log.w("MainActivity", "MANAGE_EXTERNAL_STORAGE permission denied")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission for Android 13+
        requestNotificationPermission()

        // Request storage permission
        requestStoragePermission()

        setContent {
            FileDownloadManagerTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                MainScreen(navController = navController, drawerState = drawerState)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("MainActivity", "Notification permission already granted")
                }
                else -> {
                    Log.d("MainActivity", "Requesting notification permission")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d("MainActivity", "Notification permission not required for this Android version")
        }
    }

    private fun requestStoragePermission() {
        when {
            // Android 11+ (API 30+): Need MANAGE_EXTERNAL_STORAGE
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (!Environment.isExternalStorageManager()) {
                    Log.d("MainActivity", "Requesting MANAGE_EXTERNAL_STORAGE permission")
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = Uri.parse("package:$packageName")
                        manageStorageLauncher.launch(intent)
                    } catch (e: Exception) {
                        // Fallback for devices that don't support the specific intent
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        manageStorageLauncher.launch(intent)
                    }
                } else {
                    Log.d("MainActivity", "MANAGE_EXTERNAL_STORAGE already granted")
                }
            }
            // Android 10 (API 29): requestLegacyExternalStorage handles this
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                Log.d("MainActivity", "Using requestLegacyExternalStorage for Android 10")
            }
            // Android 9 and below (API <= 28): Need WRITE_EXTERNAL_STORAGE
            else -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d("MainActivity", "Requesting WRITE_EXTERNAL_STORAGE permission")
                    storagePermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    )
                } else {
                    Log.d("MainActivity", "Storage permissions already granted")
                }
            }
        }
    }
}