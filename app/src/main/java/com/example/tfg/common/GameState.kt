package com.example.tfg.common

data class GameState(
    var board: Board,
    var moves: List<Move> = emptyList(),
    var pointer: Int = 0,
) {
    fun clone(): GameState {
        return this.copy(board = board.clone())
    }


}
