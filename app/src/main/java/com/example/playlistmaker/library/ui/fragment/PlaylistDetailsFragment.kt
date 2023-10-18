package com.example.playlistmaker.library.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistDetailsBinding
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.domain.model.states.PlaylistDetailsScreenState
import com.example.playlistmaker.library.ui.TrackInPlayListAdapter
import com.example.playlistmaker.library.ui.view_model.PlaylistDetailsViewModel
import com.example.playlistmaker.main.ui.view_model.MainViewModel
import com.example.playlistmaker.player.ui.activity.PlayerActivity
import com.example.playlistmaker.player.ui.model.PlayerTrack
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.util.BindingFragment
import com.example.playlistmaker.util.debounce
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistDetailsFragment : BindingFragment<FragmentPlaylistDetailsBinding>() {

    private val playlistDetailsViewModel: PlaylistDetailsViewModel by viewModel()
    private val mainViewModel by activityViewModel<MainViewModel>()
    private var tracksBottomSheetBehavior: BottomSheetBehavior<LinearLayout>? = null
    private var playlistBottomSheetBehavior: BottomSheetBehavior<LinearLayout>? = null
    private var playlist: Playlist? = null
    private val tracks = mutableListOf<Track>()
    private var listIdTracksTemp: ArrayList<String>? = null
    private var trackInPlaylistAdapter: TrackInPlayListAdapter? = null
    private lateinit var onTrackClickDebounce: (Track) -> Unit
    private var playlistTemp: Playlist? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPlaylistDetailsBinding {
        return FragmentPlaylistDetailsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlist = mainViewModel.getPlaylist().value!!

        playlistDetailsViewModel.getFlowPlaylistById(playlist!!.id)

        Log.d("PLAYLIST oncreate", playlist.toString())

        playlistDetailsViewModel.getStatePlaylistLiveData().observe(viewLifecycleOwner) {
            renderTracksInPlaylist(it)
        }

        binding.ivBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        onTrackClickDebounce = debounce<Track>(
            CLICK_DEBOUNCE_DELAY_MS,
            viewLifecycleOwner.lifecycleScope,
            false
        ) { track ->
            val playerIntent = Intent(requireContext(), PlayerActivity::class.java)
            playerIntent.putExtra(PlayerActivity.TRACK_DATA_KEY, PlayerTrack.mappingTrack(track))
            requireContext().startActivity(playerIntent)
        }

        tracksBottomSheetBehavior = BottomSheetBehavior.from(binding.tracksBottomSheet)

        binding.tracksBottomSheet.post {
            val buttonLocation = IntArray(2)
            binding.ivShareButton.getLocationOnScreen(buttonLocation)
            val openMenuHeightFromBottom =
                binding.root.height - buttonLocation[1] - resources.getDimensionPixelSize(R.dimen.layout_margin_very_low)
            tracksBottomSheetBehavior?.peekHeight = openMenuHeightFromBottom.coerceAtLeast(100)
        }

        playlistBottomSheetBehavior = BottomSheetBehavior.from(binding.playlistBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        playlistBottomSheetBehavior?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.playlistOverlay.isVisible = false
                        binding.rvTrack.isVisible = true
                    }

                    else -> {
                        binding.playlistOverlay.isVisible = true
                        binding.rvTrack.isVisible = false
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        binding.ivMenuButton.setOnClickListener {
            playlistBottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            showMenu(playlist!!)
        }

        binding.ivShareButton.setOnClickListener {
            Log.d("PLAYLIST onclick", playlist.toString())
            share()
        }
    }

    private fun renderTracksInPlaylist(state: PlaylistDetailsScreenState) {
        when (state) {
            is PlaylistDetailsScreenState.NoTracks -> showStateNoTracks()

            is PlaylistDetailsScreenState.WithTracks -> showStateWithTracks(
                state.listTracks,
                state.durationSumTime
            )

            is PlaylistDetailsScreenState.DeletedTrack -> showStateDeletedTrack(
                state.listTracks,
                state.durationSumTime,
                state.counterTracks
            )

            is PlaylistDetailsScreenState.DeletedPlaylist -> {
                showStateDeletedPlaylist()
            }

            is PlaylistDetailsScreenState.InitPlaylist -> initPlaylist(state.playlist)

            is PlaylistDetailsScreenState.Error -> Log.e(
                "ErrorQueryOnDb",
                getString(R.string.empty_playlist)
            )
        }
    }

    private fun showStateNoTracks() {
        binding.tvEmptyPlaylistMessage.isVisible = true
        binding.tvEmptyPlaylistMessage.setText(R.string.empty_playlist)
        binding.tvTracksTime.text = requireActivity().resources.getQuantityString(
            R.plurals.minutes,
            0, 0
        )
        binding.tvTracksCount.text = requireActivity().resources.getQuantityString(
            R.plurals.track_count,
            0, 0
        )
        tracks.clear()
        trackInPlaylistAdapter?.notifyDataSetChanged()
    }

    private fun showStateWithTracks(listTracks: List<Track>, durationSumTime: Long) {
        binding.tvEmptyPlaylistMessage.isVisible = false
        val updatedTracks = listTracks.map { track ->
            track.copy(artworkUrl100 = track.artworkUrl100?.replaceAfterLast('/', "60x60bb.jpg"))
        }
        binding.tvTracksTime.text = requireActivity().resources.getQuantityString(
            R.plurals.minutes,
            durationSumTime.toInt(), durationSumTime
        )
        tracks.clear()
        tracks.addAll(updatedTracks.reversed())
        trackInPlaylistAdapter?.notifyDataSetChanged()
        binding.rvTrack.isVisible = true
        if (updatedTracks.isEmpty()) showMistakeDialog()
    }

    private fun showStateDeletedTrack(
        listTracks: List<Track>,
        durationSumTime: Long,
        counterTracks: Int
    ) {
        val updatedTracks = listTracks.map { track ->
            track.copy(artworkUrl100 = track.artworkUrl100?.replaceAfterLast('/', "60x60bb.jpg"))
        }
        binding.tvTracksTime.text = requireActivity().resources.getQuantityString(
            R.plurals.minutes,
            durationSumTime.toInt(), durationSumTime
        )
        binding.tvTracksCount.text = requireActivity().resources.getQuantityString(
            R.plurals.track_count,
            counterTracks, counterTracks
        )
        tracks.clear()
        tracks.addAll(updatedTracks)
        trackInPlaylistAdapter?.notifyDataSetChanged()
        if (updatedTracks.isEmpty()) showMistakeDialog()
    }

    private fun showStateDeletedPlaylist() {
        findNavController().popBackStack()
    }

    private fun initPlaylist(playlist: Playlist?) {

        playlistTemp = playlist

        listIdTracksTemp = if (playlist?.trackIds?.isNotEmpty() == true) {
            playlist.trackIds as ArrayList<String>?
        } else {
            ArrayList()
        }

        playlistDetailsViewModel.getTracksFromPlaylistByIds(listIdTracksTemp)

        binding.apply {

            Glide.with(requireActivity())
                .load(playlist?.playlistCoverPath)
                .placeholder(R.drawable.placeholder_large)
                .into(ivCover)

            tvTitle.text = playlist?.playlistTitle
            if (playlist?.playlistDescription.isNullOrEmpty()) {
                tvDescription.isVisible = false
            } else {
                tvDescription.text = playlist?.playlistDescription
            }

            tvTracksTime.text = requireActivity().resources.getQuantityString(
                R.plurals.minutes,
                0, 0
            )
            tvTracksCount.text = playlist?.numberOfTracks?.let {
                requireActivity().resources.getQuantityString(
                    R.plurals.track_count,
                    it, playlist.numberOfTracks
                )
            }

            trackInPlaylistAdapter = TrackInPlayListAdapter(
                tracks,
                MaterialAlertDialogBuilder(
                    requireActivity(),
                    R.style.AlertDialogTheme
                )
                    .setTitle(R.string.delete_track)
                    .setMessage(R.string.delete_track_warning),
                { track ->
                    onTrackClickDebounce(track)
                },
                { track ->
                    playlist?.id?.let {
                        playlistDetailsViewModel.removeTrackFromPlaylist(
                            it,
                            track.trackId.toInt()
                        )
                    }
                })

            rvTrack.adapter = trackInPlaylistAdapter
        }
    }

    private fun showMistakeDialog() {
        MaterialAlertDialogBuilder(requireActivity(), R.style.AlertDialogTheme)
            .setMessage(R.string.share_empty_playlist)
            .setPositiveButton(
                R.string.ok
            ) { _, _ -> }.show()

    }

    private fun showMenu(playlist: Playlist) {
        binding.apply {
            Glide.with(requireActivity())
                .load(playlist.playlistCoverPath)
                .placeholder(R.drawable.placeholder)
                .transform(
                    CenterCrop(),
                    RoundedCorners(
                        requireContext().resources.getDimensionPixelSize(R.dimen.album_cover_corners_radius)
                    )
                )
                .into(playlistItem.ivPlaylistCoverSmall)

            playlistItem.tvPlaylistName.text = playlist.playlistTitle
            val countText = playlist.numberOfTracks?.let {
                requireActivity().resources.getQuantityString(
                    R.plurals.track_count,
                    it, playlist.numberOfTracks
                )
            }
            playlistItem.tvNumberOfTracks.text = countText

            tvDeleteTextMenu.setOnClickListener {
                playlistBottomSheetBehavior?.apply {
                    state = BottomSheetBehavior.STATE_HIDDEN
                }
                MaterialAlertDialogBuilder(requireActivity(), R.style.AlertDialogTheme)
                    .setTitle(R.string.delete_playlist)
                    .setMessage(
                        requireActivity().resources.getString(R.string.delete_playlist_warning)
                            .format(playlist.playlistTitle)
                    )
                    .setNegativeButton(R.string.cancel) { _, _ ->
                    }
                    .setPositiveButton(R.string.delete) { _, _ ->
                        playlistDetailsViewModel.deletePlaylist(playlist)
                    }.show()
            }

            tvShareTextMenu.setOnClickListener {
                playlistBottomSheetBehavior?.apply {
                    state = BottomSheetBehavior.STATE_HIDDEN
                }

                share()
            }

            tvUpdateTextMenu.setOnClickListener {
                mainViewModel.setPlaylist(playlist)
                findNavController().navigate(
                    R.id.playlistEditingFragment
                )
            }
        }
    }

    private fun share() {
        Log.d("PLAYLIST SHARE", playlist.toString())
        if (playlist?.numberOfTracks!! > 0) trackInPlaylistAdapter?.let {
            playlistDetailsViewModel.sharePlaylist(
                playlist!!,
                it.tracks
            )
        }
        else showMistakeDialog()
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY_MS = 1000L
    }
}