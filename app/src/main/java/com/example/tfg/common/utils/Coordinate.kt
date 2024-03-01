package com.example.tfg.common.utils

data class Coordinate(val row: Int, val column: Int) {
    fun moveRight(numColumns: Int, value: Int = 1): Coordinate? {
        if (column >= numColumns - value) return null
        return Coordinate(row, column + value)
    }

    fun moveLeft(value: Int = 1): Coordinate? {
        if (column <= value - 1) return null
        return Coordinate(row, column - value)
    }

    fun moveUp(value: Int = 1): Coordinate? {
        require(value>0)
        if (row <= value - 1) return null
        return Coordinate(row - value, column)
    }

    fun moveDown(numRows: Int, value: Int = 1): Coordinate? {
        if (row >= numRows - value) return null
        return Coordinate(row + value, column)
    }

    fun move(direction: Direction, numRows: Int, numColumns: Int, value: Int = 1): Coordinate? {
        return when(direction){
            Direction.NORTH -> moveUp(value = value)
            Direction.EAST -> moveRight(value = value, numColumns = numColumns)
            Direction.WEST -> moveLeft(value = value)
            Direction.SOUTH -> moveDown(value = value, numRows = numRows)
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
