package com.example.playlistmaker.library.data.db

import com.example.playlistmaker.library.data.db.entity.PlaylistDb
import com.example.playlistmaker.library.domain.model.Playlist
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlaylistDbConverter {
    fun mapFromPlaylistDbToPlaylist(playlistDb: PlaylistDb): Playlist {
        val trackIdsList =
            if (playlistDb.trackIds != "null" && !playlistDb.trackIds.isNullOrEmpty()) {
                fromString(playlistDb.trackIds)
            } else {
                emptyList()
            }

        return Playlist(
            id = playlistDb.id,
            playlistTitle = playlistDb.playlistTitle,
            playlistDescription = playlistDb.playlistDescription,
            playlistCoverPath = playlistDb.playlistCoverPath,
            trackIds = trackIdsList,
            numberOfTracks = playlistDb.numberOfTracks ?: 0,
        )
    }

    fun mapFromPlaylistToPlaylistDb(playlist: Playlist): PlaylistDb {
        return PlaylistDb(
            id = playlist.id,
            playlistTitle = playlist.playlistTitle,
            playlistDescription = playlist.playlistDescription,
            playlistCoverPath = playlist.playlistCoverPath,
            trackIds = fromList(playlist.trackIds),
            numberOfTracks = playlist.trackIds?.size
        )
    }

    private fun fromString(value: String?): List<String>? {
        if (value == null) {
            return null
        }
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    private fun fromList(list: List<String>?): String? {
        if (list == null) {
            return null
        }
        val gson = Gson()
        return gson.toJson(list)
    }

}