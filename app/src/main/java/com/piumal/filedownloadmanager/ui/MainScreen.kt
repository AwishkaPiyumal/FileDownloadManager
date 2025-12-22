@file:OptIn(ExperimentalMaterial3Api::class)

package com.piumal.filedownloadmanager.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.piumal.filedownloadmanager.ui.components.AppDrawer
import com.piumal.filedownloadmanager.ui.downloads.components.MoreOptionsMenu
import com.piumal.filedownloadmanager.ui.downloads.viewmodel.MoreOptionsViewModel
import com.piumal.filedownloadmanager.ui.navigation.NavigationGraph
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme
import kotlinx.coroutines.launch
@Composable
fun MainScreen(
    navController: NavHostController,
    drawerState: DrawerState,
    modifier: Modifier = Modifier,
    moreOptionsViewModel: MoreOptionsViewModel = hiltViewModel(),
    content: @Composable (NavHostController) -> Unit = { NavigationGraph(it) }
) {
    val scope = rememberCoroutineScope()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: ""

    // Collect menu state from ViewModel
    val isMenuExpanded by moreOptionsViewModel.isMenuExpanded.collectAsState()

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
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    CenterAlignedTopAppBar(
                        modifier = Modifier.height(105.dp),

                        title = {
                            Text(
                                "File Download Manager",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .wrapContentHeight(Alignment.CenterVertically),
                                maxLines = 1

                            )
                        },


                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(start = 16.dp, end = 4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = com.piumal.filedownloadmanager.R.drawable.menu_24px),
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(35.dp)
                                )
                            }
                        },
                        actions = {
                            // Box wrapper to position the dropdown menu correctly
                            Box {
                                IconButton(
                                    onClick = { moreOptionsViewModel.toggleMenu() },
                                    modifier = Modifier
                                        .fillMaxHeight()
                                ) {
                                    Icon(
                                        painter = painterResource(id = com.piumal.filedownloadmanager.R.drawable.more_vert_24px),
                                        contentDescription = "More",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier
                                            .padding(end = 16.dp)
                                            .size(30.dp)
                                    )
                                }

                                // Dropdown menu appears near the icon button
                                MoreOptionsMenu(
                                    expanded = isMenuExpanded,
                                    onDismiss = { moreOptionsViewModel.hideMenu() },
                                    onMenuItemClick = { action ->
                                        moreOptionsViewModel.onMenuItemSelected(action)
                                    }
                                )
                            }
                        },

                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
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