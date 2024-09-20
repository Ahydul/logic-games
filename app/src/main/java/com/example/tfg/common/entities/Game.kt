package com.example.tfg.common.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.Games
import java.time.LocalDateTime

@Entity
data class Game(
    @PrimaryKey(autoGenerate = true)
    val gameId: Long = 0,
    val gameType: Games,
    val abstractGameId: Long,
    val difficulty: Difficulty,
    val startDate: LocalDateTime = LocalDateTime.now(),
    val seed: Long,
    var endDate: LocalDateTime? = null,
    var playerWon: Boolean = false,
    var errors: MutableSet<Pair<Int,Int>> = mutableSetOf(),
    var numErrors: Int = errors.size,
    var numClues: Int = 0,
    var timer: Int = 0
) {

    fun endGame(playerWon: Boolean): LocalDateTime {
        this.playerWon = playerWon
        endDate = LocalDateTime.now()
        return endDate!!
    }

    fun addError(error: Pair<Int, Int>): Boolean {
        val res = errors.add(error)
        if (res) numErrors++
        return res
    }

    fun addClue() {
        numClues++
    }

    companion object {
        fun create(gameType: Games, abstractGameId: Long, difficulty: Difficulty, seed: Long): Game {
            return Game(
                gameType = gameType,
                abstractGameId = abstractGameId,
                difficulty = difficulty,
                seed = seed
            )
        }
    }
}

