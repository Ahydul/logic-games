package com.example.tfg.common.entities

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "game_state")
data class GameState(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: Long,
    var board: Board,
    @Embedded(prefix = "moves_")
    var moves: MutableList<Move> = mutableListOf(),
    var pointer: Int = -1,
) : Parcelable {
    fun clone(): GameState {
        return this.copy(board = board.clone())
    }
}
