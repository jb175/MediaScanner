package fr.isep.mediascanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import fr.isep.mediascanner.R
import fr.isep.mediascanner.database.remote.FirebaseSingleton
import fr.isep.mediascanner.model.Request

class RequestsAdapter(private val requests: MutableList<Request>) : RecyclerView.Adapter<RequestsAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val email: TextView = view.findViewById(R.id.email)
        val accept: Button = view.findViewById(R.id.accept)
        val deny: Button = view.findViewById(R.id.deny)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.request_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]

        FirebaseSingleton.getDatabaseInstance().getReference("emailsToUids")
        .orderByValue().equalTo(request.senderUid)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val email = snapshot.children.first().key?.replace(",", ".")
                    holder.email.text = email
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        holder.accept.setOnClickListener {
            val receiverUid = FirebaseSingleton.getAuthInstance().currentUser?.uid
            if (receiverUid != null) {
                FirebaseSingleton.getDatabaseInstance().getReference("users").child(receiverUid).child("accessList")
                    .child(request.senderUid).setValue(true)
                    .addOnSuccessListener {
                        // Remove request using its ID
                        FirebaseSingleton.getDatabaseInstance().getReference("accessRequests").child(request.id).removeValue()
                            .addOnSuccessListener {
                                requests.removeAt(position)
                                notifyItemRemoved(position)
                                Toast.makeText(holder.itemView.context, "User added to access list and request removed", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(holder.itemView.context, "Failed to remove request: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(holder.itemView.context, "Failed to add user to access list: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        holder.deny.setOnClickListener {
            // Remove request using its ID
            FirebaseSingleton.getDatabaseInstance().getReference("accessRequests").child(request.id).removeValue()
                .addOnSuccessListener {
                    requests.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(holder.itemView.context, "Request removed", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(holder.itemView.context, "Failed to remove request: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount() = requests.size
}