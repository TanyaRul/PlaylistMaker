package com.example.playlistmaker.search.domain

import com.example.playlistmaker.search.domain.model.Track

interface TracksInteractor {
    fun search(text: String, consumer: TracksConsumer)
    fun readSearchHistory(): List<Track>
    fun saveSearchHistory(tracks: List<Track>)
    fun clearSearchHistory()

    interface TracksConsumer {
        fun consume(foundTracks: List<Track>?, errorMessage: String?)
    }
}