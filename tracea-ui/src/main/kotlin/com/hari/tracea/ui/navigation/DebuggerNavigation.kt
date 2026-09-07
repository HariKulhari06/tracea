package com.hari.tracea.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.compose.material.icons.filled.Laptop
import com.hari.tracea.ui.screens.detail.RequestDetailScreen
import com.hari.tracea.ui.screens.network.NetworkListScreen
import com.hari.tracea.ui.screens.settings.SettingsScreen
import com.hari.tracea.ui.screens.timeline.TimelineScreen
import com.hari.tracea.ui.screens.mocks.MockRulesScreen
import com.hari.tracea.ui.screens.web.WebDashboardScreen
import kotlinx.serialization.Serializable

@Serializable object NetworkRoute
@Serializable data class RequestDetailRoute(val eventId: String)
@Serializable object TimelineRoute
@Serializable object MocksRoute
@Serializable object WebDashboardRoute
@Serializable object SettingsRoute

enum class DebuggerTab(val label: String, val icon: ImageVector) {
    NETWORK("Network", Icons.Default.List),
    MOCKS("Mocks", Icons.Default.Tune),
    WEB("PC Web", Icons.Default.Laptop)
}

@Composable
fun DebuggerNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NetworkRoute) {
        composable<NetworkRoute> {
            NetworkListScreen(
                onEventClick = { id -> navController.navigate(RequestDetailRoute(id)) }
            )
        }
        composable<RequestDetailRoute> { backStackEntry ->
            val route: RequestDetailRoute = backStackEntry.toRoute()
            RequestDetailScreen(
                eventId = route.eventId,
                onBack = { navController.popBackStack() }
            )
        }
        composable<TimelineRoute> {
            TimelineScreen(
                onEventClick = { id -> navController.navigate(RequestDetailRoute(id)) }
            )
        }
        composable<MocksRoute> {
            MockRulesScreen()
        }
        composable<WebDashboardRoute> {
            WebDashboardScreen()
        }
        composable<SettingsRoute> {
            SettingsScreen()
        }
    }
}
