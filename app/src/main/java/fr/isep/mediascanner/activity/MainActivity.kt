package fr.isep.mediascanner.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.zxing.integration.android.IntentIntegrator
import fr.isep.mediascanner.R
import fr.isep.mediascanner.fragment.AccountFragment
import fr.isep.mediascanner.fragment.SavedMediaFragment
import fr.isep.mediascanner.fragment.ScanFragment

class MainActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_REQUEST_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ScanFragment())
            .commit()

        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_scan -> ScanFragment()
                R.id.nav_saved_media -> SavedMediaFragment()
                R.id.nav_account -> AccountFragment()
                else -> null
            }

            if (fragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
            }

            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (fragment is ScanFragment) {
                    fragment.startBarcodeScanner()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val scannedData = result.contents
                Log.println(Log.INFO, "ScanResult", scannedData)
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (fragment is ScanFragment) {
                    fragment.requestProductDetails(scannedData)
                }
            }
        }
    }

}
