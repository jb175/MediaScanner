package fr.isep.mediascanner.activity.productDetails

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.IntentCompat
import fr.isep.mediascanner.R
import fr.isep.mediascanner.model.local.Product

class OtherAccountProductDetailsActivity : BaseProductDetailsActivity() {

    private lateinit var actionBar : LinearLayout
    override fun initProduct(): Product {
        return IntentCompat.getParcelableExtra(intent, "PRODUCT", Product::class.java)!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar = findViewById(R.id.actionBar)
        actionBar.visibility = View.GONE
    }
}