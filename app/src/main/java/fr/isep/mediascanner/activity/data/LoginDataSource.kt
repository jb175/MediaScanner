package fr.isep.mediascanner.activity.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import fr.isep.mediascanner.activity.data.model.LoggedInUser
import fr.isep.mediascanner.database.remote.FirebaseSingleton
import kotlinx.coroutines.tasks.await
import java.io.IOException

class LoginDataSource {

    private var auth: FirebaseAuth = Firebase.auth
    private var database =  FirebaseSingleton.getDatabaseInstance()

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        return try {
            val result = try {
                val signUpResult = auth.createUserWithEmailAndPassword(username, password).await()
                // New user has been created, store their email and uid in the database
                val reference = database.getReference("emailsToUids")
                reference.child(username.replace(".", ",")).setValue(signUpResult.user?.uid)
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Error storing email-uid combination", exception)
                    }
                signUpResult
            } catch (e: FirebaseAuthUserCollisionException) {
                // User already exists, just sign them in
                auth.signInWithEmailAndPassword(username, password).await()
            }
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = LoggedInUser(firebaseUser.uid, firebaseUser.displayName ?: "")
                Result.Success(user)
            } else {
                Result.Error(IOException("Error logging in"))
            }
        } catch (e: Exception) {
            Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        auth.signOut()
    }
}