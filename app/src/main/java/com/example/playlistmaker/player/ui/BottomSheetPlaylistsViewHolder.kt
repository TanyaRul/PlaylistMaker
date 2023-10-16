package com.example.playlistmaker.player.ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.library.domain.model.Playlist

class BottomSheetPlaylistsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val cover: ImageView = view.findViewById(R.id.ivPlaylistCoverSmall)
    private val title: TextView = view.findViewById(R.id.tvPlaylistName)
    private val numberOfTracks: TextView = view.findViewById(R.id.tvNumberOfTracks)


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