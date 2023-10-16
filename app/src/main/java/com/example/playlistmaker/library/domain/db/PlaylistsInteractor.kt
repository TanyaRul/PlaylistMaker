package com.example.playlistmaker.library.domain.db

import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsInteractor {
    suspend fun createPlaylist(playlist: Playlist)
    suspend fun updatePlaylist(playlist: Playlist)
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(playlistId: Int): Playlist
    suspend fun insertTrack(track: Track)
    suspend fun addTrackToPlaylist(playlistId: Int, track: Track)
    fun saveImageToPrivateStorage(uriFile: String?): String?
}