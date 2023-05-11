package com.example.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {
    companion object {
        const val PLM_PREFERENCES = "plm_preferences"
        const val SWITCH_KEY = "key_for_switch"
        lateinit var sharedPrefs: SharedPreferences
    }

    var darkTheme = false

    override fun onCreate() {
        super.onCreate()

        sharedPrefs = getSharedPreferences(PLM_PREFERENCES, MODE_PRIVATE)
        darkTheme = sharedPrefs.getBoolean(SWITCH_KEY, false)
        switchTheme(darkTheme)

    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        sharedPrefs.edit()
            .putBoolean(SWITCH_KEY, darkTheme)
            .apply()
    }

}