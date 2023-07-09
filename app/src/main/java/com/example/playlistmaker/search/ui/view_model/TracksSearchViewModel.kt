package com.example.playlistmaker.search.ui.view_model

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.search.domain.TracksInteractor
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.ui.TrackScreenState
import com.example.playlistmaker.search.ui.SingleLiveEvent

class TracksSearchViewModel(application: Application): AndroidViewModel(application) {

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private val SEARCH_REQUEST_TOKEN = Any()

        fun getViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TracksSearchViewModel(this[APPLICATION_KEY] as Application)
            }
        }

    }

    init {
        Log.d("TEST", "vm created")
    }

    private val tracksInteractor = Creator.provideTracksInteractor(getApplication())
    private val handler = Handler(Looper.getMainLooper())

    private val stateLiveData = MutableLiveData<TrackScreenState>()

    private val showToast = SingleLiveEvent<String>()
    fun observeShowToast(): LiveData<String> = showToast

    private var latestSearchText: String? = null

    private val mediatorStateLiveData = MediatorLiveData<TrackScreenState>().also { liveData ->
        liveData.addSource(stateLiveData) { trackState ->
            liveData.value = when (trackState) {
                is TrackScreenState.Content -> TrackScreenState.Content(trackState.trackList.sortedByDescending { it.inFavorite })
                is TrackScreenState.Empty -> trackState
                is TrackScreenState.Error -> trackState
                is TrackScreenState.Loading -> trackState
            }
        }
    }
    fun observeState(): LiveData<TrackScreenState> = mediatorStateLiveData

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacksAndMessages(SEARCH_REQUEST_TOKEN)
    }

    fun searchDebounce(changedText: String) {
        if (latestSearchText == changedText) {
            return
        }

        this.latestSearchText = changedText
        handler.removeCallbacksAndMessages(SEARCH_REQUEST_TOKEN)

        val searchRunnable = Runnable { searchTrack(changedText) }

        val postTime = SystemClock.uptimeMillis() + SEARCH_DEBOUNCE_DELAY
        handler.postAtTime(
            searchRunnable,
            SEARCH_REQUEST_TOKEN,
            postTime,
        )
    }

    fun toggleFavorite(track: Track) {
        if (track.inFavorite) {
            tracksInteractor.removeTrackFromFavorites(track)
        } else {
            tracksInteractor.addTrackToFavorites(track)
        }

        updateTrackContent(track.trackId, track.copy(inFavorite = !track.inFavorite))
    }

    private fun updateTrackContent(trackId: String, newTrack: Track) {
        val currentState = stateLiveData.value

        if (currentState is TrackScreenState.Content) {
            val trackIndex = currentState.trackList.indexOfFirst { it.trackId == trackId }
            if (trackIndex != -1) {
                stateLiveData.value = TrackScreenState.Content(
                    currentState.trackList.toMutableList().also {
                        it[trackIndex] = newTrack
                    }
                )
            }
        }
    }

    private fun searchTrack(newSearchText: String) {
        if (newSearchText.isNotEmpty()) {
            renderState(TrackScreenState.Loading)

            tracksInteractor.search(newSearchText, object : TracksInteractor.TracksConsumer {
                override fun consume(foundTracks: List<Track>?, errorMessage: String?) {
                    val trackList = mutableListOf<Track>()
                    if (foundTracks != null) {
                        trackList.addAll(foundTracks)
                    }

                    when {
                        errorMessage != null -> {
                            renderState(
                                TrackScreenState.Error(
                                    errorMessage = getApplication<Application>().getString(R.string.no_connection),
                                )
                            )
                            showToast.postValue(errorMessage)
                        }

                        trackList.isEmpty() -> {
                            renderState(
                                TrackScreenState.Empty(
                                    message = getApplication<Application>().getString(R.string.nothing_found),
                                )
                            )
                        }

                        else -> {
                            renderState(
                                TrackScreenState.Content(
                                    trackList = trackList,
                                )
                            )
                        }
                    }
                }
            })
        }
    }

    private fun renderState(state: TrackScreenState) {
        stateLiveData.postValue(state)
    }
}