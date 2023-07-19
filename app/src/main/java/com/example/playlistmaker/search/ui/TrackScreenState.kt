package com.example.playlistmaker.search.ui

import com.example.playlistmaker.search.domain.model.Track

sealed interface TrackScreenState {
    object Loading : TrackScreenState

    data class Content(
        val trackList: List<Track>
    ) : TrackScreenState

    data class Error(
        val errorMessage: String
    ) : TrackScreenState

    data class Empty(
        val message: String
    ) : TrackScreenState
}