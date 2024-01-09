package fr.isep.mediascanner.dao.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.isep.mediascanner.model.local.Offer

@Dao
interface OfferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(offer: Offer)

    @Query("DELETE FROM Offer WHERE productId = :productId")
    suspend fun deleteOffersForProduct(productId: Int)

    @Query("SELECT * FROM Offer")
    suspend fun getAll(): List<Offer>

    @Query("SELECT MAX(id) FROM Offer")
    suspend fun getHighestId(): Int?
}