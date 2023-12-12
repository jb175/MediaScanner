package fr.isep.mediascanner.database

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.isep.mediascanner.dao.OfferDao
import fr.isep.mediascanner.dao.ProductDao
import fr.isep.mediascanner.dao.RoomDao
import fr.isep.mediascanner.model.local.Offer
import fr.isep.mediascanner.model.local.Product
import fr.isep.mediascanner.model.local.Room

@Database(entities = [Room::class, Product::class, Offer::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao
    abstract fun productDao(): ProductDao
    abstract fun offerDao(): OfferDao
}