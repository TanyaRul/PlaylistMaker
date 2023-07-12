package com.example.playlistmaker.player.domain

interface PlayerInteractor {

    fun preparePlayer(url: String)
    fun startPlayer()
    fun pausePlayer()
    fun release()
}


