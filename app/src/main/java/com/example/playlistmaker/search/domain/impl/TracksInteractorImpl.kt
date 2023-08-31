package com.example.playlistmaker.search.domain.impl

import com.example.playlistmaker.util.Resource
import com.example.playlistmaker.search.domain.TracksInteractor
import com.example.playlistmaker.search.data.repository.TracksRepository
import com.example.playlistmaker.search.domain.model.SearchResult
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TracksInteractorImpl(private val repository: TracksRepository) : TracksInteractor {

    override fun search(text: String): Flow<SearchResult<List<Track>?, String?>> {
        return repository.search(text).map { result ->
            when (result) {
                is Resource.Success -> {
                    SearchResult(result.data, null)
                }

                is Resource.Error -> {
                    SearchResult(null, result.message)
                }
            }
        }
    }

    override fun readSearchHistory(): List<Track> {
        return repository.readSearchHistory()
    }

    override fun saveSearchHistory(tracks: List<Track>) {
        return repository.saveSearchHistory(tracks)
    }

    override fun clearSearchHistory() {
        repository.clearSearchHistory()
    }
}