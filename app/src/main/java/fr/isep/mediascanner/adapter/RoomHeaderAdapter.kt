package fr.isep.mediascanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.isep.mediascanner.R
import fr.isep.mediascanner.model.local.Room

abstract class RoomHeaderAdapter(private val room: Room) : RecyclerView.Adapter<RoomHeaderAdapter.HeaderViewHolder>() {

    protected lateinit var context: Context

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.header_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.header_item, parent, false)
        context = parent.context
        return HeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.textView.text = room.name
    }

    override fun getItemCount() = 1
}