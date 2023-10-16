package com.example.playlistmaker.player.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.library.domain.model.Playlist

class BottomSheetPlaylistsAdapter(
    var playlists: ArrayList<Playlist>,
    private val onPlaylistClick: (Playlist) -> Unit
) :
    RecyclerView.Adapter<BottomSheetPlaylistsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomSheetPlaylistsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.playlist_view_small, parent, false)
        return BottomSheetPlaylistsViewHolder(view)
    }

    override fun onBindViewHolder(holder: BottomSheetPlaylistsViewHolder, position: Int) {
        holder.bind(playlists[position])
        holder.itemView.setOnClickListener {
            onPlaylistClick.invoke(playlists[position])
        }
    }

    override fun getItemCount(): Int {
        return playlists.size
    }
}