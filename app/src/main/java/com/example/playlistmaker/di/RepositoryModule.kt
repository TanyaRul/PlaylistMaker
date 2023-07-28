package com.example.playlistmaker.di

import android.content.Context
import android.media.MediaPlayer
import com.example.playlistmaker.player.data.impl.PlayerRepositoryImpl
import com.example.playlistmaker.player.data.repository.PlayerRepository
import com.example.playlistmaker.search.data.impl.TracksRepositoryImpl
import com.example.playlistmaker.search.data.repository.TracksRepository
import com.example.playlistmaker.settings.data.impl.SettingsRepositoryImpl
import com.example.playlistmaker.settings.data.impl.SettingsRepositoryImpl.Companion.SETTINGS_PREFS
import com.example.playlistmaker.settings.data.repository.SettingsRepository
import com.example.playlistmaker.sharing.data.impl.ExternalNavigatorImpl
import com.example.playlistmaker.sharing.data.repository.ExternalNavigator
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {

    single<TracksRepository> {
        TracksRepositoryImpl(networkClient = get(), searchHistoryStorage = get())
    }

    factory<PlayerRepository> {
        PlayerRepositoryImpl(mediaPlayer = get())
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(sharedPrefs = get())
    }

    single<ExternalNavigator> {
        ExternalNavigatorImpl(context = get())
    }

    single {
        androidContext().getSharedPreferences(
            SETTINGS_PREFS,
            Context.MODE_PRIVATE
        )
    }

    factory {
        MediaPlayer()
    }

}