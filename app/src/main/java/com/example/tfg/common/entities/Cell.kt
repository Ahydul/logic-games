package com.example.tfg.common.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


/*
* Manages the content of a cell
* */
@Entity
data class Cell(
    @PrimaryKey(autoGenerate = true)
    var cellId: Long = 0,
    var value: Int,
    var notes: IntArray,
    val readOnly: Boolean = false,
    val backgroundColor: Int,
    val isError: Boolean = false
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

    fun copyOnlyIndex(value: Int): Cell {
        return Cell(cellId = this.cellId, value = value, notes = emptyNotes(), backgroundColor = 0)
    }


    fun copy(noteIndex: Int, noteValue: Int): Cell {
        return Cell(cellId = this.cellId, value = this.value, notes = copyNotesChanging(noteIndex, noteValue), backgroundColor = this.backgroundColor)
    }

    fun copyErase(): Cell {
        return Cell(cellId = this.cellId, value = 0, notes = emptyNotes(), readOnly = this.readOnly, backgroundColor = this.backgroundColor)
    }

    private fun emptyNotes(): IntArray {
        return IntArray(9){0}
    }

    fun isErrorAndHasErrorBackground(): Boolean {
        return this.backgroundColor == ERROR_CELL_BACKGROUND_COLOR && this.isError
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cell

        if (value != other.value) return false
        if (!notes.contentEquals(other.notes)) return false
        if (readOnly != other.readOnly) return false
        if (backgroundColor != other.backgroundColor) return false
        return isError == other.isError
    }

    override fun hashCode(): Int {
        var result = value
        result = 31 * result + notes.contentHashCode()
        result = 31 * result + readOnly.hashCode()
        result = 31 * result + backgroundColor
        result = 31 * result + isError.hashCode()
        return result
    }

    override fun toString(): String {
        return "Cell(value=$value, notes=${notes.contentToString()}, backgroundColor=$backgroundColor, isError=$isError)"
    }


    companion object {
        const val ERROR_CELL_BACKGROUND_COLOR = -7796214

        private fun emptyCell(backgroundColor: Int = 0) =
            Cell(value = 0, notes = emptyNotes(), backgroundColor = backgroundColor)

        private fun readOnlyCell(value: Int) =
            Cell(value = value, notes = IntArray(0), readOnly = true, backgroundColor = 0)

        fun emptyNotes(): IntArray {
            return IntArray(9){0}
        }

        fun create(value: Int) : Cell {
            return if (value == 0) emptyCell()
            else readOnlyCell(value)
        }

        fun create(str: String) : Cell {
            val value = str.toInt()
            return if (value == 0) emptyCell()
            else readOnlyCell(value)
        }

        fun exampleWithNote(): Cell {
            val notes = emptyNotes()
            notes[0] = 2
            notes[1] = 3
            notes[2] = 4
            return Cell(value = 0, notes = notes, backgroundColor = 0)
        }

        fun exampleError(): Cell {
            return Cell(value = 3, notes = emptyNotes(), isError = true, backgroundColor = 0)
        }

        fun exampleBackgroundError(): Cell {
            return Cell(value = 1, notes = emptyNotes(), backgroundColor = ERROR_CELL_BACKGROUND_COLOR)
        }

        fun exampleBackgroundErrorWithError(): Cell {
            return Cell(value = 1, notes = emptyNotes(), isError = true, backgroundColor = ERROR_CELL_BACKGROUND_COLOR)
        }
    }

}