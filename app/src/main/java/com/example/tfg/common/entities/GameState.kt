package com.example.tfg.common.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GameState(
    @PrimaryKey(autoGenerate = true)
    val gameStateId: Long = 0,
    var pointer: Int = -1,
    var gameId: Long, // Foreign key referencing Game
)
