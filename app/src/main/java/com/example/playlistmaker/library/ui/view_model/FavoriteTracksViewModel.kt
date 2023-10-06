package com.example.playlistmaker.library.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.library.domain.db.FavoritesInteractor
import com.example.playlistmaker.library.ui.FavoritesScreenState
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.launch

class FavoriteTracksViewModel(
    private val favoritesInteractor: FavoritesInteractor,
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
            renderState(FavoritesScreenState.Empty)
        } else {
            renderState(FavoritesScreenState.Content(tracks))
        }
    }

    private fun renderState(state: FavoritesScreenState) {
        _favoriteStateLiveData.postValue(state)
    }

}