package fr.isep.mediascanner.database.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FirebaseSingleton {
    companion object {
        @Volatile
        private var AUTH_INSTANCE: FirebaseAuth? = null
        @Volatile
        private var DATABASE_INSTANCE: FirebaseDatabase? = null

        fun getAuthInstance(): FirebaseAuth {
            val tempInstance = AUTH_INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = FirebaseAuth.getInstance()
                AUTH_INSTANCE = instance
                return instance
            }
        }

        fun getDatabaseInstance(): FirebaseDatabase {
            val tempInstance = DATABASE_INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = FirebaseDatabase.getInstance("https://mediascanner-81c4b-default-rtdb.europe-west1.firebasedatabase.app")
                DATABASE_INSTANCE = instance
                return instance
            }
        }
    }
}