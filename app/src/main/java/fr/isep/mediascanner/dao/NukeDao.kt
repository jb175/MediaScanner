package fr.isep.mediascanner.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface NukeDao {
    @Query("DELETE FROM Offer")
    fun nukeTableOffer()
    @Query("DELETE FROM Product")
    fun nukeTableProduct()
    @Query("DELETE FROM Room")
    fun nukeTableRoom()
}