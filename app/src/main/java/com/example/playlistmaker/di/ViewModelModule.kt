package com.example.playlistmaker.di

import com.example.playlistmaker.main.ui.view_model.MainViewModel
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import com.example.playlistmaker.search.ui.view_model.TracksSearchViewModel
import com.example.playlistmaker.settings.ui.view_model.SettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        MainViewModel(settingsInteractor = get())
    }

    viewModel {
        PlayerViewModel(playerInteractor = get())
    }

    viewModel {
        TracksSearchViewModel(application = androidApplication(), tracksInteractor = get())
    }

    viewModel {
        SettingsViewModel(sharingInteractor = get(), settingsInteractor = get())
    }

}