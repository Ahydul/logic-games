package com.example.tfg.common.utils

data class Coordinate(val row: Int, val column: Int) {
    fun moveRight(numColumns: Int): Coordinate? {
        if (column >= numColumns - 1) return null
        return Coordinate(row, column + 1)
    }

    fun moveLeft(): Coordinate? {
        if (column <= 0) return null
        return Coordinate(row, column - 1)
    }

    fun moveUp(): Coordinate? {
        if (row <= 0) return null
        return Coordinate(row - 1, column)
    }

    fun moveDown(numRows: Int): Coordinate? {
        if (row >= numRows - 1) return null
        return Coordinate(row + 1, column)
    }

    fun move(direction: Direction, numRows: Int, numColumns: Int): Coordinate? {
        return when(direction){
            Direction.NORTH -> moveUp()
            Direction.EAST -> moveRight(numColumns)
            Direction.WEST -> moveLeft()
            Direction.SOUTH -> moveDown(numRows)
        }
    }

    fun toIndex(numRows: Int, numColumns: Int): Int? {
        if (invalidParameters(row = row, column = column, numRows = numRows, numColumns = numColumns)) return null
        return row * numColumns + column
    }

    fun isOutOfBounds(numRows: Int, numColumns: Int): Boolean {
        return (row < 0 || column < 0 || row >= numRows || column >= numColumns)
    }


    companion object {
        fun fromIndex(index: Int, numRows: Int, numColumns: Int): Coordinate {
            val row = index / numColumns
            val column = index % numColumns
            require(!invalidParameters(row = row, column = column, numRows = numRows, numColumns = numColumns))
            {"index:$index;row:$row;column:$column;numRows:$numRows;numColumns:$numColumns"}

            return Coordinate(row = row, column = column)
        }
        private fun invalidParameters(row: Int, column: Int, numRows: Int, numColumns: Int): Boolean {
            return (row < 0 || column < 0 || row >= numRows || column >= numColumns)
        }
    }
}
