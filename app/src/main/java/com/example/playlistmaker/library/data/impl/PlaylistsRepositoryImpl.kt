package com.example.playlistmaker.library.data.impl

import com.example.playlistmaker.library.data.db.AppDatabase
import com.example.playlistmaker.library.data.db.PlaylistDbConverter
import com.example.playlistmaker.library.data.db.TrackInPlaylistConverter
import com.example.playlistmaker.library.data.db.entity.PlaylistDb
import com.example.playlistmaker.library.data.db.entity.TrackInPlaylist
import com.example.playlistmaker.library.data.storage.ImageStorage
import com.example.playlistmaker.library.domain.db.PlaylistsRepository
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PlaylistsRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val playlistDbConverter: PlaylistDbConverter,
    private val trackInPlaylistConverter: TrackInPlaylistConverter,
    private val imageStorage: ImageStorage
) : PlaylistsRepository {

    override suspend fun createPlaylist(playlist: Playlist) {
        val playlistDb = convertFromPlaylist(playlist)
        appDatabase.playlistDao().insertPlaylist(playlistDb = playlistDb)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val playlistDb = convertFromPlaylist(playlist)
        appDatabase.playlistDao().updatePlaylist(playlistDb = playlistDb)
    }

    override fun getPlaylists(): Flow<List<Playlist>> = flow {
        val playlists = appDatabase.playlistDao().getPlaylists()
        emit(convertFromPlaylistDb(playlists))
    }

    override suspend fun getPlaylistById(playlistId: Int): Playlist {
        return playlistDbConverter.map(appDatabase.playlistDao().getPlaylistById(playlistId))
    }

    override suspend fun insertTrack(track: Track) {
        val trackInPlaylist = convertFromTrack(track)
        appDatabase.trackInPlaylistDao().insertTrack(trackInPlaylist = trackInPlaylist)
    }

    override suspend fun addTrackToPlaylist(playlistId: Int, track: Track) {
        insertTrack(track)
        val gottenPlaylist = getPlaylistById(playlistId)
        gottenPlaylist.trackIds = gottenPlaylist.trackIds?.plus(track.trackId)
        gottenPlaylist.numberOfTracks = gottenPlaylist.numberOfTracks?.plus(1)
        updatePlaylist(gottenPlaylist)
    }

    override fun saveImageToPrivateStorage(uriFile: String?): String? {
        return imageStorage.saveImageToPrivateStorage(uriFile)
    }

    private fun convertFromPlaylistDb(playlists: List<PlaylistDb>): List<Playlist> {
        return playlists.map(playlistDbConverter::map)
    }

    private fun convertFromPlaylist(playlist: Playlist): PlaylistDb {
        return playlistDbConverter.map(playlist)
    }

    private fun convertFromTrack(track: Track): TrackInPlaylist {
        return trackInPlaylistConverter.map(track)
    }
}