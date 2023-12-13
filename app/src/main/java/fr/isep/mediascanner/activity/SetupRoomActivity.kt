package fr.isep.mediascanner.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.isep.mediascanner.R
import fr.isep.mediascanner.database.AppDatabase
import fr.isep.mediascanner.database.AppDatabaseSingleton
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
        val submitRoomButton: Button = findViewById(R.id.submitRoomButton)

        submitRoomButton.setOnClickListener {
            val roomName = roomNameEditText.text.toString()
            if (roomName.isNotEmpty()) {
                
                db = AppDatabaseSingleton.getDatabase(applicationContext)

                val newRoom = Room(
                    id = 0,
                    name = roomName
                )

                lifecycleScope.launch {
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
                // Show an error message if the room name is empty
                roomNameEditText.error = "Room name cannot be empty"
            }
        }
    }
}