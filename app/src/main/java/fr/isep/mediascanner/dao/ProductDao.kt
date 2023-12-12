package fr.isep.mediascanner.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.isep.mediascanner.model.local.Product

@Dao
interface ProductDao {
    @Query("SELECT * FROM Product")
    suspend fun getAll(): List<Product>

    @Query("SELECT * FROM Product WHERE roomId = :roomId")
    suspend fun getProductsForRoom(roomId: Int): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(productItem: Product): Long

    @Delete
    suspend fun delete(productItem: Product)
}