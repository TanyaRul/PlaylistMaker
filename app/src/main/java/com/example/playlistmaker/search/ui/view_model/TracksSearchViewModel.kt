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

class TracksSearchViewModel(application: Application) : AndroidViewModel(application) {

    init {
        Log.d("TEST", "vm created")
    }

    private var historyTrackList = ArrayList<Track>()
    private val tracksInteractor = Creator.provideTracksInteractor(getApplication())
    private val handler = Handler(Looper.getMainLooper())

    private val _stateLiveData = MutableLiveData<TrackScreenState>()
    val stateLiveData: LiveData<TrackScreenState> = _stateLiveData

    init {
        historyTrackList.addAll(tracksInteractor.readSearchHistory())
    }

    private var latestSearchText: String? = null


    private val mediatorStateLiveData = MediatorLiveData<TrackScreenState>().also { liveData ->
        liveData.addSource(_stateLiveData) { trackState ->
            liveData.value = when (trackState) {
                is TrackScreenState.Content -> TrackScreenState.Content(trackState.trackList) //.sortedByDescending { it.inFavorite }
                is TrackScreenState.Empty -> trackState
                is TrackScreenState.Error -> trackState
                is TrackScreenState.Loading -> trackState
            }
        }
    }

    fun observeState(): LiveData<TrackScreenState> = mediatorStateLiveData

    override fun onCleared() {
        super.onCleared()
        tracksInteractor.saveSearchHistory(historyTrackList)
        handler.removeCallbacksAndMessages(SEARCH_REQUEST_TOKEN)
    }

    fun addTrackToHistory(track: Track) {
        historyTrackList = tracksInteractor.readSearchHistory() as ArrayList<Track>
        historyTrackList.remove(track)
        if (historyTrackList.size >= HISTORY_MAX_SIZE) {
            historyTrackList.removeAt(historyTrackList.size - 1)
        }
        historyTrackList.add(0, track)
        tracksInteractor.saveSearchHistory(historyTrackList)

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

    fun clearHistory() {
        tracksInteractor.clearSearchHistory()
        historyTrackList.clear()
        renderState(
            TrackScreenState.Content(
                trackList = historyTrackList,
            )
        )
    }

    fun clearSearchText() {
        renderState(
            TrackScreenState.Content(
                trackList = historyTrackList,
            )
        )
    }

    fun fillHistory() {
        historyTrackList.clear()
        historyTrackList.addAll(tracksInteractor.readSearchHistory())
    }

    fun searchTrack(newSearchText: String) {
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
        _stateLiveData.postValue(state)
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private val SEARCH_REQUEST_TOKEN = Any()
        const val HISTORY_MAX_SIZE = 10

        fun getViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TracksSearchViewModel(this[APPLICATION_KEY] as Application)
            }
        }

    }
}