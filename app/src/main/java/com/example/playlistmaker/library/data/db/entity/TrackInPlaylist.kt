package com.example.playlistmaker.library.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_in_playlist_table")
data class TrackInPlaylist(
    @PrimaryKey @ColumnInfo(name = "track_id")
    val id: String,
    @ColumnInfo(name = "track_name")
    val trackName: String?,
    @ColumnInfo(name = "artist_name")
    val artistName: String?,
    @ColumnInfo(name = "track_time_millis")
    val trackTimeMillis: Long?,
    @ColumnInfo(name = "artwork_url_100")
    val artworkUrl100: String?,
    @ColumnInfo(name = "collection_name")
    val collectionName: String?,
    @ColumnInfo(name = "release_date")
    val releaseDate: String?,
    @ColumnInfo(name = "primary_genre_name")
    val primaryGenreName: String?,
    val country: String?,
    @ColumnInfo(name = "previewUrl")
    val previewUrl: String?,
    @ColumnInfo(name = "adding_time")
    val addingTime: Long = System.currentTimeMillis(),
)