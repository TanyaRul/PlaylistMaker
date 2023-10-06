package com.example.playlistmaker.search.data.storage

import com.example.playlistmaker.search.domain.model.Track

interface SearchHistoryStorage {
    fun saveSearchHistory(tracks: ArrayList<Track>)
    fun readSearchHistory(): ArrayList<Track>
    fun clearSearchHistory()
}