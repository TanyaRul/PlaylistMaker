package com.example.playlistmaker.di

import android.content.Context
import android.media.MediaPlayer
import com.example.playlistmaker.library.data.db.converters.PlaylistDbConverter
import com.example.playlistmaker.player.data.impl.PlayerRepositoryImpl
import com.example.playlistmaker.player.data.repository.PlayerRepository
import com.example.playlistmaker.library.data.db.converters.TrackDbConverter
import com.example.playlistmaker.library.data.db.converters.TrackInPlaylistConverter
import com.example.playlistmaker.library.data.impl.FavoritesRepositoryImpl
import com.example.playlistmaker.library.data.impl.PlaylistsRepositoryImpl
import com.example.playlistmaker.search.data.impl.TracksRepositoryImpl
import com.example.playlistmaker.search.data.repository.TracksRepository
import com.example.playlistmaker.library.domain.db.FavoritesRepository
import com.example.playlistmaker.library.domain.db.PlaylistsRepository
import com.example.playlistmaker.settings.data.impl.SettingsRepositoryImpl
import com.example.playlistmaker.settings.data.impl.SettingsRepositoryImpl.Companion.SETTINGS_PREFS
import com.example.playlistmaker.settings.data.repository.SettingsRepository
import com.example.playlistmaker.sharing.data.impl.ExternalNavigatorImpl
import com.example.playlistmaker.sharing.data.repository.ExternalNavigator
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {

    single<TracksRepository> {
        TracksRepositoryImpl(
            networkClient = get(),
            searchHistoryStorage = get(),
            context = get(),
            appDatabase = get(),
        )
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

    factory { TrackDbConverter() }

    single<FavoritesRepository> {
        FavoritesRepositoryImpl(appDatabase = get(), trackDbConverter = get())
    }

    factory { PlaylistDbConverter() }

    factory { TrackInPlaylistConverter() }

    single<PlaylistsRepository> {
        PlaylistsRepositoryImpl(
            appDatabase = get(),
            playlistDbConverter = get(),
            trackInPlaylistConverter = get(),
            imageStorage = get(),
        )
    }

}