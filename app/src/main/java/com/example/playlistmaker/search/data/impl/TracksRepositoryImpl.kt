package com.example.playlistmaker.search.data.impl

import android.content.Context
import com.example.playlistmaker.R
import com.example.playlistmaker.library.data.db.AppDatabase
import com.example.playlistmaker.search.data.dto.TracksSearchRequest
import com.example.playlistmaker.search.data.dto.TracksSearchResponse
import com.example.playlistmaker.search.data.network.NetworkClient
import com.example.playlistmaker.search.data.repository.TracksRepository
import com.example.playlistmaker.search.data.storage.SearchHistoryStorage
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TracksRepositoryImpl(
    private val networkClient: NetworkClient,
    private val searchHistoryStorage: SearchHistoryStorage,
    private val context: Context,
    private val appDatabase: AppDatabase,
) : TracksRepository {

    override fun search(text: String): Flow<Resource<List<Track>>> = flow {
        val response = networkClient.doRequest(TracksSearchRequest(text))

        when (response.resultCode) {
            ERROR_NO_CONNECTION -> {
                emit(Resource.Error(context.getString(R.string.no_connection_message)))
            }

            SEARCH_SUCCESS -> {
                with(response as TracksSearchResponse) {
                    val data = results.map {
                        Track(
                            trackId = it.trackId,
                            trackName = it.trackName,
                            artistName = it.artistName,
                            trackTimeMillis = it.trackTimeMillis,
                            artworkUrl100 = it.artworkUrl100,
                            collectionName = it.collectionName,
                            releaseDate = it.releaseDate,
                            primaryGenreName = it.primaryGenreName,
                            country = it.country,
                            previewUrl = it.previewUrl,
                        )
                    }
                    val favoriteTracksIds = appDatabase.trackDao().getFavoriteTracksIds()
                    markFavorites(data, favoriteTracksIds)
                    emit(Resource.Success(data))
                }
            }

            else -> {
                emit(Resource.Error(context.getString(R.string.server_error_message)))
            }
        }
    }

    override fun readSearchHistory(): List<Track> {
        return searchHistoryStorage.readSearchHistory().map {
            Track(
                trackId = it.trackId,
                trackName = it.trackName,
                artistName = it.artistName,
                trackTimeMillis = it.trackTimeMillis,
                artworkUrl100 = it.artworkUrl100,
                collectionName = it.collectionName,
                releaseDate = it.releaseDate,
                primaryGenreName = it.primaryGenreName,
                country = it.country,
                previewUrl = it.previewUrl,
            )
        }
    }

    override fun saveSearchHistory(tracks: List<Track>) {
        searchHistoryStorage.saveSearchHistory(tracks as ArrayList<Track>)
    }

    override fun clearSearchHistory() {
        searchHistoryStorage.clearSearchHistory()
    }

    private fun markFavorites(tracks: List<Track>, favoriteTracksIds: List<String>) {
        for (track in tracks) {
            favoriteTracksIds.contains(track.trackId)
            if (favoriteTracksIds.contains(track.trackId)) {
                track.isFavorite = true
            }
        }
    }

    companion object {
        const val ERROR_NO_CONNECTION = -1
        const val SEARCH_SUCCESS = 200
    }
}