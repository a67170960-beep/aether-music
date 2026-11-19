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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false))
    override fun onBindViewHolder(h: VH, pos: Int) {
        val t = items[pos]
        h.title.text = t.title
        h.artist.text = "${t.artist} · ${formatMs(t.durationMs)}"
        h.fav.setImageResource(if (t.favorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
        h.itemView.setOnClickListener { onClick(pos, t) }
        h.fav.setOnClickListener { onFav(t) }
    }
    override fun getItemCount() = items.size
    fun submit(list: List<Track>) { items = list; notifyDataSetChanged() }
}
