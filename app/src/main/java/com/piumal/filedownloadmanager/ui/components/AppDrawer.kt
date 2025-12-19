// Kotlin
@file:OptIn(ExperimentalMaterial3Api::class)
package com.piumal.filedownloadmanager.ui.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.piumal.filedownloadmanager.ui.navigation.bottomNavItems
import com.piumal.filedownloadmanager.ui.theme.FileDownloadManagerTheme
import com.piumal.filedownloadmanager.ui.theme.White

@Composable
fun AppDrawer(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    onExit: (() -> Unit)? = null
) {
val context = LocalContext.current

    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerShape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp),
        drawerContainerColor = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = { onCloseDrawer() }) {
                        Icon(
                            painter = painterResource(com.piumal.filedownloadmanager.R.drawable.menu_open_24px),
                            contentDescription = "menu open icon",
                            tint = White,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text = "File Download Manager",
                            style = MaterialTheme.typography.headlineSmall,
                            color = White,
                            maxLines = 1
                        )
                    }
                }


            Spacer(modifier = Modifier.height(50.dp))



            Column(modifier = Modifier.weight(1f)) {
                bottomNavItems.forEach { screen ->
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = screen.title,
                                color = MaterialTheme.colorScheme.onSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        selected = currentRoute == screen.route,
                        icon = {
                            when {
                                screen.icon != null -> {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        tint = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                                screen.iconRes != null -> {
                                    Icon(
                                        painter = painterResource(id = screen.iconRes),
                                        contentDescription = screen.title,
                                        tint = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }
                        },
                        onClick = {
                            onNavigate(screen.route)
                            onCloseDrawer()
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedTextColor = MaterialTheme.colorScheme.onSecondary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSecondary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            selectedIconColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            NavigationDrawerItem(
                label = {
                    Text(
                        text = "Exit",
                        color = MaterialTheme.colorScheme.onSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                selected = false,
                icon = {
                    Icon(
                        painter = painterResource(com.piumal.filedownloadmanager.R.drawable.logout_24px),
                        contentDescription = "Exit",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                },
                onClick = {
                    onCloseDrawer()
                    // prefer explicit callback when provided (for tests/preview). Otherwise close app safely.
                    onExit?.invoke() ?: runCatching {
                        (context as? Activity)?.finishAffinity()
                    }.getOrNull()
                },
                modifier = Modifier.padding(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun AppDrawerLightPreview() {
    FileDownloadManagerTheme(darkTheme = false) {
        AppDrawer(
            currentRoute = "downloads",
            onNavigate = {},
            onCloseDrawer = {},
            onExit = {}
        )
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
fun AppDrawerDarkPreview() {
    FileDownloadManagerTheme(darkTheme = true) {
        AppDrawer(
            currentRoute = "downloads",
            onNavigate = {},
            onCloseDrawer = {},
            onExit = {}
        )
    }
}
