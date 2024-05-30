package com.example.tfg.common.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Move(
    @PrimaryKey(autoGenerate = true)
    val moveId: Long = 0,
    val position: Int,
    val gameStateId: Long
)