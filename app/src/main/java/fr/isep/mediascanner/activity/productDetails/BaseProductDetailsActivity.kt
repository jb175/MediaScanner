package fr.isep.mediascanner.activity.productDetails

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import fr.isep.mediascanner.R
import fr.isep.mediascanner.model.local.Product

abstract class BaseProductDetailsActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var brandTextView: TextView
    private lateinit var isbnTextView: TextView
    private lateinit var publisherTextView: TextView
    private lateinit var categoryTextView: TextView
    private lateinit var imageView: ImageView

    protected var imageURL = ""
    protected lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        titleTextView = findViewById(R.id.textViewTitle)
        descriptionTextView = findViewById(R.id.textViewDescription)
        brandTextView = findViewById(R.id.textViewBrand)
        isbnTextView = findViewById(R.id.textViewISBN)
        publisherTextView = findViewById(R.id.textViewPublisher)
        categoryTextView = findViewById(R.id.textViewCategory)
        imageView = findViewById(R.id.imageViewProduct)

        product = initProduct()

        titleTextView.text = product.title  ?: getString(R.string.product_details_unknown)
        descriptionTextView.text = product.description  ?: getString(R.string.product_details_unknown)
        brandTextView.text = String.format(getString(R.string.product_details_brand), (product.brand ?: getString(R.string.product_details_unknown)))
        isbnTextView.text = String.format(getString(R.string.product_details_isbn), (product.isbn ?: getString(R.string.product_details_unknown)))
        publisherTextView.text = String.format(getString(R.string.product_details_editor), (product.publisher ?: getString(R.string.product_details_unknown)))
        categoryTextView.text = String.format(getString(R.string.product_details_category), (product.category ?: getString(R.string.product_details_unknown)))

        if (!product.images.isNullOrEmpty()) {
            Picasso.get().load(product.images).fetch(object : Callback {
                override fun onSuccess() {
                    Picasso.get().load(product.images).into(imageView)
                    imageURL = product.images!!
                }

                override fun onError(e: java.lang.Exception?) {
                    //do nothing
                }
            })
        }
    }

    protected abstract fun initProduct(): Product
}
