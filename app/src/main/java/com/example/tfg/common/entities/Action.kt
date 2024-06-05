package com.example.tfg.common.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["moveId", "cellIndex"],
    foreignKeys = [
        ForeignKey(
            entity = Move::class,
            parentColumns = ["moveId"],
            childColumns = ["moveId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Action(
    @Embedded(prefix = "new_")
    val newCell: Cell,
    @Embedded(prefix = "previous_")
    val previousCell: Cell,
    val cellIndex: Int,
    val moveId: Long
)
