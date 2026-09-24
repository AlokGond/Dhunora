package com.alok.dhunora.ui.ui.component

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.alok.dhunora.ui.viewModel.SharedViewModel
import kotlin.reflect.KClass

@Composable
fun LiquidGlassAppBottomNavigationBar(
    startDestination: Any? = null,
    navController: NavController,
    backdrop: Any? = null,
    viewModel: SharedViewModel,
    isScrolledToTop: Boolean,
    showMixForYouTab: Boolean,
    onOpenNowPlaying: () -> Unit,
    reloadDestinationIfNeeded: (KClass<*>) -> Unit,
) {
    // Fallback to regular bottom navigation bar
    // TODO: Implement Liquid Glass variant
    AppBottomNavigationBar(
        navController = navController,
        showMixForYouTab = showMixForYouTab,
        reloadDestinationIfNeeded = reloadDestinationIfNeeded,
    )
}
