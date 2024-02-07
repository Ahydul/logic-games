package com.example.tfg.common

import androidx.compose.ui.graphics.Color

data class Cell private constructor(
    var value: Int,
    var notes: IntArray,
    val readOnly: Boolean,
    var backGroundColor: Color = Color.DarkGray,
) {
    companion object {
        private fun emptyCell() = Cell(value = 0, notes = IntArray(9){0}, readOnly = false)
        private fun readOnlyCell(value: Int) = Cell(value = value, notes = IntArray(0), readOnly = true)

        private fun allNotes(): Cell {
            var arr = arrayOf(1,2,3,4,5,6,7,8,9)
            return Cell(value = 0, notes = arr.toIntArray(), readOnly = false)
        }

        fun create(value: Int) : Cell {
            if (value == 0) return allNotes()
            else return readOnlyCell(value)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cell

        if (value != other.value) return false
        if (!notes.contentEquals(other.notes)) return false
        if (readOnly != other.readOnly) return false
        return backGroundColor == other.backGroundColor
    }

    override fun hashCode(): Int {
        var result = value
        result = 31 * result + notes.contentHashCode()
        result = 31 * result + readOnly.hashCode()
        result = 31 * result + backGroundColor.hashCode()
        return result
    }

}