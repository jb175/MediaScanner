package fr.isep.mediascanner.activity

import android.Manifest
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import fr.isep.mediascanner.R
import fr.isep.mediascanner.RequestCodes
import fr.isep.mediascanner.database.AppDatabase
import fr.isep.mediascanner.database.AppDatabaseSingleton
import fr.isep.mediascanner.fragment.AccountFragment
import fr.isep.mediascanner.fragment.MediaFragment
import fr.isep.mediascanner.fragment.ScanFragment
import android.net.Network
import com.google.firebase.auth.FirebaseAuth
import fr.isep.mediascanner.dao.remote.FirebaseDao

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var firebaseDao: FirebaseDao
    lateinit var auth: FirebaseAuth

    private val scanContract = registerForActivityResult(ScanContract()) { result: ScanIntentResult? ->
        if (result==null || result.contents == null) {
            Log.println(Log.INFO, "ScanResult", "Cancelled scan")
        } else {
            Log.println(Log.INFO, "ScanResult", "Scanned: " + result.contents)
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (fragment is ScanFragment) {
                fragment.requestProductDetails(result.contents)
            }
        }
    }
    private val scanOptions = ScanOptions()
        .setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
        .setPrompt("Scan a barcode")
        .setCameraId(0)
        .setBeepEnabled(false)
        .setBarcodeImageEnabled(false)

    private val setupProductDetailsRefreshForActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            refreshSavedMediaFragment()
        }
    }

    private val setupProductDetailsReadOnlyRefreshForActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //TODO
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.i("NetworkCallback", "Network is available")
            firebaseDao.syncronizeDataWithFirebase()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                        R.id.nav_account -> AccountFragment()
                        else -> null
                    }

            if (fragment != null) {
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

    fun refreshSavedMediaFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, MediaFragment())
        transaction.commit()
    }

    fun refreshAccountFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, AccountFragment())
        transaction.commit()
    }


    //scan
    fun startScan() {
        if (checkCameraPermission()) {
            scanContract.launch(scanOptions)
        } else {
            requestCameraPermission()
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), RequestCodes.CAMERA_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == RequestCodes.CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanContract.launch(scanOptions)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun getSetupProductDetailsRefreshForActivityResult() = setupProductDetailsRefreshForActivityResult
    fun getsetupProductDetailsReadOnlyRefreshForActivityResult() = setupProductDetailsReadOnlyRefreshForActivityResult
}
