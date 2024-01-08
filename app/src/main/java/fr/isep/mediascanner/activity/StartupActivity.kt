package fr.isep.mediascanner.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import fr.isep.mediascanner.R

class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)

        val webView: WebView = findViewById(R.id.webView)
        webView.loadUrl("https://raw.githubusercontent.com/jb175/MediaScanner/main/privacy_policy_local.html")

        val acceptButton: Button = findViewById(R.id.acceptButton)
        acceptButton.setOnClickListener {
            // Save acceptance in SharedPreferences
            val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putBoolean("acceptedTerms", true)
                apply()
            }

            // Start MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val declineButton: Button = findViewById(R.id.declineButton)
        declineButton.setOnClickListener {
            // Close the app
            finishAffinity()
        }
    }
}