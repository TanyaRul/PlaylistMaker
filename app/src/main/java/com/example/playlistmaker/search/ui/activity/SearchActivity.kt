package com.example.playlistmaker.search.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.R
import com.example.playlistmaker.player.ui.activity.PlayerActivity
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.player.ui.model.PlayerTrack
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.ui.TrackAdapter
import com.example.playlistmaker.search.ui.TrackScreenState
import com.example.playlistmaker.search.ui.view_model.TracksSearchViewModel

class SearchActivity : ComponentActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var tracksSearchViewModel: TracksSearchViewModel
    private val handler = Handler(Looper.getMainLooper())
    private val trackList = ArrayList<Track>()
    private var isClickAllowed = true
    private var textWatcher: TextWatcher? = null
    private var searchText: String = ""
    private var searchHistoryList = ArrayList<Track>()

    private val trackAdapter = TrackAdapter(trackList) {
        //searchHistory.addTrackToHistory(it)
        tracksSearchViewModel.addTrackToHistory(it)
        if (clickDebounce()) {
            val playerIntent = Intent(this, PlayerActivity::class.java)
            playerIntent.putExtra(PlayerActivity.TRACK_DATA_KEY, PlayerTrack.mappingTrack(it))
            startActivity(playerIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)


        tracksSearchViewModel = ViewModelProvider(
            this,
            TracksSearchViewModel.getViewModelFactory()
        )[TracksSearchViewModel::class.java]


        Log.d("TEST", "activity created")

        binding.recyclerViewTrackList.adapter = trackAdapter


        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = binding.inputEditText.text.toString()
                binding.clearSearchText.visibility = clearButtonVisibility(s)
                tracksSearchViewModel.searchDebounce(
                    changedText = s?.toString() ?: ""
                )
                if (!s.isNullOrEmpty()) {
                    binding.recyclerViewTrackList.visibility = View.VISIBLE
                    //trackList.clear()
                    //trackAdapter.trackList = trackList

                    hideHistoryScreen()
                }
                tracksSearchViewModel.fillHistory()
                if (s?.isEmpty() == true && searchHistoryList.isNotEmpty()) {
                    showHistoryScreen()
                    binding.recyclerViewTrackList.visibility = View.VISIBLE
                    binding.placeholderScreen.visibility = View.GONE
                } else {
                    binding.recyclerViewTrackList.visibility = View.GONE
                    binding.placeholderScreen.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        textWatcher?.let { binding.inputEditText.addTextChangedListener(it) }

        tracksSearchViewModel.observeState().observe(this) {
            render(it)
        }

        binding.inputEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && binding.inputEditText.text.isEmpty() && searchHistoryList.isNotEmpty()) {
                showHistoryScreen()
            } else {
                hideHistoryScreen()
            }
            trackAdapter.trackList = searchHistoryList
            trackAdapter.notifyDataSetChanged()
        }

        binding.clearSearchText.setOnClickListener {
            tracksSearchViewModel.clearSearchText()
            binding.inputEditText.setText("")
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
            trackList.clear()
            binding.placeholderScreen.visibility = View.INVISIBLE
            trackAdapter.notifyDataSetChanged()
            tracksSearchViewModel.fillHistory()
            if (searchHistoryList.isNotEmpty()) {
                binding.recyclerViewTrackList.visibility = View.VISIBLE
                showHistoryScreen()
            } else {
                hideHistoryScreen()
            }

        }

        binding.backToSettings.setOnClickListener {
            finish()
        }

        binding.refreshButton.setOnClickListener {
            tracksSearchViewModel.searchTrack(searchText)
        }

        binding.clearHistoryButton.setOnClickListener {
            hideHistoryScreen()
            binding.recyclerViewTrackList.visibility = View.VISIBLE
            trackAdapter.notifyDataSetChanged()
            tracksSearchViewModel.clearHistory()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        textWatcher?.let { binding.inputEditText.removeTextChangedListener(it) }
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY_MS)
        }
        return current
    }

    private fun render(state: TrackScreenState) {
        when (state) {
            is TrackScreenState.Loading -> showLoading()
            is TrackScreenState.Content -> showContent(state.trackList)
            is TrackScreenState.Error -> {
                showError(state.errorMessage)
                binding.placeholderText.setText(R.string.no_connection)
                binding.placeholderImage.setImageResource(R.drawable.no_connection)
                binding.refreshButton.visibility = View.VISIBLE
            }

            is TrackScreenState.Empty -> {
                showEmpty(state.message)
                binding.placeholderText.setText(R.string.nothing_found)
                binding.placeholderImage.setImageResource(R.drawable.not_found)
                binding.refreshButton.visibility = View.GONE
            }
        }
    }

    private fun showHistoryScreen() {
        binding.searchHistoryTextView.visibility = View.VISIBLE
        binding.clearHistoryButton.visibility = View.VISIBLE
        trackAdapter.trackList = searchHistoryList
        trackAdapter.notifyDataSetChanged()
    }

    private fun hideHistoryScreen() {
        binding.searchHistoryTextView.visibility = View.GONE
        binding.clearHistoryButton.visibility = View.GONE
    }

    private fun showLoading() {
        binding.recyclerViewTrackList.visibility = View.GONE
        binding.placeholderScreen.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun showError(errorMessage: String) {
        binding.recyclerViewTrackList.visibility = View.GONE
        binding.placeholderScreen.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.placeholderText.text = errorMessage
        trackList.clear()
        trackAdapter.notifyDataSetChanged()
        hideHistoryScreen()
    }

    private fun showEmpty(emptyMessage: String) {
        showError(emptyMessage)
    }

    private fun showContent(trackList: List<Track>) {
        binding.recyclerViewTrackList.visibility = View.VISIBLE
        binding.placeholderScreen.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.searchHistoryTextView.visibility = View.GONE
        binding.clearHistoryButton.visibility = View.GONE

        trackAdapter.trackList.clear()
        trackAdapter.trackList.addAll(trackList)
        trackAdapter.notifyDataSetChanged()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(SEARCH_TEXT).toString()
    }


    companion object {
        const val SEARCH_TEXT = "SEARCH_TEXT"
        private const val CLICK_DEBOUNCE_DELAY_MS = 1000L
    }

}