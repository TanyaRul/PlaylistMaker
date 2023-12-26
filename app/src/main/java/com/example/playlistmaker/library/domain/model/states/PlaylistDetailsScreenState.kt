package com.example.playlistmaker.library.domain.model.states

import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track

sealed class PlaylistDetailsScreenState {

    object NoTracks : PlaylistDetailsScreenState()

    data class WithTracks(val listTracks: List<Track>, val durationSumTime: Long) :
        PlaylistDetailsScreenState()

    data class DeletedTrack(
        val listTracks: List<Track>,
        val durationSumTime: Long,
        val counterTracks: Int,
        val playlist: Playlist
    ) : PlaylistDetailsScreenState()

    object Error : PlaylistDetailsScreenState()

    object DeletedPlaylist : PlaylistDetailsScreenState()

    class InitPlaylist(val playlist: Playlist?) : PlaylistDetailsScreenState()
}