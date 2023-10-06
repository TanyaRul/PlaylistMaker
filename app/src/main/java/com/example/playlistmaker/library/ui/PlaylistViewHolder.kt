package com.example.playlistmaker.library.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.library.domain.model.Playlist

class PlaylistViewHolder(
    parentView: ViewGroup,
    itemView: View = LayoutInflater.from(parentView.context)
        .inflate(R.layout.playlist_view, parentView, false)
) : RecyclerView.ViewHolder(itemView) {

    private val cover: ImageView = itemView.findViewById(R.id.ivPlaylistCover)
    private val title: TextView = itemView.findViewById(R.id.tvPlaylistTitle)
    private val numberOfTracks: TextView = itemView.findViewById(R.id.tvNumberOfTracks)

    fun bind(playlist: Playlist) {
        Glide.with(itemView)
            .load(playlist.playlistCoverPath)
            .placeholder(R.drawable.placeholder)
            .transform(
                CenterCrop(),
                RoundedCorners(
                    itemView.context.resources.getDimensionPixelSize(R.dimen.album_cover_corners_radius_large)
                )
            )
            .into(cover)

        title.text = playlist.playlistTitle

        numberOfTracks.text = playlist.numberOfTracks?.let {
            itemView.resources.getQuantityString(
                R.plurals.track_count,
                it, playlist.numberOfTracks
            )
        }
    }
}