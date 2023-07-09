package com.example.playlistmaker.search.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.player.ui.activity.PlayerActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.search.data.impl.SearchRepository
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.ui.TrackAdapter
import com.example.playlistmaker.search.ui.TrackScreenState
import com.example.playlistmaker.search.ui.view_model.TracksSearchViewModel

class SearchActivity : ComponentActivity() {

    companion object {
        //const val SEARCH_TEXT = "SEARCH_TEXT"
        private const val CLICK_DEBOUNCE_DELAY_MS = 1000L
    }

    /*private val trackAdapter = TrackAdapter (
        object : TrackAdapter.MovieClickListener {
            override fun onTrackClick(track: Track) {
                if (clickDebounce()) {
                    val playerIntent = Intent(this@SearchActivity, PlayerActivity::class.java)
                    playerIntent.putExtra(PlayerActivity.TRACK_DATA_KEY, it)
                    startActivity(playerIntent)
                }
            }

            override fun onFavoriteToggleClick(track: Track) {
                viewModel.toggleFavorite(track)
            }
        }
    )*/

    private val trackList = ArrayList<Track>()

    private val trackAdapter = TrackAdapter(trackList) {
        searchHistory.addTrackToHistory(it)
        if (clickDebounce()) {
            val playerIntent = Intent(this, PlayerActivity::class.java)
            playerIntent.putExtra(PlayerActivity.TRACK_DATA_KEY, it)
            startActivity(playerIntent)
        }
    }

    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var backButton: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeholderScreen: LinearLayout
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var refreshButton: Button
    //private lateinit var trackAdapter: TrackAdapter
    private lateinit var searchHistoryTextView: TextView
    private lateinit var clearHistoryButton: Button
    private lateinit var searchHistory: SearchRepository
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var progressBar: ProgressBar

    private var isClickAllowed = true

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var viewModel: TracksSearchViewModel

    private var textWatcher: TextWatcher? = null



    //private var text: String = ""

    //private var searchHistoryList = ArrayList<Track>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initView()

        viewModel = ViewModelProvider(this, TracksSearchViewModel.getViewModelFactory())[TracksSearchViewModel::class.java]
        Log.d("TEST", "activity created")

        recyclerView.adapter = trackAdapter

        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchDebounce(
                    changedText = s?.toString() ?: ""
                )
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        textWatcher?.let { inputEditText.addTextChangedListener(it) }

        viewModel.observeState().observe(this) {
            render(it)
        }

        viewModel.observeShowToast().observe(this) {
            showToast(it)
        }



        /*sharedPrefs = getSharedPreferences(SearchRepository.SHARED_PREFS, MODE_PRIVATE)
        inputEditText.setText(text)
        searchHistory = SearchRepository(applicationContext)
        trackAdapter = TrackAdapter(trackList) {
            searchHistory.addTrackToHistory(it)
            if (clickDebounce()) {
                val playerIntent = Intent(this, PlayerActivity::class.java)
                playerIntent.putExtra(PlayerActivity.TRACK_DATA_KEY, it)
                startActivity(playerIntent)
            }
        }

        trackAdapter.trackList = trackList

        fillHistory()

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                text = inputEditText.text.toString()
                clearButton.visibility = clearButtonVisibility(s)
                searchDebounce()

                if (!s.isNullOrEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                    trackList.clear()
                    trackAdapter.trackList = trackList
                    hideHistoryScreen()
                }
                fillHistory()
                if (s?.isEmpty() == true && searchHistoryList.isNotEmpty()) {
                    showHistoryScreen()
                    recyclerView.visibility = View.VISIBLE
                    placeholderScreen.visibility = View.GONE
                } else {
                    recyclerView.visibility = View.GONE
                    placeholderScreen.visibility = View.GONE
                }

            }

            override fun afterTextChanged(s: Editable?) {
                // empty
            }
        }

        inputEditText.addTextChangedListener(simpleTextWatcher)

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchTrack()
                true
            }
            false
        }

        inputEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && inputEditText.text.isEmpty() && searchHistoryList.isNotEmpty()) {
                showHistoryScreen()
            } else {
                hideHistoryScreen()
            }
            trackAdapter.trackList = searchHistoryList
            trackAdapter.notifyDataSetChanged()
        }

        clearButton.setOnClickListener {
            inputEditText.setText("")
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
            trackList.clear()
            placeholderScreen.visibility = View.INVISIBLE
            trackAdapter.notifyDataSetChanged()
            fillHistory()
            if (searchHistoryList.isNotEmpty()) {
                recyclerView.visibility = View.VISIBLE
                showHistoryScreen()
            } else {
                hideHistoryScreen()
            }

        }

        backButton.setOnClickListener {
            finish()
        }

        refreshButton.setOnClickListener {
            searchTrack()
        }

        clearHistoryButton.setOnClickListener {
            clearSearchHistory()
            searchHistoryList.clear()
            hideHistoryScreen()
            recyclerView.visibility = View.VISIBLE
            trackAdapter.notifyDataSetChanged()
        }*/

    }

    override fun onDestroy() {
        super.onDestroy()
        textWatcher?.let { inputEditText.removeTextChangedListener(it) }
    }

    private fun initView() {
        inputEditText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.clearIcon)
        backButton = findViewById(R.id.backToSettings)
        recyclerView = findViewById(R.id.recyclerViewTrackList)
        placeholderScreen = findViewById(R.id.placeholderScreen)
        placeholderImage = findViewById(R.id.placeholderImage)
        placeholderText = findViewById(R.id.placeholderText)
        refreshButton = findViewById(R.id.refreshButton)
        searchHistoryTextView = findViewById(R.id.searchHistoryTextView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun showToast(additionalMessage: String) {
        Toast.makeText(this, additionalMessage, Toast.LENGTH_LONG).show()
    }

    private fun render(state: TrackScreenState) {
        when (state) {
            is TrackScreenState.Loading -> showLoading()
            is TrackScreenState.Content -> showContent(state.trackList)
            is TrackScreenState.Error -> showError(state.errorMessage)
            is TrackScreenState.Empty -> showEmpty(state.message)
        }
    }

    private fun showLoading() {
        recyclerView.visibility = View.GONE
        placeholderScreen.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun showError(errorMessage: String) {
        recyclerView.visibility = View.GONE
        placeholderScreen.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        placeholderText.text = errorMessage
    }

    private fun showEmpty(emptyMessage: String) {
        showError(emptyMessage)
    }

    private fun showContent(trackList: List<Track>) {
        recyclerView.visibility = View.VISIBLE
        placeholderScreen.visibility = View.GONE
        progressBar.visibility = View.GONE

        trackAdapter.trackList.clear()
        trackAdapter.trackList.addAll(trackList)
        trackAdapter.notifyDataSetChanged()
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY_MS)
        }
        return current
    }




    /*override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT, text)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        text = savedInstanceState.getString(SEARCH_TEXT).toString()
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun clearSearchHistory() {
        sharedPrefs
            .edit()
            .remove(SearchRepository.SEARCH_HISTORY_KEY)
            .apply()
    }


    }

    private fun showMessage(state: NetworkStatus) {
        when (state) {
            NetworkStatus.SUCCESS -> {
                trackAdapter.notifyDataSetChanged()
                recyclerView.visibility = View.VISIBLE
                placeholderScreen.visibility = View.GONE
                searchHistoryTextView.visibility = View.GONE
                clearHistoryButton.visibility = View.GONE
            }

            NetworkStatus.EMPTY -> {
                recyclerView.visibility = View.GONE
                placeholderScreen.visibility = View.VISIBLE
                trackList.clear()
                trackAdapter.notifyDataSetChanged()
                placeholderText.setText(R.string.nothing_found)
                placeholderImage.setImageResource(R.drawable.not_found)
                refreshButton.visibility = View.GONE
                searchHistoryTextView.visibility = View.GONE
                clearHistoryButton.visibility = View.GONE
            }

            NetworkStatus.ERROR -> {
                recyclerView.visibility = View.GONE
                placeholderScreen.visibility = View.VISIBLE
                trackList.clear()
                trackAdapter.notifyDataSetChanged()
                placeholderText.setText(R.string.no_connection)
                placeholderImage.setImageResource(R.drawable.no_connection)
                refreshButton.visibility = View.VISIBLE
                searchHistoryTextView.visibility = View.GONE
                clearHistoryButton.visibility = View.GONE
            }
        }
    }

    private fun showHistoryScreen() {
        searchHistoryTextView.visibility = View.VISIBLE
        clearHistoryButton.visibility = View.VISIBLE
        trackAdapter.trackList = searchHistoryList
        trackAdapter.notifyDataSetChanged()
    }

    private fun hideHistoryScreen() {
        searchHistoryTextView.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
    }

    private fun fillHistory() {
        searchHistoryList.clear()
        searchHistoryList.addAll(searchHistory.readSearchHistory())
    }


*/



}