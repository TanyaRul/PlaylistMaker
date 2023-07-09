package com.example.playlistmaker.search.domain

import com.example.playlistmaker.search.domain.model.Track

interface TracksInteractor {
    fun search(text: String, consumer: TracksConsumer)
    fun addTrackToFavorites(track: Track)
    fun removeTrackFromFavorites(track: Track)

    interface TracksConsumer {
        fun consume(foundTracks: List<Track>?, errorMessage: String?)
    }
}