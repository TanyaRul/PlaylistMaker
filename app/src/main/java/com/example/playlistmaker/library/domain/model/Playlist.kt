package com.example.playlistmaker.library.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Playlist(
    var id: Int,
    var playlistTitle: String,
    var playlistDescription: String?,
    var playlistCoverPath: String?,
    var trackIds: List<String>?,
    var numberOfTracks: Int?
) : Parcelable