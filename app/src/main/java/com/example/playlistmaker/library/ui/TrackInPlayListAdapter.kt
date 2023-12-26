package com.example.playlistmaker.library.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.ui.TrackViewHolder
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TrackInPlayListAdapter(
    val tracks: List<Track>,
    private val deleteAlertDialog: MaterialAlertDialogBuilder,
    private val clickListener: (Track) -> Unit,
    private val longClickListener: (Track) -> Unit
) : RecyclerView.Adapter<TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(parent)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
        holder.itemView.setOnClickListener {
            clickListener.invoke(tracks[position])
        }
        holder.itemView.setOnLongClickListener {
            deleteAlertDialog
                .setNegativeButton(R.string.cancel) { _, _ ->
                }
                .setPositiveButton(R.string.delete) { _, _ ->
                    longClickListener.invoke(tracks[position])
                }
            deleteAlertDialog.show()
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return tracks.size
    }
}