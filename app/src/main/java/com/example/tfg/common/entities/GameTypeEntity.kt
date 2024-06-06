package com.example.tfg.common.entities

import com.example.tfg.games.GameType
import com.example.tfg.games.Games
import com.example.tfg.games.Score
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
    fun toGameType(): GameType {
        return when(type){
            Games.HAKYUU -> Hakyuu(
                numColumns = numColumns,
                numRows = numRows,
                seed = seed,
                score = score as HakyuuScore,
                completedBoard = completedBoard,
                regions = boardRegions,
                startBoard = startBoard,
            )
        }
    }
    companion object{
        fun create(gameType: GameType): GameTypeEntity {
            return GameTypeEntity(
                type = gameType.type,
                numColumns = gameType.numColumns,
                numRows = gameType.numRows,
                seed = gameType.seed,
                score = gameType.score,
                completedBoard = gameType.completedBoard,
                boardRegions = gameType.boardRegions,
                startBoard = gameType.startBoard,
            )
        }
    }
}