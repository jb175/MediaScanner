package fr.isep.mediascanner.adapter.search

import android.content.Intent
import fr.isep.mediascanner.R
import fr.isep.mediascanner.activity.MainActivity
import fr.isep.mediascanner.activity.SetupRoomActivity
import fr.isep.mediascanner.model.local.Room

class SearchRoomHeaderAdapter(private val room: Room, private val email: String) : fr.isep.mediascanner.adapter.RoomHeaderAdapter(room) {

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.textView.text = String.format("%s %s %s", room.name, context.getString(R.string.mainActivity_search_textHeader), email)

        holder.textView.setOnClickListener {
            val intent = Intent(context, SetupRoomActivity::class.java).apply {
                putExtra("ROOM", room)
            }
            if (context is MainActivity) {
                (context as MainActivity).getSetupProductDetailsSearchResultLauncher().launch(intent)
            }
        }
    }
}