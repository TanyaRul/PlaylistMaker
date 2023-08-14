package com.example.playlistmaker.library.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import com.example.playlistmaker.library.ui.view_model.PlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : BindingFragment<FragmentPlaylistsBinding>() {

    private val playlistsViewModel by viewModel<PlaylistsViewModel>()

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPlaylistsBinding {
        return FragmentPlaylistsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }

}