package com.example.playlistmaker.library.ui.view_model

import com.example.playlistmaker.library.domain.db.PlaylistsInteractor

class PlaylistEditingViewModel(val playlistsInteractor: PlaylistsInteractor) :
    NewPlaylistViewModel(playlistsInteractor) {
}