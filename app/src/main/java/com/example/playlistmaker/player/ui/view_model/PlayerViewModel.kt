package com.example.playlistmaker.player.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.db.FavoritesInteractor
import com.example.playlistmaker.library.domain.db.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.ui.PlaylistsScreenState
import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.player.ui.states.AddTrackState
import com.example.playlistmaker.player.domain.model.PlayerState
import com.example.playlistmaker.player.ui.states.PlayerToastState
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor,
    private val favoritesInteractor: FavoritesInteractor,
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private var timerJob: Job? = null

    private var isPlayerCreated = false

    private var currentTimerLiveData = MutableLiveData(0)
    fun getCurrentTimerLiveData(): LiveData<Int> = currentTimerLiveData

    private var _playerStateLiveData = MutableLiveData<PlayerState>()
    val playerStateLiveData: LiveData<PlayerState> = _playerStateLiveData

    private var _isFavoriteLiveData = MutableLiveData<Boolean>()
    val isFavoriteLiveData: LiveData<Boolean> = _isFavoriteLiveData

    private var _isInPlaylistLiveData = MutableLiveData<AddTrackState>()
    fun observeAddTrackState(): LiveData<AddTrackState> = _isInPlaylistLiveData

    private val _playlistsStateLiveData = MutableLiveData<PlaylistsScreenState>()
    fun observeState(): LiveData<PlaylistsScreenState> = _playlistsStateLiveData

    private val toastStateLivaData = MutableLiveData<PlayerToastState>(PlayerToastState.None)
    fun observeToastState(): LiveData<PlayerToastState> = toastStateLivaData

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

    fun fillData() {
        viewModelScope.launch {
            playlistsInteractor
                .getPlaylists()
                .collect { playlists ->
                    processResult(playlists = playlists)
                }
        }
    }

    private fun processResult(playlists: List<Playlist>) {
        if (playlists.isEmpty()) {
            renderState(PlaylistsScreenState.Empty)
        } else {
            renderState(PlaylistsScreenState.Content(playlists))
        }
    }

    private fun renderState(state: PlaylistsScreenState) {
        _playlistsStateLiveData.postValue(state)
    }

    fun onPlaylistClicked(playlist: Playlist, track: Track) {
        viewModelScope.launch {
            val trackIds = playlist.trackIds.toString()
            if (trackIds.isEmpty() || !trackIds.contains(track.trackId)) {
                playlistsInteractor.addTrackToPlaylist(playlist.id, track)
                renderAddTrackState(AddTrackState.Added(playlist.playlistTitle))
            } else {
                renderAddTrackState(AddTrackState.Exist(playlist.playlistTitle))
            }
        }
    }

    private fun renderAddTrackState(state: AddTrackState) {
        _isInPlaylistLiveData.postValue(state)
    }

    fun toastWasShown() {
        toastStateLivaData.postValue(PlayerToastState.None)
    }

    fun showToast(message: String) {
        toastStateLivaData.postValue(PlayerToastState.ShowMessage(message))
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