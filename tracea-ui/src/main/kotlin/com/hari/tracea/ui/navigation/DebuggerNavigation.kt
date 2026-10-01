package com.hari.tracea.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.hari.tracea.ui.screens.detail.RequestDetailScreen
import com.hari.tracea.ui.screens.network.NetworkListScreen
import com.hari.tracea.ui.screens.settings.DomainFilterSettingsScreen
import com.hari.tracea.ui.screens.settings.SettingsScreen
import com.hari.tracea.ui.screens.timeline.TimelineScreen
import com.hari.tracea.ui.screens.mocks.MockRulesScreen
import com.hari.tracea.ui.screens.settings.RedactionSettingsScreen
import kotlinx.serialization.Serializable

@Serializable object NetworkRoute
@Serializable data class RequestDetailRoute(val eventId: String)
@Serializable object TimelineRoute
@Serializable object MocksRoute
@Serializable object SettingsRoute
@Serializable object DomainFilterRoute
@Serializable object RedactionRoute

enum class DebuggerTab(val label: String, val icon: ImageVector) {
    NETWORK("Network", Icons.Default.List),
    TIMELINE("Timeline", Icons.Default.Schedule),
    MOCKS("Mocks", Icons.Default.Tune)
}

@Composable
fun DebuggerNavHost(
    navController: NavHostController,
    onClose: (() -> Unit)? = null
) {
    NavHost(navController = navController, startDestination = NetworkRoute) {
        composable<NetworkRoute> {
            NetworkListScreen(
                onEventClick = { id -> navController.navigate(RequestDetailRoute(id)) },
                onSettingsClick = { navController.navigate(SettingsRoute) },
                onClose = onClose
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
        composable<RedactionRoute> {
            RedactionSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<SettingsRoute> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onDomainFilterClick = { navController.navigate(DomainFilterRoute) }
            )
        }
        composable<DomainFilterRoute> {
            DomainFilterSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
