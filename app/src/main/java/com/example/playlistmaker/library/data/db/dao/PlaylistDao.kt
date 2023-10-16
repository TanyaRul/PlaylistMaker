package com.example.playlistmaker.library.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.playlistmaker.library.data.db.entity.PlaylistDb
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert(entity = PlaylistDb::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlistDb: PlaylistDb)

    @Update(entity = PlaylistDb::class)
    suspend fun updatePlaylist(playlistDb: PlaylistDb)

    @Query("SELECT * FROM playlists_table ORDER BY id DESC")
    suspend fun getPlaylists(): List<PlaylistDb>

    @Query("SELECT * FROM playlists_table ORDER BY id DESC")
    fun getFlowPlaylists(): Flow<List<PlaylistDb>>

    @Query("SELECT * FROM playlists_table WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Int): PlaylistDb

    @Query("SELECT * FROM playlists_table WHERE id = :playlistId")
    fun getFlowPlaylistById(playlistId: Int): Flow<PlaylistDb>

    @Query("DELETE FROM playlists_table WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Int)
    //@Delete
    //suspend fun deletePlaylist(playlist: PlaylistEntity)

}