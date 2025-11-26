@file:OptIn(ExperimentalMaterial3Api::class)

package com.piumal.filedownloadmanager.ui

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.piumal.filedownloadmanager.ui.components.AppDrawer
import com.piumal.filedownloadmanager.ui.navigation.NavigationGraph
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme
import com.piumal.filedownloadmanager.ui.theme.PrimaryBlue
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
                CenterAlignedTopAppBar(
                    modifier = Modifier.height(105.dp),

                    title = { Text("File Download Manager",
                            style=MaterialTheme.typography.titleLarge,
                            modifier=Modifier
                                .fillMaxHeight()
                                .wrapContentHeight(Alignment.CenterVertically),
                                     maxLines = 1

                            )
                            },


                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } },
                            modifier= Modifier
                                .fillMaxHeight()
                                .padding(start = 16.dp, end = 4.dp)
                        ) {
                            Icon(painter = painterResource(id = com.piumal.filedownloadmanager.R.drawable.menu_24px),
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(35.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* handle more actions */ },

                            modifier= Modifier
                                .fillMaxHeight()
                               //.padding(16.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = com.piumal.filedownloadmanager.R.drawable.more_vert_24px),
                                contentDescription = "More",
                                tint = Color.White,
                                modifier = Modifier.padding(end = 16.dp).size(30.dp)
                            )
                        }
                    },

                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryBlue,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White)
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