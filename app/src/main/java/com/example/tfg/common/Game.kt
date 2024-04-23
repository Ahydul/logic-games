package com.example.tfg.common

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.games.Games
import com.example.tfg.games.hakyuu.Hakyuu2
import java.time.LocalDateTime

class Game private constructor(
    val gameType: Games,
    val difficulty: Difficulty,
    val state: SnapshotStateList<GameState>,
    val solution: IntArray,
    val regions: Map<Int, List<Coordinate>>,
    val startDate: LocalDateTime = LocalDateTime.now(),
    val endDate: LocalDateTime? = null,
    var errors: List<Move> = emptyList(),
    var numClues: Int = 0
) {


    companion object {
        private fun create(gameType: Games, board: Board, solution: IntArray, boardRegions: Map<Int, List<Coordinate>>, difficulty: Difficulty): Game {
            val gameState = GameState(board = board)
            return Game(
                gameType = gameType,
                difficulty = difficulty,
                state = mutableStateListOf(gameState),
                solution = solution,
                regions = boardRegions
            )
        }

        fun example(): Game {
            val numColumns = 8
            val numRows = 8
            val hakyuu = Hakyuu2.example()
            val boardRegions: MutableMap<Int, MutableList<Coordinate>> = mutableMapOf()
            hakyuu.getBoardRegions().forEachIndexed { position, regionId ->
                val coordinate = Coordinate.fromIndex(index = position, numRows = numRows, numColumns = numColumns )
                if (boardRegions.containsKey(regionId)) boardRegions[regionId]!!.add(coordinate)
                else boardRegions[regionId] = mutableListOf(coordinate)
            }

            return create(
                gameType = Games.HAKYUU,
                board = Board.create(numRows = numRows, numColumns = numColumns, cellValues = hakyuu.getStartBoard()),
                solution = hakyuu.getCompletedBoard(),
                boardRegions = boardRegions,
                difficulty = Difficulty.EASY
            )
        }

        fun example2(): Game {
            val numColumns = 6
            val numRows = 6
            val board = Board.exampleBoard()

            return create(
                gameType = Games.HAKYUU,
                board = board,
                solution = IntArray(36),
                boardRegions = emptyMap(),
                difficulty = Difficulty.EASY
            )
        }
    }
}

