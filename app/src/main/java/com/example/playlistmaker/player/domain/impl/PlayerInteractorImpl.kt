package com.example.playlistmaker.player.domain.impl

import com.example.playlistmaker.player.data.repository.PlayerRepository
import com.example.playlistmaker.player.domain.PlayerInteractor

class PlayerInteractorImpl(private val repository: PlayerRepository) : PlayerInteractor {

    override fun preparePlayer(url: String) {
        repository.preparePlayer(url = url)
    }

    override fun startPlayer() {
        repository.startPlayer()
    }

    override fun pausePlayer() {
        repository.pausePlayer()
    }

    override fun release() {
        repository.release()
    }

}