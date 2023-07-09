package com.example.playlistmaker.player.ui.activity

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.player.ui.view_model.TrackViewModel
import com.example.playlistmaker.search.domain.model.Track
import java.text.SimpleDateFormat
import java.util.*

class PlayerActivity : ComponentActivity() {

    companion object {
        const val TRACK_DATA_KEY = "key_for_track_data"
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val UPDATE_PLAYBACK_PROGRESS_DELAY_MS = 500L
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
    private var mediaPlayer = MediaPlayer()
    private var playerState = STATE_DEFAULT
    private val handler = Handler(Looper.getMainLooper())
    private val playbackProgressRunnable = createUpdateTimerTask()

    private lateinit var viewModel: TrackViewModel




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        initView()

        backButton.setOnClickListener {
            finish()
        }

        val track = intent.getParcelableExtra<Track>(TRACK_DATA_KEY)

        if (track != null) {
            fillTrackItem(track)
            if (track.previewUrl?.isNotEmpty() == true) {
                preparePlayer(track.previewUrl)
            }
        }

        playbackControlButton.setOnClickListener {
            if (track != null) {
                if (track.previewUrl.isNullOrEmpty()) {
                    playbackControlButton.isEnabled = false
                } else playbackControl(track)
            }
        }

    }

    private fun initView() {
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
        playbackProgress.text = getString(R.string.playback_progress_start)
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

    private fun preparePlayer(url: String) {
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            playerState = STATE_PREPARED
            playbackProgress.text = getString(R.string.playback_progress_start)
        }
        mediaPlayer.setOnCompletionListener {
            handler.removeCallbacks(playbackProgressRunnable)
            playbackControlButton.setImageResource(R.drawable.play_button)
            playerState = STATE_PREPARED
            playbackProgress.text = getString(R.string.playback_progress_start)
        }
    }

    private fun startPlayer(track: Track) {
        if (track.previewUrl?.isNotEmpty() == true) {
            mediaPlayer.start()
            handler.post(playbackProgressRunnable)
            playbackControlButton.setImageResource(R.drawable.pause_button)
            playerState = STATE_PLAYING
        }
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        handler.removeCallbacks(playbackProgressRunnable)
        playbackControlButton.setImageResource(R.drawable.play_button)
        playerState = STATE_PAUSED
    }

    private fun playbackControl(track: Track) {
        when (playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }

            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer(track)
            }
        }
    }

    private fun createUpdateTimerTask(): Runnable {
        return object : Runnable {
            override fun run() {
                playbackProgress.text = SimpleDateFormat(
                    "mm:ss",
                    Locale.getDefault()
                ).format(mediaPlayer.currentPosition)
                handler.postDelayed(this, UPDATE_PLAYBACK_PROGRESS_DELAY_MS)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacks(playbackProgressRunnable)
    }

}