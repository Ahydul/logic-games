package com.example.tfg.common

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class GameState(
    var board: Board,
    var moves: SnapshotStateList<Move> = mutableStateListOf(),
    var pointer: Int = -1,
) {
    fun clone(): GameState {
        return this.copy(board = board.clone())
    }


}
