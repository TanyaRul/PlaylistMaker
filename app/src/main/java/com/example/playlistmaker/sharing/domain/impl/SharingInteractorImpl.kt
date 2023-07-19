package com.example.playlistmaker.sharing.domain.impl


import com.example.playlistmaker.sharing.data.repository.ExternalNavigator
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor
import com.example.playlistmaker.sharing.domain.model.EmailData


class SharingInteractorImpl(
    private val externalNavigator: ExternalNavigator,
) : SharingInteractor {
    override fun shareApp(urlCourse: String) {
        externalNavigator.shareLink(getShareAppLink(urlCourse = urlCourse))
    }

    override fun openTerms(urlOffer: String) {
        externalNavigator.openLink(getTermsLink(urlOffer = urlOffer))
    }

    override fun openSupport(email: String, subject: String, message: String) {
        externalNavigator.openEmail(
            getSupportEmailData(
                email = email,
                subject = subject,
                message = message
            )
        )
    }

    private fun getShareAppLink(urlCourse: String): String {
        return urlCourse
    }

    private fun getTermsLink(urlOffer: String): String {
        return urlOffer
    }

    private fun getSupportEmailData(email: String, subject: String, message: String): EmailData {
        return EmailData(email = email, subject = subject, message = message)
    }

}