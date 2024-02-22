package com.example.tfg.common

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/*
* Manages the indexes of the different cells
* */
data class Board private constructor(
    val numColumns: Int,
    val numRows: Int,
    val cells: SnapshotStateList<Cell>, // numColumns * numRows
) {

    fun clone(): Board {
        val newCells = mutableStateListOf<Cell>()
        newCells.addAll(cells)
        return this.copy(cells = newCells)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Board

        return cells == other.cells
    }

    override fun hashCode(): Int {
        return cells.hashCode()
    }

    companion object {
        fun create(numColumns: Int, numRows: Int, cellValues: IntArray) : Board{
            require(numColumns*numRows == cellValues.size) {
                "Array must be of size $numColumns * $numRows = ${numColumns * numRows}"
            }

            val cells = mutableStateListOf<Cell>()
            cells.addAll(Array(size = numColumns*numRows, init = { Cell.create(cellValues[it]) }))

            return Board(
                numColumns = numColumns,
                numRows = numRows,
                cells = cells,
            )
        }
    }

}