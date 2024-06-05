package com.example.tfg.common.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tfg.common.IdGenerator

@Entity
data class GameState(
    @PrimaryKey
    val gameStateId: Long = generateId(),
    var pointer: Int = -1,
    val position: Int,
    var gameId: Long,
){
    companion object{
        private fun generateId() = IdGenerator.generateId("gamestate")

        fun create(from: GameState, position: Int) : GameState {
            return from.copy(gameStateId = generateId(), position = position)
        }
    }
}
