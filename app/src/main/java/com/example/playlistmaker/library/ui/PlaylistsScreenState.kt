package com.example.playlistmaker.library.ui

import com.example.playlistmaker.library.domain.model.Playlist

sealed interface PlaylistsScreenState {
    data class Content(
        val playlists: List<Playlist>
    ) : PlaylistsScreenState

    object Empty : PlaylistsScreenState
}