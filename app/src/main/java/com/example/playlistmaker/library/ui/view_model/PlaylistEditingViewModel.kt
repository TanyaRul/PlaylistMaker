package com.example.playlistmaker.library.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.db.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import kotlinx.coroutines.launch
import java.io.File

class PlaylistEditingViewModel(playlistsInteractor: PlaylistsInteractor) : NewPlaylistViewModel(playlistsInteractor) {

}