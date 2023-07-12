package com.example.playlistmaker.search.data.repository

import com.example.playlistmaker.util.Resource
import com.example.playlistmaker.search.domain.model.Track

interface TracksRepository {
    fun search(text: String): Resource<List<Track>>
    fun readSearchHistory(): List<Track>
    fun saveSearchHistory(tracks: List<Track>)
    fun clearSearchHistory()
}