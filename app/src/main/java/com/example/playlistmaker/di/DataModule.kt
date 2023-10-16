package com.example.playlistmaker.di

import android.content.Context
import androidx.room.Room
import com.example.playlistmaker.library.data.db.AppDatabase
import com.example.playlistmaker.library.data.db.AppDatabase.Companion.DB_NAME
import com.example.playlistmaker.library.data.storage.ImageStorage
import com.example.playlistmaker.library.data.storage.ImageStorageImpl
import com.example.playlistmaker.search.data.network.ItunesApi
import com.example.playlistmaker.search.data.network.NetworkClient
import com.example.playlistmaker.search.data.network.RetrofitNetworkClient
import com.example.playlistmaker.search.data.storage.SearchHistoryStorage
import com.example.playlistmaker.search.data.storage.SharedPrefsSearchHistoryStorage
import com.example.playlistmaker.search.data.storage.SharedPrefsSearchHistoryStorage.Companion.SEARCH_HISTORY_KEY
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataModule = module {

    single<ItunesApi> {
        Retrofit.Builder()
            .baseUrl("http://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesApi::class.java)
    }

    single {
        androidContext()
            .getSharedPreferences(SEARCH_HISTORY_KEY, Context.MODE_PRIVATE)
    }

    single<SearchHistoryStorage> {
        SharedPrefsSearchHistoryStorage(sharedPrefs = get(), gson = get())
    }

    single<NetworkClient> {
        RetrofitNetworkClient(itunesService = get(), androidContext())
    }

    factory { Gson() }

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, DB_NAME)
            .build()
    }

    single<ImageStorage> {
        ImageStorageImpl(get())
    }

}