package com.example.tfg.common.entities

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import com.example.tfg.common.utils.Coordinate
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "move")
data class Move(
    val coordinates: List<Coordinate>,
    @Embedded(prefix = "previous_cells_")
    val previousCells: List<Cell>,
    @Embedded(prefix = "new_cells_")
    val newCells: List<Cell>
) : Parcelable