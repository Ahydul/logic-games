package com.example.tfg.games

import com.example.tfg.common.Difficulty
import com.example.tfg.common.utils.Coordinate
import kotlin.random.Random

interface GameType {
    val type: Games
    val rules: List<String>
    val url: String
    val noNotes: Boolean
    val boardRegions: Map<Int, List<Coordinate>>
    val numColumns: Int
    val numRows: Int

    // Value, Section
    fun createNewGame(difficulty: Difficulty, random: Random): Map<Coordinate, Int>



}