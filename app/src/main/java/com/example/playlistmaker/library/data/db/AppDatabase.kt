package com.example.playlistmaker.library.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.playlistmaker.library.data.db.dao.PlaylistDao
import com.example.playlistmaker.library.data.db.dao.TrackDao
import com.example.playlistmaker.library.data.db.dao.TrackInPlaylistDao
import com.example.playlistmaker.library.data.db.entity.PlaylistDb
import com.example.playlistmaker.library.data.db.entity.TrackDb
import com.example.playlistmaker.library.data.db.entity.TrackInPlaylist

@Database(version = 1, entities = [TrackDb::class, PlaylistDb::class, TrackInPlaylist::class])

abstract class AppDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun trackInPlaylistDao(): TrackInPlaylistDao

    companion object {
        const val DB_NAME = "playlistmaker-database.db"
    }
}