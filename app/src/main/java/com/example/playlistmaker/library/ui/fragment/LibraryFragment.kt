package com.example.playlistmaker.library.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentLibraryBinding
import com.example.playlistmaker.library.ui.LibraryViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

const val FRAGMENT_1 = 0
const val FRAGMENT_2 = 1

class LibraryFragment : BindingFragment<FragmentLibraryBinding>() {

    private lateinit var tabMediator: TabLayoutMediator

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLibraryBinding {
        return FragmentLibraryBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.adapter = LibraryViewPagerAdapter(
            childFragmentManager,
            lifecycle
        )

        tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                FRAGMENT_1 -> tab.text = getString(R.string.favorite_tracks)
                FRAGMENT_2 -> tab.text = getString(R.string.playlists)
            }
        }
        tabMediator.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabMediator.detach()
    }
}