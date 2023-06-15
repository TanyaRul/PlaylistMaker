package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.Track

interface TracksRepository {
    fun search(text: String): List<Track>
}