package com.example.playlistmaker.library.data.db

import com.example.playlistmaker.library.data.db.entity.TrackEntity
import com.example.playlistmaker.search.domain.model.Track

class TrackDbConverter {

    fun map(trackEntity: TrackEntity): Track {
        return Track(
            trackEntity.id,
            trackEntity.trackName,
            trackEntity.artistName,
            trackEntity.trackTimeMillis,
            trackEntity.artworkUrl100,
            trackEntity.collectionName,
            trackEntity.releaseDate,
            trackEntity.primaryGenreName,
            trackEntity.country,
            trackEntity.previewUrl,
        )
    }

    fun map(track: Track): TrackEntity {
        return TrackEntity(
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