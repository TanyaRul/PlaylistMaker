package com.example.playlistmaker.settings.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.SettingsInteractor
import com.example.playlistmaker.settings.domain.model.ThemeSettings
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor

class SettingsViewModel(
    private val sharingInteractor: SharingInteractor,
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val _themeSettingsLiveData = MutableLiveData<ThemeSettings>()
    val themeSettingsLiveData: LiveData<ThemeSettings> = _themeSettingsLiveData

    init {
        _themeSettingsLiveData.postValue(settingsInteractor.getThemeSettings())
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        val settings = ThemeSettings(darkThemeEnabled)
        _themeSettingsLiveData.postValue(settings)
        settingsInteractor.updateThemeSettings(settings)
    }

    fun shareApp(urlCourse: String) {
        sharingInteractor.shareApp(urlCourse)
    }

    fun openTerms(urlOffer: String) {
        sharingInteractor.openTerms(urlOffer)
    }

    fun openSupport(email: String, subject: String, message: String) {
        sharingInteractor.openSupport(email, subject, message)
    }

}