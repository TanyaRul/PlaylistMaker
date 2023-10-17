package com.example.playlistmaker.library.data.impl

import com.example.playlistmaker.library.data.db.AppDatabase
import com.example.playlistmaker.library.data.db.converters.TrackDbConverter
import com.example.playlistmaker.library.data.db.entity.TrackDb
import com.example.playlistmaker.library.domain.db.FavoritesRepository
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FavoritesRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val trackDbConverter: TrackDbConverter,
) : FavoritesRepository {

    override suspend fun addTrackToFavorites(track: Track) {
        val trackDb = convertFromTrack(track)
        appDatabase.trackDao().insertTrackEntity(trackDb = trackDb)
    }

    override suspend fun removeTrackFromFavorites(track: Track) {
        val trackDb = convertFromTrack(track)
        appDatabase.trackDao().deleteTrackEntity(trackDb = trackDb)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> = flow {
        val tracks = appDatabase.trackDao().getFavoriteTracks()
        emit(convertFromTrackEntity(tracks))
    }

    override fun getFavoriteTracksIds(): Flow<List<String>> = flow {
        val tracksIds = appDatabase.trackDao().getFavoriteTracksIds()
        emit(tracksIds)
    }

    private fun convertFromTrackEntity(tracks: List<TrackDb>): List<Track> {
        return tracks.map(trackDbConverter::map)
    }

    private fun convertFromTrack(track: Track): TrackDb {
        return trackDbConverter.map(track)
    }
}