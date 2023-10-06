package com.example.playlistmaker.player.ui.states

sealed interface AddTrackState {
    data class Added(val playlistTitle: String? = null) : AddTrackState
    data class Exist(val playlistTitle: String? = null) : AddTrackState
}

