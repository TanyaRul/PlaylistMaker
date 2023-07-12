package com.example.playlistmaker.player.ui.activity

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.player.domain.model.PlayerState
import com.example.playlistmaker.player.ui.model.PlayerTrack
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import java.text.SimpleDateFormat
import java.util.*

class PlayerActivity : ComponentActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var viewModel: PlayerViewModel

    private var playerState = PlayerState.STATE_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backToSearch.setOnClickListener {
            finish()
        }

        viewModel = ViewModelProvider(
            this,
            PlayerViewModel.getViewModelFactory()
        )[PlayerViewModel::class.java]

        val track = intent.getParcelableExtra<PlayerTrack>(TRACK_DATA_KEY)

        if (track != null) {
            fillTrackItem(track, binding)
            if (track.previewUrl?.isNotEmpty() == true) {
                viewModel.prepare(track.previewUrl)
            }
        }

        binding.playbackControlButton.setOnClickListener {
            if (track != null) {
                if (track.previewUrl.isNullOrEmpty()) {
                    binding.playbackControlButton.isEnabled = false
                } else playbackControl(track)
            }
        }

    }

    private fun fillTrackItem(track: PlayerTrack, binding: ActivityPlayerBinding) {
        Glide.with(binding.cover)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.album_cover_corners_radius_large)))
            .into(binding.cover)

        binding.trackName.text = track.trackName
        binding.artistName.text = track.artistName
        binding.playbackProgress.text = getString(R.string.playback_progress_start)
        binding.currentTrackTime.text =
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)

        if (track.collectionName.isNullOrEmpty()) {
            binding.albumName.visibility = View.GONE
            binding.currentAlbumName.visibility = View.GONE
        } else {
            binding.currentAlbumName.text = track.collectionName
        }

        if (!track.releaseDate.isNullOrEmpty()) {
            binding.currentReleaseDate.text = getFormattedYear(track)
        }

        binding.currentGenre.text = track.primaryGenreName
        binding.currentCountry.text = track.country
    }

    private fun getFormattedYear(track: PlayerTrack): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = format.parse(track.releaseDate) as Date
        return calendar.get(Calendar.YEAR).toString()
    }

    fun preparePlayer(url: String) {
        binding.playbackProgress.text = getString(R.string.playback_progress_start)
        binding.playbackControlButton.setImageResource(R.drawable.play_button)
    }


    private fun startPlayer(track: PlayerTrack) {
        if (track.previewUrl?.isNotEmpty() == true) {
            viewModel.play()
            binding.playbackControlButton.setImageResource(R.drawable.pause_button)
        }
    }

    private fun pausePlayer() {
        binding.playbackControlButton.setImageResource(R.drawable.play_button)
        binding.playbackControlButton.setImageResource(R.drawable.play_button)
        viewModel.pause()
    }

    private fun playbackControl(track: PlayerTrack) {
        when (playerState) {
            PlayerState.STATE_PLAYING -> {
                pausePlayer()
            }

            PlayerState.STATE_PREPARED, PlayerState.STATE_PAUSED -> {
                startPlayer(track)
            }

            else -> {}
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.release()
        //handler.removeCallbacks(playbackProgressRunnable)
    }


    companion object {
        const val TRACK_DATA_KEY = "key_for_track_data"
    }

}