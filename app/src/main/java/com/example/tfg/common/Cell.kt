package com.example.tfg.common

import androidx.compose.ui.graphics.Color

class Cell private constructor(
    var value: Int,
    var notes: IntArray,
    val readOnly: Boolean,
    var backGroundColor: Color = Color.DarkGray,
) {
    companion object {
        fun emptyCell() = Cell(value = 0, notes = IntArray(9), readOnly = false)
        fun readOnlyCell(value: Int) = Cell(value = value, notes = IntArray(0), readOnly = true)

        fun create(value: Int) : Cell {
            if (value == 0) return emptyCell()
            else return readOnlyCell(value)
        }
    }

}