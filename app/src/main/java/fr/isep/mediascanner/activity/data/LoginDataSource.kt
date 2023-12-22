package fr.isep.mediascanner.activity.data

import fr.isep.mediascanner.activity.data.model.LoggedInUser
import java.io.IOException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class LoginDataSource {

    private var auth: FirebaseAuth = Firebase.auth

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        return try {
            val result = try {
                auth.createUserWithEmailAndPassword(username, password).await()
            } catch (e: FirebaseAuthUserCollisionException) {
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