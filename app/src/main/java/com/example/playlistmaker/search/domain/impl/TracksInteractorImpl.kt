package com.example.playlistmaker.search.domain.impl

import com.example.playlistmaker.util.Resource
import com.example.playlistmaker.search.domain.TracksInteractor
import com.example.playlistmaker.search.data.repository.TracksRepository
import com.example.playlistmaker.search.domain.model.Track
import java.util.concurrent.Executors

class TracksInteractorImpl(private val repository: TracksRepository) : TracksInteractor {

    private val executor = Executors.newCachedThreadPool()

    override fun search(text: String, consumer: TracksInteractor.TracksConsumer) {
        executor.execute {
            when (val resource = repository.search(text)) {
                is Resource.Success -> {
                    consumer.consume(resource.data, null)
                }

                is Resource.Error -> {
                    consumer.consume(null, resource.message)
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