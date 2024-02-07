package com.example.tfg.common

data class Coordinate(val row: Int, val column: Int) {
    fun moveRight(): Coordinate {
        return Coordinate(row, column + 1)
    }

    fun moveLeft(): Coordinate {
        return Coordinate(row, column - 1)
    }

    fun moveUp(): Coordinate {
        return Coordinate(row - 1, column)
    }

    fun moveDown(): Coordinate {
        return Coordinate(row + 1, column)
    }

    fun toIndex(numRows: Int, numColumns: Int): Int? {
        if (row < 0 || column < 0 || row >= numRows || column >= numColumns) return null
        return row * numColumns + column
    }

    fun isOutOfBounds(numRows: Int, numColumns: Int): Boolean {
        return (row < 0 || column < 0 || row >= numRows || column >= numColumns)
    }
}
