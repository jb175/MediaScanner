package fr.isep.mediascanner.activity.productDetails

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
import fr.isep.mediascanner.R
import fr.isep.mediascanner.database.local.AppDatabase
import fr.isep.mediascanner.database.local.AppDatabaseSingleton
import fr.isep.mediascanner.model.local.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class LocalBaseProductDetailsActivity : BaseProductDetailsActivity() {
    protected lateinit var spinnerRooms: Spinner
    protected lateinit var buttonAddToRoom: Button

    protected lateinit var rooms: List<Room>
    protected lateinit var db: AppDatabase

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        spinnerRooms = findViewById(R.id.spinnerRooms)
        buttonAddToRoom = findViewById(R.id.buttonAddToRoom)

        db = AppDatabaseSingleton.getDatabase(applicationContext)

        lifecycleScope.launch {
            rooms = withContext(Dispatchers.IO) { db.roomDao().getAll() }

            // If no rooms exist, create a default one
            if (rooms.isEmpty()) {
                var defaultRoom = Room(id = 0, name = "Default")
                withContext(Dispatchers.IO) {
                    val roomIdLong = db.roomDao().insert(defaultRoom)
                    val roomId = roomIdLong.toInt()
                    defaultRoom = db.roomDao().getById(roomId)!!
                    Log.println(Log.INFO, "RoomMediaScanner", String.format("new Default Room %d", roomId))
                }
                rooms = listOf(defaultRoom)
            }

            val adapter = ArrayAdapter(this@LocalBaseProductDetailsActivity, android.R.layout.simple_spinner_item, rooms.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerRooms.adapter = adapter

            spinnerRooms.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    onSpinnerSelected(parent, view, position, id)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            afterOnCreate()
        }
    }

    abstract fun afterOnCreate()

    open fun onSpinnerSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        //nothing by default
    }
}