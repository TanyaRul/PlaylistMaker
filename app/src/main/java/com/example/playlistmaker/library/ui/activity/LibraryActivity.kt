package com.example.playlistmaker.library.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityLibraryBinding
import com.example.playlistmaker.library.ui.LibraryViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

const val FRAGMENT_1 = 0
const val FRAGMENT_2 = 1

class LibraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLibraryBinding

    private lateinit var tabMediator: TabLayoutMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = LibraryViewPagerAdapter(
            supportFragmentManager,
            lifecycle
        )

        binding.backToMainButton.setOnClickListener {
            finish()
        }

        tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                FRAGMENT_1 -> tab.text = getString(R.string.favorite_tracks)
                FRAGMENT_2 -> tab.text = getString(R.string.playlists)
            }
        }
        tabMediator.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        tabMediator.detach()
    }
}