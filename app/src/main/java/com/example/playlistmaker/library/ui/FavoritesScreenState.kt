package com.example.playlistmaker.library.ui

import com.example.playlistmaker.search.domain.model.Track

sealed interface FavoritesScreenState {
    data class Content(
        val tracks: List<Track>
    ) : FavoritesScreenState

    object Empty : FavoritesScreenState
}