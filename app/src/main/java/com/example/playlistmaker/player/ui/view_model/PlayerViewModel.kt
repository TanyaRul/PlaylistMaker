package com.example.playlistmaker.player.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.player.domain.model.PlayerState
import com.example.playlistmaker.library.domain.db.FavoritesInteractor
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor,
    private val favoritesInteractor: FavoritesInteractor,
) : ViewModel() {

    private var timerJob: Job? = null

    private var isPlayerCreated = false

    private var currentTimerLiveData = MutableLiveData(0)
    fun getCurrentTimerLiveData(): LiveData<Int> = currentTimerLiveData

    private var _playerStateLiveData = MutableLiveData<PlayerState>()
    val playerStateLiveData: LiveData<PlayerState> = _playerStateLiveData

    private var _isFavoriteLiveData = MutableLiveData<Boolean>()
    val isFavoriteLiveData: LiveData<Boolean> = _isFavoriteLiveData

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

    fun onFavoriteClicked(track: Track) {
        viewModelScope.launch {
            val newFavoriteStatus = if (!track.isFavorite) {
                favoritesInteractor.addTrackToFavorites(track)
                true
            } else {

                favoritesInteractor.removeTrackFromFavorites(track)
                false
            }

            _isFavoriteLiveData.postValue(newFavoriteStatus)
            track.isFavorite = newFavoriteStatus
        }
    }

    suspend fun isTackFavorite(trackId: String): Boolean {
        val favoriteTracks: Flow<List<String>> = favoritesInteractor.getFavoriteTracksIds()

        val favoriteTracksIds: MutableList<String> = mutableListOf()

        favoriteTracks.collect { list ->
            favoriteTracksIds.addAll(list)
        }

        return favoriteTracksIds.contains(trackId)
    }

    fun prepare(url: String) {
        timerJob?.cancel()
        if (!isPlayerCreated) {
            playerInteractor.preparePlayer(url)
        }
        isPlayerCreated = true
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