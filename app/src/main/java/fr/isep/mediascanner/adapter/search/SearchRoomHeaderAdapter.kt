package fr.isep.mediascanner.adapter.search

import fr.isep.mediascanner.model.local.Room

class SearchRoomHeaderAdapter(private val room: Room, private val email: String) : fr.isep.mediascanner.adapter.RoomHeaderAdapter(room) {

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.textView.text = String.format("%s from %s", room.name, email)
    }
}