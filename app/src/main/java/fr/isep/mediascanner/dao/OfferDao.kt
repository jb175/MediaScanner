package fr.isep.mediascanner.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.isep.mediascanner.model.local.Offer

@Dao
interface OfferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(offer: Offer)

    @Query("SELECT * FROM Offer WHERE productId = :productId")
    suspend fun getOffersForProduct(productId: Int): List<Offer>

    @Query("DELETE FROM Offer WHERE productId = :productId")
    suspend fun deleteOffersForProduct(productId: Int)
}