package com.example.tfg.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameState(
    var board: Board,
    var moves: MutableList<Move> = mutableListOf(),
    var pointer: Int = -1,
) : Parcelable {
    fun clone(): GameState {
        return this.copy(board = board.clone())
    }


}
