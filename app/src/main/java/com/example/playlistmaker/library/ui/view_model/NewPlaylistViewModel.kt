package com.example.playlistmaker.library.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.db.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import kotlinx.coroutines.launch

class NewPlaylistViewModel(private val playlistsInteractor: PlaylistsInteractor) : ViewModel() {

    fun createPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistsInteractor.createPlaylist(playlist)
        }
    }

    fun saveImageToPrivateStorage(uriFile: String?) {
        viewModelScope.launch {
            playlistsInteractor.saveImageToPrivateStorage(uriFile)
        }
    }

}