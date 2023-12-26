package com.example.playlistmaker.library.ui.view_model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.library.domain.db.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.domain.model.states.PlaylistDetailsScreenState
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistDetailsViewModel(
    private val playlistsInteractor: PlaylistsInteractor,
    private val sharingInteractor: SharingInteractor,
    private val application: Application,
) : ViewModel() {

    private var playlistObserveJob: Job? = null

    private val _tracksLiveData = MutableLiveData<List<Track>>()
    val tracksLiveData: LiveData<List<Track>> get() = _tracksLiveData

    private val statePlaylistLiveData = MutableLiveData<PlaylistDetailsScreenState>()
    fun getStatePlaylistLiveData(): LiveData<PlaylistDetailsScreenState> = statePlaylistLiveData

    fun getFlowPlaylistById(playlistId: Int) {
        playlistObserveJob = viewModelScope.launch {
            playlistsInteractor.getFlowPlaylistById(playlistId).collect {
                renderStateTracksInPlaylist(it)
            }
        }
    }

    fun getTracksFromPlaylistByIds(trackIds: List<String>?) {
        val listOfIds = trackIds?.toList()
        if (listOfIds != null) {
            viewModelScope.launch {
                playlistsInteractor.getTracksFromPlaylistByIds(listOfIds)
                    .collect {
                        renderStateTracksInPlaylist(it)
                    }
            }
        } else {
            renderStateTracksInPlaylist(PlaylistDetailsScreenState.NoTracks)//_tracksLiveData.postValue(listOf())
        }
    }

    fun sharePlaylist(playlist: Playlist, tracks: List<Track>) {
        val message = generateMessage(playlist, tracks)
        sharingInteractor.sharePlaylist(message)
    }

    private fun generateMessage(playlist: Playlist, tracks: List<Track>): String {
        val sb = StringBuilder()

        sb.append(playlist.playlistTitle).append("\n")

        if (playlist.playlistDescription?.isNotEmpty() == true) {
            sb.append(playlist.playlistDescription).append("\n")
        }

        val trackWord = playlist.numberOfTracks?.let {
            application.resources.getQuantityString(
                R.plurals.track_count,
                it, playlist.numberOfTracks
            )
        }
        sb.append(trackWord).append("\n")

        tracks.forEachIndexed { index, track ->
            val trackDuration = SimpleDateFormat(
                "mm:ss",
                Locale.getDefault()
            ).format(track.trackTimeMillis)
            sb.append("${index + 1}. ${track.artistName} - ${track.trackName} ($trackDuration)")
                .append("\n")
        }

        return sb.toString()
    }

    fun removeTrackFromPlaylist(playlistId: Int, trackId: Int) {
        viewModelScope.launch {
            playlistsInteractor.removeTrackFromPlaylist(playlistId, trackId).collect {
                renderStateTracksInPlaylist(it)
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        playlistObserveJob?.cancel()
        viewModelScope.launch {
            playlistsInteractor.deletePlaylistById(playlist.id).collect {
                renderStateTracksInPlaylist(it)
            }
        }
    }

    private fun renderStateTracksInPlaylist(state: PlaylistDetailsScreenState) {
        statePlaylistLiveData.postValue(state)
    }
}

