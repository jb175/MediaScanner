package fr.isep.mediascanner.adapter.search

import android.content.Intent
import fr.isep.mediascanner.model.local.Product
import fr.isep.mediascanner.activity.MainActivity
import fr.isep.mediascanner.activity.productDetails.OtherAccountProductDetailsActivity

class ProductItemAdapter(private val products: List<Product>) : fr.isep.mediascanner.adapter.ProductItemAdapter(products) {

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val product = products[position]

        // Set the click listener
        holder.itemView.setOnClickListener {
            val intent = Intent(context, OtherAccountProductDetailsActivity::class.java).apply {
                putExtra("PRODUCT", product)
            }
        
            if (context is MainActivity) {
                (context as MainActivity).getSetupProductDetailsSearchResultLauncher().launch(intent)
            }
        }
    }
}