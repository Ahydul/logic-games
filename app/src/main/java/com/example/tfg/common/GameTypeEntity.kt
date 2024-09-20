package com.example.tfg.common

import com.example.tfg.games.common.AbstractGame
import com.example.tfg.games.common.Games
import com.example.tfg.games.common.Score
import com.example.tfg.games.hakyuu.Hakyuu
import com.example.tfg.games.hakyuu.HakyuuScore

class GameTypeEntity(
    val type: Games,
    val numColumns: Int,
    val numRows: Int,
    val seed: Long,
    val score: Score,
    var completedBoard: IntArray = IntArray(numColumns * numRows),
    var boardRegions: IntArray = IntArray(numColumns * numRows),
    var startBoard: IntArray = IntArray(numColumns * numRows)
){
    fun toGameType(): AbstractGame {
        return when(type){
            Games.HAKYUU -> Hakyuu(
                numColumns = numColumns,
                numRows = numRows,
                seed = seed,
                score = score as HakyuuScore,
                completedBoard = completedBoard,
                boardRegions = boardRegions,
                startBoard = startBoard,
            )
        }
    }
    companion object{
        fun create(abstractGame: AbstractGame): GameTypeEntity {
            return GameTypeEntity(
                type = abstractGame.type,
                numColumns = abstractGame.numColumns,
                numRows = abstractGame.numRows,
                seed = abstractGame.seed,
                score = abstractGame.score,
                completedBoard = abstractGame.completedBoard,
                boardRegions = abstractGame.boardRegions,
                startBoard = abstractGame.startBoard,
            )
        }
    }
}