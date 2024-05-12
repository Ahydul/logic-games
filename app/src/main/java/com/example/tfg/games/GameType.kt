package com.example.tfg.games

import com.example.tfg.common.utils.Colors
import com.example.tfg.common.utils.Coordinate
import kotlin.math.max
import kotlin.random.Random

abstract class GameType(
    open val type: Games,
    open val noNotes: Boolean = false,
    open val numColumns: Int,
    open val numRows: Int,
    protected open val random: Random,
    protected val score: Score
) {
    protected val numPositions: Int = numColumns * numRows
    val completedBoard: IntArray = IntArray(numPositions)
    val boardRegions: IntArray = IntArray(numPositions)
    val startBoard: IntArray = IntArray(numPositions)
    val maxRegionSize = max(numColumns, numRows)

    private val colors: Colors = Colors()

    protected open fun reset() {
        completedBoard.map { 0 }
        boardRegions.map { 0 }
        startBoard.map { 0 }
    }

    fun getScore(): Int {
        return score.get()
    }

    fun printBoard() {
        return printBoard(completedBoard)
    }

    fun printBoard(board: IntArray) {
        val colorMap = mutableMapOf<Int,String>()

        var htmlCode = """<table style="font-size: large; border-collapse: collapse; margin: 20px auto;"><tbody>"""

        (0..<numPositions).forEach {
            val num = board[it]
            val id = boardRegions[it]

            if (!colorMap.containsKey(id)) {
                val color = colors.newColor()
                colorMap[id] = color
            }

            if (it%numColumns == 0){
                htmlCode += """<tr>"""
            }

            htmlCode += """<td style="background-color: ${colorMap[id]}; vertical-align: middle; text-align: center; height: 40px; width: 40px;">$num</td>"""

            if (it%numColumns == numColumns-1){
                htmlCode += """</tr>"""
            }

        }
        htmlCode +="""</tbody></table>"""
        print(htmlCode)
    }

    fun getRegionStatData(): IntArray {
        val ls = boardRegions.groupBy { it }.map { it.value.size }
        val result = IntArray(size = ls.max())
        ls.forEach {
            result[it-1]++
        }
        return result
    }

    fun getRegions(): Map<Int, List<Coordinate>> {
        val res: MutableMap<Int, MutableList<Coordinate>> = mutableMapOf()
        boardRegions.forEachIndexed { position, regionId ->
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

    abstract fun createGame(): Boolean
    protected abstract fun solveBoard(board: IntArray): Boolean

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
        return boardRegions.withIndex().filter { (_, id) -> id == regionId }.map { (index, _) -> index }.toSet()
    }

    fun boardMeetsRules(): Boolean {
        return boardMeetsRules(completedBoard)
    }

    protected abstract fun boardMeetsRules(board: IntArray): Boolean

    protected fun boardPopulated(actualValues: IntArray): Boolean {
        return actualValues.all { it != 0 }
    }

    abstract fun checkValue(position: Int, value: Int, actualValues: IntArray): Set<Int>
    fun isError(position: Int, value: Int): Boolean {
        return completedBoard[position] != value
    }

}