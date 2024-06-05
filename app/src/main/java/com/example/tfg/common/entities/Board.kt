package com.example.tfg.common.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tfg.common.IdGenerator


/*
* Manages the indexes of the different cells
* */
@Entity
data class Board(
    @PrimaryKey
    val boardId: Long = generateId(),
    val numColumns: Int,
    val numRows: Int,
    val gameStateId: Long,
) {
    companion object {
        private fun generateId() = IdGenerator.generateId("board")

        fun create(from: Board, gameStateId: Long): Board {
            return from.copy(boardId = generateId(), gameStateId = gameStateId)
        }
    }
}