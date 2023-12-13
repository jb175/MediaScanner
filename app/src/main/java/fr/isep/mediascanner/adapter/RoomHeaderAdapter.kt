package fr.isep.mediascanner.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import fr.isep.mediascanner.R
import fr.isep.mediascanner.activity.ProductDetailsActivity
import fr.isep.mediascanner.activity.SetupRoomActivity
import fr.isep.mediascanner.model.local.Room
import fr.isep.mediascanner.RequestCodes
import kotlinx.coroutines.launch

class RoomHeaderAdapter(private val room: Room, private val scope: LifecycleCoroutineScope
) : RecyclerView.Adapter<RoomHeaderAdapter.HeaderViewHolder>() {

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.header_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.header_item, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.textView.text = room.name

        holder.textView.setOnClickListener {
            val context = it.context
            scope.launch {
                val intent = Intent(context, SetupRoomActivity::class.java).apply {
                    putExtra("ROOM", room)
                }
                if (context is Activity) {
                    context.startActivityForResult(intent, RequestCodes.SETUP_ROOM_REQUEST_CODE)
                }
            }
        }
    }

    override fun getItemCount() = 1
}