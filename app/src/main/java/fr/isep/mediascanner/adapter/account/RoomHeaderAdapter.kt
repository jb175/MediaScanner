package fr.isep.mediascanner.adapter.account

import androidx.lifecycle.LifecycleCoroutineScope
import fr.isep.mediascanner.model.local.Room

class RoomHeaderAdapter(private val room: Room, private val scope: LifecycleCoroutineScope) : fr.isep.mediascanner.adapter.RoomHeaderAdapter(room, scope) {
    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
    }
}