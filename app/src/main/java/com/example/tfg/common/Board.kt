package com.example.tfg.common

import androidx.compose.ui.graphics.Color

/*
* Board manages the indexes of the different cells and the sections that may contain several cells
* */
data class Board(
    val numColumns: Int,
    val numRows: Int,
    val cells: Array<Cell>, // numColumns * numRows
    private val sections: IntArray // numColumns * numRows
) {


    fun indexToInt(row: Int, column: Int) : Int? {
        if(row < 0 || column < 0 || row >= numRows || column >= numColumns) return null
        return row * numColumns + column
    }

    fun getCell(index: Int): Cell {
        return cells[index]
    }
    fun getCell(row: Int, column: Int): Cell {
        return cells[indexToInt(row,column)!!]
    }
    fun setCellValue(index: Int, value: Int) {
        var newCell = cells[index]
        newCell.value = value
        cells[index] = newCell
    }
    fun setCellColor(index: Int, color: Color) {
        var newCell = cells[index]
        newCell.backGroundColor = color
        cells[index] = newCell
    }
    fun setCellNote(index: Int, noteIndex: Int, note: Int) {
        var newCell = cells[index]
        newCell.notes[noteIndex] = note
        cells[index] = newCell
    }

    fun getCellValue(row: Int, column: Int): Int {
        val index: Int = indexToInt(row,column) !!
        return cells[index].value
    }

    private fun fromSameSection(index1: Int, index2: Int): Boolean{
        return sections[index1] == sections[index2]
    }

    private fun drawDivisor(row: Int, column: Int, row2: Int, column2: Int): Boolean {
        val index: Int = indexToInt(row = row, column = column) !!
        val index2: Int? = indexToInt(row = row2, column = column2)

        return !(index2 == null || fromSameSection(index, index2))
    }

    fun drawDividerRight(row: Int, column: Int): Boolean {
        return drawDivisor(row,column,row,column + 1)
    }
    fun drawDividerDown(row: Int, column: Int): Boolean {
        return drawDivisor(row,column,row + 1,column)
    }
    fun drawDividerLeft(row: Int, column: Int): Boolean {
        return drawDivisor(row,column,row,column - 1)
    }
    fun drawDividerUp(row: Int, column: Int): Boolean {
        return drawDivisor(row,column,row - 1,column)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Board

        return cells.contentEquals(other.cells)
    }

    override fun hashCode(): Int {
        return cells.contentHashCode()
    }


    companion object {
        fun create(numColumns: Int, numRows: Int, cellValues: IntArray, sections: IntArray) : Board{
            require(numColumns*numRows == cellValues.size) {
                "Array must be of size $numColumns * $numRows = ${numColumns * numRows}"
            }
            return Board(
                numColumns = numColumns,
                numRows = numRows,
                cells = Array(size = numColumns*numRows, init = { Cell.create(cellValues[it]) }),
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