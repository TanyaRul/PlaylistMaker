package com.example.playlistmaker.library.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.library.domain.model.Playlist

class PlaylistAdapter(
    var playlists: ArrayList<Playlist>,
    private val onPlaylistClick: (Playlist) -> Unit
) :
    RecyclerView.Adapter<PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder(parent)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
        holder.itemView.setOnClickListener {
            onPlaylistClick.invoke(playlists[position])
        }
    }

    override fun getItemCount(): Int {
        return playlists.size
    }
}