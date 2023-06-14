package com.example.playlistmaker.presentation.settings

import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.playlistmaker.App
import com.example.playlistmaker.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton = findViewById<ImageView>(R.id.backToMainActivity)
        val shareApp = findViewById<TextView>(R.id.share)
        val support = findViewById<TextView>(R.id.support)
        val termsOfUse = findViewById<TextView>(R.id.terms_of_use)
        val themeSwitcher = findViewById<SwitchCompat>(R.id.themeSwitcher)

        backButton.setOnClickListener {
            finish()
        }

        shareApp.setOnClickListener {
            val urlCourse = getString(R.string.url_course)
            val shareIntent = Intent(ACTION_SEND).apply {
                type = "text/plain"
                putExtra(EXTRA_TEXT, urlCourse)
            }
            startActivity(shareIntent)
        }

        support.setOnClickListener {
            val email = getString(R.string.email_address)
            val subject = getString(R.string.email_subject)
            val message = getString(R.string.email_message)
            val supportIntent = Intent(ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(EXTRA_EMAIL, arrayOf(email))
                putExtra(EXTRA_SUBJECT, subject)
                putExtra(EXTRA_TEXT, message)
            }
            startActivity(supportIntent)
        }

        termsOfUse.setOnClickListener {
            val urlOffer = getString(R.string.url_offer)
            val termsOfUseIntent = Intent(ACTION_VIEW).apply {
                data = Uri.parse(urlOffer)
            }
            startActivity(termsOfUseIntent)
        }

        themeSwitcher.isChecked = (applicationContext as App).darkTheme

        themeSwitcher.setOnCheckedChangeListener { switcher, checked ->
            (applicationContext as App).switchTheme(checked)
        }
    }
}