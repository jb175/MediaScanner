package fr.isep.mediascanner.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val title: String,
    val roomId: Int,
    val ean: String?,
    val upc: String?,
    val gtin: String?,
    val asin: String?,
    val description: String?,
    val isbn: String?,
    val publisher: String?,
    val brand: String?,
    val model: String?,
    val dimension: String?,
    val weight: String?,
    val category: String?,
    val currency: String?,
    val lowest_recorded_price: Double?,
    val highest_recorded_price: Double?,
    val images: String?,
)
