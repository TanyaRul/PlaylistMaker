package com.example.playlistmaker.library.ui.view_model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.library.ui.FavoritesScreenState
import com.example.playlistmaker.library.domain.db.FavoritesInteractor
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.launch

class FavoriteTracksViewModel(
    private val favoritesInteractor: FavoritesInteractor,
    private val application: Application,
) : ViewModel() {

    private val _favoriteStateLiveData = MutableLiveData<FavoritesScreenState>()
    fun observeState(): LiveData<FavoritesScreenState> = _favoriteStateLiveData

    fun fillData() {
        viewModelScope.launch {
            favoritesInteractor
                .getFavoriteTracks()
                .collect { tracks ->
                    processResult(tracks)
                }
        }
    }

    private fun processResult(tracks: List<Track>) {
        if (tracks.isEmpty()) {
            renderState(FavoritesScreenState.Empty(application.getString(R.string.empty_library)))
        } else {
            renderState(FavoritesScreenState.Content(tracks))
        }
    }

    private fun renderState(state: FavoritesScreenState) {
        _favoriteStateLiveData.postValue(state)
    }

}