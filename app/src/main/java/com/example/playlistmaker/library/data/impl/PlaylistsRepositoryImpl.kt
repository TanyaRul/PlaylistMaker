package com.example.playlistmaker.library.data.impl

import android.util.Log
import com.example.playlistmaker.library.data.db.AppDatabase
import com.example.playlistmaker.library.data.db.PlaylistDbConverter
import com.example.playlistmaker.library.data.db.TrackInPlaylistConverter
import com.example.playlistmaker.library.data.db.entity.PlaylistDb
import com.example.playlistmaker.library.data.db.entity.TrackInPlaylist
import com.example.playlistmaker.library.data.storage.ImageStorage
import com.example.playlistmaker.library.domain.db.PlaylistsRepository
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

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

    /*override suspend fun getFlowPlaylistById(id: Int): Flow<Playlist> {
        val flowPlaylist = appDatabase.playlistDao().getFlowPlaylistById(id)
        return (flowPlaylist.map { playlist -> playlistDbConverter.mapFromPlaylistDbToPlaylist(playlist) })
    }*/

    override suspend fun getFlowPlaylistById(id: Int): Flow<Playlist?> {
        val flowPlaylist = appDatabase.playlistDao().getFlowPlaylistById(id)
        return (flowPlaylist.map { playlist ->
            if (playlist != null) {
                //Log.d("PLAYLIST REP get notnull","notnull")
                playlistDbConverter.mapFromPlaylistDbToPlaylist(playlist)
            } else {
                //Log.d("PLAYLIST REP get null", "null")
                null
            }
        })
    }

    override suspend fun deletePlaylistById(playlistId: Int): Flow<Unit?> {
        try {
            val playlist = appDatabase.playlistDao().getPlaylistById(playlistId)
            val listTrackIds = takeFromJson(playlist.trackIds)
            //Log.d("PLAYLIST listTrackIds", listTrackIds.toString())
            //Log.d("PLAYLIST playlistId", playlistId.toString())
            return if (listTrackIds.isEmpty()) {
                //Log.d("PLAYLIST 1", "1")
                //Log.d("PLAYLIST 1", playlist.id.toString())
                appDatabase.playlistDao().deletePlaylistById(playlist.id)
                //Log.d("PLAYLIST 1 listTrackIds", listTrackIds.toString())
                flow { emit(Unit) }
            } else {
                //Log.d("PLAYLIST 2", "2")
                for (i in 0 until listTrackIds.size) {
                    removeTrackFromCommonTable(listTrackIds[i])
                }
                appDatabase.playlistDao().deletePlaylistById(playlistId)
                flow { emit(Unit) }
            }

        } catch (ext: Throwable) {
            //Log.d("PLAYLIST 3", "3")
            return flow { emit(null) }
        }
    }

    /*//Log.d("PLAYLIST delete REPO", "enter")
    //appDatabase.playlistDao().deletePlaylistById(playlistId)
    val playlist = appDatabase.playlistDao().getPlaylistById(playlistId)
    //Log.d("PLAYLIST playlist", playlist.toString())

    val listTrackIds = takeFromJson(playlist.trackIds)
    //Log.d("PLAYLIST listId1", listTrackIds.toString())

    if (listTrackIds.isEmpty()) {
        appDatabase.playlistDao().deletePlaylistById(playlistId)
        //Log.d("PLAYLIST empty del", "ok")
        emit(Unit)
    } else {
        for (i in 0 until listTrackIds.size) {
            //Log.d("PLAYLIST listId[i]", listTrackIds[i].toString())
            //Log.d("PLAYLIST playlistId", playlistId.toString())
            removeTrackFromCommonTable(listTrackIds[i])
        }
        appDatabase.playlistDao().deletePlaylistById(playlistId)
        //Log.d("PLAYLIST !empty del", "ok")
        emit(Unit)
    }
}*/

    private suspend fun removeTrackFromCommonTable(trackId: Int) {
        //Log.d("PLAYLIST idTrack", trackId.toString())

        val allPlaylists = appDatabase.playlistDao().getPlaylists()
        //Log.d("PLAYLIST allPlaylists", allPlaylists.toString())

        var trackEntry = 0

        allPlaylists.map {
            val listTrackIds = takeFromJson(it.trackIds)
            val playlistIds = ArrayList(allPlaylists.map { it.id })
            //Log.d("PLAYLIST listTrackIds", listTrackIds.toString())

            if (listTrackIds.contains(trackId)) {
                trackEntry += 1
            }
        }

        if (trackEntry > 1) return else
            appDatabase.trackInPlaylistDao().deleteTrackByIdFromTracksInPlaylists(trackId)
    }

    private suspend fun removeTrackFromPlaylistDb(playlistId: Int, trackId: Int) {
        /*Log.d("PLAYLIST RM plid ", playlistId.toString())
        Log.d("PLAYLIST RM trid", trackId.toString())
        val playlist = appDatabase.playlistDao().getPlaylistById(playlistId)
        Log.d("PLAYLIST RM playlist", playlist.toString())
        val listTrackIds = takeFromJson(playlist.trackIds)
        Log.d("PLAYLIST RM listIds", listTrackIds.toString())

        listTrackIds.remove(trackId)

        val newModifiedPlaylist = PlaylistDb(
            id = playlist.id,
            playlistTitle = playlist.playlistTitle,
            playlistDescription = playlist.playlistDescription,
            playlistCoverPath = playlist.playlistCoverPath,
            trackIds = toJsonFromArray(listTrackIds),
            numberOfTracks = playlist.numberOfTracks?.minus(1)
        )

        appDatabase.playlistDao().insertPlaylist(newModifiedPlaylist)*/
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

            val playlist = appDatabase.playlistDao().getPlaylistById(playlistId)
            val listTrackIds = takeFromJson(playlist.trackIds)
            val stringListIds: List<String> = listTrackIds.map { it.toString() }
            //Log.d("PLAYLIST stringListIds RM", stringListIds.toString())

            return if (stringListIds.isEmpty()) {
                flow { emit(ArrayList<Track>()) }
            } else {
                getTracksFromPlaylistByIds(stringListIds)
            }
        } catch (exp: Throwable) {
            return null
        }

        //removeTrackFromPlaylistDb(playlistId, trackId)
        //removeTrackFromCommonTable(trackId)

        /*val playlist = appDatabase.playlistDao().getPlaylistById(playlistId)
            Log.d("PLAYLIST playlist RM", playlist.toString())

            val listTrackIds = takeFromJson(playlist.trackIds)
            Log.d("PLAYLIST listIds RM", listTrackIds.toString())

            val stringListIds: List<String> = listTrackIds.map { it.toString() }
            //Log.d("PLAYLIST stringListIds RM", stringListIds.toString())

            if (oldPlaylistTrackIds.isEmpty()) {
                flow { emit(ArrayList<Track>()) }
            } else {
                val tracksFromPlaylist = getTracksFromPlaylistByIds(oldPlaylistTrackIds)
                Log.d("PLAYLIST TracksFromPlaylist RM", tracksFromPlaylist.toString())
            }
        return null*/
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

    private fun toJsonFromArray(listIds: ArrayList<Int>): String {
        val gson = Gson()
        return gson.toJson(listIds)
    }
}