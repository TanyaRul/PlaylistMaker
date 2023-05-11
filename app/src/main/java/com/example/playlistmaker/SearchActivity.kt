package com.example.playlistmaker

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    companion object {
        const val SEARCH_TEXT = "SEARCH_TEXT"
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        findViewById()

        sharedPrefs = getSharedPreferences(App.PLM_PREFERENCES, MODE_PRIVATE)
        inputEditText.setText(text)

        searchHistory = SearchHistory(sharedPrefs)
        trackAdapter = TrackAdapter(trackList, searchHistory)
        recyclerView.adapter = trackAdapter
        trackAdapter.trackList = trackList

        searchHistoryList.clear()
        searchHistoryList.addAll(searchHistory.readSearchHistory())

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                text = inputEditText.text.toString()
                clearButton.visibility = clearButtonVisibility(s)

                if (inputEditText.hasFocus() && s?.isEmpty() == true && searchHistoryList.isNotEmpty()) {
                    showHistoryScreen()
                }
                else {
                    trackAdapter.trackList = trackList
                    hideHistoryScreen()
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
            recyclerView.visibility = View.VISIBLE
            trackAdapter.notifyDataSetChanged()
            searchHistoryList.clear()
            searchHistoryList.addAll(searchHistory.readSearchHistory())
            if (searchHistoryList.size > 0) {
                showHistory()
            }
            else {
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
            sharedPrefs
                .edit()
                .remove(SearchHistory.SEARCH_HISTORY_KEY)
                .apply()
            searchHistoryList.clear()
            hideHistoryScreen()
            recyclerView.visibility = View.VISIBLE
            trackAdapter.notifyDataSetChanged()
        }

    }

    private fun findViewById() {
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
            itunesService.search(text)
                .enqueue(object : Callback<TracksResponse> {
                    override fun onResponse(
                        call: Call<TracksResponse>,
                        response: Response<TracksResponse>
                    ) {
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

                    override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
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
    }

    private fun hideHistoryScreen() {
        searchHistoryTextView.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
    }

    private fun showHistory() {
        showHistoryScreen()
        searchHistoryList.clear()
        searchHistoryList.addAll(searchHistory.readSearchHistory())
        trackAdapter.trackList = searchHistoryList
        trackAdapter.notifyDataSetChanged()
    }

}