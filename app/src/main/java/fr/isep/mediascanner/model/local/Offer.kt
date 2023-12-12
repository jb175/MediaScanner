package fr.isep.mediascanner.model.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(
    entity = Product::class,
    parentColumns = ["id"],
    childColumns = ["productId"],
    onDelete = ForeignKey.CASCADE
)])
data class Offer(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val productId: Int, // Foreign key
    val merchant: String?,
    val domain: String?,
    val title: String?,
    val currency: String?,
    val list_price: String?, // Consider changing this to Double if it's a numeric value
    val price: Double?,
    val shipping: String?,
    val condition: String?,
    val availability: String?,
    val link: String?,
    val updated_t: Double?
)