package fr.isep.mediascanner.adapter.search

import fr.isep.mediascanner.R
import fr.isep.mediascanner.model.local.Room

class OtherSearchRoomHeaderAdapter(private val room: Room, private val email: String) : fr.isep.mediascanner.adapter.RoomHeaderAdapter(room) {

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.textView.text = String.format("%s %s %s", room.name, context.getString(R.string.mainActivity_search_textHeader), email)
    }
}