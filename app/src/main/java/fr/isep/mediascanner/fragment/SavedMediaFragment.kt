package fr.isep.mediascanner.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.isep.mediascanner.R
import fr.isep.mediascanner.activity.SetupRoomActivity
import fr.isep.mediascanner.activity.MainActivity
import fr.isep.mediascanner.adapter.ProductItemAdapter
import fr.isep.mediascanner.adapter.RoomHeaderAdapter
import fr.isep.mediascanner.database.AppDatabaseSingleton
import fr.isep.mediascanner.model.local.Product
import fr.isep.mediascanner.model.local.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedMediaFragment : Fragment() {

    private val setupRoomResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && context is MainActivity) {
            (context as MainActivity).refreshSavedMediaFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_saved_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (context is MainActivity) {

            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
            lifecycleScope.launch {

                val db = AppDatabaseSingleton.getDatabase((context as MainActivity).applicationContext)

                val rooms: List<Room> = withContext(Dispatchers.IO) { db.roomDao().getAll() }

                val adapters = mutableListOf<RecyclerView.Adapter<*>>()
                for (room in rooms) {
                    val products: List<Product> = withContext(Dispatchers.IO) { db.productDao().getProductsForRoom(room.id) }
                    adapters.add(RoomHeaderAdapter(room, lifecycleScope))
                    adapters.add(ProductItemAdapter(products, lifecycleScope))

                    Log.println(Log.INFO, "RoomMediaScanner", String.format("room #%d %s contain %s", room.id, room.name, products.toString()))
                }
            
                withContext(Dispatchers.Main) {
                    recyclerView.adapter = ConcatAdapter(*adapters.toTypedArray())
                }
            }

            val createRoomButton: Button = view.findViewById(R.id.createRoomButton)
            createRoomButton.setOnClickListener {
                val intent = Intent(activity, SetupRoomActivity::class.java)
                setupRoomResultLauncher.launch(intent)
            }
        }
    }
}