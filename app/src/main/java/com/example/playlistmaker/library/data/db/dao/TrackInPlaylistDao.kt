package com.example.playlistmaker.library.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.playlistmaker.library.data.db.entity.TrackInPlaylist

@Dao
interface TrackInPlaylistDao {
    @Insert(entity = TrackInPlaylist::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(trackInPlaylist: TrackInPlaylist)
}