package com.example.playlistmaker.library.ui.fragment

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentNewPlaylistBinding
import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.library.ui.view_model.NewPlaylistViewModel
import com.example.playlistmaker.util.BindingFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.markodevcic.peko.PermissionRequester
import com.markodevcic.peko.PermissionResult
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewPlaylistFragment : BindingFragment<FragmentNewPlaylistBinding>() {

    private val newPlaylistViewModel by viewModel<NewPlaylistViewModel>()

    private val requester = PermissionRequester.instance()

    private var imagePath: String? = null

    private var confirmDialog: MaterialAlertDialogBuilder? = null

    private var pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Glide.with(this)
                    .load(uri.toString())
                    .placeholder(R.drawable.add_photo)
                    .transform(
                        CenterCrop(),
                        RoundedCorners(
                            requireContext().resources.getDimensionPixelSize(R.dimen.album_cover_corners_radius_large)
                        )
                    )
                    .into(binding.ivPlaylistCover)

                imagePath = uri.toString()
            }
        }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNewPlaylistBinding {
        return FragmentNewPlaylistBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        confirmDialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle(R.string.complete_playlist_creation)
            .setMessage(R.string.data_loss_warning)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }.setPositiveButton(R.string.complete) { _, _ ->
                navigateBack()
            }

        binding.btnBack.setOnClickListener {
            checkDataForDialog()
        }

        binding.ivPlaylistCover.setOnClickListener {
            askPermissions()
        }

        binding.tvCreatePlaylist.setOnClickListener {
            createPlaylist()
        }

        setupTextChangeListener()

        onBackPress()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("imagePath", imagePath)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        imagePath = savedInstanceState?.getString("imagePath")
        if (imagePath != null) {
            Glide.with(this)
                .load(imagePath)
                .placeholder(R.drawable.add_photo)
                .transform(
                    CenterCrop(),
                    RoundedCorners(
                        requireContext().resources.getDimensionPixelSize(R.dimen.album_cover_corners_radius_large)
                    )
                )
                .into(binding.ivPlaylistCover)
        }
    }

    private fun getCheckedStorageConst(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private fun setupTextChangeListener() {
        binding.etPlaylistTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                binding.tvCreatePlaylist.isEnabled = s?.isNotEmpty() == true
            }
        })
    }

    private fun askPermissions() {
        lifecycleScope.launch {
            requester.request(getCheckedStorageConst()).collect { result ->
                when (result) {
                    is PermissionResult.Granted -> {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }

                    is PermissionResult.Denied.NeedsRationale -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.permission_rationale,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is PermissionResult.Denied.DeniedPermanently -> {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.data =
                            Uri.fromParts("package", requireContext().packageName, null)
                        requireContext().startActivity(intent)
                    }

                    is PermissionResult.Cancelled -> {
                        return@collect
                    }
                }
            }
        }
    }

    private fun createPlaylist() {
        val playlist = Playlist(
            id = 0,
            playlistTitle = binding.etPlaylistTitle.text.toString(),
            playlistDescription = binding.etPlaylistDescription.text.toString(),
            playlistCoverPath = imagePath,
            trackIds = null,
            numberOfTracks = 0
        )

        newPlaylistViewModel.createPlaylist(playlist)

        Toast.makeText(
            requireContext(),
            getString(R.string.playlist_created, playlist.playlistTitle),
            Toast.LENGTH_SHORT
        ).show()

        navigateBack()
    }

    private fun checkDataForDialog() {
        if (imagePath != null ||
            !binding.etPlaylistTitle.text.isNullOrEmpty() ||
            !binding.etPlaylistDescription.text.isNullOrEmpty()
        ) {
            confirmDialog?.show()
        } else {
            navigateBack()
        }
    }

    private fun onBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    checkDataForDialog()
                }
            })
    }

    private fun navigateBack() {
        if (parentPlayerActivity) {
            requireActivity().findViewById<ScrollView>(R.id.playerScrollView).isVisible = true
            parentFragmentManager.popBackStack()
            parentPlayerActivity = false
        } else {
            findNavController().popBackStack()
        }
    }

    companion object {
        private var parentPlayerActivity = false
        fun newInstance(flagParentPlayerActivity: Boolean): NewPlaylistFragment {
            parentPlayerActivity = flagParentPlayerActivity
            return NewPlaylistFragment()
        }
    }
}