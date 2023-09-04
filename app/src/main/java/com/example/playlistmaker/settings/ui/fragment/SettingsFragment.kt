package com.example.playlistmaker.settings.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSettingsBinding
import com.example.playlistmaker.settings.ui.view_model.SettingsViewModel
import com.example.playlistmaker.util.BindingFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : BindingFragment<FragmentSettingsBinding>() {

    private val viewModel by viewModel<SettingsViewModel>()

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        viewModel.themeSettingsLiveData.observe(viewLifecycleOwner) { themeSettings ->
            binding.themeSwitcher.isChecked = themeSettings.darkTheme
        }

        binding.themeSwitcher.setOnCheckedChangeListener { switcher, checked ->
            viewModel.switchTheme(checked)
        }

    }
}