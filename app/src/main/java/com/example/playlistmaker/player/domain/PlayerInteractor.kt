package com.example.playlistmaker.player.domain

import com.example.playlistmaker.search.domain.model.Track

interface PlayerInteractor {
    /*fun play(trackId: String, statusObserver: StatusObserver)
    fun pause(trackId: String)
    fun seek(trackId: String, position: Float)

    fun release(trackId: String)

    interface StatusObserver {
        fun onProgress(progress: Float)
        fun onStop()
        fun onPlay()
    }*/

    fun preparePlayer(url: String)
    fun startPlayer(track: Track)
    fun pausePlayer()
    fun playbackControl(track: Track)
}


