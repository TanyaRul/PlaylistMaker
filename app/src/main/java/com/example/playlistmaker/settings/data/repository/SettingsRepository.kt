package com.example.playlistmaker.settings.data.repository

import com.example.playlistmaker.settings.domain.model.ThemeSettings

interface SettingsRepository {
    fun getThemeSettings(): ThemeSettings
    fun setThemeSettings()
    fun updateThemeSettings(settings: ThemeSettings)
}