package com.example.playlistmaker.library.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.playlistmaker.library.data.db.entity.PlaylistDb
import com.example.playlistmaker.library.data.db.entity.TrackInPlaylist

@Dao
interface TrackInPlaylistDao {
    @Insert(entity = TrackInPlaylist::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(trackInPlaylist: TrackInPlaylist)

    @Query("SELECT * FROM track_in_playlist_table")
    suspend fun getTracksFromPlaylists(): List<TrackInPlaylist>?

    @Query("DELETE FROM track_in_playlist_table WHERE track_id = :id")
    suspend fun deleteTrackByIdFromTracksInPlaylists(id: Int)
}