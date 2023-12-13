package fr.isep.mediascanner.activity

import android.app.Activity
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
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import fr.isep.mediascanner.R
import fr.isep.mediascanner.database.AppDatabase
import fr.isep.mediascanner.database.AppDatabaseSingleton
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


        // Récupération des données du produit depuis l'intent
        val productItem: ProductItem? = intent.getParcelableExtra("PRODUCT_ITEM")
        val product: Product? = intent.getParcelableExtra("PRODUCT")

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
            // Assignation des vues du layout
            val titleTextView = findViewById<TextView>(R.id.textViewTitle)
            val descriptionTextView = findViewById<TextView>(R.id.textViewDescription)
            val brandTextView = findViewById<TextView>(R.id.textViewBrand)
            val isbnTextView = findViewById<TextView>(R.id.textViewISBN)
            val publisherTextView = findViewById<TextView>(R.id.textViewPublisher)
            val categoryTextView = findViewById<TextView>(R.id.textViewCategory)

            // Affichage des détails du produit dans les vues
            titleTextView.text = productItem.title  ?: "Unknown"
            descriptionTextView.text = productItem.description  ?: "Unknown"
            brandTextView.text = String.format("Marque: %s", (productItem.brand ?: "Unknown"))
            isbnTextView.text = String.format("ISBN: %s", (productItem.isbn ?: "Unknown"))
            publisherTextView.text = String.format("Éditeur: %s", (productItem.publisher ?: "Unknown"))
            categoryTextView.text = String.format("Catégorie: %s", (productItem.category ?: "Unknown"))

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
                                val productDB = Product(
                                    id = 0,
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

                        val toast = Toast.makeText(this.applicationContext, "Not a media", Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.TOP or Gravity.END, 0, 0)
                        toast.show()
                    }
                }
            }
            buttonAddToRoom.text = "Add to Room"

        } else if(product != null) {

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
            buttonAddToRoom.text = "Delete from Room"

        }
    }
}
