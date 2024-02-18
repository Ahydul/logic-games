package com.example.tfg.common

import androidx.compose.runtime.Stable

/*
* Manages the content of a cell
* */
@Stable
data class Cell private constructor(
    var value: Int,
    var notes: IntArray,
    val readOnly: Boolean,
    val backgroundColor: Int,
) {

    fun isEmpty(): Boolean {
        return value == 0 && notes.all { it == 0 }
    }

    fun getNote(index: Int): Int {
        return notes[index]
    }

    //Find index of note or null
    fun findNote(note: Int): Int? {
        notes.forEachIndexed { index, n -> if (n == note) return index }
        return null
    }
    //Find last empty index or null
    private fun lastEmptyIndex() : Int? {
        val res = notes.indexOfFirst { it == 0 }
        return if (res == -1) null
            else res
    }

    private fun copyNotesChanging(index: Int, value: Int): IntArray {
        val newNotes = notes.copyOf()
        newNotes[index] = value
        return newNotes
    }

    fun addNote(note: Int): IntArray {
        val index = lastEmptyIndex() ?: return notes
        val newNotes = copyNotesChanging(index = index, value = note)
        newNotes.sort(toIndex = index+1)
        return newNotes
    }

    fun removeNote(index: Int): IntArray {
        val newNotes = notes.copyOf()
        newNotes.forEachIndexed { i, _ ->
            if (i >= index)
                newNotes[i] =
                    if (i+1 == newNotes.size) 0
                    else newNotes[i+1]
        }
        return newNotes
    }

    fun copy(value: Int = this.value, notes: IntArray = this.notes, backgroundColor: Int = this.backgroundColor): Cell {
        return Cell(value = value, notes = notes, readOnly = false, backgroundColor = backgroundColor)
    }

    fun copy(noteIndex: Int, noteValue: Int): Cell {
        return Cell(value = this.value, notes = copyNotesChanging(noteIndex, noteValue), readOnly = false, backgroundColor = this.backgroundColor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cell

        if (value != other.value) return false
        if (!notes.contentEquals(other.notes)) return false
        if (readOnly != other.readOnly) return false
        return backgroundColor == other.backgroundColor
    }

    override fun hashCode(): Int {
        var result = value
        result = 31 * result + notes.contentHashCode()
        result = 31 * result + readOnly.hashCode()
        result = 31 * result + backgroundColor
        return result
    }

    companion object {
        private fun emptyCell() = Cell(value = 0, notes = emptyNotes(), readOnly = false, backgroundColor = 0)
        private fun readOnlyCell(value: Int) = Cell(value = value, notes = IntArray(0), readOnly = true, backgroundColor = 0)

        private fun allNotes(): Cell {
            val arr = arrayOf(1,2,3,4,5,6,7,8,9)
            return Cell(value = 0, notes = arr.toIntArray(), readOnly = false, backgroundColor = 0)
        }

        fun emptyNotes(): IntArray {
            return IntArray(9){0}
        }

        fun create(value: Int) : Cell {
            return if (value == 0) emptyCell()
            else readOnlyCell(value)
        }
        fun create2(value: Int) : Cell {
            return if (value == 0) allNotes()
            else readOnlyCell(value)
        }

    }


}