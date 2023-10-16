package com.example.playlistmaker.library.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists_table")
data class PlaylistDb(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "playlist_title")
    val playlistTitle: String,
    @ColumnInfo(name = "playlist_description")
    val playlistDescription: String?,
    @ColumnInfo(name = "playlist_cover_path")
    val playlistCoverPath: String?,
    @ColumnInfo(name = "track_ids")
    val trackIds: String?,
    @ColumnInfo(name = "number_of_tracks")
    val numberOfTracks: Int?,
)