package com.example.playlistmaker.library.domain.model.states

sealed interface AddTrackState {
    data class Added(val playlistTitle: String? = null) : AddTrackState
    data class Exist(val playlistTitle: String? = null) : AddTrackState
}

