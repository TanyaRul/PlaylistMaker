package com.example.playlistmaker.search.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.model.Track
import java.text.SimpleDateFormat
import java.util.*

//class TrackViewHolder(private val binding: RecyclerItemBinding): RecyclerView.ViewHolder(binding.root)
class TrackViewHolder(
    parentView: ViewGroup,
    itemView: View = LayoutInflater.from(parentView.context)
        .inflate(R.layout.track_view, parentView, false)
) : RecyclerView.ViewHolder(itemView) {
    private val albumCoverView: ImageView = itemView.findViewById(R.id.albumCover)
    private val trackNameView: TextView = itemView.findViewById(R.id.trackName)
    private val artistNameView: TextView = itemView.findViewById(R.id.artistName)
    private val trackTimeView: TextView = itemView.findViewById(R.id.trackTime)
    //var inFavoriteToggle: ImageView = itemView.findViewById(R.id.favorite)

    fun bind(item: Track) {

        //binding.title.text = item.text
        //binding.field.text = item.field

        Glide.with(itemView)
            .load(item.artworkUrl100)
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .transform(
                RoundedCorners(
                    itemView.resources.getDimensionPixelSize(R.dimen.album_cover_corners_radius)
                )
            )
            .into(albumCoverView)
        trackNameView.text = item.trackName
        artistNameView.text = item.artistName
        trackTimeView.text =
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(item.trackTimeMillis)

        //inFavoriteToggle.setImageDrawable(getFavoriteToggleDrawable(item.inFavorite))
        //itemView.setOnClickListener { clickListener.onMovieClick(item) }
        //inFavoriteToggle.setOnClickListener { clickListener.onFavoriteToggleClick(item) }
    }

    /*private fun getFavoriteToggleDrawable(inFavorite: Boolean): Drawable? {
        return itemView.context.getDrawable(
            if(inFavorite) R.drawable.active_favorite else R.drawable.inactive_favorite
        )
    }*/
}