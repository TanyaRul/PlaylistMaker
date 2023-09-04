package com.example.playlistmaker.main.ui.view_model

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.SettingsInteractor

class MainViewModel(private val settingsInteractor: SettingsInteractor) : ViewModel() {

    var itFirstTime = true

    fun setTheme() {
        if (itFirstTime) {
            settingsInteractor.setThemeSettings()
            itFirstTime = false
        }
    }

}