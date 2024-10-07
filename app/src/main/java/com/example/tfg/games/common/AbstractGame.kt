package com.example.tfg.games.common

import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.tfg.common.utils.Colors
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.games.hakyuu.NumberValue
import kotlin.math.max
import kotlin.random.Random

abstract class AbstractGame(
    @PrimaryKey
    var id: Long,
    var type: Games,
    val numColumns: Int,
    val numRows: Int,
    val seed: Long,
    var score: Score,

    var completedBoard: IntArray = IntArray(numColumns * numRows),
    var boardRegions: IntArray = IntArray(numColumns * numRows),
    var startBoard: IntArray = IntArray(numColumns * numRows)
) {

    // Helper variables
    @Ignore
    private val colors: Colors = Colors()
    @Ignore
    protected var random: Random = Random(seed)
    @Ignore
    var maxAmountOfBruteForces = 20

    protected fun numPositions(): Int = numColumns * numRows
    open fun maxRegionSize(): Int = max(numColumns, numRows)

    protected fun getPositions(): IntRange {
        return (0..< numPositions())
    }

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

    @Ignore
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

    abstract fun createCompleteBoard(remainingPositions: MutableSet<Int>)

    protected open fun createGame(difficulty: Difficulty) {

        val remainingPositions = getPositions().toMutableSet()
        createCompleteBoard(remainingPositions)

        // Create startBoard

        remainingPositions.clear()
        startBoard.indices.forEach {
            startBoard[it] = completedBoard[it]
            remainingPositions.add(it) // Helper variable
        }

        maxAmountOfBruteForces = score.getMaxBruteForceValue(difficulty)
        var actualScore: Score? = null

        while (remainingPositions.isNotEmpty()) {
            // Remove random value from startBoard
            val randomPosition = remainingPositions.random(random)
            remainingPositions.remove(randomPosition)
            startBoard[randomPosition] = 0

            val tmpBoard = startBoard.clone()

            val res = solveBoard(tmpBoard)

            if (res == null || res.isTooHighForDifficulty(difficulty)) {
                // Add the value back
                startBoard[randomPosition] = completedBoard[randomPosition]
            }
            else {
                actualScore = res
            }
        }

        score.add(actualScore)
    }

    /**
     * Initialize board with the values that can be calculated at the beginning
     * and possibleValues with the possible values that that position can take.
     *
     * If there is a way to reduce the possible values that only needs to be used once, that is
     * included in this function, not inside the function populateValues)
     * **/
    protected abstract fun fillPossibleValues(possibleValues: Array<MutableList<Int>>, board: IntArray): Score

    fun solveBoard(board: IntArray): Score? {
        val possibleValues = Array(numPositions()) { mutableListOf<Int>() }
        val scoreResult = fillPossibleValues(possibleValues = possibleValues, board = board)

        val result = solveBoard(BoardData.create(possibleValues, board, type)).get() ?: return null

        scoreResult.add(result)

        return scoreResult
    }

    private fun solveBoard(boardData: BoardData, amountOfBruteForces: Int = 0): PopulateResult {
        val score = Score.create(type)

        while (getRemainingPositions(boardData.actualValues).isNotEmpty())
        {
            val res = solveBoardOneStep(boardData, amountOfBruteForces).let {
                it.get() ?: return it // Found error -> can't populate
            }

            score.add(res)
        }

        return PopulateResult.success(score)
    }

    private fun solveBoardOneStep(boardData:BoardData, amountOfBruteForces: Int): PopulateResult {
        val res = populateValues(boardData)

        return if (res.gotNoChangesFound()) bruteForce(boardData, amountOfBruteForces + 1)
        else res
    }

    /**
     * Attempt to reduce the possibleValues with different strategies and update the actualValues
     * if the possible values of a position is only one value.
     *
     * PopulateResult is a data type that includes a score and a Result enum
     * Result will be:
     * SUCCESS if the algorithm didn't find any problem.
     * NO_CHANGES if the algorithm didn't change anything.
     * CONTRADICTION if a contradiction was found.
     * NOT_UNIQUE_SOLUTION if there are more than one solution available (when using brute force).
     * MAX_BF_OVERPASSED if the maximum value of brute forces was overpassed.
     * **/
    protected abstract fun populateValues(boardData: BoardData): PopulateResult

    fun getRemainingPositions(actualValues: IntArray): List<Int> {
        return getPositions().filter { actualValues[it] == 0 }
    }

    // For debug
    @Ignore
    private val debugAmountOfBruteForces = 0
    fun solveBoardOneStep(boardData: BoardData): PopulateResult {
        if (boardData.possibleValues.all { it.size == 0 }) {
            fillPossibleValues(boardData.possibleValues, boardData.actualValues)
        }

        return solveBoardOneStep(boardData, debugAmountOfBruteForces)
    }

    private fun bruteForceAValue(
        chosenValue: Int,
        position: Int,
        boardData: BoardData,
        amountOfBruteForces: Int
    ): BruteForceResult {
        boardData.possibleValues[position].clear()
        boardData.possibleValues[position].add(chosenValue)

        val result = solveBoard(boardData, amountOfBruteForces)

        return if (result.gotSuccess()) BruteForceResult.success(BruteForceValues(boardData, result.get()!!))
        else result.errorToBruteForceResult()
    }

    private fun bruteForce(boardData: BoardData, amountOfBruteForces: Int): PopulateResult {
        if (amountOfBruteForces > maxAmountOfBruteForces) return PopulateResult.maxBFOverpassed()

        val possibleValues = boardData.possibleValues
        val actualValues = boardData.actualValues

        val (position, minPossibleValues) = getRemainingPositions(actualValues)
            .map { it to possibleValues[it] }
            .minBy { (_, values) -> values.size }

        val results = mutableListOf<BruteForceResult>()
        for(chosenValue in minPossibleValues.toList()) {
            possibleValues[position].remove(chosenValue)

            // If we have the completed board we can check the value early
            //val realValue = completedBoard[position]
            //val earlyContradiction = realValue != 0 && realValue != chosenValue
            //val result = if (earlyContradiction) BruteForceResult.contradiction()
            //    else bruteForceAValue(chosenValue, position, boardData.clone(), amountOfBruteForces)
            val result = bruteForceAValue(chosenValue, position, boardData.clone(), amountOfBruteForces)

            // Filter contradictions
            if (!result.gotContradiction()) {
                results.add(result)

                // Multiple not contradictory results
                if (results.size > 1) return PopulateResult.boardNotUnique()
            }
        }

        if (results.isEmpty()) return PopulateResult.contradiction()

        //results must have 1 element only

        val result = results.first()
        return if (result.gotSuccess()) {
            // Got only 1 valid result
            val (newBoardData, score) = result.get()!!
            boardData.replaceDataWith(newBoardData)
            score.addScoreBruteForce()
            PopulateResult.success(score)
        } else {
            // Propagate negative result
            result.errorToPopulateResult()
        }
    }



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

    protected fun regionIsOneCell(regionId: Int, position: Int): Boolean {
        return !boardRegions.withIndex().any { (pos, id) -> id == regionId && position != pos }
    }

    protected fun addValueToActualValues(
        values:  MutableList<Int>,
        actualValues: IntArray,
        position: Int,
        score: Score
    ) {
        actualValues[position] = values.first()
        values.clear()
        score.addScoreNewValue()
    }

    protected fun boardPopulated(board: IntArray): Boolean {
        return !board.any { it == 0 }
    }

    private fun boardPopulatedPrintingInfo(board: IntArray): Boolean {
        val res = board.withIndex().filter { it.value == 0 }
        if (res.isNotEmpty()){
            println("The next positions were empty ${res.joinToString(separator = ", "){ it.index.toString() }}")
            return false
        }
        return true
    }

    fun boardMeetsRulesPrintingInfo(): Boolean {
        return boardMeetsRulesPrintingInfo(completedBoard) && boardPopulatedPrintingInfo(completedBoard)
    }

    protected abstract fun boardMeetsRulesStr(board: IntArray): String
    protected fun boardMeetsRules(board: IntArray) = boardMeetsRulesStr(board) == ""
    private fun boardMeetsRulesPrintingInfo(board: IntArray): Boolean {
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

    protected fun getRowPositions(rowIndex: Int): IntProgression {
        val n = rowIndex * numColumns
        return (n ..< numColumns+n)
    }

    protected fun getColumnPositions(columnIndex: Int) = (columnIndex..< numPositions() step numColumns)
}