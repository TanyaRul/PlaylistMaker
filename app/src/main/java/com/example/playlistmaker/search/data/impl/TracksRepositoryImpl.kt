package com.example.playlistmaker.search.data.impl

import com.example.playlistmaker.search.data.network.NetworkClient
import com.example.playlistmaker.util.Resource
import com.example.playlistmaker.search.data.dto.TracksSearchRequest
import com.example.playlistmaker.search.data.dto.TracksSearchResponse
import com.example.playlistmaker.search.data.repository.TracksRepository
import com.example.playlistmaker.search.data.storage.SearchHistoryStorage
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TracksRepositoryImpl(
    private val networkClient: NetworkClient,
    private val searchHistoryStorage: SearchHistoryStorage,
) : TracksRepository {

    override fun search(text: String): Flow<Resource<List<Track>>> = flow {
        val response = networkClient.doRequest(TracksSearchRequest(text))

        when (response.resultCode) {
            ERROR_NO_CONNECTION -> {
                emit(Resource.Error("Проверьте подключение к интернету"))
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
                    emit(Resource.Success(data))
                }
            }

            else -> {
                emit(Resource.Error("Ошибка сервера"))
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

    companion object {
        const val ERROR_NO_CONNECTION = -1
        const val SEARCH_SUCCESS = 200
    }
}