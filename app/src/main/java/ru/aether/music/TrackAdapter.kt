package ru.aether.music

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(
    private var items: List<Track> = emptyList(),
    private val onClick: (Int, Track) -> Unit,
    private val onFav: (Track) -> Unit
) : RecyclerView.Adapter<TrackAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.trackTitle)
        val artist: TextView = v.findViewById(R.id.trackArtist)
        val fav: ImageButton = v.findViewById(R.id.btnFav)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = items[position]
        holder.title.text = t.title
        holder.artist.text = "${t.artist} · ${formatMs(t.durationMs)}"
        holder.fav.setImageResource(
            if (t.favorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
        )
        holder.itemView.setOnClickListener { onClick(position, t) }
        holder.fav.setOnClickListener { onFav(t) }
    }

    override fun getItemCount() = items.size

    fun submit(list: List<Track>) {
        items = list
        notifyDataSetChanged()
    }

    fun current() = items
}

fun formatMs(ms: Long): String {
    val s = (ms / 1000).toInt().coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}
