package com.example.playlistmaker.search.ui.view_model

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.TracksInteractor
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.ui.TrackScreenState

class TracksSearchViewModel(
    private val context: Application,
    private val tracksInteractor: TracksInteractor
) : ViewModel() {

    private var historyTrackList = ArrayList<Track>()

    private val handler = Handler(Looper.getMainLooper())

    private val _searchStateLiveData = MutableLiveData<TrackScreenState>()
    fun observeState(): LiveData<TrackScreenState> = mediatorStateLiveData

    private var _historyLiveData = MutableLiveData<ArrayList<Track>>()
    val historyLiveData: LiveData<ArrayList<Track>> = _historyLiveData

    init {
        historyTrackList.addAll(tracksInteractor.readSearchHistory())
        _historyLiveData.postValue(historyTrackList)
    }

    private var latestSearchText: String? = null

    private val mediatorStateLiveData = MediatorLiveData<TrackScreenState>().also { liveData ->
        liveData.addSource(_searchStateLiveData) { trackState ->
            liveData.value = when (trackState) {
                is TrackScreenState.Content -> TrackScreenState.Content(trackState.trackList)
                is TrackScreenState.Empty -> trackState
                is TrackScreenState.Error -> trackState
                is TrackScreenState.Loading -> trackState
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tracksInteractor.saveSearchHistory(historyTrackList)
        handler.removeCallbacksAndMessages(SEARCH_REQUEST_TOKEN)
    }

    //добавление трека в историю
    fun addTrackToHistory(track: Track) {
        historyTrackList = tracksInteractor.readSearchHistory() as ArrayList<Track>
        historyTrackList.remove(track)
        if (historyTrackList.size >= HISTORY_MAX_SIZE) {
            historyTrackList.removeAt(historyTrackList.size - 1)
        }
        historyTrackList.add(0, track)
        tracksInteractor.saveSearchHistory(historyTrackList)
        _historyLiveData.postValue(historyTrackList)
    }

    //поиск с задержкой
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

    //очистить историю поиска
    fun clearHistory() {
        tracksInteractor.clearSearchHistory()
        historyTrackList.clear()
        renderState(
            TrackScreenState.Content(
                trackList = historyTrackList,
            )
        )
    }

    //извлечь историю поиска из sharedPrefs
    fun fillHistory() {
        historyTrackList.clear()
        historyTrackList.addAll(tracksInteractor.readSearchHistory())
        _historyLiveData.postValue(historyTrackList)
    }

    //поиск трека
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
                                    errorMessage = context.getString(R.string.no_connection),
                                )
                            )
                        }

                        trackList.isEmpty() -> {
                            renderState(
                                TrackScreenState.Empty(
                                    message = context.getString(R.string.nothing_found),
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

    //передаем состояние экрана
    private fun renderState(state: TrackScreenState) {
        _searchStateLiveData.postValue(state)
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private val SEARCH_REQUEST_TOKEN = Any()
        const val HISTORY_MAX_SIZE = 10
    }
}