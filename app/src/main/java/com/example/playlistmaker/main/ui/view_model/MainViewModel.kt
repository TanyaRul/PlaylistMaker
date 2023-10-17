package com.example.playlistmaker.main.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.settings.domain.SettingsInteractor

class MainViewModel(private val settingsInteractor: SettingsInteractor) : ViewModel() {

    var itFirstTime = true

    private val playlistLiveData = MutableLiveData<Playlist>()

    fun setTheme() {
        if (itFirstTime) {
            settingsInteractor.setThemeSettings()
            itFirstTime = false
        }
    }

    fun setPlaylist(playlist: Playlist) {
        playlistLiveData.postValue(playlist)
    }

    fun getPlaylist(): LiveData<Playlist> = playlistLiveData
}