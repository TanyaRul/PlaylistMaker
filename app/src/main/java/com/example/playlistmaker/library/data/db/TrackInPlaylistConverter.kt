package com.example.playlistmaker.library.data.db

import com.example.playlistmaker.library.data.db.entity.TrackInPlaylist
import com.example.playlistmaker.search.domain.model.Track

class TrackInPlaylistConverter {
    fun map(track: Track): TrackInPlaylist {
        return TrackInPlaylist(
            track.trackId,
            track.trackName,
            track.artistName,
            track.trackTimeMillis,
            track.artworkUrl100,
            track.collectionName,
            track.releaseDate,
            track.primaryGenreName,
            track.country,
            track.previewUrl,
        )
    }
}