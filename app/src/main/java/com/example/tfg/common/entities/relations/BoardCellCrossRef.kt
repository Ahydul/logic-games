package com.example.tfg.common.entities.relations

import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.tfg.common.entities.Board
import com.example.tfg.common.entities.Cell

@Entity(
    primaryKeys = ["boardId", "cellId"],
    foreignKeys = [
        ForeignKey(entity = Board::class, parentColumns = ["boardId"], childColumns = ["boardId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Cell::class, parentColumns = ["cellId"], childColumns = ["cellId"], onDelete = ForeignKey.CASCADE)
    ])
data class BoardCellCrossRef(
    val boardId: Long,
    val cellId: Long,
    val cellPosition: Int
)

