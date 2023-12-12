package fr.isep.mediascanner.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import fr.isep.mediascanner.R
import fr.isep.mediascanner.activity.ProductDetailsActivity
import fr.isep.mediascanner.database.AppDatabase
import fr.isep.mediascanner.model.convertion.convertLocalProductToApiProduct
import fr.isep.mediascanner.model.local.Product
import kotlinx.coroutines.launch

class ProductItemAdapter(
        private val products: List<Product>,
        private val db: AppDatabase,
        private val scope: LifecycleCoroutineScope
) : RecyclerView.Adapter<ProductItemAdapter.ProductViewHolder>() {
    
    private val PRODUCT_DETAILS_REQUEST_CODE = 201

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.productNameTextView.text = product.title

        // Set the click listener
        holder.itemView.setOnClickListener {
            val context = it.context
            scope.launch {
                val intent =
                    Intent(context, ProductDetailsActivity::class.java).apply {
                        putExtra("PRODUCT", product)
                    }
                    if (context is Activity) {
                        context.startActivityForResult(intent, PRODUCT_DETAILS_REQUEST_CODE)
                    }
            }
        }
    }

    override fun getItemCount() = products.size
}
