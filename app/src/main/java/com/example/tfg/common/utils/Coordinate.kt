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

    override fun toString(): String {
        return "($row,$column)"
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

        fun parseString(str: String, reverse: Boolean = false): Coordinate {
            val spl = str.substring(1,str.length-1).split(',')
            if (reverse) {
                return Coordinate(row = spl[1].toInt(), column = spl[0].toInt())
            }
            return Coordinate(row = spl[0].toInt(), column = spl[1].toInt())
        }

        fun move(direction: Direction, position:Int, numRows: Int, numColumns: Int, value: Int = 1): Int? {
            require(value>0)
            return when(direction){
                Direction.NORTH -> moveUp(position = position, numColumns = numColumns, value = value)
                Direction.EAST -> moveRight(position = position, value = value, numColumns = numColumns)
                Direction.WEST -> moveLeft(position = position, value = value, numColumns = numColumns)
                Direction.SOUTH -> moveDown(position = position, value = value, numColumns = numColumns, numRows = numRows)
            }
        }

        fun move(direction1: Direction, direction2: Direction, position:Int, numRows: Int, numColumns: Int, value: Int = 1): Int? {
            val tmp = move(direction1, position, numRows, numColumns, value) ?: return null
            return move(direction2,
                tmp,
                numRows,
                numColumns,
                value)
        }

        private fun moveRight(position: Int, numColumns: Int, value: Int = 1): Int? {
            if (((position % numColumns) + value) >= numColumns) return null
            return position + value
        }

        private fun moveLeft(position: Int, numColumns: Int, value: Int = 1): Int? {
            if (((position % numColumns) - value) < 0) return null
            return position - value
        }

        private fun moveUp(position: Int, numColumns: Int, value: Int = 1): Int? {
            val newPosition = position - value*numColumns
            if (newPosition < 0) return null
            return newPosition
        }

        private fun moveDown(position: Int, numColumns: Int, numRows: Int, value: Int = 1): Int? {
            val newPosition = position + value*numColumns
            if (newPosition >= numColumns*numRows) return null
            return newPosition
        }
    }
}
