package com.example.playlistmaker.player.ui.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.player.domain.model.PlayerState

class PlayerViewModel(application: Application): AndroidViewModel(application) {

    private val playerInteractor = Creator.providePlayerInteractor(application)


    private var playerStateLiveData = MutableLiveData<PlayerState>()
    fun observeState(): LiveData<PlayerState> = playerStateLiveData


    fun prepare(url: String) {
        playerInteractor.preparePlayer(url)
    }

    fun play(){
        playerInteractor.startPlayer()
    }

    fun pause() {
        playerInteractor.pausePlayer()
    }

    fun release() {
        playerInteractor.release()
    }

    override fun onCleared() {
        super.onCleared()
        release()
        //handler.removeCallbacks(playbackProgressRunnable)
    }

    companion object {
        fun getViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlayerViewModel(this[APPLICATION_KEY] as Application)
            }
        }

    }
}