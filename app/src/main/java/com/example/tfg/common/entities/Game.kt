package com.example.tfg.common.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tfg.common.Difficulty
import com.example.tfg.games.GameType
import java.time.LocalDateTime

@Entity
data class Game(
    @PrimaryKey(autoGenerate = true)
    val gameId: Long = 0,
    @Embedded
    val gameTypeEntity: GameTypeEntity,
    val difficulty: Difficulty,
    val startDate: LocalDateTime = LocalDateTime.now(),
    var endDate: LocalDateTime? = null,
    var errors: MutableSet<Pair<Int,Int>> = mutableSetOf(),
    var numClues: Int = 0,
    var timer: Int = 0
) {

    fun setEndTime() {
        endDate = LocalDateTime.now()
    }

    companion object {
        fun create(gameType: GameType, difficulty: Difficulty): Game {
            return Game(
                gameTypeEntity = GameTypeEntity.create(gameType),
                difficulty = difficulty,
            )
        }
        fun create(gameTypeEntity: GameTypeEntity, difficulty: Difficulty): Game {
            return Game(
                gameTypeEntity = gameTypeEntity,
                difficulty = difficulty,
            )
        }

    }
}

