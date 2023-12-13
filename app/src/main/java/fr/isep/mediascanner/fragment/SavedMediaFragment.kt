package fr.isep.mediascanner.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import fr.isep.mediascanner.R
import fr.isep.mediascanner.activity.SetupRoomActivity
import fr.isep.mediascanner.adapter.ProductItemAdapter
import fr.isep.mediascanner.adapter.RoomHeaderAdapter
import fr.isep.mediascanner.database.AppDatabaseSingleton
import fr.isep.mediascanner.model.local.Product
import fr.isep.mediascanner.model.local.Room
import fr.isep.mediascanner.RequestCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

class SavedMediaFragment : Fragment() {

    private var activityRef: WeakReference<Activity>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            activityRef = WeakReference(context)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        val activity = activityRef?.get()
        if (activity != null) {
            lifecycleScope.launch {

                val db = AppDatabaseSingleton.getDatabase(activity.applicationContext)

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
                startActivityForResult(intent, RequestCodes.SETUP_ROOM_REQUEST_CODE)

                val newFragment = ScanFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, newFragment)
                    .commit()
                val bottomNavView: BottomNavigationView = activity.findViewById(R.id.bottom_navigation) ?: return@setOnClickListener
                bottomNavView.menu.findItem(R.id.nav_scan)?.isChecked = true

            }
        }
    }
}