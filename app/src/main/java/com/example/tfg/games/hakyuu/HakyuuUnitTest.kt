package com.example.tfg.games.hakyuu

import com.example.tfg.common.GameFactory
import com.example.tfg.common.utils.CustomTestWatcher
import com.example.tfg.games.common.AbstractGame
import com.example.tfg.games.common.AbstractGameUnitTest
import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.Games
import com.example.tfg.games.common.JankoBoard
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

data class JankoHakyuuBoard(
    override val boardId: Int,
    override val difficulty: String,
    override val numColumns: Int,
    override val numRows: Int,
    override val problem: String,
    override val areas: String,
    override val solution: String,
): JankoBoard {
    override fun getStartBoard(): IntArray {
        return problem.replace("\n", " ").split(" ").map { it.toIntOrNull() ?: 0 }.toIntArray()
    }
}

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@ExtendWith(CustomTestWatcher::class)
class HakyuuUnitTest : AbstractGameUnitTest(
    enumEntries = HakyuuStrategy.entries,
    maxSize = Games.HAKYUU.maxSize,
    minSize = Games.HAKYUU.minSize,
    canHaveUnequalSize = true,
    getScore = { gameBoard: AbstractGame ->
        (gameBoard.score as HakyuuScore).toString()
    }
) {

    override fun loadJankoData(data: String?): List<JankoBoard> {
        val json = data ?: File("src/test/testdata/hakyuu-data.json").readText()
        return Gson().fromJson(json, object : TypeToken<List<JankoHakyuuBoard?>?>() {}.type)
    }

    override fun getGameBoardJanko(seed: Long, jankoBoard: JankoBoard): AbstractGame {
        val regions = jankoBoard.getRegions()
        return Hakyuu.createTesting(
            seed = seed,
            numColumns = jankoBoard.numColumns,
            numRows = jankoBoard.numRows,
            startBoard = jankoBoard.getStartBoard(),
            completedBoard = jankoBoard.getCompletedBoard(),
            boardRegions = regions
        )
    }

    override fun solveGameBoardJanko(seed: Long, jankoBoard: JankoBoard): AbstractGame {
        val regions = jankoBoard.getRegions()
        return Hakyuu.solveBoard(
            seed = seed,
            numColumns = jankoBoard.numColumns,
            numRows = jankoBoard.numRows,
            startBoard = jankoBoard.getStartBoard(),
            completedBoard = jankoBoard.getCompletedBoard(),
            regions = regions
        )
    }


    override suspend fun getGameBoard(numRows: Int, numColumns: Int, seed: Long, difficulty: Difficulty): AbstractGame {
        return Hakyuu.createTesting(
            numRows = numRows,
            numColumns = numColumns,
            seed = seed,
            difficulty = difficulty
        )
    }

    @Test
    fun testOkJankoBoard() {
        val boardId = 1 // 274
        testOkJankoBoard(boardId)
    }

    @Test
    fun testSeededBoard() {
        val numRows = 13
        val numColumns = 13
        val seed: Long = 135252665
        val difficulty = Difficulty.MASTER
        testOkSeededBoard(numRows, numColumns, seed, difficulty)
    }

    @Test
    fun testOkBoard() {
        val hakyuu = Hakyuu.solveBoard(seed = 0, boardToSolve = GameFactory.START_STR, boardRegions = GameFactory.REGION_STR)
        val completed = Hakyuu.parseBoardString(GameFactory.COMPLETED_STR)
        assert(completed.contentEquals(hakyuu.completedBoard))
    }
}