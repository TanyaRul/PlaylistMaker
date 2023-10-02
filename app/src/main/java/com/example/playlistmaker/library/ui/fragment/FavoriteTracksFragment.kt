package com.example.playlistmaker.library.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.playlistmaker.databinding.FragmentFavoriteTracksBinding
import com.example.playlistmaker.library.ui.FavoritesScreenState
import com.example.playlistmaker.library.ui.view_model.FavoriteTracksViewModel
import com.example.playlistmaker.player.ui.activity.PlayerActivity
import com.example.playlistmaker.player.ui.model.PlayerTrack
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.ui.TrackAdapter
import com.example.playlistmaker.util.BindingFragment
import com.example.playlistmaker.util.debounce
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteTracksFragment : BindingFragment<FragmentFavoriteTracksBinding>() {

    private val favoriteTracksViewModel by viewModel<FavoriteTracksViewModel>()

    private val _trackList = ArrayList<Track>()
    private lateinit var onTrackClickDebounce: (Track) -> Unit

    private val trackAdapter = TrackAdapter(_trackList) {
        onTrackClickDebounce(it)
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFavoriteTracksBinding {
        return FragmentFavoriteTracksBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvTrackList.adapter = trackAdapter

        favoriteTracksViewModel.fillData()

        favoriteTracksViewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }

        onTrackClickDebounce = debounce<Track>(
            CLICK_DEBOUNCE_DELAY_MS,
            viewLifecycleOwner.lifecycleScope,
            false
        ) { track ->
            val playerIntent = Intent(requireContext(), PlayerActivity::class.java)
            playerIntent.putExtra(PlayerActivity.TRACK_DATA_KEY, PlayerTrack.mappingTrack(track))
            startActivity(playerIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        favoriteTracksViewModel.fillData()
    }

    private fun render(state: FavoritesScreenState) {
        when (state) {
            is FavoritesScreenState.Content -> showContent(state.tracks)
            is FavoritesScreenState.Empty -> showEmpty(state.emptyMessage)
        }
    }

    private fun showEmpty(emptyMessage: String) {
        binding.rvTrackList.isVisible = false
        binding.placeholderTextNoFavorites.isVisible = true
        binding.placeholderImageNoFavorites.isVisible = true
        binding.placeholderTextNoFavorites.text = emptyMessage
    }

    private fun showContent(tracks: List<Track>) {
        binding.rvTrackList.isVisible = true
        binding.placeholderTextNoFavorites.isVisible = false
        binding.placeholderImageNoFavorites.isVisible = false

        trackAdapter.trackList.clear()
        trackAdapter.trackList.addAll(tracks)
        trackAdapter.notifyDataSetChanged()
    }

    companion object {
        fun newInstance() = FavoriteTracksFragment()
        private const val CLICK_DEBOUNCE_DELAY_MS = 1000L
    }
}