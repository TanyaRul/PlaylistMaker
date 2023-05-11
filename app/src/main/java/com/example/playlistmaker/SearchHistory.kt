package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val sharedPrefs: SharedPreferences) {
    companion object {
        const val SEARCH_HISTORY_KEY = "key_for_search_history"
        const val HISTORY_MAX_SIZE = 10
    }

    fun readSearchHistory(): ArrayList<Track> {
        val json = sharedPrefs.getString(SEARCH_HISTORY_KEY, null) ?: return ArrayList()
        val myType = object : TypeToken<ArrayList<Track>>() {}.type
        return Gson().fromJson(json, myType)
    }

    fun addTrackToHistory(track: Track) {
        val historyTrackList: ArrayList<Track> = readSearchHistory()
        historyTrackList.remove(track)
        if (historyTrackList.size >= HISTORY_MAX_SIZE) {
            historyTrackList.removeAt(historyTrackList.size - 1)
        }
        historyTrackList.add(0, track)
        sharedPrefs.edit()
            .putString(SEARCH_HISTORY_KEY, Gson().toJson(historyTrackList))
            .apply()
    }
}