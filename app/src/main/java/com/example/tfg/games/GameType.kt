package com.example.tfg.games

import com.example.tfg.common.utils.Coordinate

interface GameType {
    val type: Games
    val rules: List<String>
    val url: String
    val noNotes: Boolean
    val boardRegions: Map<Int, List<Coordinate>>
                                                                            // Value, Section
    //fun createNewGame(difficulty: Difficulty, numRows: Int, numColumns: Int): List<Pair<Int,Int>>

}