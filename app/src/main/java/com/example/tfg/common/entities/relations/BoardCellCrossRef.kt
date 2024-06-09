package com.example.tfg.common.entities.relations

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.tfg.common.entities.Board
import com.example.tfg.common.entities.Cell

@Entity(
    primaryKeys = ["boardId", "cellId"],
    foreignKeys = [
        ForeignKey(entity = Board::class, parentColumns = ["boardId"], childColumns = ["boardId"], onDelete = ForeignKey.NO_ACTION),
        ForeignKey(entity = Cell::class, parentColumns = ["cellId"], childColumns = ["cellId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["cellId"])]
)
data class BoardCellCrossRef(
    val boardId: Long,
    val cellId: Long,
    val cellPosition: Int
)

