package com.example.playlistmaker.search.ui.view_model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.TracksInteractor
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.ui.TrackScreenState
import com.example.playlistmaker.util.debounce
import kotlinx.coroutines.launch

class TracksSearchViewModel(
    private val application: Application,
    private val tracksInteractor: TracksInteractor
) : ViewModel() {

    private var historyTrackList = ArrayList<Track>()

    private val _searchStateLiveData = MutableLiveData<TrackScreenState>()
    fun observeState(): LiveData<TrackScreenState> = mediatorStateLiveData

    private var _historyLiveData = MutableLiveData<ArrayList<Track>>()
    val historyLiveData: LiveData<ArrayList<Track>> = _historyLiveData

    init {
        historyTrackList.addAll(tracksInteractor.readSearchHistory())
        _historyLiveData.postValue(historyTrackList)
    }

    private var latestSearchText: String? = null

    private val trackSearchDebounce =
        debounce<String>(SEARCH_DEBOUNCE_DELAY, viewModelScope, true) { changedText ->
            searchTrack(changedText)
        }

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
    }

    //добавление трека в историю
    fun addTrackToHistory(track: Track) {
        historyTrackList = tracksInteractor.readSearchHistory() as ArrayList<Track>

        if (historyTrackList.size > 0) {
            var foundTrack: Track? = null

            for (historyTrack in historyTrackList) {
                if (historyTrack.toString().contains(track.trackId)) {
                    foundTrack = historyTrack
                    break
                }
            }

            if (foundTrack.toString().isNotEmpty()) {
                historyTrackList.remove(foundTrack)

            }
        }

        if (historyTrackList.size >= HISTORY_MAX_SIZE) {
            historyTrackList.removeAt(historyTrackList.size - 1)
        }

        historyTrackList.add(0, track)
        tracksInteractor.saveSearchHistory(historyTrackList)
        _historyLiveData.postValue(historyTrackList)
    }

    //поиск с задержкой
    fun searchDebounce(changedText: String) {
        if (latestSearchText != changedText) {
            latestSearchText = changedText
            trackSearchDebounce(changedText)
        }
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

            viewModelScope.launch {
                tracksInteractor
                    .search(newSearchText)
                    .collect { pair ->
                        processResult(pair.resultList, pair.error)
                    }
            }
        }
    }

    private fun processResult(foundTracks: List<Track>?, errorMessage: String?) {
        val trackList = mutableListOf<Track>()
        if (foundTracks != null) {
            trackList.addAll(foundTracks)
        }

        when {
            errorMessage != null -> {
                renderState(
                    TrackScreenState.Error(
                        errorMessage = application.getString(
                            R.string.no_connection
                        )
                    )
                )
            }

            trackList.isEmpty() -> {
                renderState(
                    TrackScreenState.Empty(
                        message = application.getString(R.string.nothing_found)
                    )
                )
            }

            else -> {
                renderState(TrackScreenState.Content(trackList = trackList))
            }
        }
    }

    //передаем состояние экрана
    private fun renderState(state: TrackScreenState) {
        _searchStateLiveData.postValue(state)
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val HISTORY_MAX_SIZE = 10
    }
}