package com.example.mediatimerjp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediatimerjp.R
import com.example.mediatimerjp.model.IntervalSound
import java.util.*

class IntervalSoundAdapter(private val intervalsList: ArrayList<IntervalSound>) :
    RecyclerView.Adapter<IntervalSoundAdapter.IntervalSoundViewHolder>() {
    private var mListener: OnItemClickListener? = null
    lateinit var volume:SeekBar
    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IntervalSoundViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.interval_sound, parent, false)
        return IntervalSoundViewHolder(v, mListener)
    }

    override fun onBindViewHolder(holder: IntervalSoundViewHolder, position: Int) {
        val currentItem = intervalsList[position]
        holder.numberTextView.text = currentItem.soundNumber
        holder.inputTextView.text = currentItem.soundName
    }

    override fun getItemCount(): Int {
        return intervalsList.size
    }

    class IntervalSoundViewHolder(itemView: View, listener: OnItemClickListener?) :
        RecyclerView.ViewHolder(itemView) {
        var numberTextView: TextView = itemView.findViewById(R.id.soundNumberText)
        var inputTextView: TextView

        init {
            inputTextView = itemView.findViewById(R.id.soundNumberInput)
            itemView.setOnClickListener {
                if (listener != null) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position)
                    }
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}