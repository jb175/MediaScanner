package fr.isep.mediascanner.model.convertion

import fr.isep.mediascanner.database.AppDatabase
import fr.isep.mediascanner.model.api.ProductItem
import fr.isep.mediascanner.model.api.ProductOffer
import fr.isep.mediascanner.model.local.Product

suspend fun convertLocalProductToApiProduct(product: Product, db: AppDatabase): ProductItem {
    // Fetch offers from the database
    val offers = db.offerDao().getOffersForProduct(product.id)

    // Map each Offer to a ProductOffer
    val productOffers = offers.map { offer ->
        ProductOffer(
            merchant = offer.merchant,
            domain = offer.domain,
            title = offer.title,
            currency = offer.currency,
            list_price = offer.list_price,
            price = offer.price,
            shipping = offer.shipping,
            condition = offer.condition,
            availability = offer.availability,
            link = offer.link,
            updated_t = offer.updated_t
        )
    }

    return ProductItem(
        ean = product.ean,
        title = product.title,
        upc = product.upc,
        gtin = product.gtin,
        asin = product.asin,
        description = product.description,
        isbn = product.isbn,
        publisher = product.publisher,
        brand = product.brand,
        model = product.model,
        dimension = product.dimension,
        weight = product.weight,
        category = product.category,
        currency = product.currency,
        lowest_recorded_price = product.lowest_recorded_price,
        highest_recorded_price = product.highest_recorded_price,
        images = product.images?.let { 
            if (it != "") {
                listOf(it)
            } else {
                emptyList()
            }
        } ?: emptyList(),
        offers = productOffers // You'll need to fetch or convert offers separately
    )
}
