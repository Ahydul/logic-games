package com.example.tfg.common

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/*
* Manages the indexes of the different cells and the sections that may contain several cells
* */
data class Board private constructor(
    val numColumns: Int,
    val numRows: Int,
    val cells: SnapshotStateList<Cell>, // numColumns * numRows
    private val sections: IntArray // numColumns * numRows
) {


    private fun fromSameSection(index1: Int, index2: Int): Boolean{
        return sections[index1] == sections[index2]
    }

    private fun drawDivisorBetween(coordinate1: Coordinate, coordinate2: Coordinate): Boolean {
        val index: Int = coordinate1.toIndex(numRows = numRows, numColumns = numColumns)!!
        val index2: Int? = coordinate2.toIndex(numRows = numRows, numColumns = numColumns)

        return !(index2 == null || fromSameSection(index, index2))
    }

    fun drawDividerRight(coordinate: Coordinate): Boolean {
        return drawDivisorBetween(coordinate, coordinate.moveRight())
    }
    fun drawDividerDown(coordinate: Coordinate): Boolean {
        return drawDivisorBetween(coordinate, coordinate.moveDown())
    }
    fun drawDividerLeft(coordinate: Coordinate): Boolean {
        return drawDivisorBetween(coordinate,coordinate.moveLeft())
    }
    fun drawDividerUp(coordinate: Coordinate): Boolean {
        return drawDivisorBetween(coordinate, coordinate.moveUp())
    }

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
        fun create(numColumns: Int, numRows: Int, cellValues: IntArray, sections: IntArray) : Board{
            require(numColumns*numRows == cellValues.size) {
                "Array must be of size $numColumns * $numRows = ${numColumns * numRows}"
            }

            val cells = mutableStateListOf<Cell>()
            cells.addAll(Array(size = numColumns*numRows, init = { Cell.create2(cellValues[it]) }))

            return Board(
                numColumns = numColumns,
                numRows = numRows,
                cells = cells,
                sections = sections
            )
        }

        fun example(): Board {
            val cellValues = IntArray(size = 6*6, init = { 0 })
            cellValues[4] = 2
            cellValues[16] = 3
            cellValues[19] = 3
            cellValues[31] = 4

            val sections = intArrayOf(
                0, 1, 2, 3, 4, 5,
                0, 6, 2, 4, 4, 7,
                6, 6, 2, 8, 8, 7,
                9, 6,10, 8, 8, 7,
                9,11,10,12,13,13,
                14,10,10,12,12,12
            )

            return create(
                numColumns = 6,
                numRows = 6,
                cellValues = cellValues,
                sections = sections
            )
        }
    }

}