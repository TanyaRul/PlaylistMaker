package com.example.playlistmaker

import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton = findViewById<ImageView>(R.id.backToMainActivity)
        val shareApp = findViewById<TextView>(R.id.share)
        val support = findViewById<TextView>(R.id.support)
        val termsOfUse = findViewById<TextView>(R.id.terms_of_use)


        backButton.setOnClickListener {
            finish()
        }

        shareApp.setOnClickListener {
            val urlCourse = getString(R.string.url_course)
            val shareIntent = Intent(ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(EXTRA_TEXT, urlCourse)
            startActivity(shareIntent)
        }

        support.setOnClickListener {
            val email = getString(R.string.email_address)
            val subject = getString(R.string.email_subject)
            val message = getString(R.string.email_message)
            val supportIntent = Intent(ACTION_SENDTO)
            supportIntent.data = Uri.parse("mailto:")
            supportIntent.putExtra(EXTRA_EMAIL, arrayOf(email))
            supportIntent.putExtra(EXTRA_SUBJECT, subject)
            supportIntent.putExtra(EXTRA_TEXT, message)
            startActivity(supportIntent)
        }

        termsOfUse.setOnClickListener {
            val urlOffer = getString(R.string.url_offer)
            val termsOfUseIntent = Intent(ACTION_VIEW)
            termsOfUseIntent.data = Uri.parse(urlOffer)
            startActivity(termsOfUseIntent)
        }


    }
}