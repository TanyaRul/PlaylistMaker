package com.example.playlistmaker.library.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.ui.PlaylistAdapter
import com.example.playlistmaker.library.ui.PlaylistsScreenState
import com.example.playlistmaker.library.ui.view_model.PlaylistsViewModel
import com.example.playlistmaker.util.BindingFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : BindingFragment<FragmentPlaylistsBinding>() {

    private val playlistsViewModel by viewModel<PlaylistsViewModel>()

    private var playlists = ArrayList<Playlist>()

    private val playlistAdapter = PlaylistAdapter(playlists)

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPlaylistsBinding {
        return FragmentPlaylistsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvPlaylists.adapter = playlistAdapter

        playlistsViewModel.fillData()

        playlistsViewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }

        binding.newPlaylistButton.setOnClickListener {
            findNavController().navigate(R.id.newPlaylistFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        playlistsViewModel.fillData()
    }

    private fun render(state: PlaylistsScreenState) {
        when (state) {
            is PlaylistsScreenState.Content -> showContent(state.playlists)
            is PlaylistsScreenState.Empty -> showEmpty()
        }
    }

    private fun showEmpty() {
        binding.rvPlaylists.isVisible = false
        binding.placeholderTextNoPlaylist.isVisible = true
        binding.placeholderImageNoPlaylist.isVisible = true
        binding.placeholderTextNoPlaylist.setText(R.string.no_playlist)
    }

    private fun showContent(playlists: List<Playlist>) {
        binding.rvPlaylists.isVisible = true
        binding.placeholderTextNoPlaylist.isVisible = false
        binding.placeholderImageNoPlaylist.isVisible = false

        playlistAdapter.playlists.clear()
        playlistAdapter.playlists.addAll(playlists)
        playlistAdapter.notifyDataSetChanged()
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }

}