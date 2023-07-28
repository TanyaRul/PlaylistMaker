package com.example.playlistmaker.player.data.repository

import com.example.playlistmaker.player.domain.model.PlayerState

interface PlayerRepository {
    fun preparePlayer(url: String)
    fun startPlayer()
    fun pausePlayer()
    fun releasePlayer()
    fun currentPosition(): Int
    fun changePlayerState(newState: (PlayerState) -> Unit)
}