package fr.isep.mediascanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.isep.mediascanner.R
import fr.isep.mediascanner.model.local.Product

abstract class ProductItemAdapter(private val products: List<Product>) : RecyclerView.Adapter<ProductItemAdapter.ProductViewHolder>() {

    protected lateinit var context: Context
    
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
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
                product.category.contains("Books") -> holder.productNameTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_menu_book_24, 0, 0, 0)
                product.category.contains("Music & Sound") -> holder.productNameTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_music_note_24, 0, 0, 0)
            }
        } else {
            holder.productNameTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_error_24, 0, 0, 0)
        }
    }

    override fun getItemCount() = products.size
}
