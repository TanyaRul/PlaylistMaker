package com.example.playlistmaker.sharing.domain.interactor

interface SharingInteractor {
    fun shareApp(urlCourse: String)
    fun openTerms(urlOffer: String)
    fun openSupport(email: String, subject: String, message: String)
    fun sharePlaylist(message: String)
}