package com.example.playlistmaker.player.data.repository

import com.example.playlistmaker.search.domain.model.Track

interface PlayerRepository {
    /*// 1
    fun play(trackId: String, statusObserver: StatusObserver)
    fun pause(trackId: String)
    fun seek(trackId: String, position: Float)

    fun release(trackId: String)

    // 2
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