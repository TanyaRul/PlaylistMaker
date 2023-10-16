package com.example.playlistmaker.library.ui.view_model

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.db.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import kotlinx.coroutines.launch
import java.io.File

open class NewPlaylistViewModel(private val playlistsInteractor: PlaylistsInteractor) : ViewModel() {
    fun createPlaylist(playlist: Playlist) {
        playlist.playlistCoverPath = playlistsInteractor.saveImageToPrivateStorage(playlist.playlistCoverPath)

        viewModelScope.launch {
            playlistsInteractor.createPlaylist(playlist)
        }
    }
}