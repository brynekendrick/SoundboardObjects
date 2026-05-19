package com.example.soundboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.mobdev.R

class SoundAdapter(
    private val context: Context,
    private var soundsList: List<SoundItem>
) : RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {

    private val soundPlayer: SoundPlayer = SoundPlayer.getInstance(context)

    interface OnSoundItemClickListener {
        fun onPlayButtonClick(soundItem: SoundItem, position: Int)
        fun onFavoriteButtonClick(soundItem: SoundItem, position: Int)
        fun onSoundItemLongClick(soundItem: SoundItem, position: Int) {}
    }

    private var listener: OnSoundItemClickListener? = null

    fun setOnSoundItemClickListener(listener: OnSoundItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sounds, parent, false)
        return SoundViewHolder(view)
    }

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val soundItem = soundsList[position]

        holder.titleTextView.text = soundItem.title
        holder.categoryTextView.text = soundItem.category
        holder.durationTextView.text = soundItem.duration

        holder.favoriteButton.setImageResource(
            if (soundItem.isFavorite)
                android.R.drawable.btn_star_big_on
            else
                android.R.drawable.btn_star_big_off
        )

        if (soundPlayer.isPlaying() && soundPlayer.getCurrentSoundName() == soundItem.id) {
            holder.playButton.text = "STOP"
            holder.progressBar.visibility = View.VISIBLE
        } else {
            holder.playButton.text = "PLAY"
            holder.progressBar.visibility = View.GONE
        }

        holder.playButton.setOnClickListener {
            listener?.onPlayButtonClick(soundItem, holder.adapterPosition)
        }

        holder.favoriteButton.setOnClickListener {
            listener?.onFavoriteButtonClick(soundItem, holder.adapterPosition)
        }

        holder.itemView.setOnLongClickListener {
            listener?.onSoundItemLongClick(soundItem, holder.adapterPosition)
            true
        }
    }

    override fun getItemCount(): Int = soundsList.size

    fun updateSoundsList(newSoundsList: List<SoundItem>) {
        this.soundsList = newSoundsList
        notifyDataSetChanged()
    }

    fun updateItem(position: Int) {
        notifyItemChanged(position)
    }

    class SoundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.text_sound_title)
        val categoryTextView: TextView = itemView.findViewById(R.id.text_category)
        val durationTextView: TextView = itemView.findViewById(R.id.text_duration)
        val playButton: Button = itemView.findViewById(R.id.button_play)
        val favoriteButton: ImageButton = itemView.findViewById(R.id.button_favorite)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_playing)
    }
}