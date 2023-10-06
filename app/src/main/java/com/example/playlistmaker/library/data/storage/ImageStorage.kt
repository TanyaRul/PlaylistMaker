package com.example.playlistmaker.library.data.storage

interface ImageStorage {
    fun saveImageToPrivateStorage(uriFile: String?): String?
}