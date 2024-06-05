package com.example.tfg.common.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tfg.common.IdGenerator

@Entity
data class Move(
    @PrimaryKey
    val moveId: Long = IdGenerator.generateId("move"),
    val position: Int,
    val gameStateId: Long
)