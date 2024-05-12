package com.example.tfg.common

import android.os.Parcelable
import androidx.compose.runtime.mutableStateListOf
import kotlinx.parcelize.Parcelize

/*
* Manages the indexes of the different cells
* */
@Parcelize
data class Board private constructor(
    val numColumns: Int,
    val numRows: Int,
    val cells: MutableList<Cell>, // numColumns * numRows
) : Parcelable {

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
                "Array must be of size $numColumns * $numRows = ${numColumns * numRows}. Actual size = ${cellValues.size}"
            }

            val cells = mutableStateListOf<Cell>()
            cells.addAll(Array(size = numColumns*numRows, init = { Cell.create(cellValues[it]) }))

            return Board(
                numColumns = numColumns,
                numRows = numRows,
                cells = cells,
            )
        }

        fun exampleBoard(): Board {
            val cellValues = IntArray(size = 6*6, init = { 0 })
            cellValues[4] = 2
            cellValues[16] = 3
            cellValues[19] = 3
            cellValues[31] = 4

            return create(
                numColumns = 6,
                numRows = 6,
                cellValues = cellValues,
            )
        }

        fun emptyBoard(numColumns: Int, numRows: Int): Board {
            val cellValues = IntArray(size = numColumns*numRows, init = { 0 })
            return create(
                numColumns = numColumns,
                numRows = numRows,
                cellValues = cellValues,
            )
        }
    }
}