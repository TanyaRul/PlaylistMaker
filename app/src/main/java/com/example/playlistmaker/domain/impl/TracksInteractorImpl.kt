package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.TracksInteractor
import com.example.playlistmaker.domain.api.TracksRepository

class TracksInteractorImpl (private val repository: TracksRepository) : TracksInteractor {

    override fun search(text: String, consumer: TracksInteractor.TracksConsumer) {
        val t = Thread {
            consumer.consume(repository.search(text))
        }
        t.start()
    }
}