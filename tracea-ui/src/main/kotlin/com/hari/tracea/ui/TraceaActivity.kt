package com.hari.tracea.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hari.tracea.ui.navigation.DebuggerNavHost
import com.hari.tracea.ui.navigation.DebuggerTab
import com.hari.tracea.ui.navigation.NetworkRoute
import com.hari.tracea.ui.navigation.RequestDetailRoute
import com.hari.tracea.ui.navigation.MocksRoute
import com.hari.tracea.ui.theme.DebuggerTheme
import com.hari.tracea.ui.theme.LocalDebuggerColors

import com.hari.tracea.ui.navigation.WebDashboardRoute

class TraceaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DebuggerTheme {
                val navController = rememberNavController()
                val colors = LocalDebuggerColors.current
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val showBottomBar = currentDestination?.hasRoute<RequestDetailRoute>() != true

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = colors.surfaceVariant
                            ) {
                                NavigationItems(currentDestination, navController)
                            }
                        }
                    },
                    containerColor = colors.surface,
                    contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                            .background(colors.surface)
                    ) {
                        DebuggerNavHost(navController)
                    }
                }
            }
        }
    }

    @Composable
    private fun RowScope.NavigationItems(currentDestination: NavDestination?, navController: NavHostController) {
        val colors = LocalDebuggerColors.current
        DebuggerTab.entries.forEach { tab ->
            val selected = when (tab) {
                DebuggerTab.NETWORK -> currentDestination?.hasRoute<NetworkRoute>() == true
                DebuggerTab.MOCKS -> currentDestination?.hasRoute<MocksRoute>() == true
                DebuggerTab.WEB -> currentDestination?.hasRoute<WebDashboardRoute>() == true
            }

            NavigationBarItem(
                selected = selected,
                onClick = {
                    val route = when (tab) {
                        DebuggerTab.NETWORK -> NetworkRoute
                        DebuggerTab.MOCKS -> MocksRoute
                        DebuggerTab.WEB -> WebDashboardRoute
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { 
                            saveState = true 
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    unselectedIconColor = colors.onSurfaceVariant,
                    unselectedTextColor = colors.onSurfaceVariant,
                    indicatorColor = colors.surfaceContainer
                )
            )
        }
    }
}
