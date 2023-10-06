package com.example.playlistmaker.player.ui.states

sealed interface PlayerToastState {
    object None: PlayerToastState
    data class ShowMessage(val message: String): PlayerToastState
}