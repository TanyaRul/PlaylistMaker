package com.example.playlistmaker.library.data.db

import com.example.playlistmaker.library.data.db.entity.TrackDb
import com.example.playlistmaker.search.domain.model.Track

class TrackDbConverter {

    fun map(trackDb: TrackDb): Track {
        return Track(
            trackDb.id,
            trackDb.trackName,
            trackDb.artistName,
            trackDb.trackTimeMillis,
            trackDb.artworkUrl100,
            trackDb.collectionName,
            trackDb.releaseDate,
            trackDb.primaryGenreName,
            trackDb.country,
            trackDb.previewUrl,
        )
    }

    fun map(track: Track): TrackDb {
        return TrackDb(
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