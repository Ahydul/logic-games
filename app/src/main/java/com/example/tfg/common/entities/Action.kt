package com.example.tfg.common.entities

import androidx.room.Embedded
import androidx.room.Entity

@Entity(
    primaryKeys = ["moveId", "position"]
)
data class Action(
    @Embedded(prefix = "new_")
    val newCell: Cell,
    @Embedded(prefix = "previous_")
    val previousCell: Cell,
    val position: Int,
    val moveId: Long
)
