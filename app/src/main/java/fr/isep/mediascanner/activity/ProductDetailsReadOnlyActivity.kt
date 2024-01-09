package fr.isep.mediascanner.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat.getParcelableExtra
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import fr.isep.mediascanner.R
import fr.isep.mediascanner.model.local.Product

class ProductDetailsReadOnlyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details_read_only)
        

        val product = getParcelableExtra(intent, "PRODUCT", Product::class.java)

        if(product != null) {
            val titleTextView = findViewById<TextView>(R.id.textViewTitle)
            val descriptionTextView = findViewById<TextView>(R.id.textViewDescription)
            val brandTextView = findViewById<TextView>(R.id.textViewBrand)
            val isbnTextView = findViewById<TextView>(R.id.textViewISBN)
            val publisherTextView = findViewById<TextView>(R.id.textViewPublisher)
            val categoryTextView = findViewById<TextView>(R.id.textViewCategory)

            titleTextView.text = product.title  ?: getString(R.string.product_details_unknow)
            descriptionTextView.text = product.description  ?: getString(R.string.product_details_unknow)
            brandTextView.text = String.format(getString(R.string.product_details_brand), (product.brand ?: getString(R.string.product_details_unknow)))
            isbnTextView.text = String.format(getString(R.string.product_details_isbn), (product.isbn ?: getString(R.string.product_details_unknow)))
            publisherTextView.text = String.format(getString(R.string.product_details_editor), (product.publisher ?: getString(R.string.product_details_unknow)))
            categoryTextView.text = String.format(getString(R.string.product_details_category), (product.category ?: getString(R.string.product_details_unknow)))

            if (!product.images.isNullOrEmpty()) {
                val imageView = findViewById<ImageView>(R.id.imageViewProduct)
                Picasso.get().load(product.images).fetch(object : Callback {
                    override fun onSuccess() {
                        Picasso.get().load(product.images).into(imageView)
                    }

                    override fun onError(e: java.lang.Exception?) {
                        // Do nothing
                    }
                })
            }
        }
    }
}
