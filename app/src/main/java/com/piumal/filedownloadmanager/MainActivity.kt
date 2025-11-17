@file:OptIn(ExperimentalMaterial3Api::class)
package com.piumal.filedownloadmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.piumal.filedownloadmanager.ui.components.AppDrawer
import com.piumal.filedownloadmanager.ui.navigation.NavigationGraph
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.rounded.Menu

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FileDownloadManagerTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route ?: ""

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawer(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                            onCloseDrawer = {
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("File Download Manager") },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        scope.launch { drawerState.open() }
                                    }) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Rounded.Menu,
                                            contentDescription = "Hamburger Menu "
                                        )
                                    }
                                }
                            )
                        }
                    ) { paddingValues ->
                        Surface(modifier = androidx.compose.ui.Modifier.padding(paddingValues)) {
                            NavigationGraph(navController)
                        }
                    }
                }
            }
        }
    }
}
