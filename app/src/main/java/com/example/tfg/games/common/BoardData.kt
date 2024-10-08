package com.example.tfg.games.common

import com.example.tfg.common.utils.Utils
import com.example.tfg.games.kendoku.KendokuBoardData

open class BoardData(
    val possibleValues: Array<MutableList<Int>>,
    val actualValues: IntArray,
    private var initialized: Boolean = false
) {
    fun isInitialized(): Boolean = initialized

    fun initialize() {
        initialized = true
    }

    open fun clone(): BoardData {
        val newPossibleValues: Array<MutableList<Int>> = Array(possibleValues.size) {
            possibleValues[it].toMutableList()
        }

        return BoardData(newPossibleValues, actualValues.clone())
    }

    open fun replaceDataWith(newBoardData: BoardData) {
        Utils.replaceArray(thisArray = possibleValues, with = newBoardData.possibleValues)
        Utils.replaceArray(thisArray = actualValues, with = newBoardData.actualValues)
    }

    companion object {
        fun create(possibleValues: Array<MutableList<Int>>, board: IntArray, type: Games): BoardData {
            return when(type){
                Games.HAKYUU -> KendokuBoardData.create(possibleValues, board)
            }
        }
    }
}