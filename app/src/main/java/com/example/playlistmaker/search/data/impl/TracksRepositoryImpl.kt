package com.example.playlistmaker.search.data.impl

import com.example.playlistmaker.search.data.storage.LocalStorage
import com.example.playlistmaker.search.data.network.NetworkClient
import com.example.playlistmaker.util.Resource
import com.example.playlistmaker.search.data.dto.TracksSearchRequest
import com.example.playlistmaker.search.data.dto.TracksSearchResponse
import com.example.playlistmaker.search.data.repository.TracksRepository
import com.example.playlistmaker.search.domain.model.Track

class TracksRepositoryImpl(
    private val networkClient: NetworkClient,
    private val localStorage: LocalStorage,
) : TracksRepository {

    override fun search(text: String): Resource<List<Track>> {
        val response = networkClient.doRequest(TracksSearchRequest(text))

        return when (response.resultCode) {
            -1 -> {
                Resource.Error("Проверьте подключение к интернету")
            }
            200 -> {
                val stored = localStorage.getSavedFavorites()

                Resource.Success((response as TracksSearchResponse).results.map {
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
                        inFavorite = stored.contains(it.trackId),
                    )
                })
            }
            else -> {
                Resource.Error("Ошибка сервера")
            }
        }
    }

    override fun addTrackToFavorites(track: Track) {
        localStorage.addToFavorites(track.trackId)
    }

    override fun removeTrackFromFavorites(track: Track) {
        localStorage.removeFromFavorites(track.trackId)
    }
}