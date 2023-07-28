package com.example.playlistmaker.player.ui.view_model

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.player.domain.model.PlayerState

class PlayerViewModel(private val playerInteractor: PlayerInteractor) : ViewModel() {

    private val handler = Handler(Looper.getMainLooper())
    private val playbackProgressRunnable = createUpdateTimerTask()

    private var currentTimerLiveData = MutableLiveData(0)
    fun getCurrentTimerLiveData(): LiveData<Int> = currentTimerLiveData

    private var _playerStateLiveData = MutableLiveData<PlayerState>()
    val playerStateLiveData: LiveData<PlayerState> = _playerStateLiveData

    init {
        playerInteractor.changePlayerState { state ->
            _playerStateLiveData.postValue(state)
            if (state == PlayerState.STATE_COMPLETE) handler.removeCallbacks(
                playbackProgressRunnable
            )
        }
    }

    private fun createUpdateTimerTask(): Runnable {
        return object : Runnable {
            override fun run() {
                val currentTimerPosition = playerInteractor.currentPosition()
                handler.postDelayed(this, UPDATE_PLAYBACK_PROGRESS_DELAY_MS)
                currentTimerLiveData.postValue(currentTimerPosition)
            }
        }
    }

    fun prepare(url: String) {
        handler.removeCallbacks(playbackProgressRunnable)
        playerInteractor.preparePlayer(url)
    }

    fun play() {
        playerInteractor.startPlayer()
        handler.post(playbackProgressRunnable)
    }

    fun pause() {
        playerInteractor.pausePlayer()
        handler.removeCallbacks(playbackProgressRunnable)
    }

    override fun onCleared() {
        super.onCleared()
        playerInteractor.releasePlayer()
        handler.removeCallbacks(playbackProgressRunnable)
    }

    companion object {
        private const val UPDATE_PLAYBACK_PROGRESS_DELAY_MS = 500L
    }
}