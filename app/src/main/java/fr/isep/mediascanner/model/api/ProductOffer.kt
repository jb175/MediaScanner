package fr.isep.mediascanner.model.api

import android.os.Parcelable
import android.os.Parcel

data class ProductOffer(
    val merchant: String?,
    val domain: String?,
    val title: String?,
    val currency: String?,
    val list_price: String?,//Double?,
    val price: Double?,
    val shipping: String?,
    val condition: String?,
    val availability: String?,
    val link: String?,
    val updated_t: Double?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(merchant)
        parcel.writeString(domain)
        parcel.writeString(title)
        parcel.writeString(currency)
        parcel.writeString(list_price)
        parcel.writeValue(price)
        parcel.writeString(shipping)
        parcel.writeString(condition)
        parcel.writeString(availability)
        parcel.writeString(link)
        parcel.writeValue(updated_t)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductOffer> {
        override fun createFromParcel(parcel: Parcel): ProductOffer {
            return ProductOffer(parcel)
        }

        override fun newArray(size: Int): Array<ProductOffer?> {
            return arrayOfNulls(size)
        }
    }

}