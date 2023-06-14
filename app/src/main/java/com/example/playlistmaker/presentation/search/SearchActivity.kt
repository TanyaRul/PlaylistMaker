package com.example.playlistmaker.presentation.search

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.App
import com.example.playlistmaker.domain.models.NetworkStatus
import com.example.playlistmaker.presentation.player.PlayerActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.data.SearchHistory
import com.example.playlistmaker.data.dto.TracksSearchResponse
import com.example.playlistmaker.data.network.ItunesApi
import com.example.playlistmaker.domain.models.Track
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    companion object {
        const val SEARCH_TEXT = "SEARCH_TEXT"
        private const val CLICK_DEBOUNCE_DELAY_MS = 1000L
        private const val SEARCH_DEBOUNCE_DELAY_MS = 2000L
    }

    private var text: String = ""

    private val itunesBaseUrl = "http://itunes.apple.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(itunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val itunesService = retrofit.create(ItunesApi::class.java)

    private val trackList = ArrayList<Track>()
    private var searchHistoryList = ArrayList<Track>()

    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var backButton: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeholderScreen: LinearLayout
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var refreshButton: Button
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var searchHistoryTextView: TextView
    private lateinit var clearHistoryButton: Button
    private lateinit var searchHistory: SearchHistory
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var progressBar: ProgressBar

    private var isClickAllowed = true

    private val handler = Handler(Looper.getMainLooper())

    private val searchRunnable = Runnable { searchTrack() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initView()

        sharedPrefs = getSharedPreferences(App.PLM_PREFERENCES, MODE_PRIVATE)
        inputEditText.setText(text)
        searchHistory = SearchHistory(sharedPrefs)
        trackAdapter = TrackAdapter(trackList) {
            searchHistory.addTrackToHistory(it)
            if (clickDebounce()) {
                val playerIntent = Intent(this, PlayerActivity::class.java)
                playerIntent.putExtra(PlayerActivity.TRACK_DATA_KEY, it)
                startActivity(playerIntent)
            }
        }
        recyclerView.adapter = trackAdapter
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
            SearchHistory.clearSearchHistory()
            searchHistoryList.clear()
            hideHistoryScreen()
            recyclerView.visibility = View.VISIBLE
            trackAdapter.notifyDataSetChanged()
        }

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

    override fun onSaveInstanceState(outState: Bundle) {
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

    private fun searchTrack() {
        if (text.isNotEmpty()) {

            placeholderScreen.visibility = View.GONE
            recyclerView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            itunesService.search(text)
                .enqueue(object : Callback<TracksSearchResponse> {
                    override fun onResponse(
                        call: Call<TracksSearchResponse>,
                        response: Response<TracksSearchResponse>
                    ) {
                        progressBar.visibility = View.GONE
                        if (response.code() == 200) {
                            trackList.clear()
                            if (response.body()?.results?.isNotEmpty() == true) {
                                trackList.addAll(response.body()?.results!!)
                                showMessage(NetworkStatus.SUCCESS)
                            } else {
                                showMessage(NetworkStatus.EMPTY)
                            }
                        } else {
                            showMessage(NetworkStatus.ERROR)
                        }
                    }

                    override fun onFailure(call: Call<TracksSearchResponse>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        showMessage(NetworkStatus.ERROR)
                    }
                })
        }
    }

    private fun showMessage(state: NetworkStatus) {
        when (state) {
            NetworkStatus.SUCCESS -> {
                trackAdapter.notifyDataSetChanged()
                recyclerView.visibility = View.VISIBLE
                placeholderScreen.visibility = View.GONE
                hideHistoryScreen()
            }

            NetworkStatus.EMPTY -> {
                recyclerView.visibility = View.GONE
                placeholderScreen.visibility = View.VISIBLE
                trackList.clear()
                trackAdapter.notifyDataSetChanged()
                placeholderText.setText(R.string.nothing_found)
                placeholderImage.setImageResource(R.drawable.not_found)
                refreshButton.visibility = View.GONE
                hideHistoryScreen()
            }

            NetworkStatus.ERROR -> {
                recyclerView.visibility = View.GONE
                placeholderScreen.visibility = View.VISIBLE
                trackList.clear()
                trackAdapter.notifyDataSetChanged()
                placeholderText.setText(R.string.no_connection)
                placeholderImage.setImageResource(R.drawable.no_connection)
                refreshButton.visibility = View.VISIBLE
                hideHistoryScreen()
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

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY_MS)
        }
        return current
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY_MS)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(searchRunnable)
    }

}