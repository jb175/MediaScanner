package fr.isep.mediascanner.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import fr.isep.mediascanner.R
import fr.isep.mediascanner.RequestCodes
import fr.isep.mediascanner.database.local.AppDatabase
import fr.isep.mediascanner.database.local.AppDatabaseSingleton
import fr.isep.mediascanner.fragment.AccountFragment
import fr.isep.mediascanner.fragment.MediaFragment
import fr.isep.mediascanner.fragment.ScanFragment
import android.net.Network
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import fr.isep.mediascanner.dao.remote.FirebaseDao
import fr.isep.mediascanner.fragment.SearchFragment

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var firebaseDao: FirebaseDao
    lateinit var auth: FirebaseAuth

    private val setupProductDetailsResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            firebaseDao.synchronizeDataWithFirebase()
            switchFragment(MediaFragment())
        }
    }

    private val setupProductDetailsOtherAccountResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            switchFragment(AccountFragment())
        }
    }

    private val setupProductDetailsSearchResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //do nothing
        }
    }

    private val setupLoginResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            switchFragment(AccountFragment())
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.i("NetworkCallback", "Network is available")
            firebaseDao.synchronizeDataWithFirebase()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        FirebaseApp.initializeApp(this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())

        db = AppDatabaseSingleton.getDatabase(applicationContext)
        firebaseDao = FirebaseDao(applicationContext)
        auth = FirebaseAuth.getInstance()



        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set default fragment
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, ScanFragment())
                .commit()

        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment =
                    when (item.itemId) {
                        R.id.nav_scan -> ScanFragment()
                        R.id.nav_saved_media -> MediaFragment()
                        R.id.nav_search -> SearchFragment()
                        R.id.nav_account -> AccountFragment()
                        else -> null
                    }

            if (fragment != null) {
                val actualFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (actualFragment is ScanFragment) {
                    actualFragment.stopScan()
                }
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit()
            }

            true
        }

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val acceptedTerms = sharedPref.getBoolean("acceptedTerms", false)
        if (!acceptedTerms) {
            // Redirect to StartupActivity
            val intent = Intent(this, StartupActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == RequestCodes.CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (fragment is ScanFragment) {
                    fragment.startScan()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun getSetupProductDetailsResultLauncher() = setupProductDetailsResultLauncher
    fun getSetupProductDetailsOtherAccountResultLauncher() = setupProductDetailsOtherAccountResultLauncher

    fun getSetupProductDetailsSearchResultLauncher() = setupProductDetailsSearchResultLauncher

    fun getSetupLoginResultLauncher() = setupLoginResultLauncher

    private val fragmentMenuMap = mapOf(
        ScanFragment::class to R.id.nav_scan,
        MediaFragment::class to R.id.nav_saved_media,
        SearchFragment::class to R.id.nav_search,
        AccountFragment::class to R.id.nav_account
    )

    fun switchFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    
        val menuItemId = fragmentMenuMap[fragment::class]
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        if (menuItemId != null) {
            bottomNavigation.selectedItemId = menuItemId
        } else {
            Log.e("MainActivity", "No menu item ID found for fragment ${fragment::class}")
        }
    }
}
