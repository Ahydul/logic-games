package com.example.tfg.common

data class Cell private constructor(
    var value: Int,
    var notes: IntArray,
    val readOnly: Boolean,
    var backGroundColor: Int = -13421773 //R.color.cell_background, no hay otra que cambiar asi
) {

    fun isEmpty(): Boolean {
        return value == 0 && notes.all { it == 0 }
    }

    fun getNote(index: Int): Int {
        return notes[index]
    }

    private fun copyNotes(index: Int, value: Int): IntArray {
        val newNotes = notes.copyOf()
        newNotes[index] = value
        return newNotes
    }

    fun copy(value: Int = this.value, notes: IntArray = this.notes, backGroundColor: Int = this.backGroundColor): Cell {
        return Cell(value = value, notes = notes, backGroundColor = backGroundColor, readOnly = false)
    }
    fun copy(noteIndex: Int, noteValue: Int): Cell {
        return Cell(value = value, notes = copyNotes(noteIndex, noteValue), backGroundColor = backGroundColor, readOnly = false)
    }

    companion object {
        private fun emptyCell() = Cell(value = 0, notes = emptyNotes(), readOnly = false)
        private fun readOnlyCell(value: Int) = Cell(value = value, notes = IntArray(0), readOnly = true)

        private fun allNotes(): Cell {
            var arr = arrayOf(1,2,3,4,5,6,7,8,9)
            return Cell(value = 0, notes = arr.toIntArray(), readOnly = false)
        }

        fun create(value: Int) : Cell {
            if (value == 0) return emptyCell()
            else return readOnlyCell(value)
        }
        fun create2(value: Int) : Cell {
            if (value == 0) return allNotes()
            else return readOnlyCell(value)
        }

        fun emptyNotes(): IntArray {
            return IntArray(9){0}
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