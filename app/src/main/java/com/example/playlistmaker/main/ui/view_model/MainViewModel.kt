package com.example.playlistmaker.main.ui.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.playlistmaker.creator.Creator

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsInteractor = Creator.provideSettingsInteractor(application)

    var itFirstTime = true

    fun setTheme() {
        settingsInteractor.setThemeSettings()
    }

    companion object {
        fun getViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
            }
        }
    }
}