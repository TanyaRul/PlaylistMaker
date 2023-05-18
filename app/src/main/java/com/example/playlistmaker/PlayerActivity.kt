package com.example.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val TRACK_DATA_KEY = "key_for_track_data"
    }

    private lateinit var backButton: ImageButton
    private lateinit var albumCover: ImageView
    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var playlistButton: ImageButton
    private lateinit var playbackControlButton: ImageButton
    private lateinit var favoriteButton: ImageButton
    private lateinit var playbackProgress: TextView
    private lateinit var currentTrackTime: TextView
    private lateinit var albumName: TextView
    private lateinit var currentAlbumName: TextView
    private lateinit var currentReleaseDate: TextView
    private lateinit var currentGenre: TextView
    private lateinit var currentCountry: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        findViewById()

        backButton.setOnClickListener {
            finish()
        }

        val json: String? = intent.getStringExtra(TRACK_DATA_KEY)
        val track: Track? = Gson().fromJson(json, Track::class.java)

        if (track != null) fillTrackItem(track)
    }

    private fun findViewById() {
        backButton = findViewById(R.id.backToSearch)
        albumCover = findViewById(R.id.cover)
        trackName = findViewById(R.id.trackName)
        artistName = findViewById(R.id.artistName)
        playlistButton = findViewById(R.id.playlistButton)
        playbackControlButton = findViewById(R.id.playbackControlButton)
        favoriteButton = findViewById(R.id.favoriteButton)
        playbackProgress = findViewById(R.id.playbackProgress)
        currentTrackTime = findViewById(R.id.currentTrackTime)
        albumName = findViewById(R.id.albumName)
        currentAlbumName = findViewById(R.id.currentAlbumName)
        currentReleaseDate = findViewById(R.id.currentReleaseDate)
        currentGenre = findViewById(R.id.currentGenre)
        currentCountry = findViewById(R.id.currentCountry)
    }

    private fun fillTrackItem(track: Track) {
        Glide.with(albumCover)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.album_cover_corners_radius_large)))
            .into(albumCover)

        trackName.text = track.trackName
        artistName.text = track.artistName
        playbackProgress.text = "0:30"
        currentTrackTime.text =
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)

        if (track.collectionName.isNullOrEmpty()) {
            albumName.visibility = View.GONE
            currentAlbumName.visibility = View.GONE
        } else {
            currentAlbumName.text = track.collectionName
        }

        if (!track.releaseDate.isNullOrEmpty()) {
            currentReleaseDate.text = getFormattedYear(track)
        }

        currentGenre.text = track.primaryGenreName
        currentCountry.text = track.country
    }

    private fun getFormattedYear(track: Track): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = format.parse(track.releaseDate) as Date
        return calendar.get(Calendar.YEAR).toString()
    }

}