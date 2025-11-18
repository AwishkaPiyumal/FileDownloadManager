@file:OptIn(ExperimentalMaterial3Api::class)
package com.piumal.filedownloadmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.piumal.filedownloadmanager.ui.navigation.bottomNavItems
import com.piumal.filedownloadmanager.ui.settings.SettingsScreen
import com.piumal.filedownloadmanager.ui.theme.GreenJC


@Composable
fun AppDrawer(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet {
        Box(
            modifier = Modifier
                .background(GreenJC)
                .fillMaxWidth()
                .height(80.dp)
                .padding(20.dp)
        ) {
            Text(
                text = "File Download Manager",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        HorizontalDivider()

        bottomNavItems.forEach { screen ->
            NavigationDrawerItem(
                label = { Text(text = screen.title, color = GreenJC) },
                selected = currentRoute == screen.route,
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = GreenJC
                    )
                },
                onClick = {
                    onNavigate(screen.route)
                    onCloseDrawer()
                }
            )
        }
    }
}
@Preview
@Composable
fun AppDrawerPreview() {
    AppDrawer(
        currentRoute = "home",
        onNavigate = {},
        onCloseDrawer = {}
    )
}