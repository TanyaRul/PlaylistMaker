package com.example.playlistmaker.search.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.player.ui.activity.PlayerActivity
import com.example.playlistmaker.player.ui.model.PlayerTrack
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.ui.TrackAdapter
import com.example.playlistmaker.search.ui.TrackScreenState
import com.example.playlistmaker.search.ui.view_model.TracksSearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private val tracksSearchViewModel by viewModel<TracksSearchViewModel>()
    private val handler = Handler(Looper.getMainLooper())
    private var isClickAllowed = true
    private var textWatcher: TextWatcher? = null

    private var searchText: String = ""
    private val trackList = ArrayList<Track>()
    private var searchHistoryList = ArrayList<Track>()

    //клик на трек, добаление его в историю и переход на экран плееера
    private val trackAdapter = TrackAdapter(trackList) {
        tracksSearchViewModel.addTrackToHistory(it)
        if (clickDebounce()) {
            val playerIntent = Intent(requireContext(), PlayerActivity::class.java)
            playerIntent.putExtra(PlayerActivity.TRACK_DATA_KEY, PlayerTrack.mappingTrack(it))
            startActivity(playerIntent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //складываем в recyclerView значения из адаптера
        binding.recyclerViewTrackList.adapter = trackAdapter

        //подписка на изменение состояния экрана поиска
        tracksSearchViewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }

        //извлечение истории поиска из sharedPrefs
        tracksSearchViewModel.fillHistory()

        //подписка на изменение истории поиска
        tracksSearchViewModel.historyLiveData.observe(viewLifecycleOwner) { historyTrackList ->
            searchHistoryList = historyTrackList
        }

        //клик в поле ввода поискового запроса
        binding.inputEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && binding.inputEditText.text.isEmpty() && searchHistoryList.isNotEmpty()) {
                showHistoryScreen()
            } else {
                hideHistoryScreen()
            }
        }

        //изменение текста в поле ввода поискового запроса
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
                    trackList.clear()
                    trackAdapter.trackList = trackList
                    hideHistoryScreen()
                }

                tracksSearchViewModel.fillHistory()

                if (s?.isEmpty() == true && searchHistoryList.isNotEmpty()) {
                    showHistoryScreen()
                } else {
                    binding.recyclerViewTrackList.visibility = View.GONE
                    binding.placeholderScreen.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        textWatcher?.let { binding.inputEditText.addTextChangedListener(it) }

        //клик на кнопку очистить поле ввода поискового запроса
        binding.clearSearchText.setOnClickListener {
            binding.inputEditText.setText("")
            val inputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)

            if (searchHistoryList.isNotEmpty()) {
                showHistoryScreen()
            } else {
                hideHistoryScreen()
            }
        }

        /*//клик на кнопку назад
        binding.backToSettings.setOnClickListener {
            finish()
        }*/

        //клик на кнопку обновить поиск
        binding.refreshButton.setOnClickListener {
            tracksSearchViewModel.searchTrack(searchText)
        }

        //клик на кнопку очистить историю поиска
        binding.clearHistoryButton.setOnClickListener {
            hideHistoryScreen()
            binding.recyclerViewTrackList.visibility = View.VISIBLE
            trackAdapter.notifyDataSetChanged()
            tracksSearchViewModel.clearHistory()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        textWatcher?.let { binding.inputEditText.removeTextChangedListener(it) }
    }

    //изменение видимости кнопки очистить строку поиска
    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    //клик с задержкой
    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY_MS)
        }
        return current
    }

    //показываем нужные элементы в зависимости от состояния экрана поиска
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

    //показать экран истории поиска
    private fun showHistoryScreen() {
        binding.searchHistoryTextView.visibility = View.VISIBLE
        binding.clearHistoryButton.visibility = View.VISIBLE
        binding.recyclerViewTrackList.visibility = View.VISIBLE
        binding.placeholderScreen.visibility = View.GONE
        trackAdapter.trackList = searchHistoryList
        trackAdapter.notifyDataSetChanged()
    }

    //скрыть экран истории поиска
    private fun hideHistoryScreen() {
        binding.searchHistoryTextView.visibility = View.GONE
        binding.clearHistoryButton.visibility = View.GONE
    }

    //показать загрузку
    private fun showLoading() {
        binding.recyclerViewTrackList.visibility = View.GONE
        binding.placeholderScreen.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    //показать ошибку
    private fun showError(errorMessage: String) {
        binding.recyclerViewTrackList.visibility = View.GONE
        binding.placeholderScreen.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.placeholderText.text = errorMessage
        trackList.clear()
        trackAdapter.notifyDataSetChanged()
        hideHistoryScreen()
    }

    //показать пустой результат поиска
    private fun showEmpty(emptyMessage: String) {
        showError(emptyMessage)
    }

    //показать найденные результаты
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

    companion object {
        private const val CLICK_DEBOUNCE_DELAY_MS = 1000L
    }
}