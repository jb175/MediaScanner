package fr.isep.mediascanner.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import fr.isep.mediascanner.R
import fr.isep.mediascanner.activity.ui.login.LoginActivity
import fr.isep.mediascanner.activity.MainActivity
import fr.isep.mediascanner.dao.remote.FirebaseDao
import fr.isep.mediascanner.database.remote.FirebaseSingleton
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import fr.isep.mediascanner.adapter.account.ProductItemAdapter
import fr.isep.mediascanner.adapter.account.RoomHeaderAdapter
import fr.isep.mediascanner.model.local.Product
import fr.isep.mediascanner.model.local.Room

class AccountFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var firebaseDao: FirebaseDao
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (activity is MainActivity) {
            auth = (activity as MainActivity).auth
            firebaseDao = FirebaseDao(requireContext())
            firebaseDatabase = FirebaseSingleton.getDatabaseInstance()

            if (auth.currentUser == null) {
                val intent = Intent(context, LoginActivity::class.java)

                this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        currentUser = auth.currentUser!!
                        Log.println(Log.INFO, "MediaScannerAccount", "User logged in ${auth.currentUser}")
                        firebaseDao.uploadAllDataToFirebase()
                    }
                }.launch(intent)
            } else {
                currentUser = auth.currentUser!!
                firebaseDao.uploadAllDataToFirebase()
            }

            val searchBar = view.findViewById<SearchView>(R.id.searchBar) // Replace with your actual SearchView id
            searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    searchUserByEmail(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
        } 
    }

    // Add this function to your AccountFragment class
    fun searchUserByEmail(email: String) {
        val emailKey = email.replace(".", ",")
        val reference = FirebaseSingleton.getDatabaseInstance().getReference("emailsToUids").child(emailKey)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val uid = dataSnapshot.value as String?
                if (uid != null) {
                    displayUserRoomsAndProducts(uid)
                } else {
                    Log.i("Firebase", "No user found with email $email")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error searching for user", databaseError.toException())
            }
        })
    }

    // Add this function to your AccountFragment class
    fun displayUserRoomsAndProducts(uid: String) {
        Log.i("Firebase", "User found : $uid")

        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        val adapters = mutableListOf<RecyclerView.Adapter<*>>()

        val roomsReference = firebaseDatabase.getReference("users/$uid/rooms")
        roomsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val roomCount = snapshot.childrenCount
                var loadedRoomCount = 0

                for (roomSnapshot in snapshot.children) {
                    val room = roomSnapshot.getValue(Room::class.java)
                    if (room != null) {
                        Log.i("Firebase", "Room fetched: $room")
                        adapters.add(RoomHeaderAdapter(room, lifecycleScope))

                        val productsReference = firebaseDatabase.getReference("users/$uid/rooms/${room.id}/products")
                        productsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val products = mutableListOf<Product>()
                                for (productSnapshot in snapshot.children) {
                                    val product = productSnapshot.getValue(Product::class.java)
                                    if (product != null) {
                                        Log.i("Firebase", "Product fetched: $product")
                                        products.add(product)
                                    }
                                }
                                adapters.add(ProductItemAdapter(products, lifecycleScope))

                                loadedRoomCount++
                                if (loadedRoomCount >= roomCount) {
                                    recyclerView?.adapter = ConcatAdapter(*adapters.toTypedArray())
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.w("Firebase", "Failed to read products.", error.toException())
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read rooms.", error.toException())
                if (error.message.contains("Permission denied")) {
                    Toast.makeText(context, "You don't have access to this user's data", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}