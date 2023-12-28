package fr.isep.mediascanner.adapter.media

import android.content.Intent
import androidx.lifecycle.LifecycleCoroutineScope
import fr.isep.mediascanner.activity.SetupRoomActivity
import fr.isep.mediascanner.model.local.Room
import fr.isep.mediascanner.activity.MainActivity
import kotlinx.coroutines.launch

class RoomHeaderAdapter(private val room: Room, private val scope: LifecycleCoroutineScope) : fr.isep.mediascanner.adapter.RoomHeaderAdapter(room) {
    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.textView.setOnClickListener {
            scope.launch {
                val intent = Intent(context, SetupRoomActivity::class.java).apply {
                    putExtra("ROOM", room)
                }
                if (context is MainActivity) {
                    (context as MainActivity).getSetupProductDetailsRefreshForActivityResult().launch(intent)
                }
            }
        }
    }
}