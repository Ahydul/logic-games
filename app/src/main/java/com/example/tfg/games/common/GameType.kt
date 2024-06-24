package com.example.tfg.games.common

import androidx.room.Ignore
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
    
    private fun printBoard(board: IntArray): String {
        val res = board.withIndex().joinToString(separator = "") { (index, value) ->
            val column = Coordinate.getColumn(index = index, numColumns = numColumns)
            if (index == numPositions()-1) "$value"
            else if (column == numColumns-1) "$value\n"
            else "$value "
        }
        return res
    }

    fun printCompletedBoardHTML(): String {
        return printBoardHTML(completedBoard, boardRegions)
    }

    fun printStartBoardHTML(): String {
        return printBoardHTML(startBoard, boardRegions)
    }

    private var colorMap = mutableMapOf<Int, String>()
    protected fun printBoardHTML(board: IntArray, regions: IntArray, usePreviousColorMap: Boolean = false): String {
        if (!usePreviousColorMap) colorMap = mutableMapOf()

        var htmlCode =
            """<table style="font-size: large; border-collapse: collapse; margin: 20px auto;"><tbody>"""

        (0..<numPositions()).forEach {
            val num = board[it]
            val id = regions[it]

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

    abstract fun solveBoard2(board: IntArray): IntArray

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

    protected fun boardPopulated(board: IntArray): Boolean {
        return !board.any { it == 0 }
    }

    private fun boardPopulated2(board: IntArray): Boolean {
        val res = board.withIndex().filter { it.value == 0 }
        if (res.isNotEmpty()){
            println("The next positions were empty ${res.joinToString(separator = ", "){ it.index.toString() }}")
            return false
        }
        return true
    }

    fun boardMeetsRules(): Boolean {
        return boardMeetsRules2(completedBoard) && boardPopulated2(completedBoard)
    }

    protected abstract fun boardMeetsRulesStr(board: IntArray): String
    protected fun boardMeetsRules(board: IntArray) = boardMeetsRulesStr(board) == ""
    private fun boardMeetsRules2(board: IntArray): Boolean {
        val res = boardMeetsRulesStr(board)
        print(res)
        return res == ""
    }

    abstract fun checkValue(position: Int, value: Int, actualValues: IntArray): Set<Int>
    fun isError(position: Int, value: Int): Boolean {
        return completedBoard[position] != value
    }

    fun getValue(value: Int): GameValue {
        return NumberValue.get(value)
    }
}