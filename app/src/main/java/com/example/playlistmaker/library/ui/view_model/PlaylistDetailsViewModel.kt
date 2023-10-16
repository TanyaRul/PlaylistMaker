package com.example.playlistmaker.library.ui.view_model

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.library.domain.db.PlaylistsInteractor
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.ui.PlaylistDetailsScreenState
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistDetailsViewModel(
    private val playlistsInteractor: PlaylistsInteractor,
    private val sharingInteractor: SharingInteractor,
    private val application: Application,
    //private val playlistId: Int,
) : ViewModel() {

    private val stateLiveData = MutableLiveData<Playlist>()
    fun observeState(): LiveData<Playlist> = stateLiveData

    private val _tracksLiveData = MutableLiveData<List<Track>>()
    val tracksLiveData: LiveData<List<Track>> get() = _tracksLiveData

    private val trackListLiveData = MutableLiveData<List<Track>>()
    fun observeTrackList(): LiveData<List<Track>> = trackListLiveData

    private val statePlaylistLiveData = MutableLiveData<PlaylistDetailsScreenState>()
    fun getStatePlaylistLiveData(): LiveData<PlaylistDetailsScreenState> = statePlaylistLiveData


    /*init {
        viewModelScope.launch {
            playlistsInteractor.getFlowPlaylistById(playlistId).collect {
                renderStateTracksInPlaylist(it)
            }
        }
    }*/


    fun getFlowPlaylistById(playlistId: Int) {
        viewModelScope.launch {
            playlistsInteractor.getFlowPlaylistById(playlistId).collect {
                renderStateTracksInPlaylist(it)
            }
        }
    }

    fun getCurrentPlaylist(playlist: Playlist) {
        renderState(playlist)
    }

    private fun renderState(state: Playlist) {
        stateLiveData.postValue(state)
    }

    //fun getTracksFromPlaylistByIds(playlist: Playlist) {
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
            _tracksLiveData.postValue(listOf())
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
            ).format(track.trackTimeMillis)//convertMillisToTimeFormat(track.trackTimeMillis)
            sb.append("${index + 1}. ${track.artistName} - ${track.trackName} ($trackDuration)")
                .append("\n")
        }

        return sb.toString()
    }

    fun removeTrackFromPlaylist(playlistId: Int, trackId: Int) {
        viewModelScope.launch {
            /*playlistsInteractor.removeTrackFromPlaylist(playlist.id, track.trackId.toInt())
            playlistsInteractor.getFlowPlaylistById(playlist.id).collect {
                renderStateTracksInPlaylist(it)
            }*/
            playlistsInteractor.removeTrackFromPlaylist(playlistId, trackId).collect {
                renderStateTracksInPlaylist(it)
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistsInteractor.deletePlaylistById(playlist.id).collect {
                //Log.d("PLAYLIST state", it.toString())
                renderStateTracksInPlaylist(it)
            }
        }
    }

    private fun renderStateTracksInPlaylist(state: PlaylistDetailsScreenState) {
        //Log.d("PLAYLIST statelive", state.toString())
        statePlaylistLiveData.postValue(state)
    }
}

