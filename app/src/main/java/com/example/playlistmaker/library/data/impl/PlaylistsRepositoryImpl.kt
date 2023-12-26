package com.example.playlistmaker.library.data.impl

import com.example.playlistmaker.library.data.db.AppDatabase
import com.example.playlistmaker.library.data.db.converters.PlaylistDbConverter
import com.example.playlistmaker.library.data.db.converters.TrackInPlaylistConverter
import com.example.playlistmaker.library.data.db.entity.PlaylistDb
import com.example.playlistmaker.library.data.db.entity.TrackInPlaylist
import com.example.playlistmaker.library.data.storage.ImageStorage
import com.example.playlistmaker.library.domain.db.PlaylistsRepository
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class PlaylistsRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val playlistDbConverter: PlaylistDbConverter,
    private val trackInPlaylistConverter: TrackInPlaylistConverter,
    private val imageStorage: ImageStorage,
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
        return playlistDbConverter.mapFromPlaylistDbToPlaylist(
            appDatabase.playlistDao().getPlaylistById(playlistId)
        )
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

    override fun getTracksFromPlaylistByIds(trackIds: List<String>): Flow<List<Track>> = flow {
        val tracksFromPlaylists = appDatabase.trackInPlaylistDao().getTracksFromPlaylists()

        if (tracksFromPlaylists == null) {
            emit(emptyList())
        } else {
            val filteredTracks = tracksFromPlaylists.filter { track ->
                trackIds.contains(track.id)
            }

            val sortedTracks = filteredTracks.sortedBy { track ->
                trackIds.indexOf(track.id)
            }.reversed()

            if (sortedTracks.isEmpty()) {
                emit(emptyList())
            } else {
                emit(convertFromTrackInPlaylist(sortedTracks))
            }
        }
    }

    override suspend fun getFlowPlaylistById(id: Int): Flow<Playlist?> {
        val flowPlaylist = appDatabase.playlistDao().getFlowPlaylistById(id)
        return (flowPlaylist.map { playlist ->
            if (playlist != null) {
                playlistDbConverter.mapFromPlaylistDbToPlaylist(playlist)
            } else {
                null
            }
        })
    }

    override suspend fun deletePlaylistById(playlistId: Int): Flow<Unit?> {
        try {
            val playlist = appDatabase.playlistDao().getPlaylistById(playlistId)
            val listTrackIds = takeFromJson(playlist.trackIds)
            return if (listTrackIds.isEmpty()) {
                appDatabase.playlistDao().deletePlaylistById(playlist.id)
                flow { emit(Unit) }
            } else {
                for (i in 0 until listTrackIds.size) {
                    removeTrackFromCommonTable(listTrackIds[i])
                }
                appDatabase.playlistDao().deletePlaylistById(playlistId)
                flow { emit(Unit) }
            }
        } catch (ext: Throwable) {
            return flow { emit(null) }
        }
    }

    private suspend fun removeTrackFromCommonTable(trackId: Int) {
        val allPlaylists = appDatabase.playlistDao().getPlaylists()
        var trackEntry = 0

        allPlaylists.map {
            val listTrackIds = takeFromJson(it.trackIds)
            if (listTrackIds.contains(trackId)) {
                trackEntry += 1
            }
        }

        if (trackEntry > 1) return else
            appDatabase.trackInPlaylistDao().deleteTrackByIdFromTracksInPlaylists(trackId)
    }

    override suspend fun removeTrackFromPlaylist(
        playlistId: Int,
        trackId: Int
    ): Flow<List<Track>?>? {
        try {

            val oldPlaylist = getPlaylistById(playlistId)
            val oldPlaylistTrackIds = oldPlaylist.trackIds as ArrayList<String>

            oldPlaylistTrackIds.remove(trackId.toString())
            oldPlaylist.numberOfTracks = oldPlaylist.numberOfTracks?.minus(1)
            updatePlaylist(oldPlaylist)

            if (trackId > 0 && isUnusedPlaylistTrack(trackId)) {
                appDatabase.trackInPlaylistDao().deleteTrackByIdFromTracksInPlaylists(trackId)
            }

            val newPlaylist = appDatabase.playlistDao().getPlaylistById(playlistId)
            val newPlaylistTrackIds = takeFromJson(newPlaylist.trackIds)
            val listTrackIds: List<String> = newPlaylistTrackIds.map { it.toString() }

            return if (listTrackIds.isEmpty()) {
                flow { emit(ArrayList<Track>()) }
            } else {
                getTracksFromPlaylistByIds(listTrackIds)
            }
        } catch (exp: Throwable) {
            return null
        }
    }

    private suspend fun isUnusedPlaylistTrack(trackId: Int): Boolean {
        val playlists = getAllPlaylists().filter { playlist ->
            val listTrackIds = playlist.trackIds as ArrayList<String>
            listTrackIds.indexOf(trackId.toString()) > -1
        }
        return playlists.isEmpty()
    }

    private suspend fun getAllPlaylists(): List<Playlist> {
        return appDatabase.playlistDao().getPlaylists()
            .map { playlistEntity -> playlistDbConverter.mapFromPlaylistDbToPlaylist(playlistEntity) }
    }

    private fun convertFromPlaylistDb(playlists: List<PlaylistDb>): List<Playlist> {
        return playlists.map(playlistDbConverter::mapFromPlaylistDbToPlaylist)
    }

    private fun convertFromPlaylist(playlist: Playlist): PlaylistDb {
        return playlistDbConverter.mapFromPlaylistToPlaylistDb(playlist)
    }

    private fun convertFromTrack(track: Track): TrackInPlaylist {
        return trackInPlaylistConverter.map(track)
    }

    private fun convertFromTrackInPlaylist(tracks: List<TrackInPlaylist>): List<Track> {
        return tracks.map { track -> trackInPlaylistConverter.map(track) }
    }

    private fun takeFromJson(jsonString: String?): ArrayList<Int> {
        val gson = Gson()
        if (jsonString != null) {
            val itemType = object : TypeToken<ArrayList<Int>>() {}.type
            return gson.fromJson(jsonString, itemType)
        }
        return ArrayList<Int>()
    }

}