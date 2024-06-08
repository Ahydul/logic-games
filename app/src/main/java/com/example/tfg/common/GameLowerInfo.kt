package com.example.tfg.common

import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.Games
import java.time.LocalDateTime

class GameLowerInfo(
    val gameId: Long,
    val type: Games,
    val difficulty: Difficulty,
    val startDate: LocalDateTime = LocalDateTime.now(),
    var numErrors: Int,
    var numClues: Int,
    var timer: Int
)