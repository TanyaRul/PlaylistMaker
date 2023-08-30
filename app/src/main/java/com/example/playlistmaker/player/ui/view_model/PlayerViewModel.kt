package com.example.playlistmaker.player.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.player.domain.model.PlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerViewModel(private val playerInteractor: PlayerInteractor) : ViewModel() {

    private var timerJob: Job? = null

    private var currentTimerLiveData = MutableLiveData(0)
    fun getCurrentTimerLiveData(): LiveData<Int> = currentTimerLiveData

    private var _playerStateLiveData = MutableLiveData<PlayerState>()
    val playerStateLiveData: LiveData<PlayerState> = _playerStateLiveData

    init {
        playerInteractor.changePlayerState { state ->
            _playerStateLiveData.postValue(state)
            if (state == PlayerState.STATE_COMPLETE)
                timerJob?.cancel()
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(UPDATE_PLAYBACK_PROGRESS_DELAY_MS)
                currentTimerLiveData.postValue(playerInteractor.currentPosition())
            }
        }
    }

    fun prepare(url: String) {
        timerJob?.cancel()
        playerInteractor.preparePlayer(url)
    }

    fun play() {
        playerInteractor.startPlayer()
        startTimer()
    }

    fun pause() {
        playerInteractor.pausePlayer()
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        playerInteractor.releasePlayer()
        timerJob?.cancel()
    }

    companion object {
        private const val UPDATE_PLAYBACK_PROGRESS_DELAY_MS = 300L
    }
}