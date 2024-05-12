package com.example.tfg.common

data class GameState(
    var board: Board,
    var moves: MutableList<Move> = mutableListOf(),
    var pointer: Int = -1,
) {
    fun clone(): GameState {
        return this.copy(board = board.clone())
    }


}
