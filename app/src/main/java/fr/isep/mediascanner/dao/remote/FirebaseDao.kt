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

class FirebaseDao(private val context: Context) {
    private val firebaseDatabase = FirebaseSingleton.getDatabaseInstance()

    private fun uploadUserToFirebase(user: FirebaseUser) {
        val reference = firebaseDatabase.getReference("users").child(user.uid)

        val userData = mapOf(
            "email" to user.email,
        )

        reference.updateChildren(userData)
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error uploading user", exception)
            }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun uploadRoomsToFirebase(userId: String) {
        val roomDao = AppDatabaseSingleton.getDatabase(context).roomDao()
        GlobalScope.launch {
            val rooms = roomDao.getAll()
            val reference = firebaseDatabase.getReference("users").child(userId).child("rooms")

            for (room in rooms) {
                reference.child(room.id.toString()).setValue(room)
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
                reference.child(product.id.toString()).setValue(product)
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "Failed to upload product", exception)
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
                reference.child(offer.id.toString()).setValue(offer)
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "Failed to upload offer", exception)
                }
            }
        }
    }

    private fun uploadAllDataToFirebase() {
        val firebaseUser = FirebaseSingleton.getAuthInstance().currentUser
        if (firebaseUser != null) {
            uploadUserToFirebase(firebaseUser)
            uploadRoomsToFirebase(firebaseUser.uid)
            uploadProductsToFirebase(firebaseUser.uid)
            uploadOffersToFirebase(firebaseUser.uid)
        }
    }

    private suspend fun isLocalDataEmpty(): Boolean {
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
                                    // Set the roomId of the product to the id of the room
                                    product.roomId = room.id
                                    productDao.insert(product)
                                    Log.i("FireDownload", "product $product")

                                    val offersSnapshot = productSnapshot.child("offers")
                                    for (offerSnapshot in offersSnapshot.children) {
                                        val offer = offerSnapshot.getValue(Offer::class.java)
                                        if (offer != null) {
                                            // Create a new Offer with the desired id and other properties copied from the original Offer
                                            val newOffer = offerSnapshot.key?.let {
                                                Offer(
                                                    id = it.toInt(), // Use the snapshot key as the id
                                                    productId = product.id,
                                                    // Copy other properties from the original Offer
                                                    title = offer.title,
                                                    price = offer.price,
                                                    // Add other properties as needed...
                                                )
                                            }
                                            if (newOffer != null)
                                                offerDao.insert(newOffer)
                                            Log.i("FireDownload", "offer $newOffer")
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
    fun synchronizeDataWithFirebase() {
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