package com.example.playlistmaker.library.domain.model

import java.io.Serializable

data class Playlist(
    val id: Int,
    val playlistTitle: String,
    val playlistDescription: String?,
    var playlistCoverPath: String?,
    var trackIds: List<String>?,
    var numberOfTracks: Int?
) : Serializable