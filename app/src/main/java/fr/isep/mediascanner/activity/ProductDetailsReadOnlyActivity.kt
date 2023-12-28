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

    private var imageURL = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details_read_only)
        

        // Récupération des données du produit depuis l'intent
        val product = getParcelableExtra(intent, "PRODUCT", Product::class.java)

        if(product != null) {

            // Assignation des vues du layout
            val titleTextView = findViewById<TextView>(R.id.textViewTitle)
            val descriptionTextView = findViewById<TextView>(R.id.textViewDescription)
            val brandTextView = findViewById<TextView>(R.id.textViewBrand)
            val isbnTextView = findViewById<TextView>(R.id.textViewISBN)
            val publisherTextView = findViewById<TextView>(R.id.textViewPublisher)
            val categoryTextView = findViewById<TextView>(R.id.textViewCategory)

            // Affichage des détails du produit dans les vues
            titleTextView.text = product.title  ?: "Unknown"
            descriptionTextView.text = product.description  ?: "Unknown"
            brandTextView.text = String.format("Marque: %s", (product.brand ?: "Unknown"))
            isbnTextView.text = String.format("ISBN: %s", (product.isbn ?: "Unknown"))
            publisherTextView.text = String.format("Éditeur: %s", (product.publisher ?: "Unknown"))
            categoryTextView.text = String.format("Catégorie: %s", (product.category ?: "Unknown"))

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
