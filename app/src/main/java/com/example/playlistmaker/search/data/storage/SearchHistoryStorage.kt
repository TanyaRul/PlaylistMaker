package com.example.playlistmaker.search.data.storage

interface SearchHistoryStorage {
    fun saveSearchHistory(tracks: ArrayList<SearchHistoryTrack>)
    fun readSearchHistory(): ArrayList<SearchHistoryTrack>
    fun clearSearchHistory()
}

