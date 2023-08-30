package com.example.playlistmaker.settings.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSettingsBinding
import com.example.playlistmaker.settings.ui.view_model.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    private val viewModel by viewModel<SettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
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