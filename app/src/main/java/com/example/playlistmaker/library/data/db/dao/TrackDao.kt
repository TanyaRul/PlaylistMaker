package com.example.playlistmaker.library.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.playlistmaker.library.data.db.entity.TrackDb

@Dao
interface TrackDao {
    @Insert(entity = TrackDb::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackEntity(trackDb: TrackDb)

    @Delete(entity = TrackDb::class)
    suspend fun deleteTrackEntity(trackDb: TrackDb)

    @Query("SELECT * FROM favorite_tracks_table ORDER BY addingtime DESC")
    suspend fun getFavoriteTracks(): List<TrackDb>

    @Query("SELECT track_id FROM favorite_tracks_table")
    suspend fun getFavoriteTracksIds(): List<String>
}