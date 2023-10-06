package com.example.playlistmaker.library.domain.impl

import com.example.playlistmaker.library.domain.db.PlaylistsInteractor
import com.example.playlistmaker.library.domain.db.PlaylistsRepository
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

class PlaylistsInteractorImpl(
    private val playlistsRepository: PlaylistsRepository
) :
    PlaylistsInteractor {
    override suspend fun createPlaylist(playlist: Playlist) {
        return playlistsRepository.createPlaylist(playlist)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        return playlistsRepository.updatePlaylist(playlist)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistsRepository.getPlaylists()
    }

    override suspend fun getPlaylistById(playlistId: Int): Playlist {
        return playlistsRepository.getPlaylistById(playlistId)
    }

    override suspend fun insertTrack(track: Track) {
        return playlistsRepository.insertTrack(track)
    }

    override suspend fun addTrackToPlaylist(playlistId: Int, track: Track) {
        return playlistsRepository.addTrackToPlaylist(playlistId, track)
    }

    override fun saveImageToPrivateStorage(uriFile: String?): String? {
        return playlistsRepository.saveImageToPrivateStorage(uriFile)
    }

}