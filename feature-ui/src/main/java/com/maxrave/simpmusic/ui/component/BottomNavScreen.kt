package com.maxrave.simpmusic.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.maxrave.simpmusic.ui.navigation.destination.home.HomeDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDestination
import com.maxrave.simpmusic.ui.navigation.destination.search.SearchDestination

sealed class BottomNavScreen(
    val ordinal: Int,
    val destination: Any,
    val title: String,
    val icon: @Composable () -> Unit,
) {
    data object Home : BottomNavScreen(
        ordinal = 0,
        destination = HomeDestination,
        title = "Home",
        icon = {
            Icon(
                Icons.Default.Home,
                contentDescription = null,
            )
        },
    )

    data object Search : BottomNavScreen(
        ordinal = 1,
        destination = SearchDestination,
        title = "Search",
        icon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
            )
        },
    )

    data object Library : BottomNavScreen(
        ordinal = 2,
        destination = LibraryDestination,
        title = "Library",
        icon = {
            Icon(
                Icons.Default.LibraryMusic,
                contentDescription = null,
            )
        },
    )

    data object MixForYou : BottomNavScreen(
        ordinal = 3,
        destination = HomeDestination, // Placeholder
        title = "Mix",
        icon = {
            Icon(
                Icons.Default.Home,
                contentDescription = null,
            )
        },
    )
}
