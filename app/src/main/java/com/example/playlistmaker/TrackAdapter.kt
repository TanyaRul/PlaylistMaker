package com.example.playlistmaker

import android.content.Intent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class TrackAdapter(
    var trackList: ArrayList<Track>, private val searchHistory: SearchHistory
) : RecyclerView.Adapter<TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(parent)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(trackList[position])
        holder.itemView.setOnClickListener {
            searchHistory.addTrackToHistory(trackList[position])

            val playerIntent = Intent(it.context, PlayerActivity::class.java)
            playerIntent.putExtra(PlayerActivity.TRACK_DATA_KEY, Gson().toJson(trackList[position]))
            it.context.startActivity(playerIntent)
        }
    }

    override fun getItemCount(): Int {
        return trackList.size
    }

}