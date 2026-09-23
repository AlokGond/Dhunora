package com.alok.dhunora.ui.di

import com.alok.dhunora.ui.viewModel.AlbumViewModel
import com.alok.dhunora.ui.viewModel.AnalyticsViewModel
import com.alok.dhunora.ui.utils.VersionManager
import com.alok.dhunora.ui.viewModel.ArtistViewModel
import com.alok.dhunora.ui.viewModel.BrowseViewModel
import com.alok.dhunora.ui.viewModel.HomeViewModel
import com.alok.dhunora.ui.viewModel.ImportViewModel
import com.alok.dhunora.ui.viewModel.LibraryDynamicPlaylistViewModel
import com.alok.dhunora.ui.viewModel.LibraryViewModel
import com.alok.dhunora.ui.viewModel.LocalPlaylistViewModel
import com.alok.dhunora.ui.viewModel.MoodViewModel
import com.alok.dhunora.ui.viewModel.MoreAlbumsViewModel
import com.alok.dhunora.ui.viewModel.NowPlayingBottomSheetViewModel
import com.alok.dhunora.ui.viewModel.PlaylistViewModel
import com.alok.dhunora.ui.viewModel.RecentlySongsViewModel
import com.alok.dhunora.ui.viewModel.SearchViewModel
import com.alok.dhunora.ui.viewModel.AutoEqViewModel
import com.alok.dhunora.ui.viewModel.SettingsViewModel
import com.alok.dhunora.ui.viewModel.SharedViewModel
import com.alok.dhunora.ui.viewModel.SongSelectionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule =
    module {
        single {
            SharedViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        single {
            SearchViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            SongSelectionViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            NowPlayingBottomSheetViewModel(
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LibraryViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LibraryDynamicPlaylistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ImportViewModel(
                get(),
            )
        }
        viewModel {
            AlbumViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            HomeViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            AutoEqViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            SettingsViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ArtistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            PlaylistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            MoreAlbumsViewModel(
                get(),
            )
        }
        viewModel {
            AnalyticsViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            BrowseViewModel(
                get(),
            )
        }
        viewModel {
            RecentlySongsViewModel(
                get(),
            )
        }
        viewModel {
            LocalPlaylistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            MoodViewModel(
                get(),
                get(),
            )
        }

    }