package com.example.playlistmaker.player.domain.impl

import com.example.playlistmaker.player.data.repository.PlayerRepository
import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.player.domain.model.PlayerState

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

    override fun releasePlayer() {
        repository.releasePlayer()
    }

    override fun currentPosition(): Int {
        return repository.currentPosition()
    }

    override fun changePlayerState(newState: (PlayerState) -> Unit) {
        repository.changePlayerState(newState)
    }

}