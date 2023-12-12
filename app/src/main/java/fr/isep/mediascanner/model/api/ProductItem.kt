package fr.isep.mediascanner.model.api

import android.os.Parcel
import android.os.Parcelable

data class ProductItem(
    val ean: String?,
    val title: String?,
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
    val offers: List<ProductOffer>?
) : Parcelable {
    constructor(parcel: Parcel) : this(
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
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.createStringArrayList(),
        parcel.createTypedArrayList(ProductOffer.CREATOR)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(ean)
        parcel.writeString(title)
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
        parcel.writeTypedList(offers)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductItem> {
        override fun createFromParcel(parcel: Parcel): ProductItem {
            return ProductItem(parcel)
        }

        override fun newArray(size: Int): Array<ProductItem?> {
            return arrayOfNulls(size)
        }
    }
}