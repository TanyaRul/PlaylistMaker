package com.example.playlistmaker.library.domain.impl

import android.util.Log
import com.example.playlistmaker.library.domain.db.PlaylistsInteractor
import com.example.playlistmaker.library.domain.db.PlaylistsRepository
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.domain.model.states.PlaylistDetailsScreenState
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.Duration

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

    override fun getTracksFromPlaylistByIds(trackIds: List<String>): Flow<PlaylistDetailsScreenState> {
        return playlistsRepository.getTracksFromPlaylistByIds(trackIds).map { tracks ->
            if (tracks.isNotEmpty()) {
                var durationTimeSum: Long = 0
                for (track in tracks) {
                    durationTimeSum += (track.trackTimeMillis ?: 0)
                }
                val duration = Duration.ofMillis(durationTimeSum)
                val minutes = duration.toMinutes()
                PlaylistDetailsScreenState.WithTracks(tracks.asReversed(), minutes)
            } else {
                PlaylistDetailsScreenState.NoTracks
            }
        }

    }

    override fun getPlaylistTrackTime(tracks: List<Track>): Int {
        var playlistTime = 0L
        for (track in tracks) {
            if (track.trackTimeMillis != null) {
                playlistTime += track.trackTimeMillis
            }
        }
        val duration = Duration.ofMillis(playlistTime)
        val minutes = duration.toMinutes()
        return (minutes).toInt()
    }

    override suspend fun deletePlaylistById(playlistId: Int): Flow<PlaylistDetailsScreenState> {
        return playlistsRepository.deletePlaylistById(playlistId).map {
            when (it) {
                null -> PlaylistDetailsScreenState.Error
                else -> PlaylistDetailsScreenState.DeletedPlaylist
            }
        }
    }

    override suspend fun getFlowPlaylistById(id: Int): Flow<PlaylistDetailsScreenState> {
        return playlistsRepository.getFlowPlaylistById(id).map {
            when (it) {
                null -> PlaylistDetailsScreenState.Error
                else -> PlaylistDetailsScreenState.InitPlaylist(it)
            }
        }
    }

    override suspend fun removeTrackFromPlaylist(
        playlistId: Int,
        trackId: Int
    ): Flow<PlaylistDetailsScreenState> {
        val flowTrack = playlistsRepository.removeTrackFromPlaylist(playlistId, trackId)
        val newPlaylist = playlistsRepository.getPlaylistById(playlistId)
        Log.d("PLAYLIST newPlaylist", newPlaylist.toString())

        if (flowTrack == null) {
            return flow { emit(PlaylistDetailsScreenState.Error) }
        }

        return flowTrack.map { trackList ->
            if (trackList == null) {
                PlaylistDetailsScreenState.Error
            } else if (trackList.isEmpty()) {
                PlaylistDetailsScreenState.NoTracks
            } else {
                var durationTimeSum: Long = 0
                for (track in trackList) {
                    durationTimeSum += (track.trackTimeMillis ?: 0)
                }
                val duration = Duration.ofMillis(durationTimeSum)
                val minutes = duration.toMinutes()

                PlaylistDetailsScreenState.DeletedTrack(
                    trackList.asReversed(),
                    minutes,
                    trackList.size,
                    newPlaylist
                )
            }
        }
    }

}