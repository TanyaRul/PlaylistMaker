package com.example.playlistmaker.library.domain.db

import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface FavoritesInteractor {
    suspend fun addTrackToFavorites(track: Track)
    suspend fun removeTrackFromFavorites(track: Track)
    fun getFavoriteTracks(): Flow<List<Track>>
    fun getFavoriteTracksIds(): Flow<List<String>>
}