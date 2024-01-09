package fr.isep.mediascanner.model.local

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Product(
    @PrimaryKey val id: Int,
    val title: String?,
    var roomId: Int,
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
    val images: List<String>?,
) : Parcelable {
    constructor() : this(0, null, 0, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.createStringArrayList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeInt(roomId)
        parcel.writeString(ean)
        parcel.writeString(upc)
        parcel.writeString(gtin)
        parcel.writeString(asin)
        parcel.writeString(description)
        parcel.writeString(isbn)
        parcel.writeString(publisher)
        parcel.writeString(brand)
        parcel.writeString(model)
        parcel.writeString(dimension)
        parcel.writeString(weight)
        parcel.writeString(category)
        parcel.writeString(currency)
        parcel.writeValue(lowest_recorded_price)
        parcel.writeValue(highest_recorded_price)
        parcel.writeStringList(images)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }
}
