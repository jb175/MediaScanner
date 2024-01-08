package fr.isep.mediascanner.activity

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import fr.isep.mediascanner.R
import fr.isep.mediascanner.adapter.RequestsAdapter
import fr.isep.mediascanner.database.remote.FirebaseSingleton
import fr.isep.mediascanner.model.Request

class RequestsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
        
        getAccessRequests()
    }

    private fun getAccessRequests() {
        val userUid = FirebaseSingleton.getAuthInstance().currentUser?.uid
        if (userUid != null) {
            FirebaseSingleton.getDatabaseInstance().getReference("accessRequests").orderByChild("receiverUid").equalTo(userUid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val requests = mutableListOf<Request>()
                        for (requestSnapshot in snapshot.children) {
                            val request = requestSnapshot.getValue(Request::class.java)
                            if (request != null) {
                                requests.add(request)
                            }
                        }
                        recyclerView.adapter = RequestsAdapter(requests)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
        }
    }
}