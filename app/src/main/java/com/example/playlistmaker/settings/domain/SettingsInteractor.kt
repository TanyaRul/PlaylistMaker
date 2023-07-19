package com.example.playlistmaker.settings.domain

import com.example.playlistmaker.settings.domain.model.ThemeSettings

interface SettingsInteractor {
    fun getThemeSettings(): ThemeSettings
    fun setThemeSettings()
    fun updateThemeSettings(settings: ThemeSettings)
}