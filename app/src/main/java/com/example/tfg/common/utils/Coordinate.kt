package com.example.tfg.common.utils

import com.example.tfg.common.enums.Direction
import kotlin.math.abs


data class Coordinate(val row: Int, val column: Int) {
    fun sameRow(coordinate: Coordinate) = this.row == coordinate.row

    fun sameColumn(coordinate: Coordinate) = this.column == coordinate.column

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

    fun toIndex(numColumns: Int): Int {
        return row * numColumns + column
    }

    fun isOutOfBounds(numRows: Int, numColumns: Int): Boolean {
        return (row < 0 || column < 0 || row >= numRows || column >= numColumns)
    }

    operator fun compareTo(other: Coordinate): Int {
        return if (this.column == other.column) {
            this.row - other.row
        } else {
            this.column - other.column
        }
    }


    companion object {
        fun getRow(index: Int, numColumns: Int) = index / numColumns

        fun getColumn(index: Int, numColumns: Int) = index % numColumns

        fun fromIndex(index: Int, numRows: Int, numColumns: Int): Coordinate {
            val row = getRow(index, numColumns)
            val column = getColumn(index, numColumns)
            require(!invalidParameters(row = row, column = column, numRows = numRows, numColumns = numColumns))
            {"index:$index;row:$row;column:$column;numRows:$numRows;numColumns:$numColumns"}

            return Coordinate(row = row, column = column)
        }
        private fun invalidParameters(row: Int, column: Int, numRows: Int, numColumns: Int): Boolean {
            return (row < 0 || column < 0 || row >= numRows || column >= numColumns)
        }

        fun parseString(str: String, reverseCoordinate: Boolean = false): Coordinate {
            val spl = str.substring(1,str.length-1).split(',')
            if (reverseCoordinate) {
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

        private fun sameRow(position1: Int, position2: Int, numColumns: Int) = getRow(position1, numColumns) == getRow(position2, numColumns)

        private fun sameColumn(position1: Int, position2: Int, numColumns: Int) = getColumn(position1, numColumns) == getColumn(position2, numColumns)

        fun sameColumnOrRow(position1: Int, position2: Int, numColumns: Int): Boolean {
            return sameRow(position1, position2, numColumns) ||
                    sameColumn(position1, position2, numColumns)
        }

        fun areConnected(position1: Int, position2: Int, numColumns: Int): Boolean {
            val abs = abs(position1 - position2)
            return (abs == 1 && sameRow(position1, position2, numColumns)) || abs == numColumns
        }
    }
}
