package com.example.playlistmaker.data

import android.content.Context
import androidx.core.content.edit
import com.example.playlistmaker.domain.models.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchRepository(context: Context) {

    companion object {
        const val HISTORY_MAX_SIZE = 10
        const val SHARED_PREFS = "shared_prefs"
        const val SEARCH_HISTORY_KEY = "key_for_search_history"
    }

    private val sharedPrefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)

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
        sharedPrefs.edit {
            putString(SEARCH_HISTORY_KEY, Gson().toJson(historyTrackList))
        }
    }

}

