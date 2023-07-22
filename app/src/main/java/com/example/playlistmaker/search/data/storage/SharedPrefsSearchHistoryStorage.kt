package com.example.playlistmaker.search.data.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlistmaker.search.domain.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPrefsSearchHistoryStorage(
    private val sharedPrefs: SharedPreferences,
    private val gson: Gson
) : SearchHistoryStorage {

    override fun saveSearchHistory(tracks: ArrayList<Track>) {
        sharedPrefs.edit {
            putString(SEARCH_HISTORY_KEY, gson.toJson(tracks))
        }
    }

    override fun readSearchHistory(): ArrayList<Track> {
        val json = sharedPrefs.getString(SEARCH_HISTORY_KEY, null) ?: return ArrayList()
        val myType = object : TypeToken<ArrayList<Track>>() {}.type
        return gson.fromJson(json, myType)
    }

    override fun clearSearchHistory() {
        sharedPrefs.edit().clear().apply()
    }

    companion object {
        const val SEARCH_HISTORY_KEY = "key_for_search_history"
    }

}