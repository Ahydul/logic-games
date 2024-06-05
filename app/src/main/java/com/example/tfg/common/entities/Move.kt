package com.example.tfg.common.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.tfg.common.IdGenerator

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = GameState::class,
            parentColumns = ["gameStateId"],
            childColumns = ["gameStateId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Move(
    @PrimaryKey
    val moveId: Long = IdGenerator.generateId("move"),
    val position: Int,
    val gameStateId: Long
)