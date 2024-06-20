package com.example.tfg.common

import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.Games
import java.time.LocalDateTime

class GameLowerInfo(
    val gameId: Long,
    val type: Games,
    val difficulty: Difficulty,
    val startDate: LocalDateTime = LocalDateTime.now(),
    val numErrors: Int,
    val numClues: Int,
    val timer: Int,
    val seed: Long
)