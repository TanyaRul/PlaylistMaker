package com.example.playlistmaker.sharing.domain.interactor

import com.example.playlistmaker.library.domain.model.Playlist

interface SharingInteractor {
    fun shareApp(urlCourse: String)
    fun openTerms(urlOffer: String)
    fun openSupport(email: String, subject: String, message: String)
    fun sharePlaylist(message: String)
}