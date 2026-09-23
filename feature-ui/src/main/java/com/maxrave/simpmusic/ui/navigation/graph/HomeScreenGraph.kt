package com.alok.dhunora.ui.ui.navigation.graph

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.alok.dhunora.ui.ui.navigation.destination.home.MoodDestination
import com.alok.dhunora.ui.ui.navigation.destination.home.RecentlySongsDestination
import com.alok.dhunora.ui.ui.navigation.destination.home.SettingsDestination
import com.alok.dhunora.ui.ui.screen.home.MoodScreen
import com.alok.dhunora.ui.ui.screen.home.RecentlySongsScreen
import com.alok.dhunora.ui.ui.screen.home.SettingScreen

fun NavGraphBuilder.homeScreenGraph(
    innerPadding: PaddingValues,
    navController: NavController,
) {
    composable<MoodDestination> { entry ->
        val params = entry.toRoute<MoodDestination>().params
        MoodScreen(
            navController = navController,
            params = params,
        )
    }
    composable<RecentlySongsDestination> {
        RecentlySongsScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
    composable<SettingsDestination> {
        SettingScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
}