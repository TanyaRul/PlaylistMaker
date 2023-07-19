package com.example.playlistmaker.player.data.impl

import android.media.MediaPlayer
import com.example.playlistmaker.player.data.repository.PlayerRepository
import com.example.playlistmaker.player.domain.model.PlayerState

class PlayerRepositoryImpl : PlayerRepository {

    private var mediaPlayer = MediaPlayer()
    private var stateChangedTo: ((PlayerState) -> Unit)? = null

    override fun preparePlayer(url: String) {
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            stateChangedTo?.invoke(PlayerState.STATE_PREPARED)
        }
        mediaPlayer.setOnCompletionListener {
            stateChangedTo?.invoke(PlayerState.STATE_COMPLETE)
        }
    }

    override fun startPlayer() {
        mediaPlayer.start()
        stateChangedTo?.invoke(PlayerState.STATE_PLAYING)
    }

    override fun pausePlayer() {
        mediaPlayer.pause()
        stateChangedTo?.invoke(PlayerState.STATE_PAUSED)
    }

    override fun releasePlayer() {
        mediaPlayer.release()
        stateChangedTo?.invoke(PlayerState.STATE_DEFAULT)
    }

    override fun currentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    override fun changePlayerState(newState: (PlayerState) -> Unit) {
        stateChangedTo = newState
    }

}