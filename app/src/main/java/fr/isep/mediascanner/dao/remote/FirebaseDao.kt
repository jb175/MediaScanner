package fr.isep.mediascanner.dao.remote

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import fr.isep.mediascanner.database.AppDatabaseSingleton
import fr.isep.mediascanner.database.remote.FirebaseSingleton
import fr.isep.mediascanner.model.local.Product
import fr.isep.mediascanner.model.local.Room
import fr.isep.mediascanner.model.local.Offer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseDao(private val context: Context) {
    private val firebaseDatabase = FirebaseSingleton.getDatabaseInstance()

    @OptIn(DelicateCoroutinesApi::class)
    fun uploadUserToFirebase(user: FirebaseUser) {
        val reference = firebaseDatabase.getReference("users").child(user.uid)

        val userData = mapOf(
            "email" to user.email,
            // Add other user data here
        )

        reference.updateChildren(userData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("Firebase", "Successfully uploaded user ${user.uid}")
                } else {
                    Log.e("Firebase", "Failed to upload user", task.exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error uploading user", exception)
            }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun uploadRoomsToFirebase(userId: String) {
        val roomDao = AppDatabaseSingleton.getDatabase(context).roomDao()
        GlobalScope.launch {
            Log.i("Firebase", "Starting to upload rooms for user $userId")

            val rooms = roomDao.getAll()
            val reference = firebaseDatabase.getReference("users").child(userId).child("rooms")

            for (room in rooms) {
                Log.i("Firebase", "Uploading room ${room.id}")
                reference.child(room.id.toString()).setValue(room)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i("Firebase", "Successfully uploaded room ${room.id}")
                    } else {
                        Log.e("Firebase", "Failed to upload room", task.exception)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "Error uploading room", exception)
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun uploadProductsToFirebase(userId: String) {
        GlobalScope.launch {
            val productDao = AppDatabaseSingleton.getDatabase(context).productDao()
            val products = productDao.getAll()

            for (product in products) {
                val reference = firebaseDatabase.getReference("users").child(userId).child("rooms").child(product.roomId.toString()).child("products")
                reference.child(product.id.toString()).setValue(product).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i("Firebase", "Successfully uploaded product ${product.id}")
                    } else {
                        Log.e("Firebase", "Failed to upload product", task.exception)
                    }
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun uploadOffersToFirebase(userId: String) {
        GlobalScope.launch {
            val offerDao = AppDatabaseSingleton.getDatabase(context).offerDao()
            val productDao = AppDatabaseSingleton.getDatabase(context).productDao()
            val offers = offerDao.getAll()

            for (offer in offers) {
                val product = productDao.getProductById(offer.productId)
                val roomId = product.roomId

                val reference = firebaseDatabase.getReference("users").child(userId).child("rooms").child(roomId.toString()).child("products").child(offer.productId.toString()).child("offers")
                reference.child(offer.id.toString()).setValue(offer).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i("Firebase", "Successfully uploaded offer ${offer.id}")
                    } else {
                        Log.e("Firebase", "Failed to upload offer", task.exception)
                    }
                }
            }
        }
    }

    fun getUidByEmail(email: String) {
        val reference = firebaseDatabase.getReference("emailsToUids")
        reference.child(email.replace(".", ",")).get()
            .addOnSuccessListener { snapshot ->
                val uid = snapshot.getValue(String::class.java)
                Log.i("Firebase", "Successfully getting UID for ${email}: ${uid}")

            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error getting UID", exception)
            }
    }

    fun checkIfDataUploaded(user: FirebaseUser, onSuccess: (Boolean) -> Unit, onFailure: (Exception) -> Unit) {
        val reference = firebaseDatabase.getReference("users").child(user.uid).child("dataUploaded")

        reference.get().addOnSuccessListener { dataSnapshot ->
            val dataUploaded = dataSnapshot.getValue(Boolean::class.java) ?: false
            onSuccess(dataUploaded)
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun uploadAllDataToFirebase() {
        val firebaseUser = FirebaseSingleton.getAuthInstance().currentUser
        if (firebaseUser != null) {
            uploadUserToFirebase(firebaseUser)
            uploadRoomsToFirebase(firebaseUser.uid)
            uploadProductsToFirebase(firebaseUser.uid)
            uploadOffersToFirebase(firebaseUser.uid)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun isLocalDataEmpty(): Boolean {
        val roomDao = AppDatabaseSingleton.getDatabase(context).roomDao()
        val rooms = roomDao.getAll()
        return rooms.isEmpty()
    }
    
    @OptIn(DelicateCoroutinesApi::class)
    fun downloadAllDataFromFirebase() {
        val firebaseUser = FirebaseSingleton.getAuthInstance().currentUser
        if (firebaseUser != null) {
            Log.i("Firebase", "Starting to download all data for user ${firebaseUser.uid}")

            val roomDao = AppDatabaseSingleton.getDatabase(context).roomDao()
            val productDao = AppDatabaseSingleton.getDatabase(context).productDao()
            val offerDao = AppDatabaseSingleton.getDatabase(context).offerDao()

            val reference = firebaseDatabase.getReference("users").child(firebaseUser.uid).child("rooms")
            reference.get().addOnSuccessListener { snapshot ->
                GlobalScope.launch {
                    for (roomSnapshot in snapshot.children) {
                        val room = roomSnapshot.getValue(Room::class.java)
                        if (room != null) {
                            roomDao.insert(room)
                            Log.i("FireDownload", "room $room")

                            val productsSnapshot = roomSnapshot.child("products")
                            for (productSnapshot in productsSnapshot.children) {
                                val product = productSnapshot.getValue(Product::class.java)
                                if (product != null) {
                                    productDao.insert(product)
                                    Log.i("FireDownload", "product $product")

                                    val offersSnapshot = productSnapshot.child("offers")
                                    for (offerSnapshot in offersSnapshot.children) {
                                        val offer = offerSnapshot.getValue(Offer::class.java)
                                        if (offer != null) {
                                            offerDao.insert(offer)
                                            Log.i("FireDownload", "offer $offer")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Log.i("Firebase", "Finished downloading all data for user ${firebaseUser.uid}")
            }.addOnFailureListener { exception ->
                Log.e("Firebase", "Failed to download all data for user ${firebaseUser.uid}", exception)
            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun syncronizeDataWithFirebase() {
        GlobalScope.launch {
            if (isLocalDataEmpty()) {
                Log.i("NetworkCallback", "Local data is empty, downloading data from Firebase")
                downloadAllDataFromFirebase()
            } else {
                Log.i("NetworkCallback", "Local data is not empty, uploading data to Firebase")
                uploadAllDataToFirebase()
            }
        }
    }
}