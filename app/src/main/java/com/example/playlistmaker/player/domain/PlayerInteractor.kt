package com.example.playlistmaker.player.domain

import com.example.playlistmaker.player.domain.model.PlayerState

interface PlayerInteractor {
    fun preparePlayer(url: String)
    fun startPlayer()
    fun pausePlayer()
    fun releasePlayer()
    fun currentPosition(): Int
    fun changePlayerState(newState: (PlayerState) -> Unit)
}