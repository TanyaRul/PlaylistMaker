package com.example.playlistmaker.library.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.db.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import kotlinx.coroutines.launch

open class NewPlaylistViewModel(private val playlistsInteractor: PlaylistsInteractor) :
    ViewModel() {

    fun createPlaylist(playlist: Playlist) {
        playlist.playlistCoverPath =
            playlistsInteractor.saveImageToPrivateStorage(playlist.playlistCoverPath)

        viewModelScope.launch {
            playlistsInteractor.createPlaylist(playlist)
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistsInteractor.updatePlaylist(playlist)
        }
    }

}