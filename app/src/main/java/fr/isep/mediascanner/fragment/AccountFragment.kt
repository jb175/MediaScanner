package fr.isep.mediascanner.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import fr.isep.mediascanner.R
import fr.isep.mediascanner.activity.ui.login.LoginActivity
import kotlin.io.print

class AccountFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            val intent = Intent(context, LoginActivity::class.java)

            this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    //currentUser = auth.currentUser
                    Log.println(Log.INFO, "MediaScannerAccount", "User logged in ${auth.currentUser}")
                }
            }.launch(intent)
        } else {
            currentUser = auth.currentUser!!
        }
    }
}