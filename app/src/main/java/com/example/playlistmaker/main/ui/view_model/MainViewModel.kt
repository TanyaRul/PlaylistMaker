package com.example.playlistmaker.main.ui.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.settings.domain.SettingsInteractor

class MainViewModel(private val settingsInteractor: SettingsInteractor) : ViewModel() {

    var itFirstTime = true

    fun setTheme() {
        if (itFirstTime) {
            settingsInteractor.setThemeSettings()
            itFirstTime = false
        }
    }

    private val currentTrackLiveData = MutableLiveData<Track>()

    /*fun setCurrentTrack(track: Track) {
        Log.d("PLAYLIST MVM track", track.toString())
        currentTrackLiveData.postValue(track)
    }

    fun getCurrentTrack(): LiveData<Track> = currentTrackLiveData*/

    private val playlistLiveData = MutableLiveData<Playlist>()

    fun setPlaylist(playlist: Playlist) {
        playlistLiveData.postValue(playlist)
    }

    fun getPlaylist(): LiveData<Playlist> = playlistLiveData

}