package fr.isep.mediascanner.model.api

data class ProductResponse(
    val code: String?,
    val total: Double?,
    val items: List<ProductItem>?
)