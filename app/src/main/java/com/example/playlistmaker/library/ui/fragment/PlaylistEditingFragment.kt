package com.example.playlistmaker.library.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
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
    private var playlistIdTemp: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlist = mainViewModel.getPlaylist().value!!

        playlistIdTemp = playlist?.id!!
        playlistTitleTemp = playlist?.playlistTitle!!
        playlistDescriptionTemp = playlist?.playlistDescription
        if(playlist?.playlistCoverPath != null){
            playlistCoverTemp = playlist?.playlistCoverPath!!
            Log.d("PLAYLIST edititng playlistCoverTemp", playlistCoverTemp.toString())
        }
        playlistTrackIdsTemp = playlist?.trackIds
        playlistNumberOfTracksTemp = playlist?.numberOfTracks



        setViewAttributes()

        initPlaylistData(playlist!!)

        binding.tvCreatePlaylist.setOnClickListener{
                updatePlaylist()
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        onBackPress(false)
    }

    override fun onBackPress(switch: Boolean) {
    }

    private fun setViewAttributes() {
        binding.tvNewPlaylist.text = requireActivity().resources.getString(R.string.edit)
        binding.tvCreatePlaylist.text = requireActivity().resources.getString(R.string.save)
    }

    private fun initPlaylistData(playlist: Playlist) {
        Glide.with(this)
            .load(playlist.playlistCoverPath)
            .placeholder(R.drawable.add_photo)
            .transform(
                CenterCrop(),
                RoundedCorners(
                    requireContext().resources.getDimensionPixelSize(R.dimen.album_cover_corners_radius_large)
                )
            )
            .into(binding.ivPlaylistCover)

        Log.d("PLAYLIST edititng playlistCoverPath", playlist.playlistCoverPath.toString())

        binding.etPlaylistTitle.setText(playlist.playlistTitle)

        binding.etPlaylistDescription.setText(playlist.playlistDescription)
    }

    private fun updatePlaylist () {
        val playlist = Playlist(
            playlistIdTemp,
            playlistTitleTemp,
            playlistDescriptionTemp,
            playlistCoverTemp,
            playlistTrackIdsTemp,
            playlistNumberOfTracksTemp
        )
        Log.d("PLAYLIST update playlistCoverTemp", playlistCoverTemp.toString())
        newPlaylistViewModel.updatePlaylist(playlist)
        findNavController().popBackStack()
    }

}