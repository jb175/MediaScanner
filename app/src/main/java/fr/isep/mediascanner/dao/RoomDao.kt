package fr.isep.mediascanner.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import fr.isep.mediascanner.model.local.Room

@Dao
interface RoomDao {
    @Query("SELECT * FROM Room")
    suspend fun getAll(): List<Room>

    @Query("SELECT * FROM Room WHERE id = :id")
    suspend fun getById(id: Int): Room?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(room: Room): Long

    @Delete
    suspend fun delete(room: Room)
}