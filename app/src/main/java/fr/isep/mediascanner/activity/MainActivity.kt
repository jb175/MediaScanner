package fr.isep.mediascanner.activity

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
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
import fr.isep.mediascanner.fragment.SavedMediaFragment
import fr.isep.mediascanner.fragment.ScanFragment

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabaseSingleton.getDatabase(applicationContext)

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
                        R.id.nav_saved_media -> SavedMediaFragment()
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
    }

    fun refreshSavedMediaFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, SavedMediaFragment())
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
}
