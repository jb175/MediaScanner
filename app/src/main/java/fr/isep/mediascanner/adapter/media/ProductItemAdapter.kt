package fr.isep.mediascanner.adapter.media

import android.content.Intent
import androidx.lifecycle.LifecycleCoroutineScope
import fr.isep.mediascanner.activity.ProductDetailsActivity
import fr.isep.mediascanner.model.local.Product
import fr.isep.mediascanner.activity.MainActivity
import kotlinx.coroutines.launch

class ProductItemAdapter(private val products: List<Product>,  private val scope: LifecycleCoroutineScope) : fr.isep.mediascanner.adapter.ProductItemAdapter(products) {

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val product = products[position]

        // Set the click listener
        holder.itemView.setOnClickListener {
            scope.launch {
                val intent = Intent(context, ProductDetailsActivity::class.java).apply {
                    putExtra("PRODUCT", product)
                }
                if (context is MainActivity) {
                    (context as MainActivity).getSetupProductDetailsRefreshForActivityResult().launch(intent)
                }
            }
        }
    }
}
