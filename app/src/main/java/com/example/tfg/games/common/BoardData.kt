package com.example.tfg.games.common

import com.example.tfg.common.utils.Utils

open class BoardData(
    val possibleValues: Array<MutableList<Int>>,
    val actualValues: IntArray
) {
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
}