package com.example.playlistmaker.player.data.impl

import android.media.MediaPlayer
import com.example.playlistmaker.player.data.repository.PlayerRepository
import com.example.playlistmaker.player.domain.model.PlayerState

class PlayerRepositoryImpl(private val mediaPlayer: MediaPlayer) : PlayerRepository {

    private var stateChangedTo: ((PlayerState) -> Unit)? = null

    override fun preparePlayer(url: String) {
        mediaPlayer.apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener {
                stateChangedTo?.invoke(PlayerState.STATE_PREPARED)
            }
            setOnCompletionListener {
                stateChangedTo?.invoke(PlayerState.STATE_COMPLETE)
            }
        }
    }

    override fun startPlayer() {
        mediaPlayer.start()
        stateChangedTo?.invoke(PlayerState.STATE_PLAYING)
    }

    override fun pausePlayer() {
        if (mediaPlayer.isPlaying) {
        mediaPlayer.pause()
        }
        stateChangedTo?.invoke(PlayerState.STATE_PAUSED)
    }

    override fun releasePlayer() {
        mediaPlayer.release()
    }

    override fun currentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    override fun changePlayerState(newState: (PlayerState) -> Unit) {
        stateChangedTo = newState
    }

}