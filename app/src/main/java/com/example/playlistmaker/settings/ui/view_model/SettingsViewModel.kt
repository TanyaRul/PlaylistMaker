package com.example.playlistmaker.settings.ui.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.settings.domain.model.ThemeSettings

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharingInteractor = Creator.provideSharingInteractor(getApplication())
    private val settingsInteractor = Creator.provideSettingsInteractor(getApplication())

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

    companion object {
        fun getViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(this[APPLICATION_KEY] as Application)
            }
        }
    }
}