package fr.isep.mediascanner.service

import fr.isep.mediascanner.model.api.ProductResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductService {
    @GET("prod/trial/lookup") fun getProductInfo(@Query("upc") upc: String): Call<ProductResponse>
}
