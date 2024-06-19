package com.example.tfg.common.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.Games
import java.time.LocalDateTime

@Entity
data class WinningStreak(
    @PrimaryKey
    val startDate: LocalDateTime = LocalDateTime.now(),
    var endDate: LocalDateTime? = null,
    val gameEnum: Games?, //If null its all games
    val difficulty: Difficulty?, //If null its all difficulties
    var wins: Int = 1,
)