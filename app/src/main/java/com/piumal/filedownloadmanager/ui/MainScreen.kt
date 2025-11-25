@file:OptIn(ExperimentalMaterial3Api::class)

package com.piumal.filedownloadmanager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.piumal.filedownloadmanager.ui.components.AppDrawer
import com.piumal.filedownloadmanager.ui.navigation.NavigationGraph
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme
import kotlinx.coroutines.launch
@Composable
fun MainScreen(
    navController: NavHostController,
    drawerState: DrawerState,
    modifier: Modifier = Modifier,
    content: @Composable (NavHostController) -> Unit = { NavigationGraph(it) }
) {
    val scope = rememberCoroutineScope()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: ""

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    val startId = runCatching { navController.graph.startDestinationId }.getOrNull()
                    navController.navigate(route) {
                        startId?.let { popUpTo(it) }
                        launchSingleTop = true
                    }
                },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("File Download Manager") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Rounded.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Surface(modifier = modifier.padding(paddingValues)) {
                content(navController) // use provided content (placeholder in preview)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    FileDownloadManagerTheme {
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        MainScreen(navController = navController, drawerState = drawerState) { _ ->
            // lightweight preview content to avoid NavigationGraph runtime issues
            androidx.compose.foundation.layout.Box(modifier = Modifier.padding(16.dp)) {
                Text("Preview: MainScreen")
            }
        }
    }
}