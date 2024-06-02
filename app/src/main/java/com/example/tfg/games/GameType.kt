package com.example.tfg.games

import androidx.room.Ignore
import com.example.tfg.common.Difficulty
import com.example.tfg.common.utils.Colors
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.games.hakyuu.NumberValue
import kotlin.math.max
import kotlin.random.Random

abstract class GameType(
    val type: Games,
    val numColumns: Int,
    val numRows: Int,
    val seed: Long,
    val score: Score,

    var completedBoard: IntArray = IntArray(numColumns * numRows),
    var boardRegions: IntArray = IntArray(numColumns * numRows),
    var startBoard: IntArray = IntArray(numColumns * numRows)
) {

    @Ignore
    private val colors: Colors = Colors()
    @Ignore
    protected var random: Random = Random(seed)

    protected fun numPositions(): Int = numColumns * numRows
    fun maxRegionSize(): Int = max(numColumns, numRows)

    fun getScoreValue(): Int {
        return score.get()
    }

    fun printCompletedBoard(): String {
        return printBoard(completedBoard)
    }

    fun printStartBoard(): String {
        return printBoard(startBoard)
    }

    fun printBoard(board: IntArray): String {
        val colorMap = mutableMapOf<Int, String>()

        var htmlCode =
            """<table style="font-size: large; border-collapse: collapse; margin: 20px auto;"><tbody>"""

        (0..<numPositions()).forEach {
            val num = board[it]
            val id = boardRegions[it]

            if (!colorMap.containsKey(id)) {
                val color = colors.newColor()
                colorMap[id] = color
            }

            if (it % numColumns == 0) {
                htmlCode += """<tr>"""
            }

            htmlCode += """<td style="background-color: ${colorMap[id]}; vertical-align: middle; text-align: center; height: 40px; width: 40px;">$num</td>"""

            if (it % numColumns == numColumns - 1) {
                htmlCode += """</tr>"""
            }

        }
        htmlCode += """</tbody></table>"""
        return htmlCode
    }

    fun getRegionStatData(): IntArray {
        val ls = boardRegions.groupBy { it }.map { it.value.size }
        val result = IntArray(size = ls.max())
        ls.forEach {
            result[it - 1]++
        }
        return result
    }

    fun getRegions(): Map<Int, List<Coordinate>> {
        val res: MutableMap<Int, MutableList<Coordinate>> = mutableMapOf()
        this.boardRegions.forEachIndexed { position, regionId ->
            val coordinate = Coordinate.fromIndex(
                index = position,
                numRows = numRows,
                numColumns = numColumns
            )
            if (res.containsKey(regionId)) res[regionId]!!.add(coordinate)
            else res[regionId] = mutableListOf(coordinate)
        }
        return res
    }

    protected abstract fun createGame(difficulty: Difficulty)
    protected abstract fun solveBoard(board: IntArray): Score?

    protected fun deleteRegion(regionId: Int) {
        boardRegions.withIndex().filter { (_, id) -> id == regionId }
            .forEach { (position, _) ->
                boardRegions[position] = 0
                completedBoard[position] = 0
            }
    }

    protected fun getRegionId(position: Int): Int {
        return boardRegions[position]
    }

    protected fun getRegionPositions(regionId: Int): Set<Int> {
        return boardRegions.withIndex().filter { (_, id) -> id == regionId }
            .map { (index, _) -> index }.toSet()
    }

    protected fun getRegionSize(regionId: Int): Int {
        return boardRegions.count { id -> id == regionId }
    }

    private fun boardIsCompleted(board: IntArray): Boolean {
        return !board.any { it == 0 }
    }

    fun boardMeetsRules(): Boolean {
        return boardMeetsRules(completedBoard) && boardIsCompleted(completedBoard)
    }

    protected abstract fun boardMeetsRules(board: IntArray): Boolean

    protected fun boardPopulated(actualValues: IntArray): Boolean {
        return actualValues.all { it != 0 }
    }

    abstract fun checkValue(position: Int, value: Int, actualValues: IntArray): Set<Int>
    fun isError(position: Int, value: Int): Boolean {
        return completedBoard[position] != value
    }

    fun getValue(value: Int): GameValue {
        return NumberValue.get(value)
    }
}