package com.example.tfg.games.common

import com.example.tfg.games.kendoku.KendokuScore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.enums.EnumEntries

interface JankoBoard {
    val boardId: Int
    val difficulty: String
    val size: Int
    val problem: String
    val areas: String
    val solution: String
    var addValuesToStart: (startBoard: IntArray) -> Unit

    fun getStartBoard(): IntArray {
        val startBoard = IntArray(size*size) // Janko boards are always empty
        addValuesToStart(startBoard) // For debug
        return startBoard
    }

    fun getRegions() = areas.replace("\n"," ").split(" ").map { it.toInt() }.toIntArray()

    fun getCompletedBoard() = solution.replace("\n"," ").split(" ").map { it.toInt() }.toIntArray()
}

abstract class AbstractGameUnitTest(
    enumEntries: EnumEntries<out Enum<*>>,
    val getScore: (AbstractGame) -> String
) {

    private val scoreDebug = enumEntries.joinToString(", ") { strat ->
        strat.name.split("_").joinToString("") { s -> s.replaceFirstChar { it.uppercase() } }
    }

    private val repeat = 100


    protected abstract fun loadJankoData(): List<JankoBoard>

    protected abstract suspend fun getGameBoard(size: Int, seed: Long, difficulty: Difficulty): AbstractGame

    protected abstract fun getGameBoardJanko(seed: Long, jankoBoard: JankoBoard): AbstractGame

    protected fun testOkJankoBoard(boardID: Int) {
        val jankoBoards = loadJankoData()
        println("board, difficulty, score, times, bruteForces, regions, $scoreDebug")
        val board = jankoBoards.find { it.boardId == boardID } !!
        board.addValuesToStart = { }

        val result = testJankoBoard(board)
        assert(result == "") {
            print(result)
        }
    }

    @Test
    fun testOkJankoBoards() {
        val jankoBoards = loadJankoData()
        println("board, difficulty, score, times, brute-forces, regions, $scoreDebug")

        val result = jankoBoards.map { board ->
            board.addValuesToStart = { }
            testJankoBoard(board)
        }
        val resultNotOK = result.filter { it != "" }
        assert(resultNotOK.isEmpty()) {
            println("\nERRORS:")
            print(resultNotOK.joinToString(separator = "\n"))
        }
    }

    @Test
    fun testOkSeededBoard() {
        println("size, seed, difficulty, score, times, brute-forces, regions, $scoreDebug")
        val size = 9
        val timeout = 1500L
        val seed: Long = 1034942735
        val difficulty = Difficulty.MASTER
        val printBoards = false

        var result: String? = null
        runBlocking {
            result = withTimeoutOrNull(timeout) {
                testBoard(size, difficulty, seed, printBoards)
            }
        }

        assert(result == "") {
            print(result ?: "Timed out")
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [3,4,5,6,7,8,9])
    fun testOkBoards(size: Int) = runBlocking {
        println("size, seed, difficulty, score, times, brute-forces, regions, $scoreDebug")

        val timeout = 1500L
        val difficulty = Difficulty.MASTER
        val printBoards = false

        val seedsWithTimeout = mutableListOf<Long>()
        val result = runBlocking { (1..repeat).map {
            var res: String?
            do {
                val seed = (Math.random()*10000000000).toLong()
                res = withTimeoutOrNull(timeout) { testBoard(size, difficulty, seed, printBoards) }
                if (res == null) seedsWithTimeout.add(seed)
            } while (res == null)
            res
        } }

        println("${seedsWithTimeout.size} timeouts with timeout: $timeout")
        println("Seeds: ${seedsWithTimeout.joinToString()}")

        val resultNotOK = result.filter { it != "" }
        assert(resultNotOK.isEmpty()) {
            println("\nERRORS:")
            print(resultNotOK.joinToString(separator = "\n"))
        }
    }

    open fun testJankoBoard(board: JankoBoard, seed: Long = (Math.random()*10000000000).toLong()): String {
        val startTime = System.currentTimeMillis()
        val gameBoard = getGameBoardJanko(seed, board)
        val endTime = System.currentTimeMillis()

        val correctBoard = gameBoard.startBoard.contentEquals(gameBoard.completedBoard)
        val numBruteForces = gameBoard.score.getBruteForceValue()

        val id = board.boardId
        val difficulty = board.difficulty
        val scoreValue = gameBoard.getScoreValue()
        val time = endTime - startTime
        val regionData = gameBoard.getRegionStatData().joinToString(separator = "|")
        val score = getScore(gameBoard)

        println("$id, $difficulty, $scoreValue, $time, $numBruteForces, $regionData, $score")

        return if (correctBoard) ""
        else "\nBoard: ${board.boardId} is incorrect" +
                "\nActual board:\n${gameBoard.printStartBoard()}" +
                "\nExpected board:\n${gameBoard.printCompletedBoard()}"
    }

    private suspend fun testBoard(size: Int, difficulty: Difficulty, seed: Long, printBoards: Boolean): String {
        val mainStartTime = System.currentTimeMillis()

        val kendoku = getGameBoard(size, seed, difficulty)
        val score = kendoku.getScoreValue()
        val time = System.currentTimeMillis() - mainStartTime
        val numBruteForces = kendoku.score.getBruteForceValue()
        val regions = kendoku.getRegionStatData().joinToString(separator = "|")
        val completeScore = kendoku.score as KendokuScore

        println("${size}x${size}, $seed, $difficulty, $score, $time, $numBruteForces, $regions, $completeScore")

        if (printBoards) println(kendoku.printStartBoardHTML())

        return kendoku.boardMeetsRulesStr()
    }

}