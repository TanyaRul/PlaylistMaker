package com.example.playlistmaker.player.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.ui.PlaylistsScreenState
import com.example.playlistmaker.library.ui.fragment.NewPlaylistFragment
import com.example.playlistmaker.library.domain.model.states.AddTrackState
import com.example.playlistmaker.player.domain.model.PlayerState
import com.example.playlistmaker.player.ui.BottomSheetPlaylistsAdapter
import com.example.playlistmaker.player.ui.model.PlayerTrack
import com.example.playlistmaker.player.ui.states.PlayerToastState
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.util.debounce
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    private val playerViewModel by viewModel<PlayerViewModel>()

    private var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>? = null

    private var playlists = ArrayList<Playlist>()

    private lateinit var onPlaylistClickDebounce: (Playlist) -> Unit

    private val playlistsAdapter = BottomSheetPlaylistsAdapter(playlists) {
        onPlaylistClickDebounce(it)
    }

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
            playerViewModel.prepare(track.previewUrl)
        }

        lifecycleScope.launch {
            val isFavorite = playerViewModel.isTackFavorite(track.trackId)
            changeFavoriteIcon(isFavorite)
            track.isFavorite = isFavorite
        }

        bottomSheetBehavior = BottomSheetBehavior.from(binding.playlistsBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehavior?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlay.isVisible = false
                    }

                    else -> binding.overlay.isVisible = true
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        binding.rvTracks.adapter = playlistsAdapter

        onPlaylistClickDebounce = debounce<Playlist>(
            CLICK_DEBOUNCE_DELAY_MS,
            lifecycleScope,
            false
        ) { playlist ->
            playerViewModel.onPlaylistClicked(playlist, PlayerTrack.mappingPlayerTrack(track))
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }

        playerViewModel.playerStateLiveData.observe(this) { playerState ->
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

        playerViewModel.getCurrentTimerLiveData().observe(this) { currentTimer ->
            changeTimer(currentTimer)
        }

        playerViewModel.isFavoriteLiveData.observe(this) { isFavorite ->
            changeFavoriteIcon(isFavorite)
            track.isFavorite = isFavorite
        }

        playerViewModel.observeState().observe(this) {
            render(it)
        }

        playerViewModel.observeAddTrackState().observe(this) {
            renderAddTrack(it)
        }

        playerViewModel.observeToastState().observe(this) { toastState ->
            if (toastState is PlayerToastState.ShowMessage) {
                showMessage(toastState.message)
                playerViewModel.toastWasShown()
            }
        }

        binding.favoriteButton.setOnClickListener {
            playerViewModel.onFavoriteClicked(mappingFromPlayerTrack(track))
        }

        binding.backToSearch.setOnClickListener {
            finish()
        }

        binding.playlistButton.setOnClickListener {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            playerViewModel.fillData()
        }

        binding.btnNewPlaylist.setOnClickListener {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(
                R.id.playerFragmentContainer,
                NewPlaylistFragment.newInstance(true)
            )
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            binding.playerScrollView.isVisible = false
            binding.playerFragmentContainer.isVisible = true
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
        playerViewModel.play()
    }

    private fun pausePlayer() {
        binding.playbackControlButton.setImageResource(R.drawable.play_button)
        playerViewModel.pause()
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

    private fun render(state: PlaylistsScreenState) {
        when (state) {
            is PlaylistsScreenState.Content -> showContent(state.playlists)
            is PlaylistsScreenState.Empty -> showEmpty()
        }
    }

    private fun showEmpty() {
        binding.rvTracks.isVisible = false
        binding.placeholderTextNoPlaylist.isVisible = true
        binding.placeholderImageNoPlaylist.isVisible = false
    }

    private fun showContent(playlists: List<Playlist>) {
        binding.rvTracks.isVisible = true
        binding.placeholderTextNoPlaylist.isVisible = false
        binding.placeholderImageNoPlaylist.isVisible = false

        playlistsAdapter.playlists.clear()
        playlistsAdapter.playlists.addAll(playlists)
        playlistsAdapter.notifyDataSetChanged()
    }

    private fun renderAddTrack(state: AddTrackState) {
        when (state) {
            is AddTrackState.Exist -> {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                playerViewModel.showToast("${getString(R.string.already_in_playlist)} ${state.playlistTitle}")
            }

            is AddTrackState.Added -> {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                binding.playlistButton.setImageResource(R.drawable.playlist_button_added)
                playerViewModel.showToast("${getString(R.string.added_to_playlist)} ${state.playlistTitle}")
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior?.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    companion object {
        const val TRACK_DATA_KEY = "key_for_track_data"
        private const val CLICK_DEBOUNCE_DELAY_MS = 1000L
    }

}