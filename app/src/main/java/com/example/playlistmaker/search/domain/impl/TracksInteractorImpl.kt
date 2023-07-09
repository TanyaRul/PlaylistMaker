package com.example.playlistmaker.search.domain.impl

import com.example.playlistmaker.util.Resource
import com.example.playlistmaker.search.domain.TracksInteractor
import com.example.playlistmaker.search.data.repository.TracksRepository
import com.example.playlistmaker.search.domain.model.Track
import java.util.concurrent.Executors

class TracksInteractorImpl (private val repository: TracksRepository) : TracksInteractor {

    /*override fun search(text: String, consumer: TracksInteractor.TracksConsumer) {
        val t = Thread {
            consumer.consume(repository.search(text))
        }
        t.start()
    }*/

    private val executor = Executors.newCachedThreadPool()

    override fun search(text: String, consumer: TracksInteractor.TracksConsumer) {
        executor.execute {
            when(val resource = repository.search(text)) {
                is Resource.Success -> { consumer.consume(resource.data, null) }
                is Resource.Error -> { consumer.consume(null, resource.message) }
            }
        }
    }

    override fun addTrackToFavorites(track: Track) {
        repository.addTrackToFavorites(track)
    }

    override fun removeTrackFromFavorites(track: Track) {
        repository.removeTrackFromFavorites(track)
    }

}




