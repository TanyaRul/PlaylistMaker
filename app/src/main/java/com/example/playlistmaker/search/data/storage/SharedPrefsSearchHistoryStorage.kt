package com.example.playlistmaker.search.data.storage

import android.content.Context

import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPrefsSearchHistoryStorage(context: Context) : SearchHistoryStorage {

    val sharedPrefs = context.getSharedPreferences(SEARCH_HISTORY_KEY, Context.MODE_PRIVATE)

    override fun saveSearchHistory(tracks: ArrayList<SearchHistoryTrack>) {
        sharedPrefs.edit {
            putString(SEARCH_HISTORY_KEY, Gson().toJson(tracks))
        }
    }

    override fun readSearchHistory(): ArrayList<SearchHistoryTrack> {
        val json = sharedPrefs.getString(SEARCH_HISTORY_KEY, null) ?: return ArrayList()
        val myType = object : TypeToken<ArrayList<SearchHistoryTrack>>() {}.type
        return Gson().fromJson(json, myType)
    }

    override fun clearSearchHistory() {
        sharedPrefs.edit().clear().apply()
    }

    companion object {
        const val SEARCH_HISTORY_KEY = "key_for_search_history"
    }

}