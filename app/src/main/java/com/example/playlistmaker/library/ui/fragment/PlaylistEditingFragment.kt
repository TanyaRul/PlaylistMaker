package com.example.playlistmaker.library.ui.fragment

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentNewPlaylistBinding
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.ui.view_model.PlaylistEditingViewModel
import com.example.playlistmaker.main.ui.view_model.MainViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class PlaylistEditingFragment : NewPlaylistFragment() {

    override val newPlaylistViewModel by viewModel<PlaylistEditingViewModel>()
    private var playlist: Playlist? = null
    private val mainViewModel by activityViewModel<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlist = mainViewModel.getPlaylist().value!!

        binding.tvNewPlaylist.text = requireActivity().resources.getString(R.string.edit)

        Glide.with(this)
            .load(playlist?.playlistCoverPath)
            .placeholder(R.drawable.add_photo)
            .transform(
                CenterCrop(),
                RoundedCorners(
                    requireContext().resources.getDimensionPixelSize(R.dimen.album_cover_corners_radius_large)
                )
            )
            .into(binding.ivPlaylistCover)

        binding.etPlaylistTitle.setText(playlist?.playlistTitle)

        binding.etPlaylistDescription.setText(playlist?.playlistDescription)

        binding.tvCreatePlaylist.text = requireActivity().resources.getString(R.string.save)

    }

    override fun onBackPress() {
        findNavController().navigateUp()
    }

}