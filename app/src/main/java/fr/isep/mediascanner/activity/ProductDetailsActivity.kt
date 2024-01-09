package fr.isep.mediascanner.activity

import android.app.Activity
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat.getParcelableExtra
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import fr.isep.mediascanner.R
import fr.isep.mediascanner.database.local.AppDatabase
import fr.isep.mediascanner.database.local.AppDatabaseSingleton
import fr.isep.mediascanner.model.api.ProductItem
import fr.isep.mediascanner.model.local.Product
import fr.isep.mediascanner.model.local.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductDetailsActivity : AppCompatActivity() {

    private lateinit var rooms: List<Room>
    private lateinit var db: AppDatabase
    private var imageURL = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)
        
        db = AppDatabaseSingleton.getDatabase(applicationContext)

        // Initialize the Spinner and Button
        val spinnerRooms = findViewById<Spinner>(R.id.spinnerRooms)
        val buttonAddToRoom = findViewById<Button>(R.id.buttonAddToRoom)

        val productItem = getParcelableExtra(intent, "PRODUCT_ITEM", ProductItem::class.java)
        val product = getParcelableExtra(intent, "PRODUCT", Product::class.java)

        // Load the rooms from the database
        lifecycleScope.launch {
            rooms = withContext(Dispatchers.IO) { db.roomDao().getAll() }

            // If no rooms exist, create a default one
            if (rooms.isEmpty()) {
                var defaultRoom = Room(id = 0, name = "Default")
                withContext(Dispatchers.IO) {
                    val roomIdLong = db.roomDao().insert(defaultRoom)
                    val roomId = roomIdLong.toInt()
                    defaultRoom = db.roomDao().getById(roomId)!!
                    Log.println(Log.INFO, "RoomMediaScanner", String.format("new Default Room %d", roomId))
                }
                rooms = listOf(defaultRoom)
            }

            // Populate the Spinner with the room names
            val adapter = ArrayAdapter(this@ProductDetailsActivity, android.R.layout.simple_spinner_item, rooms.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerRooms.adapter = adapter


            if (productItem != null) {
                spinnerRooms.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }
            } else if (product != null) {
                //set spinner to the good value
                val specificRoomIndex = rooms.indexOfFirst { it.id == product.roomId }
                spinnerRooms.setSelection(specificRoomIndex)

                spinnerRooms.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedRoom = rooms[spinnerRooms.selectedItemPosition]
                        if (selectedRoom.id != product.roomId) {
                            lifecycleScope.launch {
                                withContext(Dispatchers.IO) {

                                    product.roomId = selectedRoom.id
                                    db.productDao().update(product)
                                    Log.println(
                                        Log.INFO,
                                        "RoomMediaScanner",
                                        String.format(
                                            "Product updated #%d %s from room %d to room %d",
                                            product.id,
                                            product.title,
                                            product.roomId,
                                            selectedRoom.id
                                        )
                                    )
                                }
                                withContext(Dispatchers.Main) {
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Another interface callback
                    }
                }
            }
        }

        if (productItem != null) {
            val titleTextView = findViewById<TextView>(R.id.textViewTitle)
            val descriptionTextView = findViewById<TextView>(R.id.textViewDescription)
            val brandTextView = findViewById<TextView>(R.id.textViewBrand)
            val isbnTextView = findViewById<TextView>(R.id.textViewISBN)
            val publisherTextView = findViewById<TextView>(R.id.textViewPublisher)
            val categoryTextView = findViewById<TextView>(R.id.textViewCategory)

            titleTextView.text = productItem.title  ?: getString(R.string.product_details_unknown)
            descriptionTextView.text = productItem.description  ?: getString(R.string.product_details_unknown)
            brandTextView.text = String.format(getString(R.string.product_details_brand), (productItem.brand ?: getString(R.string.product_details_unknown)))
            isbnTextView.text = String.format(getString(R.string.product_details_isbn), (productItem.isbn ?: getString(R.string.product_details_unknown)))
            publisherTextView.text = String.format(getString(R.string.product_details_editor), (productItem.publisher ?: getString(R.string.product_details_unknown)))
            categoryTextView.text = String.format(getString(R.string.product_details_category), (productItem.category ?: getString(R.string.product_details_unknown)))

            if (!productItem.images.isNullOrEmpty()) {
                val imageView = findViewById<ImageView>(R.id.imageViewProduct)
                for (url in productItem.images) {
                    Picasso.get().load(url).fetch(object : Callback {
                        override fun onSuccess() {
                            Picasso.get().load(url).into(imageView)
                            imageURL = url
                        }

                        override fun onError(e: Exception?) {
                            // Do nothing
                        }
                    })
                }
            }
            

            // Handle the button click if productItem is not null
            buttonAddToRoom.setOnClickListener {
                if (productItem.title != null && productItem.category != null) {
                    if (productItem.category.contains("Media")) {
                        val selectedRoom = rooms[spinnerRooms.selectedItemPosition]
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                // Get the highest product ID currently in the database
                                val highestProductId = db.productDao().getHighestId()
                                // Assign the new product a unique ID
                                val newProductId = if (highestProductId != null) highestProductId + 1 else 1
                        
                                val productDB = Product(
                                    id = newProductId, // Use the unique ID
                                    title = productItem.title,
                                    roomId = selectedRoom.id,
                                    ean = productItem.ean,
                                    upc = productItem.upc,
                                    gtin = productItem.gtin,
                                    asin = productItem.asin,
                                    description = productItem.description,
                                    isbn = productItem.isbn,
                                    publisher = productItem.publisher,
                                    brand = productItem.brand,
                                    model = productItem.model,
                                    dimension = productItem.dimension,
                                    weight = productItem.weight,
                                    category = productItem.category,
                                    currency = productItem.currency,
                                    lowest_recorded_price = productItem.lowest_recorded_price,
                                    highest_recorded_price = productItem.highest_recorded_price,
                                    images = imageURL
                                )
                                val productIdLong = db.productDao().insert(productDB)
                                val productId = productIdLong.toInt()
                                Log.println(
                                    Log.INFO,
                                    "RoomMediaScanner",
                                    String.format(
                                        "new Product #%d %s place on room %d",
                                        productId,
                                        productItem.title,
                                        selectedRoom.id
                                    )
                                )

                                // Insert offers
                                productItem.offers?.forEach { productOffer ->
                                    val offer = fr.isep.mediascanner.model.local.Offer(
                                        id = 0,
                                        productId = productId,
                                        merchant = productOffer.merchant,
                                        domain = productOffer.domain,
                                        title = productOffer.title,
                                        currency = productOffer.currency,
                                        list_price = productOffer.list_price,
                                        price = productOffer.price,
                                        shipping = productOffer.shipping,
                                        condition = productOffer.condition,
                                        availability = productOffer.availability,
                                        link = productOffer.link,
                                        updated_t = productOffer.updated_t
                                    )
                                    db.offerDao().insert(offer)
                                }
                            }
                            withContext(Dispatchers.Main) {
                                finish()
                            }
                        }
                    } else {
                        Log.println(Log.WARN, "ScanResult", "Not a media")

                        val toast = Toast.makeText(this.applicationContext, R.string.productDetails_toast_notAMedia, Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.TOP or Gravity.END, 0, 0)
                        toast.show()
                    }
                }
            }
            buttonAddToRoom.setText(R.string.productDetails_addToRoom)
            buttonAddToRoom.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this.applicationContext, R.color.colorTrueButton))

        } else if(product != null) {

            val titleTextView = findViewById<TextView>(R.id.textViewTitle)
            val descriptionTextView = findViewById<TextView>(R.id.textViewDescription)
            val brandTextView = findViewById<TextView>(R.id.textViewBrand)
            val isbnTextView = findViewById<TextView>(R.id.textViewISBN)
            val publisherTextView = findViewById<TextView>(R.id.textViewPublisher)
            val categoryTextView = findViewById<TextView>(R.id.textViewCategory)

            titleTextView.text = product.title  ?: getString(R.string.product_details_unknown)
            descriptionTextView.text = product.description  ?: getString(R.string.product_details_unknown)
            brandTextView.text = String.format(getString(R.string.product_details_brand), (product.brand ?: getString(R.string.product_details_unknown)))
            isbnTextView.text = String.format(getString(R.string.product_details_isbn), (product.isbn ?: getString(R.string.product_details_unknown)))
            publisherTextView.text = String.format(getString(R.string.product_details_editor), (product.publisher ?: getString(R.string.product_details_unknown)))
            categoryTextView.text = String.format(getString(R.string.product_details_category), (product.category ?: getString(R.string.product_details_unknown)))

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

            // Handle the button click if productItem is not null
            buttonAddToRoom.setOnClickListener {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        db.offerDao().deleteOffersForProduct(product.id)
                        db.productDao().delete(product)
                        Log.println(Log.INFO, "RoomMediaScanner", String.format("Product deleted #%d %s from room %d", product.id, product.title, product.roomId))

                    }
                    withContext(Dispatchers.Main) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
            buttonAddToRoom.setText(R.string.productDetails_delete)
            buttonAddToRoom.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this.applicationContext, R.color.colorFalseButton))
        }
    }
}
