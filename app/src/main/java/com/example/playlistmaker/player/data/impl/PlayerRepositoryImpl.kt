package com.example.playlistmaker.player.data.impl

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import com.example.playlistmaker.player.data.repository.PlayerRepository
import com.example.playlistmaker.player.domain.model.PlayerState
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerRepositoryImpl(val context: Context) : PlayerRepository {

    private lateinit var playbackProgress: TextView
    //private lateinit var playbackControlButton: ImageButton
    private var mediaPlayer = MediaPlayer()
    private var playerState = PlayerState.STATE_DEFAULT
    private val handler = Handler(Looper.getMainLooper())
    private val playbackProgressRunnable = createUpdateTimerTask()

    override fun preparePlayer(url: String) {
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            playerState = PlayerState.STATE_PREPARED
            //playbackProgress.text = context.getString(R.string.playback_progress_start)
        }
        mediaPlayer.setOnCompletionListener {
            handler.removeCallbacks(playbackProgressRunnable)
            //playbackControlButton.setImageResource(R.drawable.play_button)
            playerState = PlayerState.STATE_PREPARED
            //playbackProgress.text = context.getString(R.string.playback_progress_start)
        }
    }

    override fun startPlayer() {
        //if (track.previewUrl?.isNotEmpty() == true) {
        mediaPlayer.start()
        handler.post(playbackProgressRunnable)
        //playbackControlButton.setImageResource(R.drawable.pause_button)
        playerState = PlayerState.STATE_PLAYING
        //}
    }

    override fun pausePlayer() {
        mediaPlayer.pause()
        handler.removeCallbacks(playbackProgressRunnable)
        //playbackControlButton.setImageResource(R.drawable.play_button)
        playerState = PlayerState.STATE_PAUSED
    }

    override fun release() {
        mediaPlayer.release()
    }

    private fun createUpdateTimerTask(): Runnable {
        return object : Runnable {
            override fun run() {
                playbackProgress.text = SimpleDateFormat(
                    "mm:ss",
                    Locale.getDefault()
                ).format(mediaPlayer.currentPosition)
                handler.postDelayed(this, UPDATE_PLAYBACK_PROGRESS_DELAY_MS)
            }
        }
    }

    companion object {
        private const val UPDATE_PLAYBACK_PROGRESS_DELAY_MS = 500L
    }
}