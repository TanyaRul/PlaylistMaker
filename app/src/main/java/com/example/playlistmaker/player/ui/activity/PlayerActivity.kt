package com.example.playlistmaker.player.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.player.domain.model.PlayerState
import com.example.playlistmaker.player.ui.model.PlayerTrack
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    private val viewModel by viewModel<PlayerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val track =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(TRACK_DATA_KEY, PlayerTrack::class.java)
            } else {
                intent.getParcelableExtra(TRACK_DATA_KEY)
            } as PlayerTrack


        fillTrackItem(track, binding)

        if (track.previewUrl?.isNotEmpty() == true) {
            binding.playbackProgress.text = getString(R.string.playback_progress_start)
            binding.playbackControlButton.setImageResource(R.drawable.play_button)
            viewModel.prepare(track.previewUrl)
        }

        viewModel.playerStateLiveData.observe(this) { playerState ->
            binding.playbackControlButton.setOnClickListener {
                if (track.previewUrl.isNullOrEmpty()) {
                    binding.playbackControlButton.isEnabled = false
                } else {
                    playbackControl(playerState)
                }
            }
            if (playerState == PlayerState.STATE_COMPLETE) {
                binding.playbackProgress.text = getString(R.string.playback_progress_start)
                pausePlayer()
            }
        }

        viewModel.getCurrentTimerLiveData().observe(this) { currentTimer ->
            changeTimer(currentTimer)
        }

        viewModel.isFavoriteLiveData.observe(this) { isFavorite ->
            changeFavoriteIcon(isFavorite)
            track.isFavorite = isFavorite
        }

        lifecycleScope.launch {
            val isFavorite = viewModel.isTackFavorite(track.trackId)
            changeFavoriteIcon(isFavorite)
            track.isFavorite = isFavorite
        }

        binding.favoriteButton.setOnClickListener {
            viewModel.onFavoriteClicked(mappingFromPlayerTrack(track))
        }

        binding.backToSearch.setOnClickListener {
            finish()
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
        calendar.time = track.releaseDate?.let { format.parse(it) } as Date
        return calendar.get(Calendar.YEAR).toString()
    }

    private fun changeTimer(currentTimer: Int) {
        binding.playbackProgress.text =
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(currentTimer)
    }


    private fun startPlayer() {
        binding.playbackControlButton.setImageResource(R.drawable.pause_button)
        viewModel.play()
    }

    private fun pausePlayer() {
        binding.playbackControlButton.setImageResource(R.drawable.play_button)
        viewModel.pause()
    }

    private fun playbackControl(state: PlayerState) {
        when (state) {
            PlayerState.STATE_PLAYING -> {
                pausePlayer()
            }

            PlayerState.STATE_PREPARED, PlayerState.STATE_PAUSED, PlayerState.STATE_COMPLETE -> {
                startPlayer()
            }
        }
    }

    private fun mappingFromPlayerTrack(track: PlayerTrack): Track {
        return PlayerTrack.mappingPlayerTrack(track)
    }

    private fun changeFavoriteIcon(isFavorite: Boolean) {
        val buttonImageResource = if (isFavorite) {
            R.drawable.favorite_button_pressed
        } else {
            R.drawable.favorite_button
        }
        binding.favoriteButton.setImageResource(buttonImageResource)
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    companion object {
        const val TRACK_DATA_KEY = "key_for_track_data"
    }

}