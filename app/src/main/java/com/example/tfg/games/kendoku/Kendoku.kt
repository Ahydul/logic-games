package com.example.tfg.games.kendoku

import androidx.room.Entity
import androidx.room.Ignore
import com.example.tfg.common.IdGenerator
import com.example.tfg.common.enums.Direction
import com.example.tfg.common.utils.Coordinate
import com.example.tfg.common.utils.Curves
import com.example.tfg.common.utils.Utils
import com.example.tfg.games.common.AbstractGame
import com.example.tfg.games.common.BoardData
import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.Games
import com.example.tfg.games.common.PopulateResult
import com.example.tfg.games.common.Score

@Entity
class Kendoku(
    id: Long = IdGenerator.generateId("kendokuGame"),
    size: Int,
    seed: Long,
    score: KendokuScore = KendokuScore(),
    completedBoard: IntArray = IntArray(size * size),
    startBoard: IntArray = IntArray(size * size),
    regions: IntArray = IntArray(size * size),
    private val operationPerRegion: MutableMap<Int, KendokuOperation> = mutableMapOf(),
    private val allowedOperations: Array<KendokuOperation> = KendokuOperation.allOperations(),

    @Ignore
    private var printEachBoardState: Boolean = false
): AbstractGame(
    id = id,
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
    private val operationResultPerRegion: MutableMap<Int, Int> = mutableMapOf()
    private val knownOperations = BruteForceHelper(operationPerRegion)

    // Use this instead of numColumns or numRows
    private val size = numColumns

    override fun maxRegionSize(): Int = size

    override fun createGame(difficulty: Difficulty) {
        super.createGame(difficulty)
        score.reset()

        var actualScore: Score? = null
        val knownOperationsPerRegion = operationPerRegion.filterValues { !it.isUnknown() }.toMutableMap()
        while (knownOperationsPerRegion.isNotEmpty()) {
            // Remove random value from remainingOperationsPerRegion
            val randomRegion = knownOperationsPerRegion.keys.random(random)
            knownOperationsPerRegion.remove(randomRegion)

            // Reverse the random region operation to its unknown version
            operationPerRegion[randomRegion] = operationPerRegion[randomRegion]!!.reverse()

            val tmpBoard = startBoard.clone()

            val res = solveBoard(tmpBoard)

            if (res == null || res.isTooHighForDifficulty(difficulty)) {
                // Reverse the operation back
                operationPerRegion[randomRegion] = operationPerRegion[randomRegion]!!.reverse()
            }
            else {
                actualScore = res
            }
        }

        score.add(actualScore)
    }

    override fun createCompleteBoard(remainingPositions: MutableSet<Int>) {
        // Create regions

        while (remainingPositions.isNotEmpty()) {
            propagateRandomEmptyRegion(remainingPositions)
        }

        // Populate cells

        latinSquare(size).forEachIndexed { row, values ->
            values.forEachIndexed { column, value ->
                completedBoard[row*size + column] = value
            }
        }

        // Create operations

        boardRegions.groupBy { it }.forEach { (regionID, values) ->
            val operation = if (values.size == 1) KendokuOperation.SUM_UNKNOWN
                else KendokuOperation.knownOperations().filterNot {
                        // Filter out disallowed operations
                    !allowedOperations.contains(it) ||
                        // Subtractions can't have more than 2 operands
                    (it == KendokuOperation.SUBTRACT && values.size != 2) ||
                        // Divisions can't have more than 2 operands and must result in integers
                    (it == KendokuOperation.DIVIDE && (values.size != 2 || (values.max() % values.min() != 0))) ||
                        // Multiplications can't be result in numbers higher than 1000
                    (it == KendokuOperation.MULTIPLY && (values.reduce { acc, num -> acc * num } > 1000))
                }.random(random)

            operationResultPerRegion[regionID] = operation.operate(values)
            operationPerRegion[regionID] = operation
        }
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
                val propagation = Coordinate.move(direction = direction, position = position, numColumns=size, numRows = size)

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

    override fun fillPossibleValues(possibleValues: Array<MutableList<Int>>, board: IntArray): Score {
        // These arrays shows the possible values allowed in each column and row.
        // At the beginning its all the values (1..size)
        val columnsPossibleValues = Array(size - 1){ (1..size).toMutableSet() }
        val rowsPossibleValues = Array(size - 1){ (1..size).toMutableSet() }
        val scoreResult = KendokuScore()

        for (position in (0..<numPositions())) {
            val regionID = getRegionId(position)

            val coordinate = Coordinate.fromIndex(index = position, size, size)

            // values have the common possible values of the row and column of the coordinate
            val values = rowsPossibleValues[coordinate.row].intersect(columnsPossibleValues[coordinate.column])
            val value = if (values.size == 1) values.first() else operationResultPerRegion[regionID]!!

            // If values is only one value, that value is the only possible value in that position.
            // If not, if the region is of size 1 operationResultPerRegion[regionID] is the value in that position.
            if (board[position] == 0 && (values.size == 1 || regionIsOneCell(regionID, position))) {
                board[position] = value
                scoreResult.addScoreNewValue()

                columnsPossibleValues[coordinate.column].remove(value)
                rowsPossibleValues[coordinate.row].remove(value)
            }
            else if (board[position] == 0) {
                possibleValues[position].addAll(values)
            }
        }

        return scoreResult
    }

    private fun deduceOperation(
        regionID: Int,
        region: MutableList<Int>,
        possibleValues: Array<MutableList<Int>>,
        actualValues: IntArray
    ): KnownKendokuOperation? {
        //TODO: Maybe this isn't exhaustive: An area can be sum or multiply but the numbers are the same 2*2 = 2+2
        val operations = allowedOperations.filter {
            it.filterOperation(
                region.associate { position ->
                    val values = possibleValues[position]
                    if (values.isEmpty()) values.add(actualValues[position])
                    Coordinate.fromIndex(position, size, size) to values
                }
            )
        }
        if (operations.size == 1) {
            val res = operations.first().reverse().toKnownEnum()!!
            knownOperations.set(regionID = regionID, operation = res)
            return res
        }
        return null
    }

    override fun populateValues(boardData: BoardData): PopulateResult {
        val score = KendokuScore()

        val possibleValues = boardData.possibleValues
        val actualValues = boardData.actualValues

        val regions = mutableMapOf<Int, MutableList<Int>>()

        for (position in getPositions()) {
            val regionID = getRegionId(position)
            Utils.addToMapList(regionID, position, regions)

            if (actualValues[position] == 0) continue

            val values = possibleValues[position]
            if (values.size == 1) {
                addValueToActualValues(values, actualValues, position, score)
                val coordinate = Coordinate.fromIndex(index = position, size, size)

                val value = values.first()

                // Delete value from the possible values in the row of position
                (0..< size).filter { row -> row != coordinate.row }.mapNotNull { row ->
                    Coordinate(row = row, column = coordinate.column).toIndex(size,size)
                }.forEach { index ->
                    possibleValues[index].remove(value)
                }

                // Delete value from the possible values in the column of position
                (0..< size).filter { column -> column != coordinate.column }.mapNotNull { column ->
                    Coordinate(row = coordinate.row, column = column).toIndex(size,size)
                }.forEach { index ->
                    possibleValues[index].remove(value)
                }
            }
            else if(values.size == 0) {
                return PopulateResult.contradiction()
            }
        }

        // Possible values changed
        if (score.get() > 0) return PopulateResult.success(score)

        for ((regionID, region) in regions.entries) {
            val operation = knownOperations.get(regionID)
                ?: deduceOperation(regionID, region, possibleValues, actualValues)
                ?: continue

            val operationRes = operationResultPerRegion[regionID]!!
            val combinations = getRegionCombinations(possibleValues, actualValues, region, operationRes, operation)


            TODO()
        }


        return if (score.get() > 0) PopulateResult.success(score)
        else PopulateResult.noChangesFound()
    }

    internal fun getRegionSumCombinations(
        possibleValues: Array<MutableList<Int>>,
        region: MutableList<Int>,
        sum: Int
    ): List<IntArray> {
        val combinations = mutableListOf<IntArray>()
        val regionSize = region.size
        val lastCombination = IntArray(regionSize)

        val helper = region.mapIndexed { index, position1  ->
            region.slice(0..< index).reversed().filter { position2 ->
                Coordinate.sameColumnOrRow(position1 = position1, position2 = position2, numColumns = size)
            }.map { region.indexOf(it) }
        }.toTypedArray()

        val valueAlreadyInRowOrColumn = { index: Int, value: Int ->
            helper[index].any { lastCombination[it] == value }
        }

        fun backtrack(index: Int = 0, actualSum: Int = 0): Boolean {
            val values = possibleValues[region[index]]
            if (index == regionSize - 1) {
                val subtraction = sum - actualSum
                if (values.contains(subtraction) && !valueAlreadyInRowOrColumn(index, subtraction)) {
                    lastCombination[index] = subtraction
                    combinations.add(lastCombination.clone())
                }
                return values.last() > subtraction
            }

            for (value in values.reversed()) {
                val newActualSum = actualSum + value
                if (newActualSum >= sum) continue
                if (valueAlreadyInRowOrColumn(index, value)) continue

                lastCombination[index] = value
                if (!backtrack(index = index + 1, actualSum = newActualSum)) break
            }

            return true
        }


        backtrack()

        return combinations
    }

    internal fun getRegionSubtractCombinations(
        possibleValues: Array<MutableList<Int>>,
        region: MutableList<Int>,
        subtraction: Int
    ): List<IntArray> {
        val combinations = mutableListOf<IntArray>()
        val possibleValues1 = possibleValues[region[0]]
        val possibleValues2 = possibleValues[region[1]]
        (subtraction+1 .. size).forEach { int ->
            val int2 = int-subtraction
            if (possibleValues1.contains(int) && possibleValues2.contains(int2))
                combinations.add(intArrayOf(int, int2))
            if (possibleValues1.contains(int2) && possibleValues2.contains(int))
                combinations.add(intArrayOf(int2, int))
        }
        return combinations
    }

    internal fun getRegionMultiplyCombinations(
        possibleValues: Array<MutableList<Int>>,
        region: MutableList<Int>,
        multiplication: Int
    ): List<IntArray> {
        val combinations = mutableListOf<IntArray>()
        val regionSize = region.size
        val lastCombination = IntArray(regionSize)

        val helper = region.mapIndexed { index, position1  ->
            region.slice(0..< index).reversed().filter { position2 ->
                Coordinate.sameColumnOrRow(position1 = position1, position2 = position2, numColumns = size)
            }.map { region.indexOf(it) }
        }.toTypedArray()

        val valueAlreadyInRowOrColumn = { index: Int, value: Int ->
            helper[index].any { lastCombination[it] == value }
        }

        fun backtrack(index: Int = 0, actualMultiplication: Int = 1): Boolean {
            val values = possibleValues[region[index]]
            if (index == regionSize - 1) {
                val division = multiplication / actualMultiplication
                if (multiplication % actualMultiplication == 0 &&
                    values.contains(division) &&
                    !valueAlreadyInRowOrColumn(index, division))
                {
                    lastCombination[index] = division
                    combinations.add(lastCombination.clone())
                }
                return values.last() > division
            }

            for (value in values.reversed()) {
                val newActualMultiplication = actualMultiplication * value
                if (newActualMultiplication >= multiplication) continue
                if (valueAlreadyInRowOrColumn(index, value)) continue

                lastCombination[index] = value
                if (!backtrack(index = index + 1, actualMultiplication = newActualMultiplication)) break
            }

            return true
        }


        backtrack()

        return combinations
    }

    internal fun getRegionDivideCombinations(
        possibleValues: Array<MutableList<Int>>,
        region: MutableList<Int>,
        division: Int
    ): List<IntArray> {
        val combinations = mutableListOf<IntArray>()
        val possibleValues1 = possibleValues[region[0]]
        val possibleValues2 = possibleValues[region[1]]
        (2..division)
            .filter { division%it == 0 }
            .forEach { int ->
                val int2 = division/int
                if (possibleValues1.contains(int2) && possibleValues2.contains(int))
                    combinations.add(intArrayOf(int2, int))
                if (possibleValues1.contains(int) && possibleValues2.contains(int2))
                    combinations.add(intArrayOf(int, int2))
            }
        (division+1..size)
            .filter { it%division == 0 }
            .forEach { int ->
                val int2 = int/division
                if (possibleValues1.contains(int2) && possibleValues2.contains(int))
                    combinations.add(intArrayOf(int2, int))
                if (possibleValues1.contains(int) && possibleValues2.contains(int2))
                    combinations.add(intArrayOf(int, int2))
            }

        return combinations
    }


    private fun getRegionCombinations(
        possibleValues: Array<MutableList<Int>>,
        board: IntArray,
        region: MutableList<Int>,
        operationResult: Int,
        operation: KnownKendokuOperation
    ): List<IntArray> {
        return when(operation){
            KnownKendokuOperation.SUM -> getRegionSumCombinations(possibleValues, region, operationResult)
            KnownKendokuOperation.SUBTRACT -> getRegionSubtractCombinations(possibleValues, region, operationResult)
            KnownKendokuOperation.MULTIPLY -> getRegionMultiplyCombinations(possibleValues, region, operationResult)
            KnownKendokuOperation.DIVIDE -> TODO()
        }
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