package com.example.playlistmaker.di

import com.example.playlistmaker.library.ui.view_model.FavoriteTracksViewModel
import com.example.playlistmaker.library.ui.view_model.NewPlaylistViewModel
import com.example.playlistmaker.library.ui.view_model.PlaylistDetailsViewModel
import com.example.playlistmaker.library.ui.view_model.PlaylistEditingViewModel
import com.example.playlistmaker.library.ui.view_model.PlaylistsViewModel
import com.example.playlistmaker.main.ui.view_model.MainViewModel
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import com.example.playlistmaker.search.ui.view_model.TracksSearchViewModel
import com.example.playlistmaker.settings.ui.view_model.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        MainViewModel(settingsInteractor = get())
    }

    viewModel {
        PlayerViewModel(
            playerInteractor = get(),
            favoritesInteractor = get(),
            playlistsInteractor = get()
        )
    }

    viewModel {
        TracksSearchViewModel(application = get(), tracksInteractor = get())
    }

    viewModel {
        SettingsViewModel(sharingInteractor = get(), settingsInteractor = get())
    }

    viewModel {
        PlaylistsViewModel(playlistsInteractor = get())
    }

    viewModel {
        FavoriteTracksViewModel(favoritesInteractor = get())
    }

    viewModel {
        NewPlaylistViewModel(playlistsInteractor = get())
    }

    viewModel {
        PlaylistDetailsViewModel(
            playlistsInteractor = get(),
            sharingInteractor = get(),
            application = get(),
        )
    }

    viewModel {
        PlaylistEditingViewModel(playlistsInteractor = get())
    }
}