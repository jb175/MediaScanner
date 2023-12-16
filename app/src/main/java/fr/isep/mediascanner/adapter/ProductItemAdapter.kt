package fr.isep.mediascanner.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import fr.isep.mediascanner.R
import fr.isep.mediascanner.activity.ProductDetailsActivity
import fr.isep.mediascanner.model.local.Product
import fr.isep.mediascanner.activity.MainActivity
import kotlinx.coroutines.launch

class ProductItemAdapter(private val products: List<Product>,  private val scope: LifecycleCoroutineScope) : RecyclerView.Adapter<ProductItemAdapter.ProductViewHolder>() {

    private lateinit var context: Context
    
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
        val productIconImageView: ImageView = itemView.findViewById(R.id.productIconImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_product, parent, false)
        context = parent.context
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.productNameTextView.text = product.title

        if (product.category != null) {
            when {
                product.category.contains("Books") -> holder.productIconImageView.setImageResource(R.drawable.baseline_menu_book_24)
                product.category.contains("Music & Sound") -> holder.productIconImageView.setImageResource(R.drawable.baseline_music_note_24)
            }
        } else {
            holder.productIconImageView.setImageResource(R.drawable.baseline_error_24)
        }

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

    override fun getItemCount() = products.size
}
