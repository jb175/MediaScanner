package fr.isep.mediascanner.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.IntentCompat.getParcelableExtra
import androidx.lifecycle.lifecycleScope
import fr.isep.mediascanner.R
import fr.isep.mediascanner.database.local.AppDatabase
import fr.isep.mediascanner.database.local.AppDatabaseSingleton
import fr.isep.mediascanner.model.local.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SetupRoomActivity : AppCompatActivity() {
    
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_room)

        val roomNameEditText: EditText = findViewById(R.id.roomNameEditText)
        val submitRoomButton: Button = findViewById(R.id.confirmRoomButton)
        val deleteRoomButton: Button = findViewById(R.id.deleteRoomButton)

        
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }

        val room: Room? = getParcelableExtra(intent, "ROOM", Room::class.java)

        if (room != null) {
            roomNameEditText.setText(room.name)
            db = AppDatabaseSingleton.getDatabase(applicationContext)

            submitRoomButton.setText(R.string.setupRoom_modifyButton)
            deleteRoomButton.setText(R.string.setupRoom_deleteButton)

            submitRoomButton.setOnClickListener {
                room.name = roomNameEditText.text.toString()
                if (room.name != "") {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            db.roomDao().update(room)
                            Log.println(
                                Log.INFO,
                                "RoomMediaScanner",
                                String.format("Room #%d renamed %s", room.id, room.name)
                            )
                        }
                        withContext(Dispatchers.Main) {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }
                } else {
                    roomNameEditText.error = "Room name cannot be empty"
                }
            }

            deleteRoomButton.setOnClickListener {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        db.productDao().deleteProductsInRoom(room.id)
                        db.roomDao().delete(room)
                        Log.println(Log.INFO, "RoomMediaScanner", String.format("Room #%d %s deleted", room.id, room.name))
                    }
                    withContext(Dispatchers.Main) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }

        } else {

            submitRoomButton.setText(R.string.setupRoom_createButton)
            deleteRoomButton.setText(R.string.setupRoom_cancelButton)

            submitRoomButton.setOnClickListener {
                val roomName = roomNameEditText.text.toString()
                if (roomName.isNotEmpty()) {

                    db = AppDatabaseSingleton.getDatabase(applicationContext)

                    lifecycleScope.launch {
                        val newRoom = Room(
                            id = db.roomDao().getHighestId()?.plus(1) ?: 0,
                            name = roomName
                        )
                        withContext(Dispatchers.IO) {
                            val roomIdLong = db.roomDao().insert(newRoom)
                            val roomId = roomIdLong.toInt()
                            Log.println(
                                Log.INFO, "RoomMediaScanner", String.format(
                                    "new Room #%d %s",
                                    roomId,
                                    roomName
                                )
                            )
                        }
                        withContext(Dispatchers.Main) {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }
                } else {
                    roomNameEditText.error = "Room name cannot be empty"
                }
            }

            deleteRoomButton.setOnClickListener {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }
}