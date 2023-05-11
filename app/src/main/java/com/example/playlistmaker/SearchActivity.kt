package com.example.playlistmaker

import android.content.Context
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

    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var backButton: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeholderScreen: LinearLayout
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var refreshButton: Button
    private lateinit var trackAdapter: TrackAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        inputEditText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.clearIcon)
        backButton = findViewById(R.id.backToSettings)
        recyclerView = findViewById(R.id.recyclerViewTrackList)
        placeholderScreen = findViewById(R.id.placeholderScreen)
        placeholderImage = findViewById(R.id.placeholderImage)
        placeholderText = findViewById(R.id.placeholderText)
        refreshButton = findViewById(R.id.refreshButton)

        clearButton.setOnClickListener {
            inputEditText.setText("")
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
            trackList.clear()
            trackAdapter.notifyDataSetChanged()
            placeholderScreen.visibility = View.INVISIBLE
            recyclerView.visibility = View.VISIBLE
        }

        backButton.setOnClickListener {
            finish()
        }

        inputEditText.setText(text)

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                text = inputEditText.text.toString()
                clearButton.visibility = clearButtonVisibility(s)
            }

            override fun afterTextChanged(s: Editable?) {
                // empty
            }
        }

        inputEditText.addTextChangedListener(simpleTextWatcher)

        trackAdapter = TrackAdapter(trackList)

        recyclerView.adapter = trackAdapter

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchTrack()
                true
            }
            false
        }

        refreshButton.setOnClickListener {
            searchTrack()
        }

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
        if (inputEditText.text.isNotEmpty()) {
            itunesService.search(inputEditText.text.toString())
                .enqueue(object : Callback<TracksResponse> {
                    override fun onResponse(
                        call: Call<TracksResponse>,
                        response: Response<TracksResponse>
                    ) {
                        if (response.code() == 200) {
                            trackList.clear()
                            if (response.body()?.results?.isNotEmpty() == true) {
                                trackList.addAll(response.body()?.results!!)
                                trackAdapter.notifyDataSetChanged()
                                recyclerView.visibility = View.VISIBLE
                                placeholderScreen.visibility = View.GONE
                            } else {
                                showMessage(getString(R.string.nothing_found), R.drawable.not_found)
                                refreshButton.visibility = View.GONE
                            }
                        } else {
                            showMessage(getString(R.string.no_connection), R.drawable.no_connection)
                            refreshButton.visibility = View.VISIBLE
                        }
                    }

                    override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                        showMessage(getString(R.string.no_connection), R.drawable.no_connection)
                        refreshButton.visibility = View.VISIBLE
                    }
                })
        }
    }

    fun showMessage(text: String, image: Int) {
        if (text.isNotEmpty()) {
            recyclerView.visibility = View.GONE
            placeholderScreen.visibility = View.VISIBLE
            trackList.clear()
            trackAdapter.notifyDataSetChanged()
            placeholderText.text = text
            placeholderImage.setImageResource(image)
        } else {
            placeholderScreen.visibility = View.GONE
        }
    }

}