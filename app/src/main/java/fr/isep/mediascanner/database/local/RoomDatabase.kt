package fr.isep.mediascanner.database.local

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.isep.mediascanner.dao.local.OfferDao
import fr.isep.mediascanner.dao.local.ProductDao
import fr.isep.mediascanner.dao.local.RoomDao
import fr.isep.mediascanner.model.local.Offer
import fr.isep.mediascanner.model.local.Product
import fr.isep.mediascanner.model.local.Room

@Database(entities = [Room::class, Product::class, Offer::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao
    abstract fun productDao(): ProductDao
    abstract fun offerDao(): OfferDao
}