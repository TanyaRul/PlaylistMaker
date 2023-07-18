package com.example.playlistmaker.settings.data.impl

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.settings.data.repository.SettingsRepository
import com.example.playlistmaker.settings.domain.model.ThemeSettings

class SettingsRepositoryImpl(val context: Context) : SettingsRepository {

    val sharedPrefs = context.getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE)
    val darkTheme = sharedPrefs.getBoolean(SWITCH_KEY, false)

    override fun getThemeSettings(): ThemeSettings {
        return ThemeSettings(darkTheme)
    }

    override fun setThemeSettings() {
        AppCompatDelegate.setDefaultNightMode(
            if (darkTheme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    override fun updateThemeSettings(settings: ThemeSettings) {
        AppCompatDelegate.setDefaultNightMode(
            if (settings.darkTheme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        sharedPrefs.edit()
            .putBoolean(SWITCH_KEY, settings.darkTheme)
            .apply()
    }

    companion object {
        const val SETTINGS_PREFS = "settings_prefs"
        const val SWITCH_KEY = "key_for_switch"
    }

}