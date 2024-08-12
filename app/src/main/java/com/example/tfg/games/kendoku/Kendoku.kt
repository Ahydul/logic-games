package com.example.tfg.games.kendoku

import com.example.tfg.common.enums.Direction
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.common.utils.Curves
import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.GameType
import com.example.tfg.games.common.Games
import com.example.tfg.games.common.PopulateResult
import com.example.tfg.games.common.Score

class Kendoku(
    size: Int,
    seed: Long,
    score: KendokuScore = KendokuScore(),
    completedBoard: IntArray = IntArray(size * size),
    startBoard: IntArray = IntArray(size * size),
    regions: IntArray = IntArray(size * size),
    var regionOperation: Map<Int, KendokuOperation> = mutableMapOf(),
    var printEachBoardState: Boolean = false
): GameType(
    type = Games.HAKYUU,
    numColumns = size,
    numRows = size,
    seed = seed,
    score = score,
    completedBoard = completedBoard,
    startBoard = startBoard,
    boardRegions = regions
) {
    // Helper variables
    private var currentID = 0

    override fun maxRegionSize(): Int = numColumns

    /*
    override fun createGame(difficulty: Difficulty) {

        val remainingPositions = getPositions().toMutableSet()
        createCompleteBoard(remainingPositions)


        //Create start board
    }
     */

    override fun createCompleteBoard(remainingPositions: MutableSet<Int>) {
        // Create regions

        while (remainingPositions.isNotEmpty()) {
            propagateRandomEmptyRegion(remainingPositions)
        }

        // Populate cells

        latinSquare(numColumns).forEachIndexed { row, values ->
            values.forEachIndexed { column, value ->
                completedBoard[row*numColumns + column] = value
            }
        }

        // Create operations

        regionOperation = boardRegions.groupBy { it }.map { (regionID, values) ->
            val operation = KendokuOperation.entries
                .filterNot {
                    (it == KendokuOperation.DIVIDE && (values.size != 2 || (values.max() % values.min() != 0))) ||
                    (it == KendokuOperation.MULTIPLY && (values.reduce { acc, num -> acc * num } > 1000))
                }.random(random)
            regionID to operation
        }.toMap()

        println(regionOperation)
    }

    private fun propagateRandomEmptyRegion(
        remainingPositions: MutableSet<Int>,
        numPropagations: Int = randomPropagationNumber()
    ) {
        currentID++

        val seed = remainingPositions.random(random)
        boardRegions[seed] = currentID
        remainingPositions.remove(seed)

        val region = mutableListOf(seed)

        for (index in 1..numPropagations) {
            propagateOnce(remainingPositions = remainingPositions, region = region)
        }

        if (printEachBoardState) {
            print(printBoardHTML(completedBoard, boardRegions, true))
        }
    }

    private fun randomPropagationNumber(): Int {
        return ((maxRegionSize() - 1) * Curves.lowerValues(random.nextDouble(1.0))).toInt() + 1
    }

    private fun propagateOnce(remainingPositions: MutableSet<Int>, region: MutableList<Int>) {
        if (region.size == maxRegionSize()) return

        for (position in region.shuffled(random)) {
            for (direction in Direction.entries.shuffled(random)) {
                val propagation = Coordinate.move(direction = direction, position = position, numColumns=numColumns, numRows = numRows)

                if (propagation != null && remainingPositions.contains(propagation)){
                    region.add(propagation)
                    boardRegions[propagation] = currentID
                    remainingPositions.remove(propagation)
                    return
                }
            }
        }
    }

    private fun latinSquare(size: Int): Array<IntArray> {
        val latinSquare = Array(size) { IntArray(size) }

        for (i in 0 until size) {
            for (j in 0 until size) {
                latinSquare[i][j] = (i + j) % size + 1
            }
        }

        latinSquare.shuffle(random)

        val transposed = Array(size) { IntArray(size) }
        for (i in 0 until size) {
            for (j in 0 until size) {
                transposed[i][j] = latinSquare[j][i]
            }
        }
        transposed.shuffle(random)
        for (i in 0 until size) {
            for (j in 0 until size) {
                latinSquare[i][j] = transposed[j][i]
            }
        }

        val perm = (1..size).shuffled(random)
        for (i in 0 until size) {
            for (j in 0 until size) {
                latinSquare[i][j] = perm[latinSquare[i][j] - 1]
            }
        }

        return latinSquare
    }

    override fun fillPossibleValues(
        possibleValues: Array<MutableList<Int>>,
        board: IntArray
    ): Score {
        TODO("Not yet implemented")
    }

    override fun populateValues(
        possibleValues: Array<MutableList<Int>>,
        actualValues: IntArray
    ): PopulateResult {
        TODO("Not yet implemented")
    }

    override fun boardMeetsRulesStr(board: IntArray): String {
        TODO("Not yet implemented")
    }

    override fun checkValue(position: Int, value: Int, actualValues: IntArray): Set<Int> {
        TODO("Not yet implemented")
    }


    companion object {
        fun create(size: Int, seed: Long, difficulty: Difficulty, printEachBoardState: Boolean = false): Kendoku {
            val kendoku = Kendoku(
                size = size,
                seed = seed,
                printEachBoardState = printEachBoardState
            )

            kendoku.createGame(difficulty)

            return kendoku
        }
    }
}