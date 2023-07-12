package com.example.playlistmaker.player.data.repository

interface PlayerRepository {

    fun preparePlayer(url: String)
    fun startPlayer()
    fun pausePlayer()
    fun release()
}