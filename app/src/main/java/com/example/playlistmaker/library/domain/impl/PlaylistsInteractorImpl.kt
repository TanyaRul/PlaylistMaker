package com.example.playlistmaker.library.domain.impl

import android.util.Log
import com.example.playlistmaker.library.domain.db.PlaylistsInteractor
import com.example.playlistmaker.library.domain.db.PlaylistsRepository
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.ui.PlaylistDetailsScreenState
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

    //override fun getTracksFromPlaylistByIds(trackIds: List<String>): Flow<List<Track>> {
    override fun getTracksFromPlaylistByIds(trackIds: List<String>): Flow<PlaylistDetailsScreenState> {
        return playlistsRepository.getTracksFromPlaylistByIds(trackIds).map {
            if (it.isNotEmpty()) {
                var durationTimeSum: Long = 0
                for (element in it) {
                    durationTimeSum += (element.trackTimeMillis ?: 0)
                }
                val duration = Duration.ofMillis(durationTimeSum)
                val minutes = duration.toMinutes()
                //Log.d("PLAYLIST interactor tracks", it.toString())
                PlaylistDetailsScreenState.WithTracks(it.asReversed(), minutes)
            } else {
                PlaylistDetailsScreenState.NoTracks
            }
        }

    }

    override fun getPlaylistTrackTime(tracks: List<Track>): Int {
        //Log.d("PLAYLIST interactor tracks", tracks.toString())
        var playlistTime = 0L
        for (track in tracks) {
            if (track.trackTimeMillis != null) {
                playlistTime += track.trackTimeMillis
            }
        }
        //Log.d("PLAYLIST TIME", playlistTime.toString())
        val duration = Duration.ofMillis(playlistTime)
        val minutes = duration.toMinutes()
        return (minutes).toInt()
    }

    override suspend fun deletePlaylistById(playlistId: Int): Flow<PlaylistDetailsScreenState> {
        /*return playlistsRepository.deletePlaylistById(playlistId).map {
            StateTracksInPlaylist.DeletedPlaylist
        }*/

        return playlistsRepository.deletePlaylistById(playlistId).map {
            when(it){
                null-> {//Log.d("PLAYLIST null","null")
                    PlaylistDetailsScreenState.Error}
                else-> {//Log.d("PLAYLIST notnull","notnull")
                    PlaylistDetailsScreenState.DeletedPlaylist}
            }
        }
    }

    /*override suspend fun getFlowPlaylistById(id: Int): Flow<Playlist?> {
        return playlistsRepository.getFlowPlaylistById(id)
    }*/

    override suspend fun getFlowPlaylistById(id: Int): Flow<PlaylistDetailsScreenState> {
        return playlistsRepository.getFlowPlaylistById(id).map {
            when(it){
                null -> {//Log.d("PLAYLIST INT get null","null")
                    PlaylistDetailsScreenState.Error}
                else -> {//Log.d("PLAYLIST INT get notnull","notnull")
                    PlaylistDetailsScreenState.InitPlaylist(it)}
            }
        }
    }

    override suspend fun removeTrackFromPlaylist(
        playlistId: Int,
        trackId: Int
    ): Flow<PlaylistDetailsScreenState> {

        val flowTrack = playlistsRepository.removeTrackFromPlaylist(playlistId, trackId)

        if (flowTrack == null) {
            return flow { emit(PlaylistDetailsScreenState.Error) }
        }

        return flowTrack.map {trackList ->

            //Log.d("PLAYLIST flowtrackList", trackList.toString())
            if (trackList == null) {
                PlaylistDetailsScreenState.Error
            } else if (trackList.isEmpty()) {
                PlaylistDetailsScreenState.NoTracks
            } else {
                var durationTimeSum: Long = 0
                for (track in trackList) {
                    durationTimeSum += (track.trackTimeMillis ?: 0)
                }
                PlaylistDetailsScreenState.DeletedTrack(trackList.asReversed(), durationTimeSum, trackList.size)
            }
        }
    }

}