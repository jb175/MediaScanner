package fr.isep.mediascanner.dao.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import fr.isep.mediascanner.model.local.Product

@Dao
interface ProductDao {
    @Query("SELECT * FROM Product")
    suspend fun getAll(): List<Product>

    @Query("SELECT * FROM Product WHERE roomId = :roomId")
    suspend fun getProductsForRoom(roomId: Int): List<Product>

    @Query("SELECT * FROM Product WHERE id = :productId")
    suspend fun getProductById(productId: Int): Product

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(productItem: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(productItem: Product)

    @Query("DELETE FROM Product WHERE roomId = :roomId")
    suspend fun deleteProductsInRoom(roomId: Int)
}