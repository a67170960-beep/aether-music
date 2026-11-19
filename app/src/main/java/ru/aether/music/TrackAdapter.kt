package ru.aether.music

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(
    private var items: List<Track> = emptyList(),
    private val onClick: (Int, Track) -> Unit
) : RecyclerView.Adapter<TrackAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.trackTitle)
        val artist: TextView = v.findViewById(R.id.trackArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = items[position]
        holder.title.text = t.title
        holder.artist.text = t.artist
        holder.itemView.setOnClickListener { onClick(position, t) }
    }

    override fun getItemCount(): Int = items.size

    fun submit(list: List<Track>) {
        items = list
        notifyDataSetChanged()
    }
}
