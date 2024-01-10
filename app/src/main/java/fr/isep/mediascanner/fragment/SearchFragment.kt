package fr.isep.mediascanner.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.isep.mediascanner.R
import fr.isep.mediascanner.activity.MainActivity
import fr.isep.mediascanner.adapter.search.ProductItemAdapter
import fr.isep.mediascanner.dao.remote.FirebaseDao
import fr.isep.mediascanner.database.local.AppDatabaseSingleton
import fr.isep.mediascanner.database.remote.FirebaseSingleton
import fr.isep.mediascanner.model.local.Product
import fr.isep.mediascanner.model.local.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDao: FirebaseDao
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (activity is MainActivity) {
            auth = (activity as MainActivity).auth
            firebaseDao = FirebaseDao(requireContext())
            firebaseDatabase = FirebaseSingleton.getDatabaseInstance()

            val searchView = view.findViewById<SearchView>(R.id.searchView)

            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
            recyclerView?.layoutManager = LinearLayoutManager(context)
            val adapters = mutableListOf<RecyclerView.Adapter<*>>()
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(view.windowToken, 0)
                    if (auth.currentUser != null) {
                        searchInFirebase(query) { products, uid, roomName ->
                            adapters.add(ProductItemAdapter(products))
                            recyclerView?.adapter = ConcatAdapter(*adapters.toTypedArray())
                        }
                    } else {
                        searchInRoomDatabase(query) { products, roomName ->
                            adapters.add(ProductItemAdapter(products))
                            recyclerView?.adapter = ConcatAdapter(*adapters.toTypedArray())
                        }
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    recyclerView.adapter = null
                    return false
                }
            })


        }
    }

    fun searchInFirebase(query: String, callback: (List<Product>, String, String) -> Unit) {
        val emailsToUidsReference = firebaseDatabase.getReference("emailsToUids")
        emailsToUidsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (emailToUidSnapshot in snapshot.children) {
                    val uid = emailToUidSnapshot.value as? String ?: continue
                    try {
                        val roomsReference = firebaseDatabase.getReference("users/$uid/rooms")
                        roomsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (roomSnapshot in snapshot.children) {
                                    val room = roomSnapshot.getValue(Room::class.java)
                                    if (room?.name != null) {
                                        val productsReference = firebaseDatabase.getReference("users/$uid/rooms/${room.id}/products")
                                        productsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val products = mutableListOf<Product>()
                                                for (productSnapshot in snapshot.children) {
                                                    val product = productSnapshot.getValue(Product::class.java)
                                                    if (product != null) {
                                                        if (product.title != null && product.title.contains(query, ignoreCase = true)) {
                                                            products.add(product)
                                                            Log.i("SearchResult", "product found by TITLE: ${product.title}")
                                                        }
                                                        if (product.isbn == query) {
                                                            products.add(product)
                                                            Log.i("SearchResult", "product found by ISBN: ${product.isbn}")
                                                        }
                                                    }
                                                }
                                                callback(products, uid, room.name!!)
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                Log.w("Firebase", "Failed to read products.", error.toException())
                                            }
                                        })
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.w("Firebase", "Failed to read rooms for user $uid", error.toException())
                            }
                        })
                    } catch (e: DatabaseException) {
                        Log.w("Firebase", "Failed to read data for user $uid.", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read emailsToUids.", error.toException())
            }
        })
    }

    private fun searchInRoomDatabase(query: String, callback: (List<Product>, String) -> Unit) {
        lifecycleScope.launch {
            val db = AppDatabaseSingleton.getDatabase((context as MainActivity).applicationContext)
            val rooms: List<Room> = withContext(Dispatchers.IO) { db.roomDao().getAll() }

            for (room in rooms) {
                if (room.name != null) {
                    val products: List<Product> = withContext(Dispatchers.IO) { db.productDao().getProductsForRoom(room.id) }
                    val productsReturned = mutableListOf<Product>()
                    for (product in products) {
                        if (product.title != null && product.title.contains(query, ignoreCase = true)) {
                            productsReturned.add(product)
                            Log.i("SearchResult", "product found locally by TITLE: ${product.title}")
                        }
                        if (product.isbn == query) {
                            productsReturned.add(product)
                            Log.i("SearchResult", "product found locally by ISBN: ${product.isbn}")
                        }
                    }
                    callback(productsReturned, room.name!!)
                    Log.println(Log.INFO, "RoomMediaScanner", String.format("room #%d %s contain %s", room.id, room.name, products.toString()))
                }
            }
        }
    }
}