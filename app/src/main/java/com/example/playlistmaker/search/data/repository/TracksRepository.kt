package com.example.playlistmaker.search.data.repository

import com.example.playlistmaker.util.Resource
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TracksRepository {
    fun search(text: String): Flow<Resource<List<Track>>>
    fun readSearchHistory(): List<Track>
    fun saveSearchHistory(tracks: List<Track>)
    fun clearSearchHistory()
}