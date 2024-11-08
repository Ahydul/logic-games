package com.example.tfg.games.hakyuu

import com.example.tfg.common.GameFactory
import com.example.tfg.common.utils.CustomTestWatcher
import com.example.tfg.games.common.AbstractGame
import com.example.tfg.games.common.AbstractGameUnitTest
import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.JankoBoard
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import kotlin.math.sqrt

data class JankoHakyuuBoard(
    override val boardId: Int,
    override val difficulty: String,
    override val size: Int,
    override val problem: String,
    override val areas: String,
    override val solution: String,
    override var addValuesToStart: (startBoard: IntArray) -> Unit
): JankoBoard

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@ExtendWith(CustomTestWatcher::class)
class HakyuuUnitTest : AbstractGameUnitTest(
    enumEntries = HakyuuStrategy.entries,
    getScore = { gameBoard: AbstractGame ->
        (gameBoard.score as HakyuuScore).toString()
    }
) {

    override fun loadJankoData(): List<JankoBoard> {
        val file = File("src/test/testdata/hakyuu-data.json")
        return Gson().fromJson(file.readText(), object : TypeToken<List<JankoHakyuuBoard?>?>() {}.type)
    }

    override fun getGameBoardJanko(seed: Long, jankoBoard: JankoBoard): AbstractGame {
        val regions = jankoBoard.getRegions()
        return Hakyuu.solveBoard(
            seed = seed,
            size = sqrt(regions.size.toDouble()).toInt(),
            startBoard = jankoBoard.getStartBoard(),
            completedBoard = jankoBoard.getCompletedBoard(),
            regions = regions
        )
    }

    override suspend fun getGameBoard(size: Int, seed: Long, difficulty: Difficulty): AbstractGame {
        return Hakyuu.createTesting(
            numRows = size,
            numColumns = size,
            seed = seed,
            difficulty = difficulty
        )
    }

    @Test
    fun testOkJankoBoard() {
        val boardId = 1
        testOkJankoBoard(boardId)
    }

    @Test
    fun testOkBoard() {
        val hakyuu = Hakyuu.solveBoard(seed = 0, boardToSolve = GameFactory.START_STR, boardRegions = GameFactory.REGION_STR)
        val completed = Hakyuu.parseBoardString(GameFactory.COMPLETED_STR)
        assert(completed.contentEquals(hakyuu.completedBoard))
    }
}