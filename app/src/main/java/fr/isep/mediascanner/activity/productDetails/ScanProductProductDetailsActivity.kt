package fr.isep.mediascanner.activity.productDetails

import android.content.res.ColorStateList
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import fr.isep.mediascanner.R
import fr.isep.mediascanner.model.api.ProductItem
import fr.isep.mediascanner.model.local.Offer
import fr.isep.mediascanner.model.local.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanProductProductDetailsActivity : LocalBaseProductDetailsActivity() {

    private lateinit var productItem : ProductItem
    override fun afterOnCreate() {
        // Handle the button click if productItem is not null
        buttonAddToRoom.setOnClickListener {
            if (productItem.title != null && productItem.category != null) {
                if (productItem.category!!.contains("Media")) {
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
                                images = productItem.images
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
                                val offer = Offer(
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
    }

    override fun initProduct(): Product {
        productItem = IntentCompat.getParcelableExtra(intent, "PRODUCT_ITEM", ProductItem::class.java)!!
        return Product(
            id = 0, // Use the unique ID
            title = productItem.title,
            roomId = 0,
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
            images = productItem.images
        )
    }
}