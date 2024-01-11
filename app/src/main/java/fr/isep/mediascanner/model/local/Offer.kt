package fr.isep.mediascanner.model.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Product::class,
        parentColumns = ["id"],
        childColumns = ["productId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("productId")]
)
data class Offer(
    @PrimaryKey val id: Int = 0,
    val productId: Int = 0,
    val merchant: String? = null,
    val domain: String? = null,
    val title: String? = null,
    val currency: String? = null,
    val list_price: String? = null,
    val price: Double? = null,
    val shipping: String? = null,
    val condition: String? = null,
    val availability: String? = null,
    val link: String? = null,
    val updated_t: Double? = null
)