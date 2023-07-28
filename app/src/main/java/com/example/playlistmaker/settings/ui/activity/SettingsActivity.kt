package com.example.playlistmaker.settings.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import com.example.playlistmaker.settings.ui.view_model.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val viewModel by viewModel<SettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backToMainActivity.setOnClickListener {
            finish()
        }

        binding.share.setOnClickListener {
            val urlCourse = getString(R.string.url_course)
            viewModel.shareApp(urlCourse)
        }

        binding.support.setOnClickListener {
            val email = getString(R.string.email_address)
            val subject = getString(R.string.email_subject)
            val message = getString(R.string.email_message)
            viewModel.openSupport(email, subject, message)
        }

        binding.termsOfUse.setOnClickListener {
            val urlOffer = getString(R.string.url_offer)
            viewModel.openTerms(urlOffer)
        }

        viewModel.themeSettingsLiveData.observe(this) { themeSettings ->
            binding.themeSwitcher.isChecked = themeSettings.darkTheme
        }

        binding.themeSwitcher.setOnCheckedChangeListener { switcher, checked ->
            viewModel.switchTheme(checked)
        }

    }
}