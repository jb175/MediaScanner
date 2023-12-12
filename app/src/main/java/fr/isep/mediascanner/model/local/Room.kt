package fr.isep.mediascanner.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity data class Room(@PrimaryKey(autoGenerate = true) val id: Int, val name: String)
