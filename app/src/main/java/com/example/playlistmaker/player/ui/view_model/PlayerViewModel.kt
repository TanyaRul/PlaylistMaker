package com.example.playlistmaker.player.ui.view_model

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.player.domain.model.PlayerState

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val playerInteractor = Creator.providePlayerInteractor()
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

    fun release() {
        playerInteractor.releasePlayer()
        handler.removeCallbacks(playbackProgressRunnable)
    }

    override fun onCleared() {
        super.onCleared()
        release()
        handler.removeCallbacks(playbackProgressRunnable)
    }

    companion object {
        private const val UPDATE_PLAYBACK_PROGRESS_DELAY_MS = 500L

        fun getViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlayerViewModel(this[APPLICATION_KEY] as Application)
            }
        }
    }
}