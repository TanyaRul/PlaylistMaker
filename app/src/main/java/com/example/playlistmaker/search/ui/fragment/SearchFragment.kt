package com.example.playlistmaker.search.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.player.ui.activity.PlayerActivity
import com.example.playlistmaker.player.ui.model.PlayerTrack
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.ui.TrackAdapter
import com.example.playlistmaker.search.ui.TrackScreenState
import com.example.playlistmaker.search.ui.view_model.TracksSearchViewModel
import com.example.playlistmaker.util.BindingFragment
import com.example.playlistmaker.util.debounce
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : BindingFragment<FragmentSearchBinding>() {

    private val tracksSearchViewModel by viewModel<TracksSearchViewModel>()
    private var textWatcher: TextWatcher? = null

    private var searchText: String = ""
    private val trackList = ArrayList<Track>()
    private var searchHistoryList = ArrayList<Track>()

    private lateinit var onTrackClickDebounce: (Track) -> Unit

    //клик на трек, добаление его в историю и переход на экран плееера
    private val trackAdapter = TrackAdapter(trackList) {
        tracksSearchViewModel.addTrackToHistory(it)
        onTrackClickDebounce(it)
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //клик на трек с задержкой
        onTrackClickDebounce = debounce<Track>(
            CLICK_DEBOUNCE_DELAY_MS,
            viewLifecycleOwner.lifecycleScope,
            false
        ) { track ->
            val playerIntent = Intent(requireContext(), PlayerActivity::class.java)
            playerIntent.putExtra(PlayerActivity.TRACK_DATA_KEY, PlayerTrack.mappingTrack(track))
            startActivity(playerIntent)
        }

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
            hideKeyboard()

            if (searchHistoryList.isNotEmpty()) {
                showHistoryScreen()
            } else {
                hideHistoryScreen()
            }
        }

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

    //изменение видимости кнопки очистить строку поиска
    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    //убрать клавиатуру
    private fun hideKeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
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
        hideHistoryScreen()
        trackAdapter.trackList.clear()
        trackAdapter.trackList.addAll(trackList)
        trackAdapter.notifyDataSetChanged()
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY_MS = 1000L
    }
}