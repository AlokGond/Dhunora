package com.alok.dhunora.ui.ui.component

import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.alok.dhunora.ui.R
import com.alok.dhunora.ui.ui.icon.AutoGraph
import com.alok.dhunora.ui.ui.icon.Home
import com.alok.dhunora.ui.ui.icon.LibraryMusic
import com.alok.dhunora.ui.ui.icon.Search
import com.alok.dhunora.ui.ui.icon.Sensors
import com.alok.dhunora.ui.ui.icon.SimpIcons
import com.alok.dhunora.ui.ui.navigation.destination.home.AnalyticsDestination
import com.alok.dhunora.ui.ui.navigation.destination.home.HomeDestination
import com.alok.dhunora.ui.ui.navigation.destination.library.LibraryDestination
import com.alok.dhunora.ui.ui.navigation.destination.library.MixForYouDestination
import com.alok.dhunora.ui.ui.navigation.destination.search.SearchDestination
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

sealed class BottomNavScreen(
    val ordinal: Int,
    val destination: Any,
    @StringRes val title: Int,
    val icon: @Composable () -> Unit,
) {
    data object Home : BottomNavScreen(
        ordinal = 0,
        destination = HomeDestination,
        title = R.string.home,
        icon = {
            Icon(
                SimpIcons.Home,
                contentDescription = null,
            )
        },
    )

    data object Search : BottomNavScreen(
        ordinal = 1,
        destination = SearchDestination,
        title = R.string.search,
        icon = {
            Icon(
                SimpIcons.Search,
                contentDescription = null,
            )
        },
    )

    data object Library : BottomNavScreen(
        ordinal = 2,
        destination = LibraryDestination,
        title = R.string.library,
        icon = {
            Icon(
                imageVector = SimpIcons.LibraryMusic,
                contentDescription = null,
            )
        },
    )

    // Only shown when local tracking is enabled.
    data object Analytics : BottomNavScreen(
        ordinal = 3,
        destination = AnalyticsDestination,
        title = R.string.analytics,
        icon = {
            Icon(
                imageVector = SimpIcons.AutoGraph,
                contentDescription = null,
            )
        },
    )

    // Only shown while signed in to YouTube — an anonymous session gets no mixes.
    // Labelled "Mix", not "Mix for you": the full title is the widest label in the bar and forces
    // every tab to be that wide. The screen itself still uses the full title.
    data object MixForYou : BottomNavScreen(
        ordinal = 4,
        destination = MixForYouDestination,
        title = R.string.mix,
        icon = {
            Icon(
                imageVector = SimpIcons.Sensors,
                contentDescription = null,
            )
        },
    )
}
