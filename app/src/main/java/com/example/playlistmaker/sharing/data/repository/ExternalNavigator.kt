package com.example.playlistmaker.sharing.data.repository

import com.example.playlistmaker.library.domain.model.Playlist
import com.example.playlistmaker.sharing.domain.model.EmailData

interface ExternalNavigator {
    fun shareLink(shareAppLink: String)
    fun openLink(termsLink: String)
    fun openEmail(supportEmailData: EmailData)
    fun sharePlaylist(message: String)
}