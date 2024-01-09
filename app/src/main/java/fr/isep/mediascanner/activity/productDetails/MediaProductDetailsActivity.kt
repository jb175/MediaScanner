package fr.isep.mediascanner.activity.productDetails

import android.app.Activity
import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat.getParcelableExtra
import androidx.lifecycle.lifecycleScope
import fr.isep.mediascanner.R
import fr.isep.mediascanner.model.local.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediaProductDetailsActivity : LocalBaseProductDetailsActivity() {

    override fun afterOnCreate() {
        val specificRoomIndex = rooms.indexOfFirst { it.id == product.roomId }
        spinnerRooms.setSelection(specificRoomIndex)

        buttonAddToRoom.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.offerDao().deleteOffersForProduct(product.id)
                    db.productDao().delete(product)
                    Log.i("RoomMediaScanner", "Product deleted #${product.id} ${product.title} from room ${product.roomId}")

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

    override fun onSpinnerSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedRoom = rooms[position]
        if (selectedRoom.id != product.roomId) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    product.roomId = selectedRoom.id
                    db.productDao().update(product)
                    Log.i("RoomMediaScanner", "Product updated #${product.id} ${product.title} from room ${product.roomId} to room ${selectedRoom.id}")
                }
                withContext(Dispatchers.Main) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }

    override fun initProduct(): Product {
        return getParcelableExtra(intent, "PRODUCT", Product::class.java)!!
    }
}