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
import com.example.tfg.games.common.CommonStrategies
import com.example.tfg.games.common.Difficulty
import com.example.tfg.games.common.Games
import com.example.tfg.games.common.PopulateResult
import com.example.tfg.games.common.Score
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.coroutineContext
import kotlin.math.sqrt

@Entity
open class Kendoku @JvmOverloads constructor(
    id: Long = IdGenerator.generateId("kendokuGame"),
    numColumns: Int,
    numRows: Int,
    seed: Long,
    type: Games = Games.KENDOKU,
    score: Score = KendokuScore(),
    completedBoard: IntArray = IntArray(numColumns * numRows),
    startBoard: IntArray = IntArray(numColumns * numRows),
    boardRegions: IntArray = IntArray(numColumns * numRows),
    var operationPerRegion: MutableMap<Int, KendokuOperation> = mutableMapOf(),
    var allowedOperations: Array<KnownKendokuOperation> = KnownKendokuOperation.allOperations(),
): AbstractGame(
    id = id,
    type = type,
    numColumns = numColumns,
    numRows = numRows,
    seed = seed,
    score = score,
    completedBoard = completedBoard,
    startBoard = startBoard,
    boardRegions = boardRegions
) {

    @delegate:Ignore
    @get:Ignore
    private val strategies by lazy { CommonStrategies(this) }

    // Use this instead of numColumns or numRows
    @Ignore
    private val size = numColumns

    // Helper variables
    @Ignore
    private var currentID = 0
    @Ignore
    private val primes = listOf(1,2,3,5,7,11,13,17,19).takeWhile { it <= size }

    @Ignore
    var positionsPerRegion = initPositionsPerRegion()

    private fun initPositionsPerRegion() = getPositions().groupBy(::getRegionId)

    @Ignore
    val operationResultPerRegion = initOperationResultPerRegion()

    private fun initOperationResultPerRegion(): MutableMap<Int, Int> {
        return operationPerRegion.mapValues { (regionID, operation) ->
            operation.operate(positionsPerRegion[regionID]!!.map { completedBoard[it] })
        }.toMutableMap()
    }

    override fun maxRegionSize(): Int = size

    override suspend fun createGame(difficulty: Difficulty) {
        super.createGame(difficulty)

        val knownOperationsPerRegion = operationPerRegion.filterValues { !it.isUnknown() }.toMutableMap()
        while (allowedOperations.size > 1 && knownOperationsPerRegion.isNotEmpty()) {
            if (!coroutineContext.isActive) return

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
                score = res
            }
        }
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

        createOperations(initPositionsPerRegion())

        // Reinitialize positionsPerRegion
        positionsPerRegion = initPositionsPerRegion()

    }

    private fun createOperations(regions: Map<Int, List<Int>>) {
        for ((regionID, positions) in regions) {
            val values = positions.map { completedBoard[it] }
            val operation = if (values.size == 1) KendokuOperation.SUM_UNKNOWN
            else allowedOperations.filterNot {
                // Subtractions can't have more than 2 operands
                (it == KnownKendokuOperation.SUBTRACT && values.size != 2) ||
                        // Divisions can't have more than 2 operands and must result in integers
                        (it == KnownKendokuOperation.DIVIDE && (values.size != 2 || (values.max() % values.min() != 0))) ||
                        // Multiplications can't be result in numbers higher than 1000
                        (it == KnownKendokuOperation.MULTIPLY && (values.reduce { acc, num -> acc * num } > 1000))
            }.map { it.toGeneralEnum() }.randomOrNull(random)

            if (operation == null) {
                createOperations(divideRegion(regionID, positions))
                continue
            }

            operationResultPerRegion[regionID] = operation.operate(values)
            operationPerRegion[regionID] = operation
        }
    }

    internal fun divideRegion(regionID: Int, positions: List<Int>): Map<Int, List<Int>> {
        val addIfConnected = { connectedPositions: MutableList<Int>, position: Int ->
            if (connectedPositions.any { position2 -> Coordinate.areConnected(position, position2, size) }) connectedPositions.add(position)
            else false
        }
        val connectedPositions = mutableListOf(positions.first())
        positions.drop(1).forEach { position ->
            if (connectedPositions.size < positions.size/2) addIfConnected(connectedPositions, position)
        }

        val tmp = positions.filterNot { position -> connectedPositions.contains(position) }.toMutableList()
        val otherConnectedPositions = mutableListOf(tmp.first())
        tmp.drop(1).forEach { position -> addIfConnected(otherConnectedPositions, position) }

        tmp.filterNot { position -> otherConnectedPositions.contains(position) }
            .forEach { position -> if (!addIfConnected(connectedPositions, position)) addIfConnected(otherConnectedPositions, position) }

        val otherID = ++currentID
        otherConnectedPositions.forEach { position -> boardRegions[position] = otherID }
        return mapOf(regionID to connectedPositions, otherID to otherConnectedPositions)
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
        val scoreResult = KendokuScore()
        val checkedRegions = mutableListOf<Int>()

        for (position in (0..< numPositions())) {
            if (board[position] != 0) continue
            val regionID = getRegionId(position)
            if (!checkedRegions.contains(regionID) && regionIsOneCell(regionID, position)) {
                board[position] = operationResultPerRegion[regionID]!!
                scoreResult.addScoreNewValue()
            }
            else {
                checkedRegions.add(regionID)
                possibleValues[position].addAll((1..size).toMutableSet())
            }
        }

        board.withIndex().filter { it.value != 0 }.forEach { (index, value) ->
            val coordinate = Coordinate.fromIndex(index, size, size)
            getRowPositions(coordinate.row).forEach { possibleValues[it].remove(value) }
            getColumnPositions(coordinate.column).forEach { possibleValues[it].remove(value) }
        }

        return scoreResult
    }

    override fun populateValues(boardData: BoardData): PopulateResult {
        val score = KendokuScore()

        val boardData = boardData as KendokuBoardData
        if (!boardData.isInitialized()) boardData.initialize(operationPerRegion)

        val possibleValues = boardData.possibleValues
        val actualValues = boardData.actualValues
        val knownOperations = boardData.knownOperations
        val regionCombinations = boardData.regionCombinations

        for (position in getPositions()) {
            if (actualValues[position] != 0) continue

            val values = possibleValues[position]
            if (values.size == 1) {
                val value = values.first()
                addValueToActualValues(values, actualValues, position, score)
                val coordinate = Coordinate.fromIndex(index = position, size, size)

                // Delete value from the possible values in the row of position
                getRowPositions(coordinate.row).forEach { index -> possibleValues[index].remove(value) }

                // Delete value from the possible values in the column of position
                getColumnPositions(coordinate.column).forEach { index -> possibleValues[index].remove(value) }

                // When brute force has been used the value may be wrong. We check the region
                val regionID = getRegionId(position)
                val regionValues = positionsPerRegion[regionID]!!.map { actualValues[it] }
                if (regionValues.all { it != 0 } && !checkRegionIsOk(regionValues, regionID)) {
                    return PopulateResult.contradiction()
                }
            }
            else if(values.size == 0) {
                return PopulateResult.contradiction()
            }
        }

        // Possible values changed
        if (score.get() > 0) return PopulateResult.success(score)

        executeForEachLine { line, size ->
            val linePossibleValues = line.map { possibleValues[it] }.toTypedArray()
            val numPairs = strategies.cleanNakedPairsInLine(linePossibleValues)
            score.addNakedPairs(numPairs)

            val numTriples = strategies.cleanNakedTriplesInLine(linePossibleValues)
            score.addNakedTriples(numTriples)

            val numSPT = strategies.cleanHiddenSinglesPairsTriplesInline(linePossibleValues, size)
            score.addHiddenSPT(numSPT)
        }

        // Possible values changed
        if (score.get() > 0) return PopulateResult.success(score)


        for ((regionID, region) in positionsPerRegion.entries) {
            val regionValues = region.map {
                val possVal = possibleValues[it]
                if (possVal.isEmpty()) mutableListOf(actualValues[it])
                else possVal
            }.toTypedArray()

            if (regionValues.all { it.size == 1 }) { //Region is completed
                regionCombinations.remove(regionID)
                continue
            }


            val combinations = regionCombinations[regionID] ?.also { reduceCombinations(it, regionValues) }
                ?: run {
                    val operationRes = operationResultPerRegion[regionID]!!
                    knownOperations[regionID]
                        ?.let { getRegionCombinations(boardData.possibleValues, boardData.actualValues, region, operationRes, it) }
                        ?: run { forceGetRegionCombinations(boardData, regionID, region, operationRes) }
                }.also { boardData.setRegionCombinations(regionID, it) }


            val regionIndexesPerColumn = mutableMapOf<Int, MutableList<Int>>()
            val regionIndexesPerRow = mutableMapOf<Int, MutableList<Int>>()
            region.forEachIndexed { index, position ->
                val coordinate = Coordinate.fromIndex(position, size, size)
                regionIndexesPerRow.getOrPut(coordinate.row) { mutableListOf() }.add(index)
                regionIndexesPerColumn.getOrPut(coordinate.column) { mutableListOf() }.add(index)
            }

            val numValuesRemoved = reducePossibleValuesUsingCombinations(combinations, region, possibleValues)
            score.addCombinations(numValuesRemoved)

            // Possible values changed
            if (numValuesRemoved > 0) return PopulateResult.success(score)


            // This reduces combinations only
            val numCUO1 = cageUnitOverlapType1(region, combinations, actualValues, possibleValues, regionIndexesPerColumn, regionIndexesPerRow)
            score.addCageUnitOverlapType1(numCUO1)

            // This reduces combinations only
            val numChanges2 = biValueAttackOnRegion(region, possibleValues, combinations, regionIndexesPerColumn, regionIndexesPerRow)
            score.addBiValueAttack(numChanges2)

            val numCUO2 = cleanCageUnitOverlapType2(regionID, region, combinations, possibleValues)
            score.addCageUnitOverlapType2(numCUO2)

            val numValuesRemoved2 = reducePossibleValuesUsingCombinations(combinations, region, possibleValues)
            score.addCombinations(numValuesRemoved2)
        }

        // Possible values changed
        if (score.get() > 0) return PopulateResult.success(score)


        executeForEachLine { line ->
            combinationComparison(line, positionsPerRegion, regionCombinations)
        }

        // Possible values changed
        if (score.get() > 0) return PopulateResult.success(score)


        val numInniesOuties = cleanInniesAndOuties(actualValues, positionsPerRegion, possibleValues) { regionID -> knownOperations[regionID] == KnownKendokuOperation.SUM }
        score.addInniesOuties(numInniesOuties)

        // Possible values changed
        if (score.get() > 0) return PopulateResult.success(score)


        val lockedNumbersInRows = strategies.getLockedNumbersInRow(possibleValues = possibleValues)
        val lockedNumbersInColumns = strategies.getLockedNumbersInColumn(possibleValues = possibleValues)

        val numXWings = strategies.cleanXWing(possibleValues, lockedNumbersInRows, lockedNumbersInColumns)
        score.addXWings(numXWings)

        val numColoring = strategies.cleanColoring(possibleValues, size, lockedNumbersInRows, lockedNumbersInColumns)
        score.addColoring(numColoring)


        return if (score.get() > 0) PopulateResult.success(score)
        else PopulateResult.noChangesFound()
    }

    private fun checkRegionIsOk(regionValues: List<Int>, regionID: Int): Boolean {
        return allowedOperations
            .filterNot { regionValues.size > 2 && it.isDivideOrSubtract() }
            .any { it.operate(regionValues) == operationResultPerRegion[regionID] }
    }

    internal fun combinationComparison(
        line: IntProgression,
        regions: Map<Int, List<Int>>,
        regionCombinations: MutableMap<Int, MutableList<IntArray>>
    ) {
        val lineCombinations = mutableListOf<MutableList<IntArray>>()
        val combinationsIndexes = mutableListOf<MutableList<Int>>()

        fun foundContradiction(indexes: List<Int>, values: List<Int>): Boolean {
            if (indexes.isEmpty()) return false

            val index = indexes.first()
            val combIndexes = combinationsIndexes[index]
            val combinations = lineCombinations[index].filterNot { combination ->
                combIndexes.any { i -> values.contains(combination[i]) }
            }

            return combinations.isEmpty() || combinations.all { combination ->
                foundContradiction(indexes.drop(1), combination.filterIndexed { i, _ -> combIndexes.contains(i) }.plus(values))
            }
        }

        // Initialize lineCombinations and combinationsIndexes
        var position = line.first
        while (position <= line.last) {
            val regionID = getRegionId(position)
            val positions = regions[regionID]!!
            val combinations = regionCombinations[regionID]

            if (combinations == null){
                position += line.step
                continue
            }

            //if (combinations.size > 9) return

            lineCombinations.add(combinations)

            val indexes = mutableListOf<Int>()
            var index = positions.indexOf(position)
            while (index != -1) {
                indexes.add(index)
                position += line.step
                index = positions.indexOf(position)
            }
            combinationsIndexes.add(indexes)
        }

        val indexes = lineCombinations.map { it.size }.withIndex().sortedBy { it.value }.map { it.index }
        indexes.forEach { index ->
            val combinations = lineCombinations[index]
            val combIndexes = combinationsIndexes[index]
            combinations.removeIf { combination ->
                foundContradiction(indexes.filter { it != index }, combination.filterIndexed { i, _ -> combIndexes.contains(i) })
            }
        }
    }

    internal fun cleanInniesAndOuties(
        actualValues: IntArray,
        regions: Map<Int, List<Int>>,
        possibleValues: Array<MutableList<Int>>,
        getRegionID: (Int) -> Int = { getRegionId(it) },
        regionIsSum: (Int) -> Boolean
    ): Int {
        fun clean(lines: IntRange, rectangleSum: Int, getLinePositions: (Int) -> IntProgression): Int {
            var sum = 0
            val unknownPositions = mutableListOf<Int>()
            val sumRegionsInside = mutableMapOf<Int, MutableList<Int>>()

            lines.forEach { line ->
                getLinePositions(line).forEach { position ->
                    val regionID = getRegionID(position)
                    if (!regionIsSum(regionID)) {
                        val actualValue = actualValues[position]
                        sum += actualValue
                        if (actualValue == 0) {
                            if (possibleValues[position].isEmpty()) return 0
                            unknownPositions.add(position)
                        }
                    } else {
                        sumRegionsInside.getOrPut(regionID) { mutableListOf() }.add(position)
                    }
                }
            }

            if (unknownPositions.size > 3 || unknownPositions.isEmpty()) return 0

            for ((regionID, positionsInside) in sumRegionsInside.entries) {
                val positionsOutside = regions[regionID]!!.filter { !positionsInside.contains(it) }

                if (positionsOutside.any { actualValues[it] == 0 }) return 0

                val sumOutside = positionsOutside.sumOf { actualValues[it] }
                sum += operationResultPerRegion[regionID]!! - sumOutside
            }

            val combSum = rectangleSum - sum
            val combinations = getRegionSumCombinations(possibleValues, unknownPositions, combSum)

            return reducePossibleValuesUsingCombinations(
                combinations,
                unknownPositions,
                possibleValues
            )
        }

        var numValuesRemoved = 0
        (1..4).forEach { numRowsColumns ->
            (0..size-numRowsColumns).forEach { firstRowColumn ->
                val rectangleSum = (1..size).sum() * numRowsColumns
                val rows = (firstRowColumn..< firstRowColumn+numRowsColumns)
                numValuesRemoved += clean(rows, rectangleSum) { int: Int -> getRowPositions(int) }
                val columns = (firstRowColumn..< firstRowColumn+numRowsColumns)
                numValuesRemoved += clean(columns, rectangleSum) { int: Int -> getColumnPositions(int) }
            }
        }

        return numValuesRemoved
    }

    // If a number appears in a combination and that number forms a line we can delete that number from
    // the other positions in that line
    internal fun cleanCageUnitOverlapType2(
        regionID: Int,
        region: List<Int>,
        combinations: MutableList<IntArray>,
        possibleValues: Array<MutableList<Int>>
    ): Int {
        var result = 0

        val numberFrequency = IntArray(size)
        val numberRegionPositions = Array(size){ mutableSetOf<Int>() }
        combinations.forEach { combination ->
            val numberAppears = BooleanArray(size)
            combination.forEachIndexed { regionIndex, number ->
                if (!numberAppears[number-1]){
                    numberAppears[number-1] = true
                    numberFrequency[number-1] ++
                }
                val position = region[regionIndex]
                numberRegionPositions[number-1].add(position)
            }
        }

        val deleteNumberFromLinePossibleValues = { number: Int, line: IntProgression ->
            var possibleValuesChanged = false
            line.filter { getRegionId(it) != regionID }.forEach {
                val res = possibleValues[it].remove(number)
                possibleValuesChanged = possibleValuesChanged || res
            }
            possibleValuesChanged
        }

        for ((index, frequency) in numberFrequency.withIndex()) {
            val regionPositions = numberRegionPositions[index]

            // We skip if the number doesn't appear in all combinations or appears only in one position
            if (frequency < combinations.size || regionPositions.size < 2) continue

            val number = index + 1
            val positions = regionPositions.drop(1)

            val coordinate = Coordinate.fromIndex(regionPositions.first(), size, size)
            val possibleValuesChanged =
                if (positions.all { coordinate.row == Coordinate.getRow(it, size) }) { // Same row
                    deleteNumberFromLinePossibleValues(number, getRowPositions(coordinate.row))
                } else if(positions.all { coordinate.column == Coordinate.getColumn(it, size) }) { // Same column
                    deleteNumberFromLinePossibleValues(number, getColumnPositions(coordinate.column))
                } else false

            if (possibleValuesChanged) result ++
        }

        return result
    }

    internal fun cageUnitOverlapType1(
        region: List<Int>,
        combinations: MutableList<IntArray>,
        actualValues: IntArray,
        possibleValues: Array<MutableList<Int>>,
        regionIndexesPerColumn: MutableMap<Int, MutableList<Int>>,
        regionIndexesPerRow: MutableMap<Int, MutableList<Int>>
    ): Int {

        fun reduceCombinations(linePositions: IntProgression, regionIndexes: MutableList<Int>): Int {
            val regionPositions = regionIndexes.map { index -> region[index] }
                .filterNot { position -> actualValues[position] != 0 }

            if (regionPositions.size < 2) return 0

            val numberAppearances = BooleanArray(size)
            linePositions.filterNot { position -> regionPositions.contains(position) }
                .forEach {
                    possibleValues[it].forEach { number -> numberAppearances[number-1] = true }
                    val v = actualValues[it]
                    if (v != 0) numberAppearances[v-1] = true
                }
            val values = numberAppearances.withIndex().filterNot { (_, b) -> b }.map { (index, _) -> index + 1 }

            if (values.isEmpty()) return 0

            return if (combinations.removeIf { comb -> !regionIndexes.any { index -> values.contains(comb[index]) } }) 1 else 0
        }

        var result = regionIndexesPerColumn
            .map { (column, regionIndexes) -> reduceCombinations(getColumnPositions(column), regionIndexes) }
            .sum()
        result += regionIndexesPerRow
            .map { (row, regionIndexes) -> reduceCombinations(getRowPositions(row), regionIndexes) }
            .sum()
        return result
    }

    internal fun biValueAttackOnRegion(
        region: List<Int>,
        possibleValues: Array<MutableList<Int>>, 
        combinations: MutableList<IntArray>,
        regionIndexesPerColumn: MutableMap<Int, MutableList<Int>>,
        regionIndexesPerRow: MutableMap<Int, MutableList<Int>>
    ): Int {
        var result = 0
        val deleteCombinationsByBiValue = { line: IntProgression, indexes: MutableList<Int> ->
            val biValues = line
                .filter { position -> indexes.all { index -> region[index] != position } }
                .map { position -> possibleValues[position] }
                .filter { it.size == 2 }
            // Delete combinations that have biValue in more than one of the indexes of the combination
            // As its the same line the values won't be repeated
            biValues.forEach { biValue ->
                val res = combinations.removeIf { combination ->
                    indexes.filter { index -> biValue.contains(combination[index]) }.size > 1
                }
                if (res) {
                    result++
                }
            }
        }

        regionIndexesPerRow.forEach { (row, indexes) -> deleteCombinationsByBiValue(getRowPositions(row), indexes)}
        regionIndexesPerColumn.forEach { (column, indexes) -> deleteCombinationsByBiValue(getColumnPositions(column), indexes)}
        return result
    }

    private fun reduceCombinations(combinations: MutableList<IntArray>, values: Array<MutableList<Int>>) {
        combinations.removeIf { combination -> !combination.withIndex().all { (index, value) ->
            values[index].contains(value)
        } }
    }

    private fun reducePossibleValuesUsingCombinations(
        combinations: MutableList<IntArray>,
        region: List<Int>,
        possibleValues: Array<MutableList<Int>>
    ): Int {
        val possibleValuesToKeep = Array(region.size) { mutableSetOf<Int>()  }
        combinations.forEach { combination ->
            combination.forEachIndexed { index, value -> possibleValuesToKeep[index].add(value) }
        }

        var numValuesRemoved = 0
        region.forEachIndexed { index, position ->
            val valuesChanged = possibleValues[position].removeIf { !possibleValuesToKeep[index].contains(it) }
            if (valuesChanged) numValuesRemoved++
        }

        return numValuesRemoved
    }

    private fun getRegionSumCombinations(
        possibleValues: Array<MutableList<Int>>,
        region: MutableList<Int>,
        sum: Int
    ): MutableList<IntArray> {
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

    private fun getRegionMultiplyCombinations(
        possibleValues: Array<MutableList<Int>>,
        region: MutableList<Int>,
        multiplication: Int
    ): MutableList<IntArray> {
        //TODO: Calculate multiples and reduce possible values
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
                if (newActualMultiplication > multiplication) continue
                if (valueAlreadyInRowOrColumn(index, value)) continue

                lastCombination[index] = value
                if (!backtrack(index = index + 1, actualMultiplication = newActualMultiplication)) break
            }

            return true
        }


        backtrack()

        return combinations
    }

    private fun getRegionSubtractCombinations(
        possibleValues: Array<MutableList<Int>>,
        board: IntArray,
        region: List<Int>,
        subtraction: Int
    ): MutableList<IntArray> {
        val combinations = mutableListOf<IntArray>()
        val possibleValues1 = possibleValues[region[0]]
        val possibleValues2 = possibleValues[region[1]]
        val addValues = { int1: Int, int2: Int ->
            if (possibleValues1.contains(int1) && possibleValues2.contains(int2)) combinations.add(intArrayOf(int1, int2))
            if (possibleValues1.contains(int2) && possibleValues2.contains(int1)) combinations.add(intArrayOf(int2, int1))
        }

        val position = region.find { board[it] != 0 }
        if (position != null) {
            val int1 = board[position]
            possibleValues[position].add(int1)
            addValues(int1, int1 + subtraction)
            addValues(int1, int1 - subtraction)
            possibleValues[position].clear()
        }
        else (subtraction+1 .. size).forEach { addValues(it, it - subtraction) }

        return combinations
    }

    private fun getRegionDivideCombinations(
        possibleValues: Array<MutableList<Int>>,
        board: IntArray,
        region: List<Int>,
        division: Int
    ): MutableList<IntArray> {
        val combinations = mutableListOf<IntArray>()

        if (division == 1) return combinations

        val possibleValues1 = possibleValues[region[0]]
        val possibleValues2 = possibleValues[region[1]]

        val addValues = { int1: Int, int2: Int ->
            if (possibleValues1.contains(int2) && possibleValues2.contains(int1)) combinations.add(intArrayOf(int2, int1))
            if (possibleValues1.contains(int1) && possibleValues2.contains(int2)) combinations.add(intArrayOf(int1, int2))
        }

        val position = region.find { board[it] != 0 }
        if (position != null) {
            val int1 = board[position]
            possibleValues[position].add(int1)
        }

        (division..size).filter { it%division == 0 }.forEach { addValues(it, it/division) }

        if (position != null) possibleValues[position].clear()

        return combinations
    }

    /*
    * Tries to deduce the operation. If it can't, return all the combinations using all the valid operations
    * */
    private fun forceGetRegionCombinations(
        boardData: KendokuBoardData,
        regionID: Int,
        region: List<Int>,
        operationResult: Int
    ): MutableList<IntArray> {
        // TODO: Decide whether to use score here or not

        val (operation, combinations) = if (operationResult == 1) {
            KnownKendokuOperation.SUBTRACT to getRegionCombinations(boardData.possibleValues, boardData.actualValues, region, operationResult, KnownKendokuOperation.SUBTRACT)
        }
        else if (operationResult > region.size*size) {
            KnownKendokuOperation.MULTIPLY to getRegionCombinations(boardData.possibleValues, boardData.actualValues, region, operationResult, KnownKendokuOperation.MULTIPLY)
        }
        else {
            val validOps = allowedOperations.filterNot { it.isDivideOrSubtract() && (region.size > 2 || operationResult > size) }
                .map { it to getRegionCombinations(boardData.possibleValues, boardData.actualValues, region, operationResult, it) }
                .filter { (_, combinations) -> combinations.isNotEmpty() }

            if (validOps.size == 1 || validOps.size == 2 && validOps[0].second.size == validOps[1].second.size
                && validOps[0].second.withIndex().all {(i, comb) -> validOps[1].second[i].contentEquals(comb)}
            ) {
                validOps.first()
            }
            else return validOps.flatMap { it.second }.distinctBy { it.toList() }.toMutableList()
        }

        // We save the operation but the combinations are saved later
        boardData.knownOperations[regionID] = operation

        return combinations
    }

    internal fun getRegionCombinations(
        possibleValues: Array<MutableList<Int>>,
        board: IntArray,
        region: List<Int>,
        operationResult: Int,
        operation: KnownKendokuOperation
    ): MutableList<IntArray> {
        val filteredRegion = mutableListOf<Int>()
        val arraySyntax = IntArray(region.size)
        region.forEachIndexed { index, i ->
            val value = board[i]
            if (value == 0) filteredRegion.add(i)
            arraySyntax[index] = value
        }

        if (filteredRegion.isEmpty() || filteredRegion.any { possibleValues[it].isEmpty() }) return mutableListOf()

        val addFilteredValuesBack = { combinations: MutableList<IntArray> ->
            combinations.replaceAll { arr ->
                val iterator = arr.iterator()
                arraySyntax.map { if (it == 0) iterator.next() else it }.toIntArray()
            }
        }

        return when(operation){
            KnownKendokuOperation.SUBTRACT -> getRegionSubtractCombinations(possibleValues, board, region, operationResult)
            KnownKendokuOperation.DIVIDE -> getRegionDivideCombinations(possibleValues, board, region, operationResult)
            KnownKendokuOperation.SUM -> {
                val combinations = getRegionSumCombinations(possibleValues, filteredRegion,
                    sum = operationResult - arraySyntax.sum())
                if (filteredRegion.size < region.size) addFilteredValuesBack(combinations)
                combinations
            }
            KnownKendokuOperation.MULTIPLY -> {
                val combinations = getRegionMultiplyCombinations(possibleValues, filteredRegion,
                    multiplication = operationResult / (arraySyntax.filter { it != 0 }.reduceOrNull { acc, i -> acc * i } ?: 1)  )
                if (filteredRegion.size < region.size) addFilteredValuesBack(combinations)
                combinations
            }
        }
    }

    override fun boardMeetsRulesStr(board: IntArray): String {
        val regions = mutableMapOf<Int, MutableList<Int>>()
        val fillRegions = { positions: IntProgression ->
            positions.forEach { Utils.addToMapList(getRegionId(it), it, regions) }
        }

        val foundRepeatValue = { positions: IntProgression ->
            val tmp = BooleanArray(size*size)
            fillRegions(positions)
            positions.any { position ->
                val res = tmp[board[position]]
                if (!res) tmp[board[position]] = true
                res
            }
        }

        for (rowIndex in (0..< size)) {
            val rowPositions = getRowPositions(rowIndex)
            if (foundRepeatValue(rowPositions)) return "Row: $rowIndex is not made of unique values. Positions: $rowPositions"
        }

        for (columnIndex in (0..< size)) {
            val columnIndexes = getColumnPositions(columnIndex)
            if (foundRepeatValue(columnIndexes)) return "Column: $columnIndex is not made of unique values. Indexes: $columnIndexes"
        }

        for ((regionID, positions) in regions) {
            val operation = operationPerRegion[regionID]!!
            val operationResult = operation.operate(positions.map { board[it] })
            val actualResult = operation.operate(positions.map { completedBoard[it] })

            if (operationResult != actualResult) return "Region: $regionID with positions: $positions, operation: $operation doesn't result in the correct value $actualResult. Result obtained: $operationResult"
        }

        return ""
    }

    override fun checkValue(position: Int, value: Int, actualValues: IntArray): Set<Int> {
        val res = mutableSetOf<Int>()

        //Check row for same value
        getRowPositions(Coordinate.getRow(position, size))
            .filterNot { it == position }
            .filter { actualValues[it] == value }
            .forEach { res.add(it) }

        //Check column for same value
        getColumnPositions(Coordinate.getColumn(position, size))
            .filterNot { it == position }
            .filter { actualValues[it] == value }
            .forEach { res.add(it) }

        val regionID = getRegionId(position)

        val operation = operationPerRegion[regionID]!!
        val regionPositions = positionsPerRegion[regionID]!!
        if (operation.isUnknown() && regionPositions.size > 1) return res

        var opResultCalculated = 0
        var missingValues = false
        for (value2 in regionPositions.filterNot { it == position }.map { actualValues[it] }) {
            if (value2 == 0) missingValues = true
            opResultCalculated = operation.operate(listOf(opResultCalculated, value2))
        }

        val operationResult = operationResultPerRegion[regionID]!!
        if (missingValues && opResultCalculated > operationResult) res.add(position)
        else if (!missingValues && opResultCalculated != operationResult) res.add(position)

        return res
    }


    companion object {
        suspend fun create(
            size: Int,
            seed: Long,
            difficulty: Difficulty,
        ): Kendoku? {
            val kendoku = Kendoku(
                numColumns = size,
                numRows = size,
                seed = seed
            )

            kendoku.createGame(difficulty)

            return if (coroutineContext.isActive) kendoku else null
        }

        // For testing
        suspend fun createTesting(
            size: Int,
            seed: Long,
            difficulty: Difficulty,
        ): Kendoku {
            val kendoku = Kendoku(
                id = 0,
                numColumns = size,
                numRows = size,
                seed = seed
            )

            kendoku.createGame(difficulty)

            return kendoku
        }

        //For debug
        fun createTesting(
            seed: Long,
            startBoard: IntArray,
            completedBoard: IntArray,
            regions: IntArray,
            operationPerRegion: MutableMap<Int, KendokuOperation>
        ): Kendoku {
            return Kendoku(
                seed = seed,
                score = KendokuScore(500),
                numRows = sqrt(regions.size.toDouble()).toInt(),
                numColumns = sqrt(regions.size.toDouble()).toInt(),
                startBoard = startBoard,
                completedBoard = completedBoard,
                boardRegions = regions,
                operationPerRegion = operationPerRegion
            )

        }

        // For testing
        fun solveBoard(
            seed: Long,
            size: Int,
            startBoard: IntArray,
            completedBoard: IntArray,
            regions: IntArray,
            operationPerRegion: MutableMap<Int, KendokuOperation>
        ): Kendoku {
            val kendoku = Kendoku(
                id = 0,
                numColumns = size,
                numRows = size,
                seed = seed,
                boardRegions = regions,
                startBoard = startBoard,
                completedBoard = completedBoard,
                operationPerRegion = operationPerRegion
            )

            val score = runBlocking { kendoku.solveBoard(kendoku.startBoard) }
            kendoku.score.add(score)

            return kendoku
        }
    }
}