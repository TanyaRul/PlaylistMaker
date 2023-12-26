package com.example.playlistmaker.library.data.db.converters

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

    fun map(trackInPlaylist: TrackInPlaylist): Track {
        return Track(
            trackInPlaylist.id,
            trackInPlaylist.trackName,
            trackInPlaylist.artistName,
            trackInPlaylist.trackTimeMillis,
            trackInPlaylist.artworkUrl100,
            trackInPlaylist.collectionName,
            trackInPlaylist.releaseDate,
            trackInPlaylist.primaryGenreName,
            trackInPlaylist.country,
            trackInPlaylist.previewUrl,
        )
    }
}