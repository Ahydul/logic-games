package com.example.tfg.common.entities.relations

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Junction
import androidx.room.Relation
import com.example.tfg.common.entities.Board
import com.example.tfg.common.entities.Cell

data class BoardWithCells(
    @Embedded val board: Board,
    @Relation(
        parentColumn = "boardId",
        entityColumn = "cellId",
        associateBy = Junction(BoardCellCrossRef::class)
    )
    val cells: List<Cell>
)

@Entity(
    primaryKeys = ["boardId", "cellId"],
    foreignKeys = [
        ForeignKey(entity = Board::class, parentColumns = ["boardId"], childColumns = ["boardId"]),
        ForeignKey(entity = Cell::class, parentColumns = ["cellId"], childColumns = ["cellId"])
    ])
data class BoardCellCrossRef(
    val boardId: Long,
    val cellId: Long,
    val cellPosition: Int
)

