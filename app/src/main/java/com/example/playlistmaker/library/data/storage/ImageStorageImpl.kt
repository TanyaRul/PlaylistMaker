package com.example.playlistmaker.library.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageStorageImpl(private val context: Context) : ImageStorage {

    override fun saveImageToPrivateStorage(uriFile: String?): String? {
        if (uriFile != null && uriFile != "null") {
            val filePath =
                File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), DIRECTORY
                )

            if (!filePath.exists()) {
                filePath.mkdir()
            }

            val timeStamp =
                SimpleDateFormat("dd.MM.yyyy_hh:mm", Locale.getDefault()).format(Date().time)

            val file = File(filePath, "$IMAGE_NAME$timeStamp.jpg")

            val inputStream = context.contentResolver.openInputStream(uriFile.toUri())

            val outputStream = FileOutputStream(file)

            BitmapFactory
                .decodeStream(inputStream)
                .compress(Bitmap.CompressFormat.JPEG, QUALITY_IMAGE, outputStream)

            return file.toUri().toString()
        }
        return null
    }

    companion object {
        private const val QUALITY_IMAGE = 30
        private const val DIRECTORY = "playlist"
        private const val IMAGE_NAME = "image"
    }

}